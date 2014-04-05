package au.com.redboxresearchdata.curationmanager.businessdelegate;

import au.com.redboxresearchdata.curationmanager.utility.MessageResolver;

import org.apache.commons.logging.LogFactory

import au.com.redboxresearchdata.curationmanager.identityProviderResult.BaseIdentityResult;
import au.com.redboxresearchdata.curationmanager.identityProviderResult.IdentifierResult;
import au.com.redboxresearchdata.curationmanager.identityProviderResult.PromiseResult;
import au.com.redboxresearchdata.curationmanager.identityProviderService.IdentityProviderService;
import au.com.redboxresearchdata.curationmanager.jobrunner.CurationJobRunner

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import au.com.redboxresearchdata.curationmanager.entityservice.CurationManagerES;
import au.com.redboxresearchdata.curationmanager.entityvalidationexception.CurationManagerEVException;
import au.com.redboxresearchdata.curationmanager.utility.ApplicationContextHolder;
import au.com.redboxresearchdata.curationmanager.utility.DateUtil;
import au.com.redboxresearchdata.curationmanager.constants.CurationManagerConstants;
import au.com.redboxresearchdata.curationmanager.domain.Curation;
import au.com.redboxresearchdata.curationmanager.domain.CurationJob;
import au.com.redboxresearchdata.curationmanager.domain.CurationJobItems;
import au.com.redboxresearchdata.curationmanager.domain.CurationStatusLookup;

class CurationManagerBusinessDelegate {
	
	private static final Log log = LogFactory.getLog(this)

	def void doProcess(jobId) throws InterruptedException,  CurationManagerEVException, Exception {		
		log.info("I am sleeping.Job Runner.."+ Thread.currentThread().getName())
		Thread.sleep(3000);
		log.info("I am done sleeping.Job Runner.."+ Thread.currentThread().getName())	
		def results =  CurationJob.withCriteria(uniqueResult: true){
			createAlias("curationJobItems", "curJobItems")
			createAlias("curationStatusLookup", "curJobStatusLookup")
			createAlias("curJobItems.curation", "cur")
			createAlias("cur.curationStatusLookup","curStatslookup")
			eq("curStatslookup.value", "IN_PROGRESS")
			eq("curJobStatusLookup.value", CurationManagerConstants.IN_PROGRESS)
			maxResults(1)
			order("dateCreated", CurationManagerConstants.ASC)
		}
		CurationManagerES curationManagerES = new CurationManagerES();
		results.each {
		  List curationJobItems = it.getCurationJobItems();
		  updateCurationAndIdentityService(it, curationJobItems, curationManagerES);
		  updateCurationJob(curationJobItems, curationManagerES);
		} 	   
		new CurationJobRunner().executeJob();
	}
	
	def void updateCurationJob(curationJobItems, curationManagerES) throws Exception{
		Boolean curationCompleted = Boolean.FALSE;
		Boolean curationFailed = Boolean.FALSE;
		Boolean curating = Boolean.FALSE;
		Boolean progress = Boolean.FALSE;
		CurationJob curationJob;
		for(CurationJobItems curationJobItem : curationJobItems){
			curationJob = curationJobItem.getCurationJob();
			curationJob.refresh();
			for(CurationJobItems curationJobItemNew : curationJob.getCurationJobItems()){
				Curation curation = curationJobItemNew.getCuration();
				curation.refresh();
				CurationStatusLookup curationStatusLookup = curation.getCurationStatusLookup();
				if(null != curationStatusLookup && CurationManagerConstants.COMPLETED.
				equals(curationStatusLookup.getValue())){
					curationCompleted = Boolean.TRUE;
				}else if(null != curationStatusLookup && CurationManagerConstants.FAILED.
				   equals(curationStatusLookup.getValue())) {
						curationFailed = Boolean.TRUE;
				} else if(null != curationStatusLookup && CurationManagerConstants.CURATING.
					equals(curationStatusLookup.getValue())){
						curating = Boolean.TRUE
				}	else if(null != curationStatusLookup && CurationManagerConstants.IN_PROGRESS.
					equals(curationStatusLookup.getValue())){
						progress = Boolean.TRUE
				}
			}
		  }
		if(curationCompleted && !curationFailed && !curating && !progress && null != curationJob){
			curationManagerES.updateCurationJob(curationJob, CurationManagerConstants.COMPLETED)
		}else if(((!curationCompleted && curationFailed)|| (curationCompleted && curationFailed)) && null != curationJob) {
			curationManagerES.updateCurationJob(curationJob, CurationManagerConstants.FAILED)
		}
	}
		
	def void updateCurationAndIdentityService(curationJob, curationJobItems, curationManagerES)
	   throws NoSuchBeanDefinitionException, Exception{
	       Boolean updateCurationJob = Boolean.TRUE;
		   Boolean completed = Boolean.FALSE;
		   Boolean failed = Boolean.FALSE;
		   for(CurationJobItems curationJobItem : curationJobItems){
			  if(curationJobItem.getCurationJob().getId().equals(curationJob.getId())){
				Curation curation = curationJobItem.getCuration();		
				String identifierType = curation.getIdentifierType();
				String identifier = curation.getIdentifier();
				CurationStatusLookup curationStatusLookup = curation.getCurationStatusLookup();
				String curationStatus = curationStatusLookup.getValue();
				if(CurationManagerConstants.CURATING != curationStatus  && null == identifier) {
					curationManagerES.updateCuration(curation, null, CurationManagerConstants.CURATING, null, "");
					try{
						String oid = curation.getEntry().getId();
						String metaData = curation.getMetaData();
						ApplicationContext ctx = ApplicationContextHolder.getApplicationContext();
						IdentityProviderService identityProviderService =  ctx.getBean(identifierType);
						IdentityProviderService dependentIdentityProviderService = identityProviderService.
						             getDependentProviderService();
						String dependentIdentifier = null;
						if(null != dependentIdentityProviderService){
						  try{	
						    String dependentIdentifierName = dependentIdentityProviderService.getId();
						    Map depdentMataData = dependentIdentityProviderService.getMetaDataMap(metaData);
							BaseIdentityResult dependentIdentifierResult = dependentIdentityProviderService.
							                         curate(oid, metaData, curationJob.getId().toString(),
														    curation.getEntry().getEntryTypeLookup().getValue());
						    dependentIdentifier = dependentIdentifierResult.getIdentifier();
							if(null == dependentIdentifier){
								log.error("Dependent Identifier cannot be null");
								throw new Exception("Dependent Identifier cannot be null");
							}
							String type = curation.getEntry().getEntryTypeLookup().getValue();
							String title = curation.getEntry().getTitle();
							curationManagerES.insertCurationWithDependentIdentifier(metaData, oid, 
								dependentIdentifier, dependentIdentifierName, type, title);	
						  } catch(Exception ex){
				  	        log.error("Error happened in the dependent Identifier"+ex.getMessage());
							log.error(ex.getCause());
					        throw ex;
						  }			  													  
						}
						if(null != identityProviderService){
						   BaseIdentityResult baseIdentifier = identityProviderService.curate(oid, metaData, 
								                          curationJob.getId().toString(),
														  curation.getEntry().getEntryTypeLookup().getValue(),
														  dependentIdentifier);
							if(null != baseIdentifier && baseIdentifier instanceof IdentifierResult){
								IdentifierResult identifierResult = (IdentifierResult) baseIdentifier;
								if(null != identifierResult && null != identifierResult.getIdentifier()){
									curationManagerES.updateCuration(curation, identifierResult.getIdentifier(),
											CurationManagerConstants.COMPLETED, DateUtil.getW3CDate(), "");
									completed = Boolean.TRUE;
								}else {
									curationManagerES.updateCuration(curation, null, CurationManagerConstants.FAILED,
											null, CurationManagerConstants.ERROR_MESSAGE_SERVICE_NOT_AVAILABLE)
									def msg = MessageResolver.getMessage(CurationManagerConstants.
										CURATIONMANAGER_IDENTITYSERVICE_IDENTIFIER_NULL);
									log.error(msg + identifierType);
									failed = Boolean.TRUE;
								}
							} else if(null != baseIdentifier && baseIdentifier instanceof PromiseResult){
								////////// Do the Asynchronous here. This is not needed anymore. 
							   PromiseResult promiseResult = (PromiseResult) baseIdentifier;
							   if(null != promiseResult && null != promiseResult.getIdentifier()){
								  String identifierPromize = promiseResult.getIdentifier();
								  updateCurationJob = Boolean.FALSE;
							   } else{
							      def msg = MessageResolver.getMessage(CurationManagerConstants.
								   CURATIONMANAGER_IDENTITYSERVICE_IDENTIFIER_NULL);
							      log.info(msg + identifierType);
							   }	
							}else{
								curationManagerES.updateCuration(curation, null, CurationManagerConstants.FAILED,
										null, CurationManagerConstants.ERROR_MESSAGE_SERVICE_NOT_AVAILABLE)
								def msg = MessageResolver.getMessage(CurationManagerConstants.
									CURATIONMANAGER_IDENTITYSERVICE_NO_RESULT);
								log.info(msg + identifierType);
								failed = Boolean.TRUE;
							}
						}else{
							curationManagerES.updateCuration(curation, null, CurationManagerConstants.FAILED,
									null, CurationManagerConstants.ERROR_MESSAGE_SERVICE_NOT_AVAILABLE)
							def msg = MessageResolver.getMessage(CurationManagerConstants.CURATIONMANAGER_IDENTITYSERVICE_FAILED);
							log.info(msg + identifierType);
							failed = Boolean.TRUE;
						}
					}catch(Exception ex){
						curationManagerES.updateCuration(curation, null, CurationManagerConstants.FAILED,
								null, CurationManagerConstants.ERROR_MESSAGE_CONTACT_SYSTEM_ADMININSTRATOR)
						log.error(ex.getMessage());
						log.error(ex.getCause());
						throw ex;
				   }
			  }
		   }
	    }
		  if(updateCurationJob && completed && !failed){
			   curationManagerES.updateCurationJob(curationJob, CurationManagerConstants.COMPLETED)
		   }else if(updateCurationJob && (completed && failed || !completed && failed)){
			  curationManagerES.updateCurationJob(curationJob, CurationManagerConstants.FAILED)		   
		}
	   }
	
}