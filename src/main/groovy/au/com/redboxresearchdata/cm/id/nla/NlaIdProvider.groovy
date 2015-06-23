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
package au.com.redboxresearchdata.cm.id.nla

import groovy.util.logging.Slf4j
import au.com.redboxresearchdata.cm.id.*
import au.com.redboxresearchdata.cm.id.nla.sru.SruClient
import au.com.redboxresearchdata.cm.id.util.MqSender
import au.com.redboxresearchdata.cm.domain.*
import au.com.redboxresearchdata.cm.domain.nla.*
import groovy.json.*

/**
 * NlaIdProvider
 *
 * @author <a target='_' href='https://github.com/shilob'>Shilo Banihit</a>
 *
 */
@Slf4j
class NlaIdProvider implements IdentityProvider {
	String ID = "nla"
	String name = "NLA Identity Provider"
	boolean synchronous = false
	def config
	def sruClient
	def mqSender

	/* (non-Javadoc)
	 * @see au.com.redboxresearchdata.cm.id.IdentityProvider#curate(java.lang.String, java.lang.String)
	 */
	@Override
	public Result curate(String oid, String metadata) {
		Result idResult = exists(oid, metadata)
		if (!idResult) {
			// determine if this has been submitted to the OAI Harvester
			Entry entry = Entry.findByOid(oid)
			if (entry) {
				def nlaEntry = NlaCurationEntry.findByEntry(entry)
				if (!nlaEntry) {
					// build the message
					String msg = buildMessage(entry, metadata)
					// send to queue
					if (!mqSender)
						mqSender = new MqSender() 
					mqSender.sendMessage(config.id_providers.nla.mq.broker_url, config.id_providers.nla.mq.queue, msg)
					// creating record of message creation
					nlaEntry = new NlaCurationEntry(entry:entry)
					nlaEntry.save(flush:true, failOnError:true)
				}
				idResult = new FutureResult(identityProviderId:ID, oid:oid, metadata:metadata, identifier:null)
			} else {
				def msg = "Couldn't find entry when curating for NLA: ${oid}"
				log.error msg
				throw new Exception(msg)
			}
		}
		return idResult
	}

	/* (non-Javadoc)
	 * @see au.com.redboxresearchdata.cm.id.IdentityProvider#exists(java.lang.String, java.lang.String)
	 */
	@Override
	public Result exists(String oid, String metadata) {
		// hit the SRU Interface
		if (!sruClient)
			sruClient = new SruClient(config:config)
		def data = sruClient.findByRecordId(oid)
		if (data) {
			def nlaId = null
			data.records.record[0].recordData['eac-cpf'].control.otherRecordId.each {
				log.debug "Other ID: ${it}"
				if (it.toString().startsWith('http://nla.gov.au')) {
					nlaId = it.toString()
				}
			}
			log.debug "Nla Id: ${nlaId}"
			def idResult = new IdentifierResult(identityProviderId:ID, oid:oid, metadata:metadata, identifier:nlaId)
			return idResult
		}
		return null;
	}
	
	def buildMessage(entry, metadata) {
		String templateData = this.getClass().getResourceAsStream('templates/'+config.id_providers.nla.templates[entry.type.value]).text
		// replace variables
		templateData = templateData.replaceAll("<recordId>",entry.oid)
		templateData = templateData.replaceAll("<dateStamp>", new Date().format('YYYY-MM-DDThh:mm:ssZ'))
		println templateData
		def slurper = new JsonSlurper()
		def msgData = slurper.parseText(templateData)
		def metadataObj = slurper.parseText(metadata)
		msgData.metadata = metadataObj
		// TODO: refactor the OAI Components to use constants out of main object
		msgData.constants = metadataObj.constants
		return JsonOutput.toJson(msgData)
	}
}