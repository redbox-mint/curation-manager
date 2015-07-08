package au.com.redboxresearchdata.cm.id.nla

//import grails.plugins.rest.client.RestBuilder
//import grails.plugins.rest.client.RestResponse

import grails.test.mixin.integration.Integration
import grails.transaction.*
import groovyx.net.http.*

import spock.lang.*
import geb.spock.*
import groovy.json.*
import grails.util.Holders
/**
 * 
 * NlaFuncSpec
 *
 * @author <a target='_' href='https://github.com/shilob'>Shilo Banihit</a>
 *
 */
@Integration
@Rollback
class NlaFuncSpec extends GebSpec {
	def grailsApplication
	
    def setup() {
		grailsApplication = Holders.grailsApplication
    }

    def cleanup() {
    }

    void "Testing NLA integration"() {
		def uri = "http://localhost:8080/v-0.1/"
		println "Using uri: ${uri}"
		def restClient = new RESTClient(uri, 'application/json')
		restClient.defaultRequestHeaders.'Accept-Encoding' = 'application/json'
		def metadataObj = new JsonSlurper().parseText(metadata)
		def data = [
				[
					"oid": "integration-test-delay-me_1",
					"title": "Person Functional Test",
					"type": "person",
					"required_identifiers": [
						[
							"identifier_type": "nla",
							"metadata":metadataObj
						]
					]
				]
			]
        when:"Adding a new job,"
			def response = restClient.post(path:'job', body: data)
        then:"it should return a successful status code and with a job id."
			response.status == 200
			println response.data
		when:"Waiting for the curation job to complete,"
			def jobId = response.data.job_id
			def waitCount = 3
			def waitTime = grailsApplication.config.runner.job.curating.sleep_time
			def waitCtr = 0
			def waitResp = null
			while (waitCtr < waitCount) {
				Thread.sleep(waitTime)
				waitResp = restClient.get(path:"job/${jobId}")
				println waitResp.data
				waitCtr++
			}
		then:"it should eventually complete."
			waitResp != null
			waitResp.status == 200
			waitResp.data.status == "complete"
    }
	
	def metadata = """\
{
    "metadata": {
        "recordIDPrefix": "redbox-mint.googlecode.com/parties/people/",
        "data": {
            "ID": "1242",
            "Given_Name": "Monique",
            "Other_Names": "Louise",
            "Family_Name": "Krupp",
            "Pref_Name": "Monique",
            "Honorific": "Mrs",
            "Email": "monique.krupp@example.edu.au",
            "Job_Title": "Lecturer",
            "GroupID_1": "9",
            "GroupID_2": "11",
            "GroupID_3": "",
            "ANZSRC_FOR_1": "1205",
            "ANZSRC_FOR_2": "1299",
            "ANZSRC_FOR_3": "1202",
            "URI": "http://id.example.edu.au/people/1242",
            "NLA_Party_Identifier": "http://nla.gov.au/nla.party-100009",
            "ResearcherID": "http://www.researcherid.com/rid/F-1234-5686",
            "openID": "https://plus.google.com/blahblahblah",
            "Personal_URI": "",
            "Personal_Homepage": "http://www.krupp.com",
            "Staff_Profile_Homepage": "http://staff.example.edu.au/Monique.Krupp",
            "Description": "Mrs Monique Krupp is a Lecturer at the University of Examples"
        },
        "metadata": {
            "dc.identifier": "redbox-mint.googlecode.com/parties/people/1242"
        },
        "relationships": [
            {
                "identifier": "redbox-mint.googlecode.com/parties/group/9",
                "relationship": "isMemberOf",
                "reverseRelationship": "hasMember",
                "authority": true,
                "oid": "58ea8c1f2642f20e87efccabb55bf3d3",
                "isCurated": true,
                "curatedPid": "http://demo.redboxresearchdata.com.au/mint/published/detail/58ea8c1f2642f20e87efccabb55bf3d3"
            },
            {
                "identifier": "redbox-mint.googlecode.com/parties/group/11",
                "relationship": "isMemberOf",
                "reverseRelationship": "hasMember",
                "authority": true,
                "oid": "5350c453df62c9545679c54ab3966d51",
                "isCurated": true,
                "curatedPid": "http://demo.redboxresearchdata.com.au/mint/published/detail/5350c453df62c9545679c54ab3966d51"
            }
        ],
        "responses": [
            {
                "broker": "tcp://localhost:9101",
                "oid": "03abc59403657a3978e58d8b27bd486e",
                "task": "curation-pending",
                "quoteId": "redbox-mint.googlecode.com/parties/people/1242"
            }
        ]
    },
    "objectMetadata": {
        "render-pending": false,
        "repository.type": "Parties",
        "metaPid": "TF-OBJ-META",
        "jsonConfigOid": "d477decc3e982f2acfeed3d0c1ac3867",
        "ready_to_publish": "ready",
        "jsonConfigPid": "Parties_People.json",
        "repository.name": "People",
        "IngestedRelationshipsTransformer": "hasRun",
        "rulesOid": "f70c991f8b9731922430abb1e0115cb5",
        "published": true,
        "objectId": "e6656a2c87d086b015db7e4d9e60c65e",
        "scriptType": "python",
        "rulesPid": "Parties_People.py",
        "localPid": "http://demo.redboxresearchdata.com.au/mint/published/detail/e6656a2c87d086b015db7e4d9e60c65e",
        "eac_creation_date": "2014-03-18T06:09:03Z"
    },
    "constants": {
        "oai_dc": {
            "curation": {
                "pidProperty": "localPid"
            }
        },
        "eac-cpf": {
            "curation": {
                "pidProperty": "localPid",
                "nlaIntegration": {
                    "agencyCode": "AU-QQCIF",
                    "agencyName": "Queensland Cyber Infrastructure Foundation"
                }
            },
            "redbox.identity": {
                "institution": "University of Examples",
                "RIF-CS Group": "The University of Examples, Australia"
            }
        },
        "rif": {
            "urlBase": "http://localhost:9001/mint/",
            "curation": {
                "pidProperty": "localPid"
            },
            "redbox.identity": {
                "institution": "University of Examples",
                "RIF-CS Group": "The University of Examples, Australia"
            }
        }
    }
}
"""
}
