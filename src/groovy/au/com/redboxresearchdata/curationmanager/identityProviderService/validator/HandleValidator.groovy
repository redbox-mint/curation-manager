package au.com.redboxresearchdata.curationmanager.identityProviderService.validator

import java.util.Map;

import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log

import au.com.redboxresearchdata.curationmanager.utility.JsonUtil;
import au.com.redboxresearchdata.curationmanager.businesservicexception.CurationManagerBSException
import au.com.redboxresearchdata.curationmanager.identityProviderService.constants.IdentityServiceProviderConstants;

 class HandleValidator {
	
	private static final Log log = LogFactory.getLog(this)
	
	def Boolean validateMetaData(Map.Entry pairs) throws Exception{	
	  String key = pairs.getKey();	
	  String metaData = pairs.getValue();
	 	  Map metaDataMap = JsonUtil.getMapFromMetaData(metaData);
		  if(null != metaDataMap || !metaDataMap.isEmpty()) {
		     String description = metaDataMap.get("description");
		     String url = metaDataMap.get("url");
		     if(null == description && null == url){
		        log.error("No Description or Url provided for the Handle MetaData!");
		        throw new CurationManagerBSException(IdentityServiceProviderConstants.STATUS_400, 
					      "No Description or Url provided for the Handle MetaData!");
		 	    }
  	   }
	   return Boolean.TRUE
	 }
 }
