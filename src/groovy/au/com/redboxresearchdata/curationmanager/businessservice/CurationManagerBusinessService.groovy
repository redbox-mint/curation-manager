package au.com.redboxresearchdata.curationmanager.businessservice

import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log;
import au.com.redboxresearchdata.curationmanager.utility.MessageResolver;

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

import org.apache.commons.logging.Log;

import au.com.redboxresearchdata.curationmanager.businessdelegate.CurationManagerBusinessDelegate
import au.com.redboxresearchdata.curationmanager.businesservicexception.CurationManagerBSException
import au.com.redboxresearchdata.curationmanager.businessvalidator.CurationManagerBV
import au.com.redboxresearchdata.curationmanager.entityservice.CurationManagerES
import au.com.redboxresearchdata.curationmanager.entityvalidationexception.CurationManagerEVException
import au.com.redboxresearchdata.curationmanager.jobrunner.JobRunner
import au.com.redboxresearchdata.curationmanager.response.CurationManagerResponse
import au.com.redboxresearchdata.curationmanager.domain.Curation

class CurationManagerBusinessService{

	private static final Log log = LogFactory.getLog(this)
	
	def CurationManagerResponse curate(requestParams) throws CurationManagerBSException, Exception{
		 CurationManagerBV curationManagerBV = new CurationManagerBV();
		 if(curationManagerBV.validateRequestParams(requestParams)){
			  CurationManagerES curationManagerES = new CurationManagerES();
			  try{
				 Long jobId; 
				 CurationManagerResponse curationManagerResponse =  curationManagerES.
				             createOrFindEntityAndRelationship(requestParams);
				 if(null != curationManagerResponse){
					jobId = curationManagerResponse.getJobId();
			     }
			     JobRunner jobRunner = new JobRunner();
				 jobRunner.startJob(jobId);		
				 return curationManagerResponse;
			    }catch(CurationManagerEVException csex) {
			       log.error(csex.getKey() + " " +csex.getValue());
				   throw new CurationManagerBSException(csex.getKey(), csex.getValue());
			    }
		  }
     }
	
	
	def CurationManagerResponse retrieveJob(jobId) throws CurationManagerBSException, Exception{
		CurationManagerResponse curationManagerResponse;
	   try{
		 CurationManagerBV curationManagerBV = new CurationManagerBV();
		 Boolean validJobId = curationManagerBV.validateJobId(jobId);
	 	 if(validJobId){
			CurationManagerES curationManagerES = new CurationManagerES();
			curationManagerResponse = curationManagerES.retreiveJob(jobId);
	     }
	   }catch(CurationManagerEVException csex) {
			  log.error(csex.getKey() + " " +csex.getValue());
	        throw new CurationManagerBSException(csex.getKey(), csex.getValue());
	   }
		return curationManagerResponse;
	}
	
	
	def CurationManagerResponse retrieveJobByOid(oid) throws CurationManagerBSException, Exception{
		CurationManagerResponse curationManagerResponse;
	   try{
		 CurationManagerBV curationManagerBV = new CurationManagerBV();
		 Boolean validJobId = curationManagerBV.validateOID(oid);
		  if(validJobId){
			CurationManagerES curationManagerES = new CurationManagerES();
			curationManagerResponse = curationManagerES.retreiveJobByOID(oid);
		 }
	   }catch(CurationManagerEVException csex) {
		    log.error(csex.getKey() + " " +csex.getValue());
			throw new CurationManagerBSException(csex.getKey(), csex.getValue());
	   }
		return curationManagerResponse;
	}	
}