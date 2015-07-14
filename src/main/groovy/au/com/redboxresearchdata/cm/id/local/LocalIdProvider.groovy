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
package au.com.redboxresearchdata.cm.id.local

import au.com.redboxresearchdata.cm.domain.Curation
import au.com.redboxresearchdata.cm.domain.Entry
import au.com.redboxresearchdata.cm.domain.local.LocalCurationEntry
import au.com.redboxresearchdata.cm.exception.IdProviderException
import au.com.redboxresearchdata.cm.id.IdentifierResult
import au.com.redboxresearchdata.cm.id.IdentityProvider
import au.com.redboxresearchdata.cm.id.Result
import au.com.redboxresearchdata.cm.id.local.type.TemplatePlaceholder
import au.com.redboxresearchdata.cm.service.validator.ValidatorFlagService
import grails.gorm.DetachedCriteria
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * LocalIdProvider
 *
 * @author <a target='_' href='https://github.com/shilob'>Shilo Banihit</a>
 *
 */
@Slf4j
class LocalIdProvider implements IdentityProvider {
    static final String ID = "local"
    static final String name = "Local Identity Provider"
    boolean synchronous = true
    def config

    //TODO : autowire
    ValidatorFlagService validatorFlagService

    LocalIdProvider() {
        this.validatorFlagService = new ValidatorFlagService()
    }

    /* (non-Javadoc)
     * @see au.com.redboxresearchdata.cm.id.IdentityProvider#getID()
     */

    @Override
    String getID() {
        return ID
    }

    /* (non-Javadoc)
     * @see au.com.redboxresearchdata.cm.id.IdentityProvider#getName()
     */

    @Override
    String getName() {
        return name
    }

    /* (non-Javadoc)
     * @see au.com.redboxresearchdata.cm.id.IdentityProvider#isSynchronous()
     */

    @Override
    boolean isSynchronous() {
        return true;
    }

    /* (non-Javadoc)
     * @see au.com.redboxresearchdata.cm.id.IdentityProvider#curate(java.lang.String, java.lang.String)
     */

    @Override
    Result curate(String oid, String metadata) {
        Result idResult = exists(oid, metadata)
        if (!idResult) {
            Entry entry = Entry.findByOid(oid)
            log.debug("Proceeding with local curation...")
            String identifier = populateTemplate(entry)
            idResult = new IdentifierResult(identityProviderId: ID, oid: oid, metadata: metadata, identifier: identifier)
            log.debug("local id result is: " + idResult)
        }
        return idResult
    }

    /**
     *
     * @param oid
     * @param metadata
     * @return
     */
    @Override
    Result exists(String oid, String metadata) {
        DetachedCriteria criteria = new DetachedCriteria(Curation).build {
            eq 'entry', Entry.findByOid(oid)
            isNotNull 'identifier'
            eq 'identifier_type', 'local'
        }
        boolean hasLocalCuration = criteria.asBoolean()
        def idResult
        if (hasLocalCuration) {
            idResult = new IdentifierResult(identityProviderId: ID, oid: oid, metadata: metadata, identifier: criteria?.get()?.identifier)
        }
        log.debug("Does local curation exist?: " + Boolean.toString(hasLocalCuration))
        log.debug("Local curation identifier for oid: " + oid + ", is idResult: " + idResult)
        return idResult
    }

    String populateTemplate(Entry entry) {
        log.debug("Saving local curation entry...")
        def currentRecordType = entry?.type?.value
        def typeConfig = extractConfig(currentRecordType)
        if (typeConfig) {
            LocalCurationEntry.withTransaction { status ->
                try {
                    String templateData = typeConfig['template']
                    LocalCurationEntry localCurationEntry = new LocalCurationEntry(entry: entry)
                    localCurationEntry.save(flush: true, failOnError: true)
                    String identifier = TemplatePlaceholder.populate(templateData, localCurationEntry)
                    def validatorConfig = typeConfig['validators']
                    log.debug("validator config is: " + validatorConfig)
                    if (!validatorFlagService.isValid(identifier, validatorConfig)) {
                        throw new IdProviderException("Invalid template created.")
                    }
                    status.flush()
                    return identifier
                } catch (IdProviderException e) {
                    log.error("Could not complete local curation transaction. rolling back...")
                    status.setRollbackOnly()
                }
            }
        } else {
            log.warn("Aborting curation. No local curation template found. No local curation record saved.")
        }
    }

    def extractConfig(def recordType) {
        def allLocalTemplateData = config?.id_providers?.local?.templates
        log.debug("local template data is: " + allLocalTemplateData)
        log.debug("current record type is: " + recordType)
        def typeConfig
        if (allLocalTemplateData && recordType) {
            typeConfig = allLocalTemplateData[recordType]
            log.debug("type config is: " + typeConfig)
        } else {
            log.error("Unable to find local template matching record type: " + recordType)
        }
        return typeConfig
    }


}
