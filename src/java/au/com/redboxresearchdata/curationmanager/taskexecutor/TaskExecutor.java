package au.com.redboxresearchdata.curationmanager.taskexecutor;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.springframework.context.ApplicationContext;

import au.com.redboxresearchdata.curationmanager.utility.ApplicationContextHolder;


public class TaskExecutor {
	private static final Log log = LogFactory.getLog(TaskExecutor.class);
	
    public TaskExecutor(){
    	try{
    	  ApplicationContext ctx = ApplicationContextHolder.getApplicationContext();
    	  CurationTaskExecutor myTaskExecutor = (CurationTaskExecutor) ctx.getBean("curationTaskExecutor");
    	  myTaskExecutor.executeTask();
    	}catch(Exception ex){
    		log.error(ex.getMessage());
    		log.error(ex.getCause());
    	}
    }
}
