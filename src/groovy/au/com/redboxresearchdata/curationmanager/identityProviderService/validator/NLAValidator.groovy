package au.com.redboxresearchdata.curationmanager.identityProviderService.validator

import java.util.Map;

import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log

import au.com.redboxresearchdata.curationmanager.utility.JsonUtil;
import au.com.redboxresearchdata.curationmanager.businesservicexception.CurationManagerBSException
import au.com.redboxresearchdata.curationmanager.identityProviderService.constants.IdentityServiceProviderConstants;

 class NLAValidator {
	
	private static final Log log = LogFactory.getLog(this)
	
	def Boolean validateMetaData(Map.Entry pairs) throws Exception{	
	  String key = pairs.getKey();	
	  String metaData = pairs.getValue();
	     Map metaDataMap = JsonUtil.getMapFromMetaDataForNLA(metaData);
		  if(null != metaDataMap || !metaDataMap.isEmpty()) {
		     String givenName = metaDataMap.get("given_name");
			  String familyName = metaDataMap.get("family_name");
			  if(null == givenName && null == familyName) {
				 log.error("No family name or given name provided for the nla MetaData!");
				 throw new CurationManagerBSException(IdentityServiceProviderConstants.STATUS_400,
					       "No family name or given name provided for the nla MetaData!");
			  }
  		   }
	   return Boolean.TRUE
	 }
 }
