package au.com.redboxresearchdata.curationmanager.utility

import org.springframework.context.ApplicationContext
import org.codehaus.groovy.grails.context.support.PluginAwareResourceBundleMessageSource

import java.util.Locale;

class MessageResolver {

	def static  String getMessage(String name){
	  PluginAwareResourceBundleMessageSource messageSource = ApplicationContextHolder.getMessageSource();
	  def msg = messageSource.getMessage(name, null,  Locale.ENGLISH);
	  return msg;		
   }
}