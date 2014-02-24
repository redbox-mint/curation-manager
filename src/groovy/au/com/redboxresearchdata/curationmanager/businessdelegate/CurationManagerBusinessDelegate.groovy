package au.com.redboxresearchdata.curationmanager.businessdelegate

import au.com.redboxresearchdata.curationmanager.utility.MessageResolver;
import org.apache.commons.logging.LogFactory
import au.com.redboxresearchdata.curationmanager.identityProviderResult.BaseIdentityResult
import au.com.redboxresearchdata.curationmanager.identityProviderResult.IdentifierResult
import au.com.redboxresearchdata.curationmanager.identityProviderResult.PromiseResult
import au.com.redboxresearchdata.curationmanager.identityProviderService.IdentityProviderService

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.context.ApplicationContext

import au.com.redboxresearchdata.curationmanager.entityservice.CurationManagerES
import au.com.redboxresearchdata.curationmanager.entityvalidationexception.CurationManagerEVException;
import au.com.redboxresearchdata.curationmanager.utility.ApplicationContextHolder
import au.com.redboxresearchdata.curationmanager.utility.DateUtil
import au.com.redboxresearchdata.curationmanager.constants.CurationManagerConstants
import au.com.redboxresearchdata.curationmanager.domain.Curation
import au.com.redboxresearchdata.curationmanager.domain.CurationJob
import au.com.redboxresearchdata.curationmanager.domain.CurationJobItems
import au.com.redboxresearchdata.curationmanager.domain.CurationStatusLookup

class CurationManagerBusinessDelegate {
	
	private static final Log log = LogFactory.getLog(this)

	def void doProcess(jobId) throws InterruptedException,  CurationManagerEVException, Exception {
		
		log.info("I am sleeping..."+ Thread.currentThread().getName())
		Thread.sleep(3000);
		log.info("I am done sleeping..."+ Thread.currentThread().getName())
		
		def results =  CurationJob.withCriteria(uniqueResult: true){
			createAlias("curationJobItems", "curJobItems")
			createAlias("curationStatusLookup", "curJobStatusLookup")
			createAlias("curJobItems.curation", "cur")
			createAlias("cur.curationStatusLookup","curStatslookup")
			eq("curStatslookup.value", "IN_PROGRESS")
			eq("curJobStatusLookup.value", "IN_PROGRESS")
			maxResults(1)
			order("dateCreated", "asc")
		}
		CurationManagerES curationManagerES = new CurationManagerES();
		results.each {
		  List curationJobItems = it.getCurationJobItems();
		  updateCurationAndIdentityService(it, curationJobItems, curationManagerES)
		}
	 	  CurationJob curationJobNew = CurationJob.findById(jobId);
		  List curationJobItemsNew = curationJobNew.getCurationJobItems();
		  updateCurationJob(curationJobNew, curationJobItemsNew, curationManagerES);	
	  }
	
	
	def void updateCurationJob(curationJob, curationJobItems, curationManagerES){
	
		Boolean curationCompleted = Boolean.FALSE;
		Boolean curationFailed = Boolean.FALSE;
		for(CurationJobItems curationJobItem : curationJobItems){
		 if(curationJobItem.getCurationJob().getId().equals(curationJob.getId())){	
			Curation curation = curationJobItem.getCuration();
			CurationStatusLookup curationStatusLookup = curation.getCurationStatusLookup();
			if(null != curationStatusLookup && CurationManagerConstants.COMPLETED.
				equals(curationStatusLookup.getValue())){
				curationCompleted = Boolean.TRUE;
			 }else if(null != curationStatusLookup && CurationManagerConstants.FAILED.
			 equals(curationStatusLookup.getValue())) {
				curationFailed = Boolean.TRUE;
			 }
		  }
		}
		if(curationCompleted && !curationFailed){
			curationManagerES.updateCurationJob(curationJob, CurationManagerConstants.COMPLETED)
		}else {
	 	    curationManagerES.updateCurationJob(curationJob, CurationManagerConstants.FAILED)
		}	
	}
	
	def void updateCurationAndIdentityService(curationJob, curationJobItems, curationManagerES)
	   throws NoSuchBeanDefinitionException, Exception{

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
						ApplicationContext ctx = ApplicationContextHolder.getApplicationContext();
						IdentityProviderService identityProviderService =  ctx.getBean(identifierType);
						if(null != identityProviderService){
							String oid = curation.getEntry().getId();
							BaseIdentityResult baseIdentifier = identityProviderService.curate(oid, null);
							IdentifierResult identifierResult = (IdentifierResult) baseIdentifier;
							if(null != baseIdentifier && baseIdentifier instanceof IdentifierResult){
								if(null != identifierResult && null != identifierResult.getIdentifier()){
									curationManagerES.updateCuration(curation, identifierResult.getIdentifier(),
											CurationManagerConstants.COMPLETED, DateUtil.getW3CDate(), "");
									completed = Boolean.TRUE;
								}else {
									curationManagerES.updateCuration(curation, null, CurationManagerConstants.FAILED,
											null, CurationManagerConstants.ERROR_MESSAGE_SERVICE_NOT_AVAILABLE)
									def msg = MessageResolver.getMessage(CurationManagerConstants.CURATIONMANAGER_IDENTITYSERVICE_IDENTIFIER_NULL);
									log.error(msg + identifierType);
									failed = Boolean.TRUE;
								}
							} else if(null != baseIdentifier && baseIdentifier instanceof PromiseResult){
								////////// Do the Asynchronous here.
							}else{
								curationManagerES.updateCuration(curation, null, CurationManagerConstants.FAILED,
										null, CurationManagerConstants.ERROR_MESSAGE_SERVICE_NOT_AVAILABLE)
								def msg = MessageResolver.getMessage(CurationManagerConstants.CURATIONMANAGER_IDENTITYSERVICE_NO_RESULT);
								log.error(msg + identifierType);
								failed = Boolean.TRUE;
							}
						}else{
							curationManagerES.updateCuration(curation, null, CurationManagerConstants.FAILED,
									null, CurationManagerConstants.ERROR_MESSAGE_SERVICE_NOT_AVAILABLE)
							def msg = MessageResolver.getMessage(CurationManagerConstants.CURATIONMANAGER_IDENTITYSERVICE_FAILED);
							log.error(msg + identifierType);
							failed = Boolean.TRUE;
						}
					}catch(Exception ex){
						curationManagerES.updateCuration(curation, null, CurationManagerConstants.FAILED,
								null, CurationManagerConstants.ERROR_MESSAGE_CONTACT_SYSTEM_ADMININSTRATOR)
						log.error(ex.getMessage() + ex.getCause());
						failed = Boolean.TRUE;
				   }
			  }
		   }
		   if(completed && !failed){
			   curationManagerES.updateCurationJob(curationJob, CurationManagerConstants.COMPLETED)
		   }else {
			  curationManagerES.updateCurationJob(curationJob, CurationManagerConstants.FAILED)		   
		}
	  }
	}
}