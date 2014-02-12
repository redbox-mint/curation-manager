package au.com.redboxresearchdata.curationmanager.businesservicexception

class CurationManagerBSException extends Exception{

	String key;
	String value;
	
	def CurationManagerBSException(String key, String value){
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
