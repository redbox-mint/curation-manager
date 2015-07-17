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

package au.com.redboxresearchdata.cm.data

import au.com.redboxresearchdata.cm.domain.Curation
import au.com.redboxresearchdata.cm.domain.Entry
import groovy.transform.Canonical
import groovy.util.logging.Slf4j

/**
 * @version
 * @author <a href="matt@redboxresearchdata.com.au">Matt Mulholland</a>
 */
@Slf4j
@Canonical
class CurationDto {
    EntryDto entry
    String identifier_type
    String identifier
    String metadata
    static def FILTERS

    static CurationDto getInstance(def data, EntryDto entryData) {
        return new CurationDto(entry: entryData, identifier: data.identifier, identifier_type: data.identifier_type, metadata: getCheckedMetadata(data))
    }

    static CurationDto getExistingInstance(Curation curation) {
        EntryDto entryData = EntryDto.getInstance(curation.entry)
        return getInstance(curation, entryData)
    }

    static def getInstances(data) {
        EntryDto entryData = EntryDto.getInstance(data)
        return findByRequiredIdentifiers(getCurations, data, entryData)
    }

    static def getExistingInstances(item) {
        Entry entry = Entry.oidAndTypeCriteria(item).find()
        return findByRequiredIdentifiers(getExistingCurations, item, entry)
    }

    static def getCurations = { curationsIdentifierData, entry ->
        return curationsIdentifierData.collect { curation ->
            getInstance(curation, entry)
        }
    }

    static def getExistingCurations = { curationsIdentifierData, existingEntry ->
        def curations = []
        curationsIdentifierData.each {
            log.debug("looking for curations with: " + it)
            def found = Curation.entryAndTypeCriteria(existingEntry, it).find()
            log.debug("found identifier: " + found)
            if (found) {
                curations.add(getExistingInstance(found))
            }
        }
        log.debug("returning existing curations found: " + curations)
        return curations
    }

    /**
     * if curation data exists both inside and outside 'required_identifiers' block, only the data
     * inside the block is considered.
     * @param data
     * @return
     */
    static def findByRequiredIdentifiers(findClosure, item, entry) {
        def curations
        if (item.required_identifiers) {
            log.debug("checking required identifiers...")
            curations = findClosure(item.required_identifiers, entry)
        } else {
            curations = findClosure([item], entry)
        }
        return curations
    }

    static def getCheckedMetadata(data) {
        def metadata
        if (isMetadataExcluded()) {
            log.debug("excluding metadata...")
        } else {
            log.debug("including metadata...")
            metadata = data.metadata
        }
        return metadata
    }

    static def isMetadataExcluded() {
        return FILTERS.contains('metadata')
    }

    def mismatches(CurationDto other) {
        return other in CurationDto && !this.equals(other) &&
                this.entry.oid == other?.entry?.oid &&
                this.identifier_type == other?.identifier_type
    }

    def map() {
        def map = [:]
        map["oid"] = this.entry.oid
        map["type"] = this.entry.type
        map["title"] = this.entry.title
        map["identifier_type"] = this.identifier_type
        map["identifier"] = this.identifier
        map["metadata"] = this.metadata
        return map

    }
}
