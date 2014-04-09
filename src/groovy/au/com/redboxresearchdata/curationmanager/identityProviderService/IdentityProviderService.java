package au.com.redboxresearchdata.curationmanager.identityProviderService;

import java.util.Map;

import au.com.redboxresearchdata.curationmanager.identityProviderResult.BaseIdentityResult;

public interface IdentityProviderService {

	public String getId();
	
	public String getName();
	
	public Boolean isSynchronous();
	
	public Boolean validate(Map.Entry<String, String> entry, String type) throws Exception;
	
	public Map<String, String> getMetaDataMap(String metaData);
	
	public IdentityProviderService getDependentProviderService() throws Exception;
	
	public BaseIdentityResult curate(String oid, String... metaData) throws Exception;
	
	public Boolean exists(String oid, String[] metaData);
}
