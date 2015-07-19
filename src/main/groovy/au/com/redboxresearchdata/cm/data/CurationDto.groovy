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
import groovy.transform.ToString
import groovy.util.logging.Slf4j

/**
 * @version
 * @author <a href="matt@redboxresearchdata.com.au">Matt Mulholland</a>
 */
@Slf4j
class CurationDto {
    static def FILTERS

    static def getInstances(data) {
        return getRequiredIdentifiers(data).collect {
            log.debug("trying to get instance...")
            new Builder(data)
                .entryType(data?.type)
                .title(data?.title)
                .metadata(it)
                .requiredIdentifiers(it)
                .build()
        }
    }

    static def getExistingInstances(data) {
        return getRequiredIdentifiers(data).findResults {
            log.debug("looking for curations with: " + it)
            def found = Curation.existsCriteria(data.oid, it?.identifier_type).find()
            log.debug("found: " + found)
            if (found) {
                new Builder(data)
                    .entryType(found.entry.type.value)
                    .title(found.entry.title)
                    .metadata(found)
                    .requiredIdentifiers(found)
                    .build()
            }
        }
    }

    /**
     * if curation data exists both inside and outside 'required_identifiers' block, only the data
     * inside the block is considered.
     * @param data
     * @return
     */
    static def getRequiredIdentifiers(data) {
        if (data.required_identifiers) {
            log.debug("required identifiers are in first: " + data.required_identifiers)
            return data.required_identifiers
        } else {
            log.debug("required identifiers are in second: " + data)
            return data
        }
    }

    @ToString
    static class Builder {
        def map = [:]

        public Builder(def data) {
            map["oid"] = data?.oid
            map["type"] = null
            map["title"] = null
            map["identifier_type"] = null
            map["identifier"] = null
            map["metadata"] = null
        }

        public Builder entryType(String entryType) {
            map["type"] = entryType
            return this
        }

        public Builder title(String entryTitle) {
            map["title"] = entryTitle
            return this
        }

        public Builder requiredIdentifiers(def data) {
            map["identifier_type"] = data?.identifier_type
            map["identifier"] = data?.identifier
            return this
        }

        public Builder metadata(def data) {
            if (!isMetadataExcluded()) {
                map["metadata"] = data?.metadata
            }
            return this
        }

        public Map build() {
            return this.map
        }
    }

    static def isMetadataExcluded() {
        return FILTERS.contains('metadata')
    }

    static boolean mismatches(one, other) {
        return one instanceof Map && other instanceof Map &&
                one["oid"] == other["oid"] &&
                one["identifier_type"] == other["identifier_type"] &&
                !one.equals(other)
    }
}
