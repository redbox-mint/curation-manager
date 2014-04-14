package au.com.redboxresearchdata.curationmanager.identityProviderService

import java.util.Map;

import au.com.redboxresearchdata.curationmanager.businesservicexception.CurationManagerBSException
import au.com.redboxresearchdata.curationmanager.businessvalidator.CurationManagerBV
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
import au.com.redboxresearchdata.curationmanager.identityProviderService.utility.MessageResolver;

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
		return  type;
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

	public Boolean validate(Map.Entry<String, String> pairs, String requestType) throws Exception{
		if(null!= type && !type.contains(requestType)){
			def msg = MessageResolver.getMessage(IdentityServiceProviderConstants.IDENTITY_SERVICE_TYPE_NLA_DOES_NOT_MATCH);
			log.error(msg + " " + requestType);
			throw new CurationManagerBSException(IdentityServiceProviderConstants.STATUS_400, msg+ " " + requestType);
		}
		IdentityProviderService  dependentIdtityPrviderService = getDependentProviderService();
		dependentIdtityPrviderService.validate(pairs, requestType);
		NLAValidator nlaValidator = new NLAValidator();
		return nlaValidator.validateMetaData(pairs);
	}

	public String getDependentIdentityProviderName(){
		return dependentIdentityProviderName;
	}

	public IdentityProviderService getDependentProviderService() throws Exception{
		if(null!= getDependentIdentityProviderName()) {
			ApplicationContext applicationContext =  ApplicationContextHolder.getApplicationContext();
			return applicationContext.getBean(getDependentIdentityProviderName());
		} else if(null == getDependentIdentityProviderName()){
			log.error("No Dependent Identity Provider Service configured for NLA ");
			throw new Exception(IdentityServiceProviderConstants.STATUS_400,
			"No Dependent Identity Provider Service configured for NLA");
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
			Map metaDataMap = makeHarvestManagerMessage(oid, metaData[0], metaData[1], metaData[2], metaData[3]);
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

	/**
	 * Produce a message for the Harvest Manager so that a OAI-PMH record can be created for the NLA to harvest
	 * 
	 * @param oid
	 * @param metaData
	 * @param jobId
	 * @param type
	 * @param depdentIdentifier
	 * @return
	 * @throws Exception
	 */
	private Map makeHarvestManagerMessage(String oid, String metaData, String jobId, String type, String depdentIdentifier)
	throws Exception{
		//TODO: Remove this line as it shouldn't filter anything that comes from ReDBox/Mint
		Map requestJsonMap = JsonUtil.getMapFromMetaDataForNLA(metaData);
		def recordSource = "Curation Manager NLA Identity Provider"
		IdentityProviderIncrementor localIdentityProviderIncrementor = new IdentityProviderIncrementor()
		localIdentityProviderIncrementor.save();
		String idIncrementor;
		//TODO: Investigate the need for this
		def recordId = ""
		if(null != localIdentityProviderIncrementor && null != localIdentityProviderIncrementor.id){
			idIncrementor = localIdentityProviderIncrementor.id.toString();
			recordId = jobId + oid + idIncrementor;
		}else{
			recordId =  jobId + oid;
		}

		def mdPrefix = ["eac-cpf", "oai_dc"]

		//Build the record template data
		def jsonDataMap = [
			"recordId":depdentIdentifier,
			"control":[
				"maintenanceAgency":["agencyCode":getAgencyCode(), "agencyName":getAgencyName()],
				"maintenanceHistory":[
					"maintenanceEvent":[
						"eventDateTime_standardDateTime":DateUtil.getW3CDate(),
						"agent":getAgent()
					]
				]
			],
			"entityId":depdentIdentifier,
			"dateStamp":DateUtil.getW3CDate()
		]
		
		//Merge in all the metadata we received from ReDBox/Mint about the record so that it can be used to generate the eac-cpf
		jsonDataMap.putAll(requestJsonMap)

		def jsonMapData = [
			"header":[
				"type":"record_person"
			],
			"data":[
				[
					"recordId":depdentIdentifier,
					"metadataPrefix":mdPrefix,
					"source":recordSource,
					"jsonData":jsonDataMap
				]
			]
		]

		return jsonMapData
	}


	@Override
	public Boolean exists(String oid, String[] metaData) {
		return exists;
	}
}
