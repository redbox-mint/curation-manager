package au.com.redboxresearchdata.curationmanager.entityvalidator

import au.com.redboxresearchdata.curationmanager.utility.MessageResolver;
import au.com.redboxresearchdata.curationmanager.entityvalidationexception.CurationManagerEVException
import au.com.redboxresearchdata.curationmanager.constants.CurationManagerConstants;
import au.com.redboxresearchdata.curationmanager.domain.Entry


class CurationManagerEV {
	
	def void validateOidAndIdentityType(newFilters, identifierType) throws CurationManagerEVException{
		for(String newFilter : newFilters){
			 if(newFilter.equals(identifierType)){
				def msg = MessageResolver.getMessage(CurationManagerConstants.OID_IDENTIFIER_TYPE_EXISTS);
				throw new CurationManagerEVException(CurationManagerConstants.STATUS_400, msg); 
			 }
		}
	} 
	
	def void validateEntryType(oid, entry,  type) throws CurationManagerEVException{
			if(!type.equals(entry.getEntryTypeLookup().getValue())){
			   def msg = MessageResolver.getMessage(CurationManagerConstants.OID_ENTRY_TYPE_EXISTS);
		       throw new CurationManagerEVException(CurationManagerConstants.STATUS_400, msg);
	    }
	} 
}
