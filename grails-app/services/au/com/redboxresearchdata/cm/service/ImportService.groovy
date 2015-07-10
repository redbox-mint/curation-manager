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
class ImportService {
    static transactional = true
    static def CURATION_STATUS_COMPLETE_KEY
    static def STAT_MESSAGES
    static def ENTRY_TYPE_LOOKUP
    static def CONFIG

    @NotTransactional
    def batchImport(json) {
        try {
            if (json) {
                ImportDto importCollector = new ImportDto()
                for (item in json) {
                    if (isValid(item)) {
                        match(item, importCollector)
                    } else {
                        importCollector.addError(json)
                    }
                }
                log.debug("printing lists: " + importCollector)
                return [saved: importCollector.saved.size(), existing: importCollector.matched.size(), mismatched: importCollector.mismatched.size(), errors: importCollector.error.size()]
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

    @NotTransactional
    def match(item, ImportDto importCollector) {
        def incomingCurations = CurationDto.getInstances(item)
        log.debug("incoming curations: " + incomingCurations)
        def existingCurations = getExistingCurationsData(item)
        log.debug("existing curations: " + existingCurations)
        Iterator in_iterator = incomingCurations.iterator()
        Iterator ex_iterator = existingCurations.iterator()
        while (in_iterator.hasNext()) {
            log.debug("import dto is: " + importCollector)
            def incoming = in_iterator.next()
            while (ex_iterator.hasNext()) {
                def existing = ex_iterator.next()
                if (incoming.equals(existing)) {
                    importCollector.addMatched(incoming)
                    ex_iterator.remove()
                    incoming = null
                    break
                } else if (incoming.mismatches(existing)) {
                    importCollector.addMismatched(incoming)
                    ex_iterator.remove()
                    incoming = null
                    break
                }
            }
            if (incoming) {
                if (save(incoming)) {
                    importCollector.addSaved(incoming)
                } else {
                    importCollector.addError(incoming)
                }
            }
        }
    }


/**
 * if curation data exists both inside and outside 'required_identifiers' block, only the data
 * inside the block is considered.
 * @param data
 * @return
 */
    @NotTransactional
    def getExistingCurationsData(item) {
        log.debug("looking for existing curation matching: " + item)
        Entry entry = getExistingEntry(item)
        def curations
        if (item.required_identifiers) {
            curations = getExistingCurationsDataByIdentifier(item.required_identifiers, entry)
        } else {
            curations = getExistingCurationsDataByIdentifier([item], entry)
        }
        return curations
    }

    @NotTransactional
    def getExistingEntry(item) {
        def entryByOidAndTypeQuery = Entry.where {
            oid == item.oid && type.value == item.type
        }
        Entry entry = entryByOidAndTypeQuery.find()
        log.debug("existing entry is: " + entry)
        return entry
    }

    @NotTransactional
    def getExistingCurationsDataByIdentifier(curationsIdentifierData, existingEntry) {
        def curations = []
        curationsIdentifierData.each { identifier ->
            log.debug("identifier to find is: " + identifier)
            def query = Curation.where {
                entry == existingEntry && identifier_type == identifier['identifier_type']
            }
            query.find().each {
                log.debug("found identifier: " + it)
                curations.add(CurationDto.getInstance(it))
            }
        }
        return curations
    }

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
        if (!entry && entryType) {
            entry = new Entry(oid: item.oid, title: item.title, type: entryType)
            entry.save(failOnError: true)
            log.debug("Entry saved is: " + entry)
        }
        return entry
    }

}