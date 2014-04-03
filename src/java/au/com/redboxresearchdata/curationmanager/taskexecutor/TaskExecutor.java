package au.com.redboxresearchdata.curationmanager.taskexecutor;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class TaskExecutor {
	private static final Log log = LogFactory.getLog(TaskExecutor.class);
	
    public TaskExecutor(){
    	try{
    	  ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    	  taskExecutor.setCorePoolSize(1);
    	  taskExecutor.setMaxPoolSize(1);
    	  taskExecutor.setQueueCapacity(1);    	        	  
    	  CurationTaskExecutor myTaskExecutor = new CurationTaskExecutor(taskExecutor);
    	  myTaskExecutor.executeTask();
    	}catch(Exception ex){
    		log.error(ex.getMessage());
    		log.error(ex.getCause());
    	}
    }
}
