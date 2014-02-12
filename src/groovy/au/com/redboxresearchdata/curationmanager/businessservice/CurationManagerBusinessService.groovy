package au.com.redboxresearchdata.curationmanager.businessservice
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

import au.com.redboxresearchdata.curationmanager.businessdelegate.CurationManagerBusinessDelegate
import au.com.redboxresearchdata.curationmanager.businesservicexception.CurationManagerBSException
import au.com.redboxresearchdata.curationmanager.businessvalidator.CurationManagerBV
import au.com.redboxresearchdata.curationmanager.entityservice.CurationManagerES
import au.com.redboxresearchdata.curationmanager.entityvalidationexception.CurationManagerEVException
import au.com.redboxresearchdata.curationmanager.jobrunner.JobRunner
import au.com.redboxresearchdata.curationmanager.response.CurationManagerResponse
import au.com.redboxresearchdata.curationmanager.domain.Curation

class CurationManagerBusinessService{

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
				 throw new CurationManagerBSException(csex.getKey(), csex.getValue());
			  }
		 }
   }
}