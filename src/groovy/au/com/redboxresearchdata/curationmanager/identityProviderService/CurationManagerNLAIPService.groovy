package au.com.redboxresearchdata.curationmanager.identityProviderService
import au.com.redboxresearchdata.curationmanager.identityProviderResult.BaseIdentityResult
import au.com.redboxresearchdata.curationmanager.identityProviderResult.IdentifierResult

import org.springframework.beans.factory.annotation.Value

class CurationManagerNLAIPService implements IdentityProviderService{

	@Value("#{propSource[Id]}")
	String id;
	
	@Value("#{propSource[Name]}")
	String name;
	
	//@Value("#{propSource[Synchronous]}")
	Boolean isSynchronous = Boolean.TRUE;
	
	@Value("#{propSource[Template]}")
	String template;
	
	//@Value("#{propSource[Exists]}")
	Boolean exists;
	
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
		 return null;
	 }
	
	@Override
	public Boolean exists(String oid, String[] metaData) {
		return exists;
	}
}
