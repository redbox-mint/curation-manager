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

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */

@TestFor(JobController)
@Mock([Curation, CurationJobItems, CurationJob, Entry, EntryTypeLookup])
class JobControllerSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test Curation Job creation, all parameters..."() {
		def data = [
				[
			        "oid": "1",
			        "title": "title",
			        "type": "activity",
			        "required_identifiers": [
			            [
			                "identifier_type": "local"
			            ]
			        ]
			    ],
				[
					"oid": "2",
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
			println "Got response code:" + response.status
			println "Got response text:" + response.text
			response.status == 200
			response.text != null
    }
}
