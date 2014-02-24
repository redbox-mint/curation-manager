package au.com.redboxresearchdata.curationmanager.businessvalidator

import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log;
import java.util.List;
import java.util.regex.Matcher
import java.util.regex.Pattern

import au.com.redboxresearchdata.curationmanager.utility.StringUtil;
import au.com.redboxresearchdata.curationmanager.identityProviderResult.BaseIdentityResult
import au.com.redboxresearchdata.curationmanager.identityProviderResult.IdentifierResult
import au.com.redboxresearchdata.curationmanager.identityProviderResult.PromiseResult
import au.com.redboxresearchdata.curationmanager.identityProviderService.IdentityProviderService
import au.com.redboxresearchdata.curationmanager.utility.MessageResolver;

import au.com.redboxresearchdata.curationmanager.businesservicexception.CurationManagerBSException
import au.com.redboxresearchdata.curationmanager.constants.CurationManagerConstants;
import au.com.redboxresearchdata.curationmanager.utility.ApplicationContextHolder

import org.apache.commons.logging.Log;
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.context.ApplicationContext

class CurationManagerBV {
	
	private static final Log log = LogFactory.getLog(this)	
	ApplicationContext ctx = ApplicationContextHolder.getApplicationContext();
	
	def Boolean validateRequestParams(requestParams) throws CurationManagerBSException, Exception{
		List oids = new ArrayList();
		Set uniqueOids = new HashSet();
		validateNullRequestParams(requestParams);
		for(JSONObject jsonObject: requestParams){
			validateNullOid(jsonObject);
			validateNullType(jsonObject);
			validateNullTitle(jsonObject);
			validateNullType(jsonObject);
			validateNullRequiredIdType(jsonObject);
			validateRequestIdentifierType(jsonObject);
			validateIdentityService(jsonObject)
			addOIDs(jsonObject, oids, uniqueOids);
		}				
		if(oids.size() != uniqueOids.size()){
		   createException(CurationManagerConstants.DUPLICATE_OID)
		}		
		return true;
	}
	
	def void validateRequestIdentifierType(requestParams) throws CurationManagerBSException{
		String reqIdentifiers = requestParams.get(CurationManagerConstants.REQUIRED_IDENTIFIERS);
		List  newFilters = StringUtil.getFilters(reqIdentifiers)
		Set uniqueReqIdentifiers = new HashSet(newFilters);
		if(newFilters.size() != uniqueReqIdentifiers.size()){
			createException(CurationManagerConstants.DUPLICATE_IDENTIFIER_TYPE)
		 }
	}
	
	def void validateNullRequestParams(requestParams) throws CurationManagerBSException{
		if(null == requestParams){
			createException(CurationManagerConstants.REQUEST_PARAMS_NULL);
		}
	}
	
	def void addOIDs(jsonObject, oids, uniqueOids) throws Exception{
		def oid = jsonObject.get(CurationManagerConstants.OID);
		oids.add(oid);
		uniqueOids.add(oid);
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
	
	def void validateIdentityService(jsonObject) throws Exception{
		String reqIdentifiers = jsonObject.get(CurationManagerConstants.REQUIRED_IDENTIFIERS);
		List  newFilters = StringUtil.getFilters(reqIdentifiers)
		for(String identifierType : newFilters){
		  try {
		     IdentityProviderService identityProviderService =  ctx.getBean(identifierType);
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
	  return true;
	}
	
	
	def Boolean validateNullTitle(jsonObject)throws CurationManagerBSException{
		if(jsonObject.isNull(CurationManagerConstants.TITLE) || !jsonObject.has(CurationManagerConstants.TITLE)){
			createException(CurationManagerConstants.TITLE_NULL)
			}
		def type = jsonObject.get(CurationManagerConstants.TITLE);
		if(null == type || "".equals(type.toString())){
			createException(CurationManagerConstants.TITLE_EMPTY)
		}
	  return true;
	}
	
	
	def Boolean validateNullRequiredIdType(jsonObject) throws CurationManagerBSException{
			if(jsonObject.isNull(CurationManagerConstants.REQUIRED_IDENTIFIERS) || !jsonObject.has(CurationManagerConstants.REQUIRED_IDENTIFIERS)){
				createException(CurationManagerConstants.REQUIRED_IDENTIFIER_TYPE_NULL)
		     }
			def oid = jsonObject.get(CurationManagerConstants.REQUIRED_IDENTIFIERS);
			if(null == oid || "".equals(oid.toString())){
				createException(CurationManagerConstants.REQUIRED_IDENTIFIER_TYPE_EMPTY)
			}
		return true;
	}
	
	
	def void createException(name) throws CurationManagerBSException{
		def msg = MessageResolver.getMessage(name);
		String errorMsg = msg + " " + CurationManagerConstants.FAILED_VALIDATION;
		log.error(errorMsg);
		throw new CurationManagerBSException(CurationManagerConstants.STATUS_400, errorMsg);
	}
}