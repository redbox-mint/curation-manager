package au.com.redboxresearchdata.curationmanager.jobrunner


import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

import au.com.redboxresearchdata.curationmanager.response.CurationManagerResponse
import au.com.redboxresearchdata.curationmanager.businessdelegate.CurationManagerBusinessDelegate
import au.com.redboxresearchdata.curationmanager.businesservicexception.CurationManagerBSException
import au.com.redboxresearchdata.curationmanager.businessvalidator.CurationManagerBV
import au.com.redboxresearchdata.curationmanager.entityvalidationexception.CurationManagerEVException
import au.com.redboxresearchdata.curationmanager.entityservice.CurationManagerES
import au.com.redboxresearchdata.curationmanager.domain.Curation

class JobRunner {

	def void startJob(jobId){		
		ExecutorService es = Executors.newSingleThreadExecutor();
		final Future future = es.submit(new Callable() {
		public Object call() throws Exception {
			 Curation.withTransaction{
				 new CurationManagerBusinessDelegate().doProcess(jobId);
			  }
				 return null;
			 }
		});
	}
}
