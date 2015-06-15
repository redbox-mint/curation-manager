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

import au.com.redboxresearchdata.cm.id.IdentifierResult
import au.com.redboxresearchdata.cm.domain.*
import java.util.concurrent.*
import groovy.util.logging.Slf4j
import groovy.json.*

/**
 * JobRunner
 * 
 * A singleton for running jobs. See https://qcifltd.atlassian.net/wiki/display/REDBOX/Job+Runner+subsystem
 * 
 * Should be spawned ideally from BootStrap.
 * 
 * Design document is at: https://qcifltd.atlassian.net/wiki/display/REDBOX/Job+Runner+subsystem
 *
 * @author <a target='_' href='https://github.com/shilob'>Shilo Banihit</a>
 *
 */
@Slf4j
@Singleton
class JobRunner {
	def jobService
	def thread
	def config
	def statCompleted
	def statCurating
	def statFailed
	def recentJobs
	def recentJobsMax
	
	private static Object runnerMap = [:]
	
	def start(config, jobService) {
		this.config = config
		this.jobService = jobService
		this.statCompleted =  config.domain.lookups.curation_status_lookup['complete']
		this.statCurating =  config.domain.lookups.curation_status_lookup['curating']
		this.statFailed =  config.domain.lookups.curation_status_lookup['failed']
		this.recentJobs = [:]
		this.recentJobsMax = config.runner.job.max_recent
		// get the list of runners and init the flags
		def runnerIds = config.runner.job.curation_runners
		runnerIds.each { runnerId ->
			runnerMap[runnerId] = [:]
			runnerMap[runnerId].id = runnerId
			runnerMap[runnerId].runForestRun = true
			runnerMap[runnerId].thread_pool_count = config.runner.job[runnerId].thread_pool_count
			runnerMap[runnerId].sleep_time = config.runner.job[runnerId].sleep_time
			launchRunner(runnerMap[runnerId])
		}
	}
	
	def launchRunner = { runnerMapConfig ->
		runnerMapConfig.threadPool = Executors.newFixedThreadPool(runnerMapConfig.thread_pool_count)
		
		runnerMapConfig.thread = Thread.start {
			synchronized(runnerMapConfig) {
				while (runnerMapConfig.runForestRun) {
					// retrieve status jobs
					log.debug "Processing jobs with status: ${runnerMapConfig.id}"
					def jobs = jobService.getJobs(config.domain.lookups.curation_status_lookup[runnerMapConfig.id])
					def futures = jobs.collect { job ->
						log.debug "Submitting to ${runnerMapConfig.id} thread pool: ${job.id}"
						runnerMapConfig.threadPool.submit({-> processJob job} as Callable)
					}
					log.debug "Waiting for ${runnerMapConfig.id} threads to return..."
					futures.each { it.get() }
					log.debug "${runnerMapConfig.id} Runner sleeping..."
					runnerMapConfig.wait(runnerMapConfig.sleep_time) // required so other threads can grab the lock
				}
			}
		}
	}
	
	def processJob = { job ->
		def processedJob = jobService.curateJob(job)	
		if (recentJobs.size() > recentJobsMax) {
			log.debug "Recent jobs maxed out, clearing..."
			recentJobs.clear()
		}
		recentJobs[processedJob.id] = processedJob.status.value
		log.debug "Process Job: ${processedJob.id}, status: ${processedJob.status.value}"
	}
	
	def stop() {
		log.debug "Runner stopping..."
		runnerMap.each { id, runnerMapConfig ->
			synchronized(runnerMapConfig) {
				runnerMapConfig?.runForestRun = false
				runnerMapConfig?.notifyAll()
			}
			runnerMapConfig.thread?.join()
		}
		log.debug "Runner stopped."
	}
}
