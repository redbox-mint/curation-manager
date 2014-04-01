package au.com.redboxresearchdata.curationmanager.taskexecutor;

import java.util.concurrent.Semaphore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;

import au.com.redboxresearchdata.curationmanager.jobrunner.CurationJobRunner;

/* @author Devika Indla
*/

public class CurationTaskExecutor {
	private TaskExecutor taskExecutor;
	
	private static final Log log = LogFactory.getLog(CurationTaskExecutor.class);

	@Value("#{asynchronousPropSource[TimeToStartANewTask]}")
	 String timeToStartANewTask;
	
	@Value("#{asynchronousPropSource[Seconds]}")
	 String seconds;
	
	@Value("#{asynchronousPropSource[Minutes]}")
	 String minutes;
	

	@Value("#{asynchronousPropSource[MilliSeconds]}")
	 String milliSeconds;
	
	public CurationTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}
	 
	public String getTimeToStartANewTask(){
		return timeToStartANewTask;
	}
	
	public long getSeconds(){
		return Long.valueOf(seconds);
	}
	
	public long getMinutes(){
		return Long.valueOf(minutes);
	}
	
	public long getMilliSeconds(){
		return Long.valueOf(milliSeconds);
	}
	
	public void executeTask() {
		taskExecutor.execute(new CurationTask());
	}

	private class CurationTask implements Runnable {
		private final Semaphore executionSemaphore = new Semaphore(1, true);

		public CurationTask() {
		}

		public void run() {
			long timeBetweenExecution = getMilliSeconds() * getSeconds() * getMinutes();
			//long timeBetweenExecution =   1000 * 60 * 60;
			try {
				executionSemaphore.acquire();
				CurationJobRunner curationJobRunner = new CurationJobRunner();
				curationJobRunner.executeJob();
			} catch (Throwable th) {		
				log.error(th.getMessage());
				log.error(th.getCause());
			} finally {
				try {
					executionSemaphore.release();
				} catch (Throwable e) {
					log.error(e.getMessage());
					log.error(e.getCause());
					e.printStackTrace();
				}
				try {
					Thread.currentThread().sleep(timeBetweenExecution);
				} catch (Throwable e) {
					log.error(e.getMessage());
					log.error(e.getCause());
				}
				taskExecutor.execute(new CurationTask());
			}
		}
	}
}