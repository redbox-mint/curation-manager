package au.com.redboxresearchdata.curationmanager.utility

import javax.servlet.ServletContext
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.GrailsPluginManager
import org.codehaus.groovy.grails.context.support.PluginAwareResourceBundleMessageSource
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
 
@Singleton
class ApplicationContextHolder implements ApplicationContextAware {
 
   private ApplicationContext ctx
   
   private static final Map<String, Object> TEST_BEANS = [:]
 
   void setApplicationContext(ApplicationContext applicationContext) {
       ctx = applicationContext
   }
 
   static ApplicationContext getApplicationContext() {
      getInstance().ctx
   }
   
   static GrailsApplication getGrailsApplication() {
	   getBean('grailsApplication')
	}
  
	static ConfigObject getConfig() {
	   getGrailsApplication().config
	}
  
	static ServletContext getServletContext() {
	   getBean('servletContext')
	}
  
	static GrailsPluginManager getPluginManager() {
	   getBean('pluginManager')
	}
	
	static Object getBean(String name) {
		TEST_BEANS[name] ?: getApplicationContext().getBean(name)
	 }
	
	static PluginAwareResourceBundleMessageSource getMessageSource() {
	   getBean("messageSource");
	} 
}