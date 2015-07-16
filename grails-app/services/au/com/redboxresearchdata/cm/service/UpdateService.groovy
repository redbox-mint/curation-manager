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
        log.debug("import dto is: " + importCollector)
        log.debug("existing curations: " + existingCurations)
        existingCurations.any { existing ->
            if (incoming.equals(existing)) {
                importCollector.addMatched(incoming)
                existingCurations.remove(existing)
                return true
            } else if (incoming.mismatches(existing) && save(incoming)) {
                // an update overwrites existing data
                importCollector.addSaved(incoming)
                existingCurations.remove(existing)
                return true
            }
            return false
            // anything not saved/matched/mismatched is an error. Updates do not persist new records.
        } || importCollector.addError(incoming)
    }
}
