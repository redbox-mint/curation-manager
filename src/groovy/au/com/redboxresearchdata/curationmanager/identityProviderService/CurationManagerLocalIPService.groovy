package au.com.redboxresearchdata.curationmanager.identityProviderService

import au.com.redboxresearchdata.curationmanager.constants.CurationManagerConstants
import au.com.redboxresearchdata.curationmanager.identityProviderResult.BaseIdentityResult
import au.com.redboxresearchdata.curationmanager.identityProviderResult.IdentifierResult

import org.springframework.beans.factory.annotation.Value

import au.com.redboxresearchdata.curationmanager.domain.Curation
import au.com.redboxresearchdata.curationmanager.domain.CurationJob
import au.com.redboxresearchdata.curationmanager.domain.CurationJobItems
import au.com.redboxresearchdata.curationmanager.domain.CurationStatusLookup
import au.com.redboxresearchdata.curationmanager.domain.LocalIdentityProviderIncrementor



class CurationManagerLocalIPService  implements IdentityProviderService{

	@Value("#{propSource[Id]}")
	String id;
	
	@Value("#{propSource[Name]}")
	String name;
	
	//@Value("#{propSource[Synchronous]}")
	Boolean isSynchronous = Boolean.TRUE;
	
	@Value("#{propSource[Template]}")
	String template;
	
	//@Value("#{propSource[Exists]}")
	Boolean exists = Boolean.FALSE;
	
    @Override
	public String getID() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Boolean isSynchronous() {
		return isSynchronous;
	}

	public String getTemplate(){
		return template;
	}
	
	@Override
	public BaseIdentityResult curate(String oid, String[] metaData) throws Exception {
		BaseIdentityResult baseIdentifier; 
		try {
		   if(null != template){
		      String template = getTemplate();
		      String identifier = template.replace(CurationManagerConstants.OID_DELIMITER, oid);
			  if (identifier.contains(CurationManagerConstants.INC_DELIMITER)) {
				  LocalIdentityProviderIncrementor localIdentityProviderIncrementor = new LocalIdentityProviderIncrementor()
				  localIdentityProviderIncrementor.save();
			     identifier = identifier.replace(CurationManagerConstants.INC_DELIMITER, localIdentityProviderIncrementor.id.toString());
			  }
		      baseIdentifier = new IdentifierResult(identifier);
		    } else{
			    throw new Exception();
		    }
	     }catch(Exception ex){
		   throw new Exception();
		}	
		 return baseIdentifier;
	 }
	
	@Override
	public Boolean exists(String oid, String[] metaData) {
		return exists;
	}
}