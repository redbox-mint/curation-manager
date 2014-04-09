package au.com.redboxresearchdata.curationmanager.jobrunner

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import au.com.redboxresearchdata.curationmanager.constants.CurationManagerConstants
import au.com.redboxresearchdata.curationmanager.domain.Curation
import au.com.redboxresearchdata.curationmanager.domain.CurationJob
import au.com.redboxresearchdata.curationmanager.domain.CurationJobItems
import au.com.redboxresearchdata.curationmanager.domain.CurationStatusLookup
import au.com.redboxresearchdata.curationmanager.entityservice.CurationManagerES
import au.com.redboxresearchdata.curationmanager.entityvalidationexception.CurationManagerEVException;
import au.com.redboxresearchdata.curationmanager.identityProviderService.sruclient.SRUClient
import au.com.redboxresearchdata.curationmanager.utility.DateUtil

import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil;

class CurationJobRunner {
	
	private static final Log log = LogFactory.getLog(this);

	def void executeJob(){
		Curation curation;
		String nlaId;
		CurationManagerES curationManagerES = new CurationManagerES();
		try{
			SRUClient sRUClient = new SRUClient();
			Curation.withNewTransaction  {
				def results =  Curation.withCriteria(){
					createAlias("entry","entry")
					createAlias("entry.entryTypeLookup","entryTypeLookup")
					createAlias("curationStatusLookup","curStatslookup")
					eq("curStatslookup.value", CurationManagerConstants.CURATING)
					eq("entryTypeLookup.value", CurationManagerConstants.PERSON)
					eq("identifierType", CurationManagerConstants.NLA)
					order("dateCreated", CurationManagerConstants.ASC)
				}
				results.each {
					curation = it;
					String oid = curation.getEntry().getId();
					def curationResult =  Curation.withCriteria(){
						createAlias("entry","entry")
						createAlias("curationStatusLookup","curStatslookup")
						ne("identifierType", CurationManagerConstants.NLA)
						eq("entry.id", oid)
					}
					curationResult.each {
						Curation handleCuration = it;
						String identifier = handleCuration.getIdentifier();
						nlaId = sRUClient.nlaGetNationalId(identifier);
						if(null == nlaId || "".equals(nlaId)){
							log.error("Object '{}' does not yet have a national Identity in NLA"+ oid);
						}else if(null != nlaId){
							log.info("Object '{}' has a new national Identity in NLA ({}) " + oid);
                            curationManagerES.insertCuration(curation, nlaId);	
							if(null != curation) {
							  updateCurationJob(curation.getCurationJobItems(), curationManagerES)
							}
						}
					}
				}
			}
		}catch(org.springframework.dao.OptimisticLockingFailureException e){
			curationManagerES.insertCuration(curation, nlaId);
		}catch(Exception ex){
			log.error("Error in the curation job runner" + ex.getMessage())
			if(null!= curation){
				CurationStatusLookup curationStatus = CurationStatusLookup.findByValue(
						CurationManagerConstants.FAILED);
				Date dateCompleted = DateUtil.getW3CDate();
				curationManagerES.updateCuration(curation, null, curationStatus, dateCompleted,
						ex.getMessage())
			}
		}
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
}
