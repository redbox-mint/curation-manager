package au.com.redboxresearchdata.curationmanager.entityvalidator

import au.com.redboxresearchdata.curationmanager.entityvalidationexception.CurationManagerEVException
import au.com.redboxresearchdata.curationmanager.constants.CurationManagerConstants;
import au.com.redboxresearchdata.curationmanager.domain.Entry


class CurationManagerEV {
	
	def void validateOidAndIdentityType(newFilters, identifierType) throws CurationManagerEVException{
		for(String newFilter : newFilters){
			 if(newFilter.equals(identifierType)){
				throw new CurationManagerEVException(CurationManagerConstants.STATUS_400, CurationManagerConstants.FAILED_VALIDATION); 
			 }
		}
	} 
	
	def void validateEntryType(oid, entry,  type) throws CurationManagerEVException{
			if(!type.equals(entry.getEntryTypeLookup().getValue())){
		       throw new CurationManagerEVException(CurationManagerConstants.STATUS_400, CurationManagerConstants.FAILED_VALIDATION);
	    }
	} 
}
