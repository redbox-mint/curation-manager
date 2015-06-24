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
package au.com.redboxresearchdata.cm.service

import au.com.redboxresearchdata.cm.domain.*
import au.com.redboxresearchdata.cm.id.IdentifierResult
import org.grails.web.converters.exceptions.ConverterException
import groovy.json.*
import groovy.util.logging.Slf4j

/**
 * JobService
 *
 * @author <a target='_' href='https://github.com/shilob'>Shilo Banihit</a>
 *
 */
@Slf4j
class JobService {
	static transactional = true
	def grailsApplication
	
	def createJob(json) {
		def config = grailsApplication.config
		def startStat = config.domain.lookups.curation_status_lookup[config.api.job.init_status]
		def statFailedVal = config.api.job.error.failed_validation
		def statParseError = config.api.job.error.parse_error
		def statNoIdType = config.api.job.error.invalid_id_type
		def statMismatchedType = config.api.job.error.mismatch_type
		def statNothing = config.api.job.error.nothing_to_curate
		
		try {
			// array must at least have an entry...
			if (json.size() > 0) {
				// creating the Job request
				def job = new CurationJob(status:startStat)
				for (item in json) {
					// validating the entries
					// 1.1 - require oid, title, type fields
					if (!item.oid || !item.title || !item.type) {
						return [status:statFailedVal.code, message:sprintf(statFailedVal.message, sprintf("OID: '%s' Title: '%s' Type: '%s'", item.oid, item.title, item.type))]
					}
					// 1.1 - validate that we handle this item type
					
					def entryType = config.domain.lookups.entry_type_lookup[item.type]
					if (!entryType) {
						return render(status:statFailedVal.code, sprintf(statFailedVal.message, "Entry type not found on DB:" + item.type + ", " + config.domain.lookups.entry_type_lookup))
					}
					def jobItem = new CurationJobItems(job:job)
					// retrieve if there's an existing entry for this type
					def entry = Entry.findByOid("${item.oid}") // explicitly convert to string
					// 1.3 - validate to see if the OID exists but conflicting type. Implicitly, it means that duplicate items are ignored.
					if (entry && entry.type.value != item.type) {
						return [status:statMismatchedType.code, message:sprintf(statMismatchedType.message, item.type, entry.type.value)]
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
							return [status:statFailedVal.code, message:sprintf(statFailedVal.message,  "No identifier type")]
						}
						// 1.2 - list through the enabled ID providers if the type is there....
						def idProviders = config.id_providers.enabled
						def hasProvider = false
						for (idProvider in idProviders) {
							if (idProvider == reqId.identifier_type) {
								// we got a match, proceed
								hasProvider = true
								String metadata = reqId?.metadata ? JsonOutput.toJson(reqId.metadata) : "{}" // setting to empty Object as per https://qcifltd.atlassian.net/wiki/display/REDBOX/Identity+Providers#IdentityProviders-Result
								// check if there is an existing curation record
								def qCuration = Curation.where {
									entry == entry && identifier_type == reqId.identifier_type
								}
								def curation = qCuration.find()
								if (curation) {
									 log.debug "Doing nothing, curation record exists for oid:" + entry.oid
									 jobItem.addToCurations(curation)
								} else {
									curation = new Curation(entry:entry, identifier_type:reqId.identifier_type, status:startStat, jobItem:jobItem, metadata:metadata)
									curation.save()
									jobItem.addToCurations(curation)
								}
							}
						}
						if (!hasProvider) {
							// means we don't have a ID provider match
							return [status:statNoIdType.code, message:sprintf(statNoIdType.message, reqId.identifier_type)]
						}
					}
					if (!jobItem.validate()) {
						jobItem.errors.each {
							log.error it
						}
					}
					jobItem.save()
					job.addToItems(jobItem)
				}
				if (!job.validate()) {
					job.errors.each {
						log.error it
					}
				} else {
					job.save(flush:true)
				}
				// return the job data
				return getJobMap(job)
			} else {
				return [status:statNothing.code, message:statNothing.message]
			}
		} catch (ConverterException e) {
			return [status:statParseError.code, message:statParseError.message]
		}
		
	}
	
	def getJob(jobId) {
		def job = CurationJob.get(jobId)
		return getJobMap(job)
	}
	
	def getJobMap(job) {
		def data = [:]
		if (job) {
			data.job_id = job.id
			data.status = job.status.value
			data.date_created = job.dateCreated
			data.date_completed = job.dateCompleted
			
			data.job_items = []
			job.items.each { jobItem ->
				def item = [:]
				item.required_identifiers = []
				jobItem.curations.each {curation ->
					if (!item.oid) {
						// oid is in the Entry and not on the Job item, so set only once
						item.oid = curation.entry.oid
						item.title = curation.entry.title
						item.type = curation.entry.type.value
					}
					def req_id = [:]
					req_id.identifier_type = curation.identifier_type
					req_id.status = curation.status.value
					req_id.identifier = curation.identifier
					req_id.error = curation.error
					req_id.date_completed = curation.dateCompleted
					req_id.metadata = curation.metadata
					item.required_identifiers << req_id
				}
				data.job_items << item
			}
		} else {
			log.debug "Job was null, returning empty job map."
		}
		return data
	}
	
	def getOid(oid) {
		def item = [:]
		item.required_identifiers = []
		def entry = Entry.findByOid(oid)
		def curations = Curation.findByEntry(entry)
		curations?.each {curation->
			if (!item.oid) {
				item.oid = curation.entry.oid
				item.title = curation.entry.title
				item.type = curation.entry.type.value
			}
			def req_id = [:]
			req_id.identifier_type = curation.identifier_type
			req_id.status = curation.status.value
			req_id.identifier = curation.identifier
			req_id.error = curation.error
			req_id.date_completed = curation.dateCompleted
			req_id.metadata = curation.metadata
			item.required_identifiers << req_id
		}		
		return item
	}

	/**
	 * Returns a list of Jobs of the supplied status
	 * 
	 * @param status
	 * @return List of Jobs ordered by date created, ascending
	 */
	def getJobs(status) {
		return CurationJob.findAllByStatus(status, [sort:'dateCreated', order:'asc'])
	}
	
	def curateJob(job) {
		def config = grailsApplication.config
		def statCompleted =  config.domain.lookups.curation_status_lookup['complete']
		def statCurating =  config.domain.lookups.curation_status_lookup['curating']
		def statFailed =  config.domain.lookups.curation_status_lookup['failed']
		
		log.debug "Curating job: ${job.id}"
		def jobStat = statCompleted // assume the Job is completed, will change depending on conditions below
		
		job.items.each { jobItem ->
			log.debug "Job iterating getting curations..."
			jobItem.curations.each {curation ->
				log.debug "Checking if already curated..."
				// optimization: Ideally curate() will return a consistent identifier for each oid, but we check if the date completed isn't set before calling it.
				if (curation.dateCompleted != null) {
					// means this is already curated
					log.debug "Already curated. ${getIdTraceStr(curation, job)}"
					return
				}
				def idProvider = config.id_providers[curation.identifier_type].instance
				if (idProvider != null) {
					try {
						def result = idProvider.curate(String.valueOf(curation.entry.oid), JsonOutput.toJson(curation.metadata))
						if (result instanceof IdentifierResult) {
							curation.identifier = result.getIdentifier()
							curation.status = statCompleted
							curation.error = null
							curation.dateCompleted = new Date()
							log.debug "Curated. ${getIdTraceStr(curation,job)}"
						} else {
							// assume a FutureResult
							curation.identifier = null
							curation.status = statCurating
							curation.error = null
							jobStat = statCurating
						}
					} catch (Exception e) {
						log.error "Calling curate() threw an exception.  ${getIdTraceStr(curation,job)}", e
						curation.identifier = null
						curation.status = statFailed
						curation.error = e.toString()
						curation.dateCompleted = new Date()
						jobStat = statFailed
					}
					curation.save(flush:true, failOnError:true)
				} else {
					log.error "Cannot find ID Provider instance, please check your configuration. ${getIdTraceStr(curation,job)}"
				}
			}
		}
		if (jobStat != statCurating) {
			job.dateCompleted = new Date()
		}
		job.status = jobStat
		if (!job.validate()) {
			job.errors.each {
				log.debug it
			}
		}
		job.save(flush:true, failOnError:true)
		log.debug "Curate Job: ${job.id}, status: ${job.status.value}"
		return job
	}
	
	def getIdTraceStr(curation, job) {
		return "Entry OID: '${curation.entry.oid}', ID Provider: '${curation.identifier_type}', Job ID: '${job.id}'"
	}
}
