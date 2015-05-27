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
import groovy.json.*
/**
 * Job Contollers
 *
 * @author <a href="https://github.com/shilob" target="_blank">Shilo Banihit</a>
 * @since 0.1
 *
 */
class JobController {
	
	/**
	 * Shows a list of Jobs per status.
	 * 
	 * @return list of Jobs of the specified status
	 */
    def index() {
		// only will return if status is specified. 
		if (param.status) {
			// query the status
			def status = CurationStatusLookup.findByValue(param.status)
			if (status) {
				def jobs = CurationJob.findAllByStatus(status)
				jobs.each { job->
					// 
				}
			}
		}
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
	 * 
	 * @return Job information
	 */
	def create() {
		def config = grailsApplication.config
		def startStat = config.domain.lookups.curation_status_lookup[config.api.job.init_status]
		def statFailedVal = config.api.job.error.failed_validation
		def statParseError = config.api.job.error.parse_error
		def statNoIdType = config.api.job.error.invalid_id_type
		def statMismatchedType = config.api.job.error.mismatch_type
		def statNothing = config.api.job.error.nothing_to_curate
		println("Got request for a curation job...")
		try {
			def json = request.JSON
			println "Got data:" 
			println json
			// array must at least have an entry...
			if (json.size() > 0) {
				// creating the Job request
				def job = new CurationJob(status:startStat)
				for (item in json) {
					// validating the entries
					// 1.1 - require oid, title, type fields
					if (!item.oid || !item.title || !item.type) {
						return render(status:statFailedVal.code, statFailedVal.message) 
					}
					// 1.1 - validate that we handle this item type
					def entryType = config.domain.lookups.entry_type_lookup[item.type]
					if (!entryType) {
						return render(status:statFailedVal.code, statFailedVal.message)
					}
					def jobItem = new CurationJobItems()
					// retrieve if there's an existing entry for this type
					def entry = Entry.get(item.oid)
					// 1.3 - validate to see if the OID exists but conflicting type. Implicitly, it means that duplicate items are ignored.
					if (entry && entry.type.value != item.type) {
						return render(status:statMismatchedType.code, sprintf(statMismatchedType.message, item.type, entry.type.value))
					} else {
						// if entry exits, update the title
						if (entry) {
							log.debug("Existing entry title updated.")
							entry.title = item.title
						} else {
							log.debug("New entry record added.")
							entry = new Entry(oid:item.oid, title:item.title, type:entryType)
						}
						entry.save()
					}
					item.required_identifiers.each { reqId ->
						// 1.2 - validate that we handle this identity provider
						if (!reqId.identifier_type) {
							return render(status:statFailedVal.code, statFailedVal.message)
						}
						// 1.2 - list through the enabled ID providers if the type is there....
						def idProviders = config.id_providers.enabled
						def hasProvider = false
						for (idProvider in idProviders) {
							if (idProvider == reqId.identifier_type) {
								// we got a match, proceed
								hasProvider = true
								def metadata = reqId?.metadata ? JsonOutput.toJson(reqId.metadata) : null
								// check if there is an existing curation record
								def curation = Curation.findByEntry(entry)
								if (curation) {
									log.debug("Curation record exists for oid:" + entry.oid)
								} else {
									curation = new Curation(entry:entry, identifier_type:reqId.identifier_type, status:startStat, jobItem:jobItem, metadata:metadata)
									jobItem.addToCurations(curation)
									curation.save()
								}
							}
						}
						if (!hasProvider) {
							// means we don't have a ID provider match
							return render(status:statNoIdType.code, sprintf(statNoIdType.message, reqId.identifier_type))
						}
					}
					job.addToJobItems(jobItem)
				}
				
				job.save(flush:true)
				// return the job data
				render getJobInfo(job.id)
			} else {
				return render(status:statNothing.code, statNothing.message)
			}
		} catch (ConverterException e) {
			render(status:statParseError.code, statParseError.message)
		}
	}
	
	def show() {
		getJobInfo(params.id)
		render data
	}
	
	def getJobInfo(jobId) {
		def data = []
		if (jobId) {
			log.debug("Getting job id:" + params.id)
			def job = Curation.get(params.id)
			if (job) {
				data.job_id = job.id
				data.status = job.status.value
				data.date_created = job.date_created
				data.date_completed = job.date_completed
				data.job_items = []
			}
		}
		return data
	}
}
