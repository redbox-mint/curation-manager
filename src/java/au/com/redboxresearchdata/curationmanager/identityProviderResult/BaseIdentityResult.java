package au.com.redboxresearchdata.curationmanager.identityProviderResult;

public class BaseIdentityResult {
	String identifier;

	public BaseIdentityResult(String identifier) {
		 this.identifier = identifier;
	}
	
	public void setIdentifier(String identifier){
		this.identifier = identifier;
	}

	public String getIdentifier(){
		return identifier;
	}

}


