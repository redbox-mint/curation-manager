package au.com.redboxresearchdata.curationmanager.businessvalidator

import java.util.List;
import java.util.regex.Matcher
import java.util.regex.Pattern
import au.com.redboxresearchdata.curationmanager.utility.StringUtil;
import au.com.redboxresearchdata.curationmanager.identityProviderResult.BaseIdentityResult
import au.com.redboxresearchdata.curationmanager.identityProviderResult.IdentifierResult
import au.com.redboxresearchdata.curationmanager.identityProviderResult.PromiseResult
import au.com.redboxresearchdata.curationmanager.identityProviderService.IdentityProviderService

import org.springframework.beans.factory.NoSuchBeanDefinitionException
import au.com.redboxresearchdata.curationmanager.businesservicexception.CurationManagerBSException
import au.com.redboxresearchdata.curationmanager.constants.CurationManagerConstants;

import au.com.redboxresearchdata.curationmanager.utility.ApplicationContextHolder

import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.context.ApplicationContext

class CurationManagerBV {
	
	ApplicationContext ctx = ApplicationContextHolder.getApplicationContext();
	
	def Boolean validateRequestParams(requestParams) throws Exception{
		List oids = new ArrayList();
		Set uniqueOids = new HashSet();
		for(JSONObject jsonObject: requestParams){
			validateNullOid(jsonObject);
			validateNullType(jsonObject);
			validateNullTitle(jsonObject);
			validateNullType(jsonObject);
			validateNullRequiredIdType(jsonObject);
			validateIdentityService(jsonObject)
			addOIDs(jsonObject, oids, uniqueOids);
		}		
		
		if(oids.size() != uniqueOids.size()){
			throw new CurationManagerBSException(CurationManagerConstants.STATUS_400, 
				CurationManagerConstants.FAILED_VALIDATION);
		}		
		return true;
	}
	
	
	def void addOIDs(jsonObject, oids, uniqueOids) throws Exception{
		def oid = jsonObject.get(CurationManagerConstants.OID);
		oids.add(oid);
		uniqueOids.add(oid);
	}
	
	
	def void validateNullOid(jsonObject) throws Exception{
			if(jsonObject.isNull(CurationManagerConstants.OID) || !jsonObject.has(CurationManagerConstants.OID)){
				throw new CurationManagerBSException(CurationManagerConstants.STATUS_400,
					 CurationManagerConstants.FAILED_VALIDATION);
			}
			def oid = jsonObject.get(CurationManagerConstants.OID);
			if(null == oid || "".equals(oid.toString())){
				throw new CurationManagerBSException(CurationManagerConstants.STATUS_400,
					 CurationManagerConstants.FAILED_VALIDATION);
			}
	}
	
	def void validateIdentityService(jsonObject) throws Exception{
		String reqIdentifiers = jsonObject.get(CurationManagerConstants.REQUIRED_IDENTIFIERS);
		List  newFilters = StringUtil.getFilters(reqIdentifiers)
		for(String identifierType : newFilters){
		   IdentityProviderService identityProviderService =  ctx.getBean(identifierType);
		}
	}
	
	
	def Boolean validateNullType(jsonObject){
			if(jsonObject.isNull(CurationManagerConstants.TYPE) || !jsonObject.has(CurationManagerConstants.TYPE)){
				throw new CurationManagerBSException(CurationManagerConstants.STATUS_400, 
					CurationManagerConstants.FAILED_VALIDATION);
			}
			def type = jsonObject.get(CurationManagerConstants.TYPE);
			if(null == type || "".equals(type.toString())){
				throw new CurationManagerBSException(CurationManagerConstants.STATUS_400, 
					CurationManagerConstants.FAILED_VALIDATION);
			}
	  return true;
	}
	
	
	def Boolean validateNullTitle(jsonObject){
			if(jsonObject.isNull(CurationManagerConstants.TITLE) || !jsonObject.has(CurationManagerConstants.TITLE)){
				throw new CurationManagerBSException(CurationManagerConstants.STATUS_400, 
					CurationManagerConstants.FAILED_VALIDATION);
			}
			def type = jsonObject.get(CurationManagerConstants.TITLE);
			if(null == type || "".equals(type.toString())){
				throw new CurationManagerBSException(CurationManagerConstants.STATUS_400,
					 CurationManagerConstants.FAILED_VALIDATION);
			}
	  return true;
	}
	
	
	def Boolean validateNullRequiredIdType(jsonObject){
			if(jsonObject.isNull(CurationManagerConstants.REQUIRED_IDENTIFIERS) || !jsonObject.has(CurationManagerConstants.REQUIRED_IDENTIFIERS)){
			    throw new CurationManagerBSException(CurationManagerConstants.STATUS_400, 
					CurationManagerConstants.FAILED_VALIDATION);
		     }
			def oid = jsonObject.get(CurationManagerConstants.REQUIRED_IDENTIFIERS);
			if(null == oid || "".equals(oid.toString())){
			    throw new CurationManagerBSException(CurationManagerConstants.STATUS_400,
					 CurationManagerConstants.FAILED_VALIDATION);
			}
		return true;
	}
	
	
	def List getFilters(reqIdentifiers){
		List newFilters = new ArrayList();
		String[]  newFilter = reqIdentifiers.split("\"identifier_type\""+":");
        for(int i=0; i<newFilter.length; i++){
			String newFilterField =  newFilter[i];
			   if(!"[{".equals(newFilterField)){				   
				   Pattern p = Pattern.compile("\"([^\"]*)\"");
				   Matcher m = p.matcher(newFilterField);
				   while (m.find()) {
					 newFilters.add(m.group(1));
				   }
			   }
			}
		return newFilters;
	}
}
