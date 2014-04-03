package au.com.redboxresearchdata.curationmanager.utility

import javax.servlet.ServletContext
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.GrailsPluginManager
import org.codehaus.groovy.grails.context.support.PluginAwareResourceBundleMessageSource
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

import grails.util.Holders
 
@Singleton
class ApplicationContextHolder implements ApplicationContextAware {
 
  static  ApplicationContext getApplicationContext() {
        Holders.grailsApplication.mainContext
   }
   
   static GrailsApplication getGrailsApplication() {
		ApplicationContext ctx = Holders.grailsApplication.mainContext
		ctx.getBean('grailsApplication')
	}
  
	static ServletContext getServletContext() {
		 ApplicationContext ctx = Holders.grailsApplication.mainContext
		 ctx.getBean('servletContext')
	}
   static GrailsPluginManager getPluginManager() {
	   ApplicationContext ctx = Holders.grailsApplication.mainContext
	   ctx.getBean('pluginManager')
	}
	
	
	static PluginAwareResourceBundleMessageSource getMessageSource() {
		ApplicationContext ctx = Holders.grailsApplication.mainContext
	    ctx.getBean("messageSource");
	} 

	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		
	}
}
