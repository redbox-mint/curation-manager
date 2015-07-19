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
import au.com.redboxresearchdata.cm.data.ImportDto
import au.com.redboxresearchdata.cm.domain.Curation
import au.com.redboxresearchdata.cm.domain.Entry
import au.com.redboxresearchdata.cm.domain.EntryTypeLookup
import au.com.redboxresearchdata.cm.exception.IdProviderException
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
    static def ENTRY_TYPE_LOOKUP
    static def CONFIG

    def batchImport(json) throws ConverterException {
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
    }

    def isValid(item) {
        return (item.oid && item.title && item.type && ENTRY_TYPE_LOOKUP[item.type])
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

    boolean save(item) {
        def hasSaved = false
        Entry.withTransaction { status ->
            try {
                hasSaved = saveCuration(saveEntry(item), item)
                status.flush()
            } catch (Exception e) {
                log.error(e.getMessage())
                log.error("Problem with saving curation data. rolling back...")
                status.setRollbackOnly()
            } finally {
                return hasSaved
            }
        }
    }

    Entry saveEntry(item) {
        Entry entry = Entry.findByOid(item.oid)
        EntryTypeLookup entryType = ENTRY_TYPE_LOOKUP[item.type]
        //check that entry has not already persisted.
        return saveOrUpdateEntry(entry, entryType, item)
    }

    boolean saveCuration(Entry entry, item) {
        def curationStatus = CONFIG?.domain?.lookups?.curation_status_lookup[CURATION_STATUS_COMPLETE_KEY]
        def idProviders = CONFIG.id_providers.enabled
        if (!entry || !curationStatus || !idProviders.contains(item.identifier_type)) {
            throw new IllegalStateException("Required properties missing for curation save/update.")
        }
        return saveOrUpdateCuration(entry, curationStatus, item)
    }

    abstract Entry saveOrUpdateEntry(Entry entry, EntryTypeLookup entryTypeLookup, item);

    abstract boolean saveOrUpdateCuration(Entry entry, curationStatus, item);

}
