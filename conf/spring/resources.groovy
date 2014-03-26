import au.com.redboxresearchdata.curationmanager.identityProviderService.CurationManagerLocalIPService;
import au.com.redboxresearchdata.curationmanager.utility.ApplicationContextHolder;
import org.apache.activemq.ActiveMQConnectionFactory;

beans = {
	vndJsonErrorRenderer(grails.rest.render.errors.VndErrorJsonRenderer)
	jmsConnectionFactory(org.apache.activemq.ActiveMQConnectionFactory) {
		brokerURL = 'tcp://localhost:61616'
	  }
	importBeans('file:grails-app/conf/spring/identityProviderServiceApplicationContext.xml')
	applicationContextHolder(ApplicationContextHolder) { bean ->
		bean.factoryMethod = 'getInstance'
	 }
}
