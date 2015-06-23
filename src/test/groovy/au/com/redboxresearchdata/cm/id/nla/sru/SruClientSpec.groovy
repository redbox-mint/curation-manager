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

import grails.test.mixin.*
import spock.lang.*
import groovy.util.*
import au.com.redboxresearchdata.cm.service.*
/**
 *
 * JobRunnerSpec
 *
 * @author <a target='_' href='https://github.com/shilob'>Shilo Banihit</a>
 *
 */
@TestFor(JobService)
class SruClientSpec extends Specification {
	def client
	
	def setup() {
		client = new SruClient(config:grailsApplication.config)
		
	}

	def cleanup() {
		
	}
	
	void "Test URL Generation..."() {
		when:
			String query = 'rec.identifier="c2a0533fc6de694eb7dd64026e7cfdfa"'
			def url = client.generateUrl(query, null)
		then:
			url == "http://www-test.nla.gov.au/apps/srw/search/peopleaustralia?version=1.1&recordSchema=urn%3Aisbn%3A1-931666-33-4&recordPacking=xml&operation=searchRetrieve&query=rec.identifier%3D%22c2a0533fc6de694eb7dd64026e7cfdfa%22"
	}
	
	void "Test Data parsing..."() {
		when:
			client.getSruData = { url ->
				def xml = new XmlSlurper().parseText(existRecordXml)
				println "Canned XML Response:"
				println xml
				return xml
			}
			def data = client.findByRecordId("test")
		then:
			data != null
	}	
	
	
	String existRecordXml = """\
<searchRetrieveResponse xmlns="http://www.loc.gov/zing/srw/">
<version>1.1</version>
<numberOfRecords>1</numberOfRecords>
<resultSetId>o5e2n8</resultSetId>
<resultSetIdleTime>600</resultSetIdleTime>
<records xmlns:ns1="http://www.loc.gov/zing/srw/">
<record>
<recordSchema>urn:isbn:1-931666-33-4</recordSchema>
<recordPacking>xml</recordPacking>
<recordData>
<eac-cpf xmlns="urn:isbn:1-931666-33-4" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<control>...</control>
<cpfDescription>
<identity>...</identity>
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