package au.com.redboxresearchdata.cm.id.nla.sru


import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.integration.Integration
import grails.transaction.*
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.*
import au.com.redboxresearchdata.cm.id.nla.sru.SruClient

/**
 * 
 * SruClientIntSpec
 *
 * @author <a target='_' href='https://github.com/shilob'>Shilo Banihit</a>
 *
 */
@Integration
@Rollback
class SruClientIntSpec extends Specification {

	def sruClient
	
    def setup() {
		sruClient = new SruClient(config:grails.util.Holders.grailsApplication.config)
    }

    def cleanup() {
    }

    void "Test Retrieval of Person Data"() {
		when: "Accessing the SRU client,"
	        def data = sruClient.findByRecordId('c2a0533fc6de694eb7dd64026e7cfdfa')
			println data
		then: "data should not be null."
			data != null
    }
}
