package au.com.redboxresearchdata.curationmanager.businessvalidator

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
		  validateIdentityService(newFilters, ctx)
		}					
		return Boolean.TRUE;
	}
	
	def void validateNullRequestParams(requestParams) throws CurationManagerBSException{
		if(null == requestParams){
			createException(CurationManagerConstants.REQUEST_PARAMS_NULL);
		}
	}
	
	def Boolean validateJobId(jobId) throws CurationManagerBSException{
		if(null == jobId) {
		   createException(CurationManagerConstants.JOB_ID_NULL);
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
		   createException(CurationManagerConstants.OID_NULL)
		}
		def oid = jsonObject.get(CurationManagerConstants.OID);
		if(null == oid || "".equals(oid.toString())){
		   createException(CurationManagerConstants.OID_EMPTY)
		 }
	}
	
	def void validateIdentityService(Map newFilters, ApplicationContext ctx) throws Exception{
		Iterator it = newFilters.entrySet().iterator();
		while (it.hasNext()) {
		  try {
			 Map.Entry pairs = (Map.Entry)it.next();
		     IdentityProviderService identityProviderService =  ctx.getBean(pairs.getKey());
			 identityProviderService.validate(pairs);
		  } catch(Exception ex){
		     createException(CurationManagerConstants.IDENTITY_SERVICE_FAILED)
		  }
		}
	}
	
	def Boolean validateNullType(jsonObject)throws CurationManagerBSException{
		if(jsonObject.isNull(CurationManagerConstants.TYPE) || !jsonObject.has(CurationManagerConstants.TYPE)){
		   createException(CurationManagerConstants.TYPE_NULL)
		}
		def type = jsonObject.get(CurationManagerConstants.TYPE);
		if(null == type || "".equals(type.toString())){
			createException(CurationManagerConstants.TYPE_EMPTY)
		}
	    return Boolean.TRUE;
	}	
	
	def Boolean validateNullTitle(jsonObject)throws CurationManagerBSException{
	   if(jsonObject.isNull(CurationManagerConstants.TITLE) || !jsonObject.has(CurationManagerConstants.TITLE)){
		  createException(CurationManagerConstants.TITLE_NULL)
		}
		def type = jsonObject.get(CurationManagerConstants.TITLE);
		if(null == type || "".equals(type.toString())){
		   createException(CurationManagerConstants.TITLE_EMPTY)
		}
	    return Boolean.TRUE;
	}	
	
	def Boolean validateNullRequiredIdType(jsonObject) throws CurationManagerBSException{
		if(jsonObject.isNull(CurationManagerConstants.REQUIRED_IDENTIFIERS) ||
			!jsonObject.has(CurationManagerConstants.REQUIRED_IDENTIFIERS)){
			 createException(CurationManagerConstants.REQUIRED_IDENTIFIER_TYPE_NULL)
		 }
	    def oid = jsonObject.get(CurationManagerConstants.REQUIRED_IDENTIFIERS);
		if(null == oid || "".equals(oid.toString())){
		   createException(CurationManagerConstants.REQUIRED_IDENTIFIER_TYPE_EMPTY)
		}
		return Boolean.TRUE;
	}	
	
	def void createException(name) throws CurationManagerBSException{
		def msg = MessageResolver.getMessage(name);
		String errorMsg = msg + " " + CurationManagerConstants.FAILED_VALIDATION;
		log.error(errorMsg);
		throw new CurationManagerBSException(CurationManagerConstants.STATUS_400, errorMsg);
	}
}