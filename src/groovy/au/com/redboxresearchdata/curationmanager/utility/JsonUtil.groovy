package au.com.redboxresearchdata.curationmanager.utility

import java.util.List;
import java.util.regex.Matcher
import java.util.regex.Pattern

import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log;

import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.grails.web.json.JSONArray
import org.springframework.context.ApplicationContext

import au.com.redboxresearchdata.curationmanager.businesservicexception.CurationManagerBSException
import au.com.redboxresearchdata.curationmanager.constants.CurationManagerConstants;
import au.com.redboxresearchdata.curationmanager.identityProviderService.IdentityProviderService
import au.com.redboxresearchdata.curationmanager.identityProviderService.constants.IdentityServiceProviderConstants;

class JsonUtil {

	private static final Log log = LogFactory.getLog(this);
	
	def static Map getFilters(reqIdentifiers) throws CurationManagerBSException{		
	  Map identiferAndMetaData = new HashMap();
	  try{	
		ApplicationContext applicationContext =  ApplicationContextHolder.getApplicationContext();
		reqIdentifiers = "{"+"required_identifiers"+":"+ reqIdentifiers +"}";
		JSONObject jsonMetaData = new JSONObject(reqIdentifiers);
		JSONArray jsonArray = jsonMetaData.getJSONArray(CurationManagerConstants.REQUIRED_IDENTIFIERS);
	
		for(JSONObject jObject : jsonArray){		
			String identifierType = jObject.get("identifier_type");
			String metaData =  null;
			if(jObject.containsKey("metadata")){
			   metaData = jObject.get("metadata");	
			}		
			if(null != identiferAndMetaData.get(identifierType)){
				def msg = MessageResolver.getMessage(CurationManagerConstants.FAILED_VALIDATION);
				log.error(msg);
				throw new CurationManagerBSException(CurationManagerConstants.STATUS_400, new Throwable(msg));
			}			
			identiferAndMetaData.put(identifierType, metaData);
		}
		if(identiferAndMetaData.containsKey("nla")) {
			IdentityProviderService identityProviderService = applicationContext.getBean("nla");
			if(null != identityProviderService){
			   IdentityProviderService dependentIdentityProviderService = identityProviderService.getDependentProviderService();
			   String dependentServiceId = dependentIdentityProviderService.getId();
			   if(null != dependentServiceId) {
			     identiferAndMetaData.remove(dependentServiceId);
			   }
			}
		}
	  } catch (Exception ex) {
		def msg = MessageResolver.getMessage(IdentityServiceProviderConstants. ERROR_ACCESS_FILE);
		log.error( msg + ex);
		throw new CurationManagerBSException(CurationManagerConstants.STATUS_400, 
			"Failed to process required identifers.");
	  }
		return identiferAndMetaData;
	}	
	
	def static Map getMapFromMetaData(String metaData){	
		JSONObject jsonMetaData = new JSONObject(metaData);
		Map filterMap = new HashMap();
		if(null != jsonMetaData){
			String description = (String)jsonMetaData.get("description");
			String url = (String)jsonMetaData.get("url");
			filterMap.put("description", description);
			filterMap.put("url", url);
		}
		return filterMap;
	}	
	
	
	def static Map getMapFromMetaDataForNLA(String metaData){
		JSONObject jsonMetaData = new JSONObject(metaData);
		Map filterMap = new HashMap();
		if(null != jsonMetaData){
			String description = (String)jsonMetaData.get("forename");
			String url = (String)jsonMetaData.get("surname");
			filterMap.put("forename", description);
			filterMap.put("surname", url);
		}
		return filterMap;
	}
}
