import au.com.redboxresearchdata.curationmanager.identityProviderService.CurationManagerLocalIPService;
import au.com.redboxresearchdata.curationmanager.utility.ApplicationContextHolder;
import org.apache.activemq.ActiveMQConnectionFactory;
import grails.util.Environment

beans = {
	environments {
		switch(Environment.current) {
			case Environment.DEVELOPMENT:
				vndJsonErrorRenderer(grails.rest.render.errors.VndErrorJsonRenderer)
				jmsConnectionFactory(org.apache.activemq.ActiveMQConnectionFactory) {
					//brokerURL = 'tcp://localhost:61616'
					brokerURL = 'tcp://0.0.0.0:9301'
				}
				importBeans('file:/var/local/curationmanager/resource.xml')
				applicationContextHolder(ApplicationContextHolder) { bean ->
					bean.factoryMethod = 'getInstance'
				}
			break
			case Environment.PRODUCTION:		
				vndJsonErrorRenderer(grails.rest.render.errors.VndErrorJsonRenderer)
				jmsConnectionFactory(org.apache.activemq.ActiveMQConnectionFactory) {
					//brokerURL = 'tcp://localhost:61616'
					brokerURL = 'tcp://0.0.0.0:9301'
				}
				importBeans('file:/var/local/curationmanager/resource.xml')
				applicationContextHolder(ApplicationContextHolder) { bean ->
					bean.factoryMethod = 'getInstance'
				}
		}
	}	
}
