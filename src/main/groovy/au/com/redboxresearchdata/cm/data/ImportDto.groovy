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

import groovy.transform.ToString
import groovy.util.logging.Slf4j

/**
 * @version
 * @author <a href="matt@redboxresearchdata.com.au">Matt Mulholland</a>
 */
@Slf4j
class ImportDto {
    def saved = []
    def matched = []
    def mismatched = []
    def error = []

    def addSaved(def json) {
        saved.add(json)
        log.debug("added to saved: " + json)
    }

    def addMatched(def json) {
        matched.add(json)
        log.debug("added to matched: " + json)
    }

    def addMismatched(def json) {
        mismatched.add(json)
        log.debug("added to mismatched: " + json)
    }

    def addError(def json) {
        error.add(json)
        log.debug("added to error: " + json)
    }

}