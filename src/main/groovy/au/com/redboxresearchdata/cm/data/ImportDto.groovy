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

import groovy.json.JsonSlurper
import groovy.transform.ToString
import groovy.util.logging.Slf4j

/**
 * Data transfer object that stores string arrays
 * @version
 * @author <a href="matt@redboxresearchdata.com.au">Matt Mulholland</a>
 */
@Slf4j
@ToString
class ImportDto {
    def saved = []
    def matched = []
    def mismatched = []
    def error = []

    def addSaved(CurationDto datum) {
        saved.add(datum.map())
        log.debug("added to saved: " + datum)
    }

    def addMatched(CurationDto datum) {
        matched.add(datum.map())
        log.debug("added to matched: " + datum)
    }

    def addMismatched(CurationDto datum) {
        mismatched.add(datum.map())
        log.debug("added to mismatched: " + datum)
    }

    def addError(def datum) {
        def mapped
        if (datum instanceof String) {
            mapped = new JsonSlurper().parseText(datum)
        } else if (datum instanceof CurationDto) {
            mapped = datum.map()
        } else {
            log.warn("Could not map error data. Adding raw data to error count...")
            mapped = datum
        }
        error.add(mapped)
        log.debug("added to error: " + datum)
    }

    static {
        grails.converters.JSON.registerObjectMarshaller(ImportDto) { it ->
            def closure = {
                if (it instanceof Map && it?.oid) {
                    return ["oid": it.oid]
                } else {
                    return it
                }
            }
            def map = [:]
            map["count"] = ["error": it.error.size(), "matched": it.matched.size(), "mismatched": it.mismatched.size(), "saved": it.saved.size()]
            map["error"] = it.error.collect closure
            map["matched"] = it.matched.collect closure
            map["mismatched"] = it.mismatched.collect closure
            map["saved"] = it.saved.collect closure
            return map
        }
    }

}