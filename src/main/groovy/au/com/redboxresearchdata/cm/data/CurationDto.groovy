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
import au.com.redboxresearchdata.cm.domain.EntryTypeLookup
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

    static def getInstance(def data, EntryDto entryData) {
        return new CurationDto(entry: entryData, identifier: data.identifier, identifier_type: data.identifier_type, metadata: getCheckedMetadata(data))
    }

    static def getInstance(Curation curation) {
        EntryDto entryData = EntryDto.getInstance(curation.entry)
        return getInstance(curation, entryData)
    }

    /**
     * if curation data exists both inside and outside 'required_identifiers' block, only the data
     * inside the block is considered.
     * @param data
     * @return
     */
    static def getInstances(data) {
        EntryDto entryData = EntryDto.getInstance(data)
        def curations
        if (data.required_identifiers) {
            curations = getCurations(data.required_identifiers, entryData)
        } else {
            curations = getCurations([data], entryData)
        }
        return curations
    }

    static def getCurations(curationsIdentifierData, entryData) {
        def curations = []
        curationsIdentifierData.each { curation ->
            curations.add(getInstance(curation, entryData))
        }
        return curations
    }

    static def getCheckedMetadata(data) {
        def metadata
        if (isMetadataIncluded()) {
            log.debug("including metadata...")
            metadata = data.metadata
        } else {
            log.debug("excluding metadata...")
        }
        return metadata
    }

    static def isMetadataIncluded() {
        return FILTERS.contains('metadata')
    }

    def mismatches(CurationDto other) {
        return other in CurationDto && !this.equals(other) &&
                this.entry.oid == other?.entry?.oid &&
                this.identifier_type == other?.identifier_type
    }
}
