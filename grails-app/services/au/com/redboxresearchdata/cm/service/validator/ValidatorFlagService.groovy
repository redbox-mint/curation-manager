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

package au.com.redboxresearchdata.cm.service.validator

import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

/**
 * @version
 * @author <a href="matt@redboxresearchdata.com.au">Matt Mulholland</a>
 */
@Slf4j
class ValidatorFlagService {

    def validatorServices = [
            URN: new UrnValidatorService(),
            URL: new UrlValidatorService()
    ]

    boolean isValid(identifier, validatorKeys) {
        def validators = getValidators(validatorKeys)
        def hasFailed = validators.any { validator ->
            !validator.isValid(identifier)
        }
        def isValid = !hasFailed
        log.debug("validator flag service returns: " + Boolean.toString(isValid))
        return isValid
    }

    def getValidators(def validatorKeys) {
        log.debug("validator keys: " + validatorKeys)
        def flagged = validatorKeys.collect { validator ->
            log.debug("checking key: " + validator)
            return validatorServices[validator]
        }
        log.debug("flagged validators: " + flagged)
        return flagged
    }
}
