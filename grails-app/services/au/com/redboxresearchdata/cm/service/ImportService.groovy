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
 * Import service imports new records only. Existing records that require changes need to use Update service.
 * @version
 * @author <a href="matt@redboxresearchdata.com.au">Matt Mulholland</a>
 */
@Slf4j
class ImportService extends MigrateService {
    @Override
    def process(incoming, existingCurations, importCollector) {
        existingCurations.any { existing ->
            log.debug("existing is: " + existing)
            if (incoming.equals(existing)) {
                importCollector.addMatched(incoming)
                existingCurations.remove(existing)
                return true
            } else if (CurationDto.mismatches(incoming, existing)) {
                importCollector.addMismatched(incoming)
                existingCurations.remove(existing)
                return true
            }
            return false
        } || (save(incoming) ? importCollector.addSaved(incoming) : importCollector.addError(incoming))
    }

    Entry saveOrUpdateEntry(Entry entry, EntryTypeLookup entryTypeLookup, item) {
        if (!entry) {
            entry = new Entry(oid: item.oid, title: item.title, type: entryTypeLookup)
            entry.save(failOnError: true)
            log.debug("Entry saved: " + entry)
        } else if (item.title != entry.title || item.type != entry.type.value) {
            throw new IllegalStateException("Entry should match for a previous save with different curation identifier. Aborting...")
        } else {
            log.debug("entry exists. Presuming save has already been applied.")
        }
        return entry
    }

    boolean saveOrUpdateCuration(Entry entry, curationStatus, item) {
        Curation curation = new Curation(entry: entry, identifier: item.identifier, identifier_type: item.identifier_type, status: curationStatus, metadata: item.metadata, dateCompleted: new Date())
        curation.save(failOnError: true, flush: true)
        log.debug("Curation saved is: " + curation)
        return true
    }

}