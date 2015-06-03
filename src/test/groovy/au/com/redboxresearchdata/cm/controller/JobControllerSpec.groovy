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
package au.com.redboxresearchdata.cm.controller

import grails.test.mixin.*
import spock.lang.*
import groovy.json.*
import au.com.redboxresearchdata.cm.domain.*
import au.com.redboxresearchdata.cm.service.*

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */

@TestFor(JobController)
@Mock([Curation, CurationJobItems, CurationJob, CurationStatusLookup,  Entry, EntryTypeLookup, JobService])
class JobControllerSpec extends Specification {
	def jobService = new JobService()
	
    def setup() {
		initDb()
		jobService.grailsApplication = grailsApplication
		controller.jobService = jobService
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
		controller.grailsApplication.config.domain = grailsApplication.config.domain
	}

    void "test CurationJob creation, should pass..."() {
		def data = [
				[
			        "oid": "1",
			        "title": "title",
			        "type": "activity",
			        "required_identifiers": [
			            [
			                "identifier_type": "local",
							"metadata":["field1":"value1"]
			            ]
			        ]
			    ]
			]
		def orig_resp_json = null
		def get_resp_json = null
		when:
			request.method = 'POST'
			controller.params.format = 'json'
			request.JSON =  data
			controller.create()
			orig_resp_json = response.json
        then:"a pass: with record created.."
			println "Create response code:" + response.status
			println "Create response text:" + response.text
			response.status == 200
			response.json.job_id != null
			response.json.status == "in_progress"
			response.json.date_created != null
			response.json.job_items[0] != null
			response.json.job_items[0].required_identifiers[0].identifier_type == data[0].required_identifiers[0].identifier_type
			response.json.job_items[0].required_identifiers[0].status == "in_progress"
			new JsonSlurper().parseText(response.json.job_items[0].required_identifiers[0].metadata).field1 == "value1"
			response.json.job_items[0].oid == data[0].oid
			response.json.job_items[0].title == data[0].title
			response.json.job_items[0].type == data[0].type
			response.reset()
		when:
			request.method = 'GET'
			controller.params.format = ''
			controller.params.id = orig_resp_json.job_id
			controller.show()
			get_resp_json = response.json
		then:
			println "Get Job response code:" + response.status
			println "Get Job response text:" + response.text
			response.status == 200
			response.json.job_id != null
			response.json.status == "in_progress"
			response.json.date_created != null
			response.json.job_items[0] != null
			response.json.job_items[0].required_identifiers[0].identifier_type == data[0].required_identifiers[0].identifier_type
			response.json.job_items[0].required_identifiers[0].status == "in_progress"
			new JsonSlurper().parseText(response.json.job_items[0].required_identifiers[0].metadata).field1 == "value1"
			response.json.job_items[0].oid == data[0].oid
			response.json.job_items[0].title == data[0].title
			response.json.job_items[0].type == data[0].type
			response.reset()
		when:
			request.method = 'GET'
			controller.params.format = ''
			controller.params.id = data[0].oid
			controller.showOid()
		then:
			println "Get OID response code:" + response.status
			println "Get OID response text:" + response.text
			response.status == 200
			response.json.required_identifiers[0].identifier_type == data[0].required_identifiers[0].identifier_type
			response.json.required_identifiers[0].status == "in_progress"
			new JsonSlurper().parseText(response.json.required_identifiers[0].metadata).field1 == "value1"
			response.json.oid == data[0].oid
			response.json.title == data[0].title
			response.json.type == data[0].type
    }
	
	
	void "test CurationJob creation, with lacking oid parameter, should fail..."() {
		def data = [
				[
					"title": "title",
					"type": "activity",
					"required_identifiers": [
						[
							"identifier_type": "local"
						]
					]
				]
			]
		when:
			request.method = 'POST'
			controller.params.format = 'json'
			request.JSON =  data
			controller.create()
		then:"a pass: with record created.."
			println "Fail create response code:" + response.status
			println "Fail create response text:" + response.text
			response.status == 400
	}
	
}
