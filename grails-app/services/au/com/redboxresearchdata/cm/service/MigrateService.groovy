/*
 * === Copyright ===
 *
 *  Copyright (C) 2013 Queensland Cyber Infrastructure Foundation (http://www.qcif.edu.au/)
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License along
 *    with this program; if not, write to the Free Software Foundation, Inc.,
 *    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package au.com.redboxresearchdata.cm.service

import au.com.redboxresearchdata.cm.data.CurationDto
import au.com.redboxresearchdata.cm.data.EntryDto
import au.com.redboxresearchdata.cm.data.ImportDto
import au.com.redboxresearchdata.cm.domain.Curation
import au.com.redboxresearchdata.cm.domain.Entry
import grails.transaction.NotTransactional
import groovy.util.logging.Slf4j
import org.grails.web.converters.exceptions.ConverterException

/**
 * @version
 * @author <a href="matt@redboxresearchdata.com.au">Matt Mulholland</a>
 */

@Slf4j
abstract class MigrateService {
    static transactional = true

    static def CURATION_STATUS_COMPLETE_KEY
    static def STAT_MESSAGES
    static def ENTRY_TYPE_LOOKUP
    static def CONFIG

    def batchImport(json) {
        try {
            if (json) {
                ImportDto importCollector = new ImportDto()
                for (item in json) {
                    if (isValid(item)) {
                        match(item, importCollector)
                    } else {
                        log.debug("invalid item: " + item + " Adding to error...")
                        importCollector.addError(item)
                    }
                }
                log.debug("printing results: " + importCollector)
                return importCollector
            } else {
                return [status: statNothing.code, message: STAT_MESSAGES.nothing_to_import.message]
            }
        } catch (ConverterException e) {
            return [status: statParseError.code, message: STAT_MESSAGES.parse_error.message]
        }
    }

    def isValid(item) {
        return (item.oid && item.title && item.type)
    }

    def match(item, ImportDto importCollector) {
        def incomingCurations = CurationDto.getInstances(item)
        log.debug("incoming curations: " + incomingCurations)
        def existingCurations = CurationDto.getExistingInstances(item)
        incomingCurations.each {
            process(it, existingCurations, importCollector)
        }
    }

    def abstract process(incoming, existingCurations, importCollector)

    boolean save(CurationDto item) {
        try {
            def curationStatus = CONFIG?.domain?.lookups?.curation_status_lookup[CURATION_STATUS_COMPLETE_KEY]
            def identifierType = item?.identifier_type
            Entry entry = saveEntry(item.entry)
            if (!identifierType || !curationStatus || !entry) {
                log.debug("Invalid curation properties found. Aborting save.")
                return false
            }
            def curation = new Curation(entry: entry, identifier: item.identifier, identifier_type: identifierType, status: curationStatus, metadata: item.metadata, dateCompleted: new Date())
            curation.save(failOnError: true)
            log.debug("Curation saved is: " + curation)
            return true
        } catch (Exception e) {
            log.error("Problem with saving curation data.", e)
            return false
        }
    }

    Entry saveEntry(EntryDto item) throws Exception {
        def entryType = ENTRY_TYPE_LOOKUP[item.type]
        log.debug("entry type is: " + entryType)
        //check that entry has not already persisted.
        Entry entry = Entry.findByOid(item.oid)
        if ((!entry) && entryType) {
            entry = new Entry(oid: item.oid, title: item.title, type: entryType)
            entry.save(failOnError: true)
            log.debug("Entry saved.")
        }
        log.debug("Entry was: " + entry)
        return entry
    }
}
