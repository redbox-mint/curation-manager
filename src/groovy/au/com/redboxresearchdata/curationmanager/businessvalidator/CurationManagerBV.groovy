package au.com.redboxresearchdata.curationmanager.businessvalidator

import au.com.redboxresearchdata.curationmanager.identityProviderService.constants.IdentityServiceProviderConstants;
import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log;
import java.util.List;
import java.util.regex.Matcher
import java.util.regex.Pattern

import au.com.redboxresearchdata.curationmanager.utility.JsonUtil;
import au.com.redboxresearchdata.curationmanager.identityProviderResult.BaseIdentityResult
import au.com.redboxresearchdata.curationmanager.identityProviderResult.IdentifierResult
import au.com.redboxresearchdata.curationmanager.identityProviderResult.PromiseResult
import au.com.redboxresearchdata.curationmanager.identityProviderService.CurationManagerHandleIPService
import au.com.redboxresearchdata.curationmanager.identityProviderService.IdentityProviderService
import au.com.redboxresearchdata.curationmanager.identityProviderService.validator.HandleValidator
import au.com.redboxresearchdata.curationmanager.utility.MessageResolver;

import au.com.redboxresearchdata.curationmanager.businesservicexception.CurationManagerBSException
import au.com.redboxresearchdata.curationmanager.constants.CurationManagerConstants;
import au.com.redboxresearchdata.curationmanager.utility.ApplicationContextHolder

import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.context.ApplicationContext


class CurationManagerBV {
	
   private static final Log log = LogFactory.getLog(this)	
   
  private String oidMsg = "with OID";

   def Boolean validateRequestParams(requestParams) throws CurationManagerBSException, Exception{
	   validateNullRequestParams(requestParams);
	   ApplicationContext ctx = ApplicationContextHolder.getApplicationContext();
	   for(JSONObject jsonObject: requestParams){
		  validateNullOid(jsonObject);
		  validateNullType(jsonObject);
		  validateNullTitle(jsonObject);
		  validateNullType(jsonObject);
		  String reqIdentifiers = jsonObject.get(CurationManagerConstants.REQUIRED_IDENTIFIERS);
		  Map newFilters = JsonUtil.getFilters(reqIdentifiers);
		  validateNullRequiredIdType(jsonObject);
		  validateIdentityService(newFilters,jsonObject, ctx)
		}					
		return Boolean.TRUE;
	}
	
	def void validateNullRequestParams(requestParams) throws CurationManagerBSException{
		if(null == requestParams){
			createException(CurationManagerConstants.REQUEST_PARAMS_NULL, " ",  " ", "");
		}
	}
	
	def void validateJMS(ctx){
		try{
		 def jmsService = ctx.getBean(IdentityServiceProviderConstants.JMS_SERVICE);
		  if(null == jmsService){
			  log.error("Check if the JMS is running");
			  throw new CurationManagerBSException(CurationManagerConstants.STATUS_400, "Check if the JMS is running");
		  }
		}catch(Exception ex){
		 log.error(ex.getMessage());
		 log.error(ex.getCause());
		 throw new CurationManagerBSException(CurationManagerConstants.STATUS_400, "Check if the JMS is running");
		}
	}
	
	def Boolean validateJobId(jobId) throws CurationManagerBSException{
		if(null == jobId) {
		   createException(CurationManagerConstants.JOB_ID_NULL, " ",  jobId, "");
		}
		return Boolean.TRUE
	}
	
	def Boolean validateOID(oid) throws CurationManagerBSException{
		if(null == oid) {
			createException(CurationManagerConstants.OID_NULL)
		}
		return Boolean.TRUE
	}
		
	def void validateNullOid(jsonObject) throws Exception{
		if(jsonObject.isNull(CurationManagerConstants.OID) || !jsonObject.has(CurationManagerConstants.OID)){
		   createException(CurationManagerConstants.OID_NULL, "", "", oidMsg)
		}
		def oid = jsonObject.get(CurationManagerConstants.OID);
		if(null == oid || "".equals(oid.toString())){
		   createException(CurationManagerConstants.OID_EMPTY, "", "", oidMsg)
		 }
	}
	
	def void validateIdentityService(Map newFilters, jsonObject, ApplicationContext ctx) throws Exception{
		Iterator it = newFilters.entrySet().iterator();
		String type = jsonObject.get(CurationManagerConstants.TYPE);
		def oid = jsonObject.get(CurationManagerConstants.OID);
		String key = null; 
		while (it.hasNext()) {
		  try {
			 Map.Entry pairs = (Map.Entry)it.next();
			 key = pairs.getKey(); 
		     IdentityProviderService identityProviderService =  ctx.getBean(pairs.getKey());
			 identityProviderService.validate(pairs, type);
		  } catch(CurationManagerBSException bex){
		     log.error(bex.getKey() + " "+ bex.getValue());
		     throw new CurationManagerBSException(CurationManagerConstants.STATUS_400, bex.getKey()+ " "+ bex.getValue());
		  } catch(Exception ex){
		     createException(CurationManagerConstants.IDENTITY_SERVICE_FAILED, oid, key, oidMsg)
		  }
		}
	}
	
	def Boolean validateNullType(jsonObject)throws CurationManagerBSException{
		def oid = jsonObject.get(CurationManagerConstants.OID);
		if(jsonObject.isNull(CurationManagerConstants.TYPE) || !jsonObject.has(CurationManagerConstants.TYPE)){
		   createException(CurationManagerConstants.TYPE_NULL, oid, "", oidMsg)
		}
		def type = jsonObject.get(CurationManagerConstants.TYPE);
		if(null == type || "".equals(type.toString())){
			createException(CurationManagerConstants.TYPE_EMPTY, oid, "", oidMsg)
		}
	    return Boolean.TRUE;
	}	
	
	def Boolean validateNullTitle(jsonObject)throws CurationManagerBSException{
		def oid = jsonObject.get(CurationManagerConstants.OID);
	    if(jsonObject.isNull(CurationManagerConstants.TITLE) || !jsonObject.has(CurationManagerConstants.TITLE)){
		  createException(CurationManagerConstants.TITLE_NULL, oid, "", oidMsg)
		}
		def title = jsonObject.get(CurationManagerConstants.TITLE);
		if(null == title ||  (title != null && "".equals(title.toString()))){
		   createException(CurationManagerConstants.TITLE_EMPTY, oid, "", oidMsg)
		}
	    return Boolean.TRUE;
	}	
	
	def Boolean validateNullRequiredIdType(jsonObject) throws CurationManagerBSException{
		def oid = jsonObject.get(CurationManagerConstants.OID);
		if(jsonObject.isNull(CurationManagerConstants.REQUIRED_IDENTIFIERS) ||
			!jsonObject.has(CurationManagerConstants.REQUIRED_IDENTIFIERS)){
			 createException(CurationManagerConstants.REQUIRED_IDENTIFIER_TYPE_NULL, oid, "")
		 }
	    def reqIdentifiers = jsonObject.get(CurationManagerConstants.REQUIRED_IDENTIFIERS);
		if(null == reqIdentifiers || (null != reqIdentifiers && "".equals(reqIdentifiers.toString()))){
		   createException(CurationManagerConstants.REQUIRED_IDENTIFIER_TYPE_EMPTY, oid, "", oidMsg)
		}
		return Boolean.TRUE;
	}	
	
	def void createException(name, oid, extramsg, oidMsgNew) throws CurationManagerBSException{
		def msg = MessageResolver.getMessage(name);
		String errorMsg = CurationManagerConstants.FAILED_VALIDATION +" "+ msg + " " + extramsg  + " " +oidMsgNew + " "+ oid;
		log.error(errorMsg);
		throw new CurationManagerBSException(CurationManagerConstants.STATUS_400, errorMsg);
	}
}