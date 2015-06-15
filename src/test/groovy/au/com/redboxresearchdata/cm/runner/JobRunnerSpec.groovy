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

import grails.test.mixin.*
import spock.lang.*
import groovy.json.*
import au.com.redboxresearchdata.cm.domain.*
import au.com.redboxresearchdata.cm.id.*
import au.com.redboxresearchdata.cm.service.*
import au.com.redboxresearchdata.cm.controller.*
import org.springframework.beans.factory.annotation.*
/**
 * 
 * JobRunnerSpec
 *
 * @author <a target='_' href='https://github.com/shilob'>Shilo Banihit</a>
 *
 */
@TestFor(JobService)
@Mock([Curation, CurationJobItems, CurationJob, CurationStatusLookup, Entry, EntryTypeLookup])
class JobRunnerSpec extends Specification {
	def jobService //  = new JobService()
	def runner = JobRunner.instance
	
	def localProvider = [:]
	def nlaProvider = [:]

	def setup() {
		initDb()
		jobService = service
		jobService.grailsApplication = grailsApplication
	}
	

	def cleanup() {
	}
	
	def initDb() {
		def stats = ['in_progress', 'complete', 'failed', 'curating']
		def types = ['person', 'group', 'document', 'dataset', 'service', 'activity']
		grailsApplication.config.domain.lookups.curation_status_lookup = [:]
		grailsApplication.config.domain.lookups.entry_type_lookup = [:]
		
		stats.each {
			def stat = new CurationStatusLookup(value:it)
			stat.save()
			grailsApplication.config.domain.lookups.curation_status_lookup.put(it, stat)
		}
		types.each {
			def type = new EntryTypeLookup(value:it)
			type.save()
			grailsApplication.config.domain.lookups.entry_type_lookup.put(it, type)
		}
		grailsApplication.config.domain = grailsApplication.config.domain
		
		localProvider.curate = { oid, metadata ->
			println "Local Provider, curating: ${oid}"
			return new IdentifierResult(identityProviderId:'local', oid:oid, metadata:metadata, identifier:"${oid}_identifier")
		}
		nlaProvider.maxCount = 3
		nlaProvider.curate = { oid, metadata ->
			println "NLA Provider, curating: ${oid}"
			// simulate a long running action
			if (!nlaProvider[oid]) {
				nlaProvider[oid] = 0
			}
			if (nlaProvider[oid] < nlaProvider.maxCount) {
				nlaProvider[oid]++
				println "NLA Provider, count: ${nlaProvider[oid]} of ${nlaProvider.maxCount}"
				return new FutureResult(identityProviderId:'nla', oid:oid, metadata:metadata, identifier:'')
			}
			return new IdentifierResult(identityProviderId:'nla', oid:oid, metadata:metadata, identifier:"${oid}_identifier")
		}
		grailsApplication.config.id_providers['local'].instance = localProvider
		grailsApplication.config.id_providers['nla'].instance = nlaProvider
		grailsApplication.config.runner.job.in_progress.sleep_time = 100 // lessen sleep time so we can have several runs in a second
		grailsApplication.config.runner.job.curating.sleep_time = 100
	}

	void "test Job processing with synch and asynch ID provider, should pass..."() {
		when:
			def data = [
				[
					"oid": "1",
					"title": "title",
					"type": "activity",
					"required_identifiers": [
						[
							"identifier_type": "local",
							"metadata":["field1":"value1"]
						],
						[
							"identifier_type": "nla",
							"metadata":["field1":"value1"]
						]
					]
				],
				[
					"oid": "2",
					"title": "title",
					"type": "activity",
					"required_identifiers": [
						[
							"identifier_type": "nla",
							"metadata":["field1":"value1"]
						]
					]
				]
			]
			jobService.transactional = false
			def job = jobService.createJob(data)
			println job
			runner.start(grailsApplication.config, jobService)
			sleep(1000)
			runner.stop()
		then:
			job != null 
			runner.recentJobs[job.job_id] != null
			runner.recentJobs[job.job_id] == grailsApplication.config.domain.lookups.curation_status_lookup['complete'].value
	}
}
