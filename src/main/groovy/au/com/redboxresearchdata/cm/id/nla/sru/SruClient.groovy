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
package au.com.redboxresearchdata.cm.id.nla.sru

import groovy.util.logging.Slf4j
import java.net.URLEncoder
import com.squareup.okhttp.*
import java.io.*
/**
 * SruClient
 *
 * @author <a target='_' href='https://github.com/shilob'>Shilo Banihit</a>
 * 
 * <br>
 * Sample record:
 * <br>
 * http://www.nla.gov.au/apps/srw/search/peopleaustralia?version=1.1&recordSchema=urn%3Aisbn%3A1-931666-33-4&recordPacking=xml&operation=searchRetrieve&query=rec.identifier%3D%27nla.party-915373%27
 * <br>
 * http://www.nla.gov.au/apps/srw/search/peopleaustralia?version=1.1&recordSchema=urn%3Aisbn%3A1-931666-33-4&recordPacking=xml&operation=searchRetrieve&query=rec.identifier%3D%22c2a0533fc6de694eb7dd64026e7cfdfa%22
 */
@Slf4j
class SruClient {
	
	def config
	def xmlSlurper = new XmlSlurper()
	OkHttpClient client = new OkHttpClient()
	
	def findByRecordId(String id) {
		def recordData = null
		def data = getSruData(generateUrl("rec.identifier=\"${id}\"", null))
		if (Integer.valueOf(data?.numberOfRecords ? data?.numberOfRecords.toString() : '0') > 0) {
			recordData = data
		}
		return recordData
	}
	
	def getSruData = { url ->
		def dataStr = getData(url)
		log.debug "XML Data: "
		log.debug dataStr
		return xmlSlurper.parseText(dataStr)
	}
	
	public String generateUrl(String query, String operation) {
		def sruUrl = config.id_providers.nla.sru.url
		def version = encode(config.id_providers.nla.sru.version)
		def recordSchema = encode(config.id_providers.nla.sru.recordSchema)
		def recordPacking = encode(config.id_providers.nla.sru.recordPacking)
		
		if (query == null) {
			throw new Exception("Cannot generate a search URL without a search! 'query' parameter is required.");
		}
		if (operation == null) {
			operation = 'searchRetrieve';
		}
		def searchUrl = "${sruUrl}?version=${version}&recordSchema=${recordSchema}&recordPacking=${recordPacking}&operation=${encode(operation)}&query=${encode(query)}"
		log.debug "searchUrl: ${searchUrl}"
		return searchUrl;
	}
	
	private String encode(value) {
		log.debug "Encoding value: ${value}"
		return URLEncoder.encode(value, "UTF-8");
	}
	
	
	String getData(String url) throws IOException {
	  Request request = new Request.Builder()
		  .url(url)
		  .build();
	
	  Response response = client.newCall(request).execute();
	  return response.body().string();
	}
}