import au.com.redboxresearchdata.curationmanager.identityProviderService.CurationManagerLocalIPService;
import au.com.redboxresearchdata.curationmanager.utility.ApplicationContextHolder;

beans = {
	vndJsonErrorRenderer(grails.rest.render.errors.VndErrorJsonRenderer)
	importBeans('file:grails-app/conf/spring/identityProviderServiceApplicationContext.xml')
	applicationContextHolder(ApplicationContextHolder) { bean ->
		bean.factoryMethod = 'getInstance'
	 }
}
