package au.com.redboxresearchdata.curationmanager.identityProviderService

import org.springframework.context.ApplicationContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import au.com.redboxresearchdata.curationmanager.utility.JsonUtil;
import au.com.redboxresearchdata.curationmanager.identityProviderService.utility.MessageResolver;
import au.com.redboxresearchdata.curationmanager.identityProviderService.constants.IdentityServiceProviderConstants;
import au.com.redboxresearchdata.curationmanager.identityprovider.domain.IdentityProviderIncrementor
import au.com.redboxresearchdata.curationmanager.identityProviderService.utility.ApplicationContextHolder
import au.com.redboxresearchdata.curationmanager.identityProviderService.validator.HandleValidator
import au.com.redboxresearchdata.curationmanager.identityProviderResult.BaseIdentityResult;
import au.com.redboxresearchdata.curationmanager.identityProviderResult.IdentifierResult

import java.security.PrivateKey
import java.util.Map;

import net.handle.hdllib.AbstractMessage;
import net.handle.hdllib.AbstractResponse;
import net.handle.hdllib.AddValueRequest;
import net.handle.hdllib.AdminRecord;
import net.handle.hdllib.CreateHandleRequest;
import net.handle.hdllib.Encoder;
import net.handle.hdllib.ErrorResponse;
import net.handle.hdllib.HandleException;
import net.handle.hdllib.HandleResolver;
import net.handle.hdllib.HandleValue;
import net.handle.hdllib.ModifyValueRequest;
import net.handle.hdllib.PublicKeyAuthenticationInfo;
import net.handle.hdllib.Util;

import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log

import javax.jms.Message

import org.springframework.jms.core.MessagePostProcessor
import org.springframework.beans.factory.annotation.Value


class CurationManagerHandleIPService implements IdentityProviderService{
	
	private static final Log log = LogFactory.getLog(this)
	
    private PublicKeyAuthenticationInfo authentication;
	
	private Boolean exists = Boolean.FALSE;
	
	private Boolean isSynchronous = Boolean.TRUE;
	
	@Value("#{handlePropSource[Id]}")
	String id;
	
	@Value("#{handlePropSource[Name]}")
	String name;
	
	@Value("#{handlePropSource[Description]}")
	String description
	
	@Value("#{handlePropSource[Template]}")
	String template;
	
	@Value("#{handlePropSource[NamingAuthority]}")
	String namingAuthority
	
    @Value("#{handlePropSource[Prefix]}")
	String prefix
	
	@Value("#{handlePropSource[PassPhrase]}")
	String passPhrase
		
	@Value("#{handlePropSource[PrivateKeyPath]}")
	String privateKeyPath
	
	@Value("#{handlePropSource[PublishedDomain]}")
	String publishedDomain
	
    @Override
	public String getId() {
		return id;
	}
	
	public IdentityProviderService getDependentProviderService() throws Exception{
		return null;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public String getDescription(){
        return description;		
	}

	@Override
	public Boolean isSynchronous() {
		return isSynchronous;
	}
	
	public Boolean validate(Map.Entry pairs) throws Exception{
		HandleValidator handleValidator = new HandleValidator();
		return handleValidator.validateMetaData(pairs);
	}

	public String getTemplate(){
		return template;
	}	
	
	public String getNamingAuthority(){		
		return namingAuthority;
	}	
	
	public String getPrefix(){
		return prefix;
	}
	
	public String getPassPhrase(){
		return passPhrase;
	}
	
	public String getPublishedDomain(){
		return publishedDomain;
	}
	
	public String getPrivateKeyPath(){
		return privateKeyPath;
	}
	
	public Map<String, String> getMetaDataMap(String metaData) {
		return JsonUtil.getMapFromMetaData(metaData);
	}
	
	@Override
    public BaseIdentityResult curate(String oid, String... metaData) throws Exception {
	 	BaseIdentityResult baseIdentifier = null;
		try {
		  String identifier =  createHandle(oid, metaData[0]);
		  baseIdentifier = new IdentifierResult(identifier);
		}catch(Exception ex){
		  def msg = MessageResolver.getMessage(IdentityServiceProviderConstants.
			  IDENTITY_PROVIDER_SERVICE_FAILED);
	      log.error(msg+ ex.getMessage());
	   	  throw new Exception(IdentityServiceProviderConstants.STATUS_400, ex);
        }
	  return baseIdentifier;
	}
	
	@Override
	public Boolean exists(String oid, String[] metaData) {
		return exists;
	}
	
	protected String createHandle(String oid, String metaData) throws Exception{
	    String handle = null;
		try {
		 Map  metaDataMap = getMetaDataMap(metaData);
		 String description = metaDataMap.get(IdentityServiceProviderConstants.DESCRIPTION);
		 String url = metaDataMap.get(IdentityServiceProviderConstants.URL.toLowerCase());
		 String templateSuffix = resolveTemplate(oid);
		 if(null == templateSuffix){
			 def msg = MessageResolver.getMessage(IdentityServiceProviderConstants.ERROR_BUILDING_HANDLE_SUFFIX);
			 log.error(msg);
			 throw new Exception(msg);
		 }
		 String handleprefix = getPrefix() + IdentityServiceProviderConstants.FORWARD_SLASH + getNamingAuthority();
		 byte[] prefix = handleprefix.getBytes(IdentityServiceProviderConstants.UTF8);
		 HandleValue  adminVal = getAdminHandleValue(prefix)
		 HandleValue descVal = getDescriptionHandleValue(getDescription())
		 if (adminVal == null || descVal == null) {
			 def msg = MessageResolver.getMessage(IdentityServiceProviderConstants.ERROR_CREATING_HANDLE_VALUES);
			 log.error(msg);
			 throw new Exception(msg);
		 } 
		 HandleValue[] values = null;
		 if(null != url){	 
		    HandleValue urlVal = getUrlHandleValue(url);
			if (urlVal == null) {
				def msg = MessageResolver.getMessage(IdentityServiceProviderConstants.ERROR_CREATING_HANDLE_VALUES);
				log.error(msg);
				throw new Exception(msg);
			  }
		    values = [adminVal, descVal, urlVal];	   
		 }else{		 
		    values = [adminVal, descVal];
		 }
		 byte[] key = getPrivateKeyToBytes();
		 byte[] passPhrase = getPassPhraseToBytes();
		 key = Util.decrypt(key, passPhrase);
		 PrivateKey privateKey = Util.getPrivateKeyFromBytes(key, IdentityServiceProviderConstants.ZERO);
		 PublicKeyAuthenticationInfo  authentication = new PublicKeyAuthenticationInfo(prefix, 
			    IdentityServiceProviderConstants.PUBLIC_INDEX, privateKey);
		 handle = getNamingAuthority() + IdentityServiceProviderConstants.FORWARD_SLASH + templateSuffix;
		 byte[] handleToBytes = handle.getBytes(IdentityServiceProviderConstants.UTF8);
		 CreateHandleRequest req = new CreateHandleRequest(handleToBytes, values, authentication);
		 HandleResolver resolver  = new HandleResolver();
		 AbstractResponse response = resolver.processRequest(req);
		 if (response.responseCode != AbstractMessage.RC_SUCCESS){
			 if (response.responseCode == AbstractMessage.RC_HANDLE_ALREADY_EXISTS) {
				 def msg = MessageResolver.getMessage(IdentityServiceProviderConstants.HANDLE_ALREADY_IN_USE);
				 log.error(msg+ templateSuffix);
				 throw new Exception(msg + ((ErrorResponse) response).toString());
//		         if(getTemplate().contains(IdentityServiceProviderConstants.INC)){
//					 createHandle(oid, metaData);
//				 } 
			 }
			 if (response instanceof ErrorResponse) {
			    def msg = MessageResolver.getMessage(IdentityServiceProviderConstants.ERROR_CREATING_HANDLE);
				log.error(msg + ((ErrorResponse) response).toString());
				throw new Exception(msg + ((ErrorResponse) response).toString());
 		     }
			 else {
				 def msg = MessageResolver.getMessage(IdentityServiceProviderConstants.UNKNOWN_ERROR_CREATING_HANDLE);
				 def msg1 = MessageResolver.getMessage(IdentityServiceProviderConstants.MESSAGE);
				 log(msg+ msg1 +"'" + AbstractMessage.getResponseCodeMessage(response.responseCode) + "'");
				 throw new Exception(msg+ msg1 +"'" +AbstractMessage.getResponseCodeMessage(response.responseCode) + "'");
		    } 
		 }
	   } catch (Exception ex) {
	      def msg = MessageResolver.getMessage(IdentityServiceProviderConstants.ERROR_ATTEMPTING_TO_CREATING_HANDLE);
		  log.error(msg, ex);
	 	  throw new Exception(msg, ex);
       }
	   if(null != handle) {
	     return getPublishedDomain() + IdentityServiceProviderConstants.FORWARD_SLASH + handle;
	   }
	   return null;
	}
 	
   private HandleValue getUrlHandleValue(String url){
	   byte[] type = null;
	   byte[] urlBytes = null;
	   try {
	      type = IdentityServiceProviderConstants.URL.getBytes(IdentityServiceProviderConstants.UTF8);
		  urlBytes = url.trim().getBytes(IdentityServiceProviderConstants.UTF8);
	   }catch (Exception ex) {
	     def msg = MessageResolver.getMessage(IdentityServiceProviderConstants.ERROR_CREATING_URL_HANDLE_VALUE);
	     log.error(msg, ex);
	     throw new Exception(msg, ex);
       }
	   return createHandleValue(IdentityServiceProviderConstants.URL_INDEX, type, urlBytes);
   }	
   
   public String resolveTemplate(oid) throws Exception {
	   String template = getTemplate();
	   try {
		  if(null != template && !template.isEmpty()){
			 template = template.replace(IdentityServiceProviderConstants.OID_DELIMITER, oid);
			 if (template.contains(IdentityServiceProviderConstants.INC)) {
				 IdentityProviderIncrementor localIdentityProviderIncrementor = 
				   new  IdentityProviderIncrementor()
				 localIdentityProviderIncrementor.save();
				 template = template.replace(IdentityServiceProviderConstants.INC, 
					 localIdentityProviderIncrementor.id.toString());
			  }
		   } else{
				 def msg = MessageResolver.getMessage(IdentityServiceProviderConstants.
					 IDENTITY_PROVIDER_SERVICE_TEMPLATE_INC_MISSING);
				 log.error(msg);
				 throw new Exception(IdentityServiceProviderConstants.STATUS_400, new Throwable(msg));
		   }
		}catch(Exception ex){
		   def msg = MessageResolver.getMessage(IdentityServiceProviderConstants.IDENTITY_PROVIDER_SERVICE_FAILED);
		   log.error(msg+ ex.getMessage());
		   throw new Exception(IdentityServiceProviderConstants.STATUS_400, new Throwable(msg));
	   }
		return template;
   } 	
	
   private HandleValue  getAdminHandleValue(prefix) throws Exception{
	   byte[] type = null;
	   AdminRecord admin = new AdminRecord(prefix, IdentityServiceProviderConstants.PUBLIC_INDEX,
		               true, true, true, true, true, true,
		               true, true, true, true, true, true);
       try {
          type = IdentityServiceProviderConstants.HS_ADMIN.getBytes(IdentityServiceProviderConstants.UTF8); 		
	   } catch (Exception ex) {
	       def msg = MessageResolver.getMessage(IdentityServiceProviderConstants.ERROR_CREATING_ADMIN_HANDLE_VALUE);
		   log.error(msg, ex);
	       throw new Exception(msg, ex);
       }
	   return createHandleValue(IdentityServiceProviderConstants.ADMIN_INDEX, type,
			Encoder.encodeAdminRecord(admin));
   }
   
   private HandleValue getDescriptionHandleValue(String description) throws Exception{
	  byte[] descBytes = null;
	  byte[] type = null;
	  try {
  	      type = IdentityServiceProviderConstants.DESC.getBytes(IdentityServiceProviderConstants.UTF8);
	      descBytes = description.getBytes(IdentityServiceProviderConstants.UTF8);  
	  } catch (Exception ex) {
	    def msg = MessageResolver.getMessage(IdentityServiceProviderConstants.ERROR_CREATING_ADMIN_HANDLE_VALUE);
        log.error(msg, ex);  
		throw new Exception(msg, ex);
	 } 
	  return createHandleValue(IdentityServiceProviderConstants.PUBLIC_INDEX, type, descBytes);
   }
   
   private HandleValue createHandleValue(index, type, value){
 	  return new HandleValue(index, type, value,
		   HandleValue.TTL_TYPE_RELATIVE, 86400, (int) (System.currentTimeMillis() / 1000), null,
		                 true, true, true, false);
   }
	
   private byte[] getPrivateKeyToBytes() throws Exception{
	 try {
		  String keyPath = getPrivateKeyPath();
		  if(null == keyPath){
			  def msg = MessageResolver.getMessage(IdentityServiceProviderConstants.NO_PRIVATE_KEY);
			  log.error(msg);
			  throw new Exception(msg);
		  }
		  File file = new File(keyPath);
		  if(null == file){
			  def msg = MessageResolver.getMessage(IdentityServiceProviderConstants.PRIVATE_KEY_FILE_DOES_NOT_EXISTS);
			  log.error(msg+ "'" + keyPath + "'");
			  throw new Exception(msg + "'" + keyPath + "'");
		  }	  
		  FileInputStream stream = new FileInputStream(file);
	  	  byte[] response = IOUtils.toByteArray(stream);
		  stream.close();
		  return response;
	  } catch (Exception ex) {
	      def msg = MessageResolver.getMessage(IdentityServiceProviderConstants. ERROR_ACCESS_FILE);
          log.error( msg + ex);
		  throw new Exception(msg , ex);
      }
   }
	
   private byte[] getPassPhraseToBytes() throws Exception{
	 try {
		  String passPhrase = getPassPhrase();
		  return passPhrase.getBytes(IdentityServiceProviderConstants.UTF8);
	  }catch(Exception ex) {
	      def msg = MessageResolver.getMessage(IdentityServiceProviderConstants.ERROR_DURING_KEY_RESOLUTION);
		  log.error(msg+ ex);
		  throw new Exception(msg, ex);
      }
	  return null;
	}
}