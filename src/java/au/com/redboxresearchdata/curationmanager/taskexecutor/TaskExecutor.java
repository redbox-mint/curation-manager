package au.com.redboxresearchdata.curationmanager.taskexecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TaskExecutor {
	private static final Log log = LogFactory.getLog(TaskExecutor.class);
	
    public TaskExecutor(CurationTaskExecutor myTaskExecutor){
    	try{    		
    	  
    	 // myTaskExecutor.executeTask();
    	}catch(Exception ex){
    		log.error(ex.getMessage());
    		log.error(ex.getCause());
    	}
    }
}
