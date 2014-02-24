package au.com.redboxresearchdata.curationmanager.identityProviderService

import au.com.redboxresearchdata.curationmanager.identityProviderResult.BaseIdentityResult
import au.com.redboxresearchdata.curationmanager.identityProviderResult.IdentifierResult

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.codehaus.groovy.grails.context.support.PluginAwareResourceBundleMessageSource

import au.com.redboxresearchdata.curationmanager.identityprovider.domain.LocalIdentityProviderIncrementor
import au.com.redboxresearchdata.curationmanager.identityServiceProvider.constants.IdentityServiceProviderConstants
import au.com.redboxresearchdata.curationmanager.identityProviderService.messsageresolver.MessageResolver;
import au.com.redboxresearchdata.curationmanager.identityProviderService.utility.ApplicationContextHolder

import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log

import java.util.Locale;

class CurationManagerLocalIPService  implements IdentityProviderService{
	
	private static final Log log = LogFactory.getLog(this)
	
	@Value("#{propSource[Id]}")
	String id;
	
	@Value("#{propSource[Name]}")
	String name;
	
	//@Value("#{propSource[Synchronous]}")
	Boolean isSynchronous = Boolean.TRUE;
	
	@Value("#{propSource[Template]}")
	String template;
	
	//@Value("#{propSource[Exists]}")
	Boolean exists = Boolean.FALSE;
	
    @Override
	public String getID() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Boolean isSynchronous() {
		return isSynchronous;
	}

	public String getTemplate(){
		return template;
	}
	
	@Override
	public BaseIdentityResult curate(String oid, String[] metaData) throws Exception {
		BaseIdentityResult baseIdentifier = null; 
		try {
		   String template = getTemplate();
		   if(null != template && !template.isEmpty()){
		      String identifier = template.replace(IdentityServiceProviderConstants.OID_DELIMITER, oid);
			  if (identifier.contains(IdentityServiceProviderConstants.INC_DELIMITER)) {
				  LocalIdentityProviderIncrementor localIdentityProviderIncrementor = new LocalIdentityProviderIncrementor()
				  localIdentityProviderIncrementor.save();
			      identifier = identifier.replace(IdentityServiceProviderConstants.INC_DELIMITER, localIdentityProviderIncrementor.id.toString());
			   }
		        baseIdentifier = new IdentifierResult(identifier);		
		    } else{
				  def msg = MessageResolver.getMessage(IdentityServiceProviderConstants.IDENTITY_PROVIDER_SERVICE_TEMPLATE_MISSING);
				  log.error(msg);
				  throw new Exception(IdentityServiceProviderConstants.STATUS_400, new Throwable(msg));
		    }
	     }catch(Exception ex){
			 def msg = MessageResolver.getMessage(IdentityServiceProviderConstants.IDENTITY_PROVIDER_SERVICE_FAILED);
		    log.error(msg+ ex.getMessage());
		     throw new Exception(IdentityServiceProviderConstants.STATUS_400, new Throwable(msg));
		}	
		 return baseIdentifier;
	 }
	
	@Override
	public Boolean exists(String oid, String[] metaData) {
		return exists;
	}
}