package au.com.redboxresearchdata.cm.id.nla.sru


import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.integration.Integration
import grails.transaction.*
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.*
import au.com.redboxresearchdata.cm.id.nla.sru.SruClient
import au.com.redboxresearchdata.cm.service.*

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
	@Autowired
	JobService jobService  // TODO:remove when you find a way to inject grailsApplication.config
	
    def setup() {
		sruClient = new SruClient(config:jobService.grailsApplication.config)
    }

    def cleanup() {
    }

    void "Test Retrieval of Person Data"() {
		when:
	        def data = sruClient.findByRecordId('c2a0533fc6de694eb7dd64026e7cfdfa')
			println data
		then:
			data != null
    }
}
