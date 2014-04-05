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
 static ApplicationContext ctx;
   
	static ApplicationContext getApplicationContext() {
		   getInstance().ctx
	}
  
	static ServletContext getServletContext() {
		 getApplicationContext().getBean('servletContext')
	}
   static GrailsPluginManager getPluginManager() {
	   getApplicationContext().getBean('pluginManager')
	}

	static PluginAwareResourceBundleMessageSource getMessageSource() {
		getApplicationContext().getBean("messageSource");
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		ctx = applicationContext
	}
}