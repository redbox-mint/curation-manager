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

import org.grails.web.converters.exceptions.ConverterException
import au.com.redboxresearchdata.cm.domain.*
import grails.converters.JSON
import groovy.json.*
/**
 * Job Contollers
 *
 * @author <a href="https://github.com/shilob" target="_blank">Shilo Banihit</a>
 * @since 0.1
 *
 */
class JobController {
	
	def jobService
	
	/**
	 * Shows a list of Jobs per status. TODO: implement it.
	 * 
	 * @return list of Jobs of the specified status
	 */
    def index() {
	}
	
	/**
	 * Creates a new job.
	 * 
	 * e.g.:
	 * [
		    {
		        "oid": "123",
		        "title": "Jim Smith",
		        "type": "person",
		        "required_identifiers": [
		            {
		                "identifier_type": "local"
		            },
		            {
		                "identifier_type": "nla",
		                "metadata": {}
		            }
		        ]
		    },
		    {
		        "oid": "z54",
		        "title": "Human Computer research",
		        "type": "activity"
		        "required_identifiers": [
		            {
		                "identifier_type": "local"
		            }
		        ]
		    }
		    ...
		]
	 *	
	 * https://qcifltd.atlassian.net/wiki/display/REDBOX/Curation+Manager+Process+and+Validation+Rules
	 * https://qcifltd.atlassian.net/wiki/display/REDBOX/Curation+Manager+API
	 * 
	 * @return Job information
	 */
	def create() {
		def createData = jobService.createJob(request.JSON)
		if (createData.message) {
			render(status:createData.status, text:createData.message)
		} else {
			render createData as JSON
		}
	}

	def show() {
		render jobService.getJob(params.id) as JSON
	}
	
	def showOid() {		
		render jobService.getOid(params.id) as JSON
	}
}
