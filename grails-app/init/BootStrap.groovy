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
import grails.util.Environment
import au.com.redboxresearchdata.cm.domain.*
/**
 * Grails Bootstrap class
 *
 * @author <a href="https://github.com/shilob" target="_blank">Shilo Banihit</a>
 * @since 1.0
 *
 */
class BootStrap {
	def grailsApplication
	
    def init = { servletContext ->
		log.info("Curation Manager BootStrap starting...")
		// Shilo's note to self: refactor this later to become more generic, etc. 
		log.debug("Checking domain information...")
		log.debug("Checking curation_status_lookup...")
		def statusConfig = grailsApplication.config.domain.curation_status_lookup
		def statusList = CurationStatusLookup.list()
		if (statusList.size() != statusConfig.size()) {
			log.debug("Loading curation_status_lookup data...")
			def statusListValues = []
			statusList.each {
				statusListValues << it.value
			}
			def diff = getMissingRecords(statusListValues, statusConfig)
			diff.each { 
				log.debug("Adding status: ${it}")
				def newStatus = new CurationStatusLookup(value:it)
				newStatus.save(flush:true)
				if (!grailsApplication.config.domain.lookups.curation_status_lookup) {
					grailsApplication.config.domain.lookups.curation_status_lookup = [:]
				}
				grailsApplication.config.domain.lookups.curation_status_lookup.put(it, newStatus)
			}
		}
		def entryTypeConfig = grailsApplication.config.domain.entry_type_lookup
		def entryTypeList = EntryTypeLookup.list()
		if (entryTypeList.size() != entryTypeConfig.size()) {
			log.debug("Loading entry_type_lookup data...")
			def entryTypeListValues = []
			statusList.each {
				entryTypeListValues << it.value
			}
			def diff = getMissingRecords(entryTypeListValues, entryTypeConfig)
			diff.each {
				log.debug("Adding entry type: ${it}")
				def newEntryType = new EntryTypeLookup(value:it)
				newEntryType.save(flush:true)
				if (!grailsApplication.config.domain.lookups.entry_type_lookup) {
					grailsApplication.config.domain.lookups.entry_type_lookup = [:]
				}
				grailsApplication.config.domain.lookups.curation_status_lookup.put(it, newEntryType)
			}
		}
    }
	
    def destroy = {
    }
	
	def getMissingRecords = { existing, target -> 
		def commons = target.intersect(existing)
		def diff = target.plus(existing)
		diff.removeAll(commons)
		return diff
	}
	
}
