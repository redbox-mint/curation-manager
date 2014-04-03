package au.com.redboxresearchdata.curationmanager.taskexecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class TaskExecutor {
	private static final Log log = LogFactory.getLog(TaskExecutor.class);
	
    public TaskExecutor(CurationTaskExecutor myTaskExecutor){
    	try{    		
    	  
    	  myTaskExecutor.executeTask();
    	}catch(Exception ex){
    		log.error(ex.getMessage());
    		log.error(ex.getCause());
    	}
    }
}
