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
import au.com.redboxresearchdata.cm.domain.Curation
import au.com.redboxresearchdata.cm.domain.Entry
import au.com.redboxresearchdata.cm.domain.EntryTypeLookup
import groovy.util.logging.Slf4j

/**
 * Update service only updates existing records, where the relevant data differs. New records need to use Import Service.
 * @version
 * @author <a href="matt@redboxresearchdata.com.au">Matt Mulholland</a>
 */
@Slf4j
class UpdateService extends MigrateService {

    @Override
    def process(incoming, existingCurations, importCollector) {
        existingCurations.any { existing ->
            log.debug("existing is: " + existing)
            if (incoming.equals(existing)) {
                importCollector.addMatched(incoming)
                existingCurations.remove(existing)
                return true
            } else if (CurationDto.mismatches(incoming, existing)) {
                // an update overwrites existing data
                save(incoming) ? importCollector.addSaved(incoming)
                        : importCollector.addError(incoming)
                existingCurations.remove(existing)
                return true
            }
            return false
            // anything not saved/matched/mismatched is an error. Updates do not persist new records.
        } || importCollector.addError(incoming)
    }


    @Override
    Entry saveOrUpdateEntry(Entry entry, EntryTypeLookup entryTypeLookup, item) {
        if (item.title != entry.title || item.type != entryTypeLookup.value) {
            entry.title = item.title
            entry.type = entryTypeLookup
            entry.save(failOnError: true, flush: true)
            log.debug("Entry updated.")
        } else {
            log.debug("entry exists and matches. Presuming update has already been applied.")
        }
        return entry
    }

    @Override
    boolean saveOrUpdateCuration(Entry entry, curationStatus, item) {
        Curation curation = Curation.findByEntry(entry)
        if (curation && curationStatus && item) {
            curation.identifier = item.identifier
            curation.identifier_type = item.identifier_type
            curation.status = curationStatus
            curation.metadata = item.metadata
            curation.dateCompleted = new Date()
            curation.save(failOnError: true, flush: true)
            log.debug("Curation updated is: " + curation)
        } else {
            throw new IllegalStateException("Curation object and associated data should exist! There might be a problem with existing data.")
        }
        return true
    }
}
