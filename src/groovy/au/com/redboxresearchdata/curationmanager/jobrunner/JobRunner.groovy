package au.com.redboxresearchdata.curationmanager.jobrunner

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory

import au.com.redboxresearchdata.curationmanager.response.CurationManagerResponse
import au.com.redboxresearchdata.curationmanager.businessdelegate.CurationManagerBusinessDelegate
import au.com.redboxresearchdata.curationmanager.businesservicexception.CurationManagerBSException
import au.com.redboxresearchdata.curationmanager.businessvalidator.CurationManagerBV
import au.com.redboxresearchdata.curationmanager.entityvalidationexception.CurationManagerEVException
import au.com.redboxresearchdata.curationmanager.entityservice.CurationManagerES
import au.com.redboxresearchdata.curationmanager.domain.Curation
import au.com.redboxresearchdata.curationmanager.utility.MessageResolver;
import au.com.redboxresearchdata.curationmanager.constants.CurationManagerConstants


class JobRunner {
	private static final Log log = LogFactory.getLog(this)

	def void startJob(jobId){	
	  try{		
		ExecutorService es = Executors.newSingleThreadExecutor();
		final Future future = es.submit(new Callable() {
		public Object call() throws Exception {
			 Curation.withTransaction {
				 new CurationManagerBusinessDelegate().doProcess(jobId);
			  }
				 return null;
			 }
		});
	  }catch(Exception ex){
	    log.error(ex.getMessage() + ex.getCause());
	    def msg = MessageResolver.getMessage(CurationManagerConstants.UNEXPECTED_ERROR);
	  }
	}
}
