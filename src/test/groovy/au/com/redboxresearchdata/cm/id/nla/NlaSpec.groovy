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

import grails.test.mixin.*
import spock.lang.*
import groovy.json.*
import au.com.redboxresearchdata.cm.id.*
import au.com.redboxresearchdata.cm.domain.*
import au.com.redboxresearchdata.cm.id.nla.sru.SruClient
import au.com.redboxresearchdata.cm.service.*
import org.springframework.beans.factory.annotation.*
/**
 *
 * JobRunnerSpec
 *
 * @author <a target='_' href='https://github.com/shilob'>Shilo Banihit</a>
 *
 */
@TestFor(JobService)
class NlaSpec extends Specification {
	def nlaIdProvider
	def client 
	def curXml
	
	def setup() {
		client = new SruClient(config:grailsApplication.config)
		client.getSruData = { url ->
			def xml = new XmlSlurper().parseText(curXml)
			println "Canned XML Response:"
			println xml
			return xml
		}
		nlaIdProvider = new NlaIdProvider(config:grailsApplication.config, sruClient:client)
	}
	
	def cleanup() {
		
	}
	
	void "Tests exists() method"() {
		when:
			curXml = existRecordXml
			Result idResult = nlaIdProvider.exists('c2a0533fc6de694eb7dd64026e7cfdfa', '{}')
		then:
			idResult != null
			idResult.identifier == 'http://nla.gov.au/nla.party-1515199'
		when:
			curXml = notExistRecordXml
			idResult = nlaIdProvider.exists('c2a0533fc6de694eb7dd64026e7cfdfa', '{}')
		then:
			idResult == null
	}
	
	void "Test buildMessage()..."() {
		when:
			curXml = existRecordXml
			def oid = 'oid123'
			def type = new EntryTypeLookup(value:'person')
			def title = 'testTitle'
			def entry = new Entry(oid:oid, type:type, title:title)
			String metadata = "{'field1':'value1'}"
			def msg = nlaIdProvider.buildMessage(entry, metadata)
		then:	
			msg != null
			
	}
	
	
	String notExistRecordXml = """\
<searchRetrieveResponse xmlns="http://www.loc.gov/zing/srw/">
<version>1.1</version>
<numberOfRecords>0</numberOfRecords>
<echoedSearchRetrieveRequest xmlns:ns1="http://www.loc.gov/zing/srw/">
<version>1.1</version>
<query>
rec.identifier="c2a0533fc6de694eb7dd64026e7cfddddd"
</query>
<xQuery>
<ns2:searchClause xmlns:ns2="http://www.loc.gov/zing/cql/xcql/">
<ns2:index>rec.identifier</ns2:index>
<ns2:relation>
<ns2:value>=</ns2:value>
</ns2:relation>
<ns2:term>c2a0533fc6de694eb7dd64026e7cfddddd</ns2:term>
</ns2:searchClause>
</xQuery>
<recordPacking>xml</recordPacking>
<recordSchema>urn:isbn:1-931666-33-4</recordSchema>
</echoedSearchRetrieveRequest>
</searchRetrieveResponse>
"""
	String existRecordXml = """\
<searchRetrieveResponse xmlns="http://www.loc.gov/zing/srw/">
<version>1.1</version>
<numberOfRecords>1</numberOfRecords>
<resultSetId>gru5dk</resultSetId>
<resultSetIdleTime>600</resultSetIdleTime>
<records xmlns:ns1="http://www.loc.gov/zing/srw/">
<record>
<recordSchema>urn:isbn:1-931666-33-4</recordSchema>
<recordPacking>xml</recordPacking>
<recordData>
<eac-cpf xmlns="urn:isbn:1-931666-33-4" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<control>
<recordId>1515199</recordId>
<otherRecordId>http://nla.gov.au/nla.party-1515199</otherRecordId>
<otherRecordId>edec4c02-c5c9-4152-bf0f-a97d2ba46d4b</otherRecordId>
<maintenanceStatus>revised</maintenanceStatus>
<maintenanceAgency>
<agencyCode>AU-ANL:PEAU</agencyCode>
<agencyName>National Library of Australia Party Infrastructure</agencyName>
</maintenanceAgency>
<languageDeclaration>
<language languageCode="eng">English</language>
<script scriptCode="Latn">Latin</script>
</languageDeclaration>
<maintenanceHistory>
<maintenanceEvent>
<eventType>created</eventType>
<eventDateTime standardDateTime="2014-01-20T07:00:03Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa created nla.party-1515199. Record identifier not found. Identity automatically created after surname and initials check returned no potential matches.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2014-01-21T07:00:07Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2014-01-22T07:00:03Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2014-02-13T07:00:03Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2014-02-14T07:00:05Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2014-05-02T08:00:04Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2014-05-16T08:00:28Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2014-05-23T08:00:04Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2014-05-26T08:00:08Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2014-05-27T08:00:10Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2014-05-28T08:00:07Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2014-05-29T08:00:05Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2014-05-30T08:00:05Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2014-06-02T08:00:06Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2014-06-03T08:00:13Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2014-06-04T08:00:06Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2014-06-05T08:00:08Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2014-06-06T08:00:08Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2014-06-09T08:00:08Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2015-03-24T07:00:21Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2015-05-22T08:00:07Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2015-05-25T08:00:16Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2015-06-09T08:00:16Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2015-06-11T08:00:15Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2015-06-12T08:00:11Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2015-06-15T08:00:10Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2015-06-16T08:00:14Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2015-06-17T08:00:06Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2015-06-18T08:00:09Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
<maintenanceEvent>
<eventType>updated</eventType>
<eventDateTime standardDateTime="2015-06-19T08:00:09Z"/>
<agentType>machine</agentType>
<agent>harvester</agent>
<eventDescription>
AU-NU-c2a0533fc6de694eb7dd64026e7cfdfa updated nla.party-1515199.
</eventDescription>
</maintenanceEvent>
</maintenanceHistory>
</control>
<cpfDescription>
<identity>
<entityType>person</entityType>
<nameEntry>
<part localType="surname">Lagopoulos</part>
<part localType="forename">Jim</part>
</nameEntry>
</identity>
<alternativeSet>
<setComponent>
<objectXMLWrap>
<eac-cpf xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:eac-cpf="http://jefferson.village.virginia.edu/eac" xml:id="_8783bdcc-0678-4fa2-86b5-ec98e0534101">
<control>
<recordId>c2a0533fc6de694eb7dd64026e7cfdfa</recordId>
<maintenanceStatus>new</maintenanceStatus>
<maintenanceAgency>
<agencyCode>AU-NU</agencyCode>
<agencyName>The University of Sydney, Australia</agencyName>
</maintenanceAgency>
<languageDeclaration>
<language languageCode="eng">English</language>
<script scriptCode="Latn">Latin</script>
</languageDeclaration>
<maintenanceHistory>
<maintenanceEvent>
<eventType>created</eventType>
<eventDateTime standardDateTime="2014-01-20T02:45:15Z"/>
<agentType>machine</agentType>
<agent>
Mint Name Authority - The University of Sydney, Australia
</agent>
</maintenanceEvent>
</maintenanceHistory>
</control>
<cpfDescription>
<identity>
<entityId>
www.sydney.edu.au/id/party/c2a0533fc6de694eb7dd64026e7cfdfa
</entityId>
<entityType>person</entityType>
<nameEntry>
<part localType="surname">Lagopoulos</part>
<part localType="forename">Jim</part>
</nameEntry>
</identity>
<description>
<places>
<place>
<address localType="postal">
<addressLine>The University of Sydney</addressLine>
<addressLine>NSW 2008</addressLine>
<addressLine>AUSTRALIA</addressLine>
</address>
</place>
<place>
<address localType="email">
<addressLine>jim.lagopoulos@sydney.edu.au</addressLine>
</address>
</place>
<place>
<address localType="url">
<addressLine>
http://sydney.edu.au/medicine/people/academics/profiles/jim.lagopoulos.php
</addressLine>
</address>
</place>
</places>
<biogHist>
<p>
View the full record at
<a href="http://sydney.edu.au/medicine/people/academics/profiles/jim.lagopoulos.php">The University of Sydney</a>
</p>
</biogHist>
</description>
<!-- 
        
&#x9;&#x9; -->
</cpfDescription>
</eac-cpf>
</objectXMLWrap>
</setComponent>
</alternativeSet>
</cpfDescription>
</eac-cpf>
</recordData>
<recordPosition>1</recordPosition>
</record>
</records>
<echoedSearchRetrieveRequest xmlns:ns2="http://www.loc.gov/zing/srw/">
<version>1.1</version>
<query>rec.identifier="c2a0533fc6de694eb7dd64026e7cfdfa"</query>
<xQuery>
<ns3:searchClause xmlns:ns3="http://www.loc.gov/zing/cql/xcql/">
<ns3:index>rec.identifier</ns3:index>
<ns3:relation>
<ns3:value>=</ns3:value>
</ns3:relation>
<ns3:term>c2a0533fc6de694eb7dd64026e7cfdfa</ns3:term>
</ns3:searchClause>
</xQuery>
<recordPacking>xml</recordPacking>
<recordSchema>urn:isbn:1-931666-33-4</recordSchema>
</echoedSearchRetrieveRequest>
</searchRetrieveResponse>
"""
}