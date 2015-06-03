/*******************************************************************************
 * Copyright (C) 2015 Queensland Cyber Infrastructure Foundation (http://www.qcif.edu.au/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 ******************************************************************************/
package au.com.redboxresearchdata.cm.runner

import java.util.concurrent.*

/**
 * JobRunner
 * 
 * A singleton for running jobs. See https://qcifltd.atlassian.net/wiki/display/REDBOX/Job+Runner+subsystem
 * 
 * Should be spawned ideally from BootStrap.
 *
 * @author <a target='_' href='https://github.com/shilob'>Shilo Banihit</a>
 *
 */
@Singleton
class JobRunner {
	def jobService
	def thread
	def config
	def threadPool
	
	private static Object runFlag = [runForestRun:true]
	
	def start(config, jobService) {
		this.config = config
		this.jobService = jobService
		
		threadPool = Executors.newFixedThreadPool(config.runner.job.thread_pool_count)
		
		thread = Thread.start {
			synchronized(runFlag) {
				while (runFlag.runForestRun) {
					// retrieve the "in_progress" jobs...
					def startStat = config.domain.lookups.curation_status_lookup[config.api.job.init_status]
					def jobs = jobService.getJobs(startStat)
					def futures = jobs.collect{job->
						threadPool.submit({-> processJob job} as Callable)
					}
					futures.each { it.get() }
					runFlag.wait(config.runner.job.sleep_time)
				}
			}
		}
	}
	// each Job is process by its own thread...
	def processJob = { job->
		
	}
	
	def stop() {
		synchronized(runFlag) {
			runFlag.runForestRun = false
			runFlag.notifyAll()
		}
		thread.join()
	}
}
