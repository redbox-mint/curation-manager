package au.com.redboxresearchdata.curationmanager.identityProviderService

import au.com.redboxresearchdata.curationmanager.identityProviderResult.BaseIdentityResult
import au.com.redboxresearchdata.curationmanager.identityProviderResult.IdentifierResult

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.codehaus.groovy.grails.context.support.PluginAwareResourceBundleMessageSource

import au.com.redboxresearchdata.curationmanager.identityprovider.domain.IdentityProviderIncrementor
import au.com.redboxresearchdata.curationmanager.identityProviderService.constants.IdentityServiceProviderConstants
import au.com.redboxresearchdata.curationmanager.identityProviderService.utility.MessageResolver;
import au.com.redboxresearchdata.curationmanager.identityProviderService.utility.ApplicationContextHolder

import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log

import java.util.Locale;
import java.util.Map;

class CurationManagerLocalIPService  implements IdentityProviderService{
	
	private static final Log log = LogFactory.getLog(this)
	
	@Value("#{localPropSource[Id]}")
	String id;
	
	@Value("#{localPropSource[Name]}")
	String name;
	
	@Value("#{localPropSource[Template]}")
	String template;
	
	private Boolean isSynchronous = Boolean.TRUE;
	
	private Boolean exists = Boolean.FALSE;
	
    @Override
	public String getId() {
		return id;
	}
	
	public Boolean validate(Map.Entry pairs) throws Exception{
		return Boolean.TRUE;
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
	
	public IdentityProviderService getDependentProviderService() throws Exception{
		return null;
	}
	
	public Map<String, String> getMetaDataMap(String metaData) {
		return null;
	}
	
	@Override
	public BaseIdentityResult curate(String oid, String... metaData) throws Exception {
		BaseIdentityResult baseIdentifier = null; 
		try {
		   String template = getTemplate();
		   if(null != template && !template.isEmpty()){
		      String identifier = template.replace(IdentityServiceProviderConstants.OID_DELIMITER, oid);
			  if (identifier.contains(IdentityServiceProviderConstants.INC)) {
				  IdentityProviderIncrementor localIdentityProviderIncrementor = new IdentityProviderIncrementor()
				  localIdentityProviderIncrementor.save();
			      identifier = identifier.replace(IdentityServiceProviderConstants.INC, 
					  localIdentityProviderIncrementor.id.toString());
			   }
		        baseIdentifier = new IdentifierResult(identifier);		
		    } else{
				  def msg = MessageResolver.getMessage(IdentityServiceProviderConstants.IDENTITY_PROVIDER_SERVICE_TEMPLATE_INC_MISSING);
				  log.error(msg);
				  throw new Exception(IdentityServiceProviderConstants.STATUS_400, new Throwable(msg));
		    }
	     }catch(Exception ex){
			 def msg = MessageResolver.getMessage(IdentityServiceProviderConstants.IDENTITY_PROVIDER_SERVICE_FAILED);
		    log.error(msg+ ex.getMessage());
		     throw new Exception(IdentityServiceProviderConstants.STATUS_400, ex);
		}	
		 return baseIdentifier;
	 }
	
	@Override
	public Boolean exists(String oid, String[] metaData) {
		return exists;
	}
}