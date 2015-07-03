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

import au.com.redboxresearchdata.cm.Exception.IdProviderException
import au.com.redboxresearchdata.cm.domain.Curation
import au.com.redboxresearchdata.cm.domain.Entry
import au.com.redboxresearchdata.cm.domain.local.LocalCurationEntry
import au.com.redboxresearchdata.cm.domain.local.LocalCurationIncrementer
import au.com.redboxresearchdata.cm.id.*
import au.com.redboxresearchdata.cm.id.local.type.TemplatePlaceholder
import au.com.redboxresearchdata.cm.service.validator.UrlValidatorService
import au.com.redboxresearchdata.cm.service.validator.UrnValidatorService
import au.com.redboxresearchdata.cm.service.validator.ValidatorService
import grails.gorm.DetachedCriteria
import groovy.util.logging.Slf4j
import org.apache.commons.lang.StringUtils
import org.apache.tomcat.jni.Local
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * LocalIdProvider
 *
 * @author <a target='_' href='https://github.com/shilob'>Shilo Banihit</a>
 *
 */
@Slf4j
@Component
class LocalIdProvider implements IdentityProvider {
	private static final String ID = "local"
	private static final String name = "Local Identity Provider"
	boolean synchronous = true
	def config

	List<ValidatorService> validatorServiceList

    //TODO : replace with autowiring
	LocalIdProvider() {
		this.validatorServiceList = new ArrayList<>()
		validatorServiceList.add(new UrlValidatorService())
		validatorServiceList.add(new UrnValidatorService())
	}

	/* (non-Javadoc)
	 * @see au.com.redboxresearchdata.cm.id.IdentityProvider#getID()
	 */
	@Override
	public String getID() {
		return ID
	}

	/* (non-Javadoc)
	 * @see au.com.redboxresearchdata.cm.id.IdentityProvider#getName()
	 */
	@Override
	public String getName() {
		return name
	}

	/* (non-Javadoc)
	 * @see au.com.redboxresearchdata.cm.id.IdentityProvider#isSynchronous()
	 */
	@Override
	public boolean isSynchronous() {
		return true;
	}

	/* (non-Javadoc)
	 * @see au.com.redboxresearchdata.cm.id.IdentityProvider#curate(java.lang.String, java.lang.String)
	 */
	@Override
	public Result curate(String oid, String metadata) {
		Result idResult = exists(oid, metadata)
		if (!idResult) {
			Entry entry = getExistingEntry(oid)
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
	public Result exists(String oid, String metadata) {
		DetachedCriteria criteria = new DetachedCriteria(Curation).build {
			eq 'entry', getExistingEntry(oid)
			isNotNull 'identifier'
		}
		boolean hasLocalCuration = criteria.asBoolean()
		def idResult
		if  (hasLocalCuration) {
			idResult = new IdentifierResult(identityProviderId: ID, oid: oid, metadata: metadata, identifier: criteria?.get()?.identifier)
		}
		log.debug("Does local curation exist?: " + Boolean.toString(hasLocalCuration))
		log.debug("Local curation identifier for oid: " + oid + ", is idResult: " + idResult)
		return idResult
	}

	private Entry getExistingEntry(String oid) {
		Entry entry = Entry.findByOid(oid)
		if (entry) {
			log.debug("found existing entry: " + entry)
			return entry
		} else {
			log.error "Couldn't find existing entry to undertake local curation for oid: ${oid}"
			throw new IllegalStateException("Local Id provider should have an existing entry.")
		}
	}

	private String populateTemplate(Entry entry) {
		log.debug("Saving local curation entry...")
		LocalCurationEntry localCurationEntry = new LocalCurationEntry(entry: entry)
		LocalCurationEntry.withTransaction { status ->
			try {
				localCurationEntry.save(flush: true, failOnError: true)
				String templateData = config.id_providers.local.template
				String identifier = TemplatePlaceholder.populate(templateData, localCurationEntry)
				this.validatorServiceList.each { validator ->
					  if (!validator.isValid(identifier)) {
						  throw new IdProviderException("Invalid template created.")
					  }
				}
				status.flush()
				return identifier
			}  catch (IdProviderException e){
				log.error("Could not complete local curation transaction. rolling back...")
                status.setRollbackOnly()
			}
		}
	}


}
