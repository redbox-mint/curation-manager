package au.com.redboxresearchdata.curationmanager.identityProviderService;

import au.com.redboxresearchdata.curationmanager.identityProviderResult.BaseIdentityResult;

public interface IdentityProviderService {

	public String getID();
	
	public String getName();
	
	public Boolean isSynchronous();
	
	public BaseIdentityResult curate(String oid, String[] metaData) throws Exception;
	
	public Boolean exists(String oid, String[] metaData);
}
