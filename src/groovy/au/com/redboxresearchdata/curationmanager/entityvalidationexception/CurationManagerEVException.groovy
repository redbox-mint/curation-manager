package au.com.redboxresearchdata.curationmanager.entityvalidationexception

class CurationManagerEVException extends Exception{

	String key;
	String value;
	
	def CurationManagerEVException(String key, String value){
	  this.key = key;
	  this.value = value;
	}
	
	def String getKey(){
		return key;
	}
	
	def void setKey(String key){
		this.key = key;
	}
	
	
	def String getValue(){
		return value;
	}
	
	
	def void setValue(String value){
		this.value = value;
	}
}
