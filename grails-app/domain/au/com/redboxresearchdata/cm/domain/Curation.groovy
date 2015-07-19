/*******************************************************************************
 * Copyright (C) 2015 Queensland Cyber Infrastructure Foundation (http://www.qcif.edu.au/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 ******************************************************************************/
package au.com.redboxresearchdata.cm.domain

import grails.gorm.DetachedCriteria
import groovy.json.JsonSlurper
import groovy.transform.ToString

/**
 * Curation 
 *
 * @author <a href="https://github.com/shilob" target="_blank">Shilo Banihit</a>
 * @author <a href="matt@redboxresearchdata.com.au">Matt Mulholland</a>
 * @since 0.1
 *
 */
@ToString
class Curation {
    Entry entry
    String identifier_type
    String identifier
    CurationStatusLookup status
    String error
    Date dateCompleted
    String metadata

    Date dateCreated

    def getJsonMetadata() {
        return new JsonSlurper().parse(metadata)
    }

    static mapping = {
        id column: 'curation_id'
        entry index: 'Entry_Idx', lazy: false
        identifier_type index: 'Entry_Idx,IdentifierType_Idx'
        status lazy: false
        metadata type: 'text'
    }

    static constraints = {
        dateCompleted nullable: true
        error nullable: true
        metadata nullable: true
        identifier nullable: true
    }

    static def existsCriteria = { oid, curationType ->
        log.debug("looking for oid with: " + oid)
        return new DetachedCriteria(Curation).build {
            eq 'entry', Entry.findByOid(oid)
            isNotNull 'identifier'
            eq 'identifier_type', curationType
        }
    }
}
