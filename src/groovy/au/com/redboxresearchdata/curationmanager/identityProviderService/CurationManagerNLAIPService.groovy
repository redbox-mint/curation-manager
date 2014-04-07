package au.com.redboxresearchdata.curationmanager.identityProviderService

import java.util.Map;

import au.com.redboxresearchdata.curationmanager.identityProviderService.utility.ApplicationContextHolder;
import au.com.redboxresearchdata.curationmanager.identityProviderService.validator.NLAValidator
import au.com.redboxresearchdata.curationmanager.identityProviderService.constants.IdentityServiceProviderConstants;
import au.com.redboxresearchdata.curationmanager.identityProviderResult.BaseIdentityResult
import au.com.redboxresearchdata.curationmanager.identityProviderResult.IdentifierResult
import au.com.redboxresearchdata.curationmanager.identityProviderResult.PromiseResult
import grails.converters.JSON

import javax.xml.bind.JAXBException

import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log

import au.com.redboxresearchdata.curationmanager.identityProviderService.utility.JsonUtil;
import au.com.redboxresearchdata.curationmanager.identityprovider.domain.IdentityProviderIncrementor

import au.com.redboxresearchdata.curationmanager.identityProviderService.utility.DateUtil;

class CurationManagerNLAIPService implements IdentityProviderService{
	
	private static final Log log = LogFactory.getLog(this)

	@Value("#{nlaPropSource[Id]}")
	String id;
	
	@Value("#{nlaPropSource[Name]}")
	String name;
	
	@Value("#{nlaPropSource[Type]}")
	String type;
	
	@Value("#{nlaPropSource[Template]}")
	String template;
	
	@Value("#{nlaPropSource[Agent]}")
	String agent
	
	@Value("#{nlaPropSource[AgencyCode]}")
	String agencyCode;
	
	@Value("#{nlaPropSource[AgencyName]}")
	String agencyName;
	
	@Value("#{nlaPropSource[MetadataPrefix]}")
	String metadataPrefix;
	
	@Value("#{nlaPropSource[RecordSource]}")
	String recordSource;
	
	@Value("#{nlaPropSource[DependentIdentityProviderName]}")
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
	public String getType() {
		return type;
	}
	
	@Override
	public String getAgent() {
		return agent;
	}
	
	@Override
	public String getRecordSource() {
		return recordSource;
	}
	
	@Override
	public String[] getMetadataPrefix() {
		 return metadataPrefix.split(",");
	}
	
	public Map<String, String> getMetaDataMap(String metaData) {
		return Boolean.TRUE;
	}
	
	public Boolean validate(Map.Entry pairs) throws Exception{
		IdentityProviderService  dependentIdtityPrviderService = getDependentProviderService();
		if(dependentIdtityPrviderService.validate(pairs)){
		   if(getType() != IdentityServiceProviderConstants.PERSON){
			   return Boolean.FALSE;
		   }
		   NLAValidator nlaValidator = new NLAValidator();
		   return nlaValidator.validateMetaData(pairs);
		}
		return Boolean.FALSE;
	}
	
	public String getDependentIdentityProviderName(){
		return dependentIdentityProviderName;
	}
	
	public IdentityProviderService getDependentProviderService() throws Exception{
	  if(null!= getDependentIdentityProviderName()) {
		 ApplicationContext applicationContext =  ApplicationContextHolder.getApplicationContext();
		 return applicationContext.getBean(getDependentIdentityProviderName());
	  } else if(null == getDependentIdentityProviderName()){
		log.error("No Dependent Identity Provider Service configured");
	    throw new Exception(IdentityServiceProviderConstants.STATUS_400, 
				  "No Dependent Identity Provider Service configured");
	  }
	}

	@Override
	public Boolean isSynchronous() {
		return isSynchronous;
	}

	public String getTemplate(){
		return template;
	}
	
	public String getAgencyCode(){
		return agencyCode;
	}
	
	public String getAgencyName(){
		return agencyName;
	}
	
	@Override
	public BaseIdentityResult curate(String oid, String... metaData) throws Exception {	 
    if(null!= getDependentProviderService() && null == metaData[3]){
		log.error("No Dependent Identifier");
        throw new Exception("No Dependent Identifier");
    }
	 BaseIdentityResult baseIdentityResult = null;
	 try{	
	   ApplicationContext applicationContext =  ApplicationContextHolder.getApplicationContext();
	   Map metaDataMap = mapFromMetaData(oid, metaData[0], metaData[1], metaData[2], metaData[3]);	 
	   JSON jsonMessage = map2Json(metaDataMap)
	   String validJson = jsonMessage.toString();
	   def jmsService = applicationContext.getBean(IdentityServiceProviderConstants.JMS_SERVICE);
	   jmsService.send(queue:"oaiPmhFeed", validJson);
	   baseIdentityResult = new PromiseResult()
	   baseIdentityResult.setIdentifier(oid);
	}catch(Exception ex){
	  log.error(ex.getMessage()+ "NLA Identy provider failed" + ex.getCause());
	  throw new Exception("NLA Identy provider failed");
	}
	  return baseIdentityResult;
	} 
	
	private JSON map2Json(Map metaDataMap) throws Exception{
		JSON json = metaDataMap as JSON
		if(null != json){
		  return json;
		}
		return null;
	}
	
	private Map mapFromMetaData(String oid, String metaData, String jobId, String type, String depdentIdentifier)
	  throws Exception{	 	  
	  Map<String, String> event = new HashMap();
	  Map requestJsonMap = JsonUtil.getMapFromMetaDataForNLA(metaData);
	  event.put("eventDateTime_standardDateTime", requestJsonMap.get("eventDateTime_standardDateTime"));
	  event.put("agent", getAgent());
	  	  
	  Map maintenanceEvent = new HashMap();
	  maintenanceEvent.put("maintenenceEvent", event);
	  
	  Map<String, String> agency = new HashMap<String, String>();
	  agency.put("agencyCode", getAgencyCode());
	  agency.put("agencyName", getAgencyName());
	  
	  Map<String, Map> maintenanceAgency = new HashMap();
	  maintenanceAgency.put("maintenanceAgency", agency);
	  maintenanceAgency.put("maintenanceHistory", maintenanceEvent);
	  
	  Map<String, String> recordId = new HashMap();
	  IdentityProviderIncrementor localIdentityProviderIncrementor = new IdentityProviderIncrementor()
	  localIdentityProviderIncrementor.save();
	  String idIncrementor;
	  if(null != localIdentityProviderIncrementor && null != localIdentityProviderIncrementor.id){
		  idIncrementor = localIdentityProviderIncrementor.id.toString();
		  recordId.put("recordId", jobId + oid + idIncrementor);
	  }else{
	     recordId.put("recordId", jobId + oid);
	  }
	  recordId.put("metadataPrefix", getMetadataPrefix());
	  recordId.put("source", getRecordSource()); 
	  
	  Map jsonData = new HashMap();
	  jsonData.put("recordId", oid);
	  jsonData.put("control", maintenanceAgency);
	  jsonData.put("entityId", depdentIdentifier);
	  jsonData.put("surname", requestJsonMap.get("family_name"));
	  jsonData.put("forename", requestJsonMap.get("given_name"));
	  jsonData.put("description", requestJsonMap.get("description"));
	  jsonData.put("salutation", requestJsonMap.get("salutation"));
	  jsonData.put("dateStamp", DateUtil.getW3CDate());
	   
	  recordId.put("jsonData", jsonData);
	  
	  Map<String, Map> data = new HashMap();
	  data.put("data", recordId);

	  Map<String, Map> typeRecord = new TreeMap();
	  String record = "record";
	  if(null != getType()){
		 record =  "record"+"_"+getType()
	  }
	  typeRecord.put("type", record);	
	 
	  Map<String, Map> header = new TreeMap<String, Map>();
	  header.put("header",  typeRecord);
	  header.put("data",  recordId);

	  return header;
	}
	
	@Override
	public Boolean exists(String oid, String[] metaData) {
		return exists;
	}
}
