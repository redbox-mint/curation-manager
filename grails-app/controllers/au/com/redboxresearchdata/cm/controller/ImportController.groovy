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

package au.com.redboxresearchdata.cm.controller

import grails.converters.JSON
import org.grails.web.converters.exceptions.ConverterException

/**
 * @version
 * @author <a href="matt@redboxresearchdata.com.au">Matt Mulholland</a>
 */
class ImportController {
    static def STAT_ERROR

    def importService, updateService

    def batchImport() {
        parseRequest(request, {
            importService.batchImport it
        })
    }

    def batchUpdate() {
        parseRequest(request, {
            updateService.batchImport it
        })
    }

    def parseRequest(data, service) {
        try {
            if (!data) {
                renderPost([status: STAT_ERROR.nothing_to_import.code, message: STAT_ERROR.nothing_to_import.message])
            }
            def result = service(data.JSON)
            renderPost(result)
        } catch (ConverterException e) {
            renderPost([status: STAT_ERROR.parse_error.code, message: STAT_ERROR.parse_error.message])
        }
    }

    def renderPost(data) {
        if (data instanceof Map && data?.message) {
            render(status: data.status, text: data.message)
        } else {
            render data as JSON
        }
    }
}