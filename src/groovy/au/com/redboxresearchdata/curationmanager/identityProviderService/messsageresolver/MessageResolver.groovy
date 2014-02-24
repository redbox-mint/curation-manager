package au.com.redboxresearchdata.curationmanager.identityProviderService.messsageresolver

import java.util.Locale;

import au.com.redboxresearchdata.curationmanager.identityProviderService.utility.ApplicationContextHolder;

import org.springframework.context.ApplicationContext
import org.codehaus.groovy.grails.context.support.PluginAwareResourceBundleMessageSource

class MessageResolver {

 def static  String getMessage(String name){		
	   PluginAwareResourceBundleMessageSource messageSource = ApplicationContextHolder.getMessageSource();
	   def msg = messageSource.getMessage(name, null, Locale.ENGLISH);
	   println " -------11111111111111-------- This is the Message -----------------------"
	   println msg
	   return msg;
  }
}
