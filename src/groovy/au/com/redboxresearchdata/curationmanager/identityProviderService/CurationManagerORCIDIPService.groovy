package au.com.redboxresearchdata.curationmanager.identityProviderService;

import identityProviderResult.BaseIdentityResult;
import java.util.Map;

import au.com.redboxresearchdata.curationmanager.identityProviderResult.IdentifierResult;
import au.com.redboxresearchdata.curationmanager.identityProviderResult.BaseIdentityResult;

import org.springframework.beans.factory.annotation.Value;

class CurationManagerORCIDIPService implements IdentityProviderService{

	@Value("#{orcPropSource[Id]}")
	String id;
	
	@Value("#{orcPropSource[Name]}")
	String name;
	
	@Value("#{nlaPropSource[Type]}")
	String type;
	
	@Value("#{orcPropSource[Description]}")
	String description;
	
	@Value("#{orcPropSource[DependentIdentityProviderName]}")
	String dependentIdentityProviderName;
	
	private Boolean isSynchronous = Boolean.FALSE;
	
	private Boolean exists;
	
    @Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}	
	
	@Override
	public String[] getType() {
		return  type.split(",");
	}
	
	
	public Boolean validate(Map.Entry<String, String> pairs, String type) throws Exception{
		return Boolean.TRUE;
	}
	
	public String getDependentIdentityProviderName(){
		return dependentIdentityProviderName;
	}
	
	public IdentityProviderService getDependentProviderService() throws Exception{
		return null;
	}

	public String getDescription(){
		return description;
	}	
	
	@Override
	public Boolean isSynchronous() {
		return isSynchronous;
	}
	
	@Override
	public BaseIdentityResult curate(String oid, String... metaData) throws Exception {
		 return null;
	 }
	
	@Override
	public Boolean exists(String oid, String[] metaData) {
		return exists;
	}
	
	public Map<String, String> getMetaDataMap(String metaData) {
		return null;
	}
}