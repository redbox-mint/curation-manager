package au.com.redboxresearchdata.curationmanager.identityProviderService.utility

import java.util.Locale;
import au.com.redboxresearchdata.curationmanager.identityProviderService.utility.ApplicationContextHolder;
import org.springframework.context.ApplicationContext
import org.codehaus.groovy.grails.context.support.PluginAwareResourceBundleMessageSource
import org.codehaus.groovy.tools.shell.util.MessageSource

class MessageResolver {

 def static  String getMessage(String name){		
	    PluginAwareResourceBundleMessageSource messageSource = ApplicationContextHolder.getMessageSource();
	   def msg = messageSource.getMessage(name, null, Locale.ENGLISH);
	   return msg;
  }
}
