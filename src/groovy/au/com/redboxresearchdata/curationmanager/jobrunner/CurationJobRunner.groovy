package au.com.redboxresearchdata.curationmanager.jobrunner

import org.apache.commons.logging.Log;

import au.com.redboxresearchdata.curationmanager.constants.CurationManagerConstants
import au.com.redboxresearchdata.curationmanager.utility.DateUtil
import au.com.redboxresearchdata.curationmanager.domain.Curation;
import au.com.redboxresearchdata.curationmanager.domain.CurationJob
import au.com.redboxresearchdata.curationmanager.domain.CurationJobItems
import au.com.redboxresearchdata.curationmanager.domain.CurationStatusLookup
import au.com.redboxresearchdata.curationmanager.entityvalidationexception.CurationManagerEVException;
import au.com.redboxresearchdata.curationmanager.identityProviderService.sruclient.SRUClient

import org.apache.commons.logging.LogFactory

class CurationJobRunner {

	private static final Log log = LogFactory.getLog(this);
	
	def void executeJob(){		
		Curation.withNewTransaction  {
			
		def results =  Curation.withCriteria(uniqueResult: true){
			createAlias("curationStatusLookup","curStatslookup")
			eq("curStatslookup.value", "CURATING")
			eq("identifierType", "nla")
			order("dateCreated", "asc")
		}		
		results.each {
			Curation curation = it;
			println "-----I am here--in the curations---"
			println curation;
			String oid = curation.getEntry().getId();
			def curationResult =  Curation.withCriteria(uniqueResult: true){
				createAlias("entry","entry")
				createAlias("curationStatusLookup","curStatslookup")
				eq("identifierType", "handle")
				eq("entry.id", oid)
			}
			curationResult.each {
		 	  Curation handleCuration = it;
			  SRUClient sRUClient = new SRUClient();
			  String identifier = handleCuration.getIdentifier();
			  println "------------------------------------------------------------------";
			  println identifier;
			  String nlaId = sRUClient.nlaGetNationalId("b4c535606557c854b0560cd45d454640");
			  if(null == nlaId || "".equals(nlaId)){
				  log.error("Object '{}' does not yet have a national Identity in NLA"+ oid);
			  }else {
			      println "-------------------------NLA------------------------------------------";
			      println nlaId;
			      log.error("Object '{}' has a new national Identity in NLA ({})" + oid);
			 curation.setIdentifier(nlaId);
			 CurationStatusLookup completedCurationStatusLookup = CurationStatusLookup.findByValue(
				  CurationManagerConstants.COMPLETED);
			  curation.setCurationStatusLookup(completedCurationStatusLookup);
			  Date dateW3C = DateUtil.getW3CDate();
			  curation.setDateCompleted(dateW3C);
			  List curationJobItems = curation.getCurationJobItems();
			  CurationJob curationJob = curationJobItems.getCurationJob();
			  updateCuration(curationJob, curationJobItems);
			  curation.save();
			}
		  }
		 }		
		}
	 }
	
	def void updateCuration(curationJob, curationJobItems){
		Boolean curationCompleted = Boolean.FALSE;
		Boolean curating = Boolean.FALSE
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
			 }else if(null != curationStatusLookup && CurationManagerConstants.CURATING.
				equals(curationStatusLookup.getValue())) {
				curating = Boolean.TRUE;
			 }
		  }
		}
		if(curationCompleted && !curationFailed && !curating){
			updateCurationJob(curationJob, CurationManagerConstants.COMPLETED)
		}else if(curationCompleted && curationFailed && curating){
			updateCurationJob(curationJob, CurationManagerConstants.FAILED)
		} else if(curationCompleted && !curationFailed && curating){
		    updateCurationJob(curationJob, CurationManagerConstants.CURATING);
		}else if(!curationCompleted && !curationFailed && curating){
		    updateCurationJob(curationJob, CurationManagerConstants.CURATING);
		}else{
		    updateCurationJob(curationJob, CurationManagerConstants.FAILED)
		}
	}
	
	
	def void updateCurationJob(curationJob, curationStatus) throws CurationManagerEVException, Exception{		
	  try{
		  CurationStatusLookup curationStatusLookup = CurationStatusLookup.findByValue(curationStatus);
		  curationJob.setCurationStatusLookup(curationStatusLookup);
		  curationJob.setDateCompleted(DateUtil.getW3CDate());
		  curationJob.lock();
		} catch(Exception ex){
	  }
	}
}