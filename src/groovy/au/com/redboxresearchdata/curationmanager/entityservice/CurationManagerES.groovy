package au.com.redboxresearchdata.curationmanager.entityservice

import java.util.List;
import java.util.regex.Matcher
import java.util.regex.Pattern

import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil;
import org.codehaus.groovy.grails.web.json.JSONObject

import au.com.redboxresearchdata.curationmanager.utility.StringUtil;

import au.com.redboxresearchdata.curationmanager.response.CurationManagerResponse
import au.com.redboxresearchdata.curationmanager.entityvalidationexception.CurationManagerEVException
import au.com.redboxresearchdata.curationmanager.entityvalidator.CurationManagerEV
import au.com.redboxresearchdata.curationmanager.utility.DateUtil
import au.com.redboxresearchdata.curationmanager.constants.CurationManagerConstants
import au.com.redboxresearchdata.curationmanager.domain.Curation
import au.com.redboxresearchdata.curationmanager.domain.CurationJob
import au.com.redboxresearchdata.curationmanager.domain.CurationJobItems
import au.com.redboxresearchdata.curationmanager.domain.CurationStatusLookup
import au.com.redboxresearchdata.curationmanager.domain.Entry
import au.com.redboxresearchdata.curationmanager.domain.EntryTypeLookup

import org.hibernate.FetchMode

class CurationManagerES {

	def CurationManagerResponse createOrFindEntityAndRelationship(requestParams) 
	   throws CurationManagerEVException, Exception {
		
		Date dateW3C = DateUtil.getW3CDate();
		CurationStatusLookup curationStatusLookup = statusLookup(CurationManagerConstants.IN_PROGRESS)
		CurationJob curationJob = createCurationJobEntity(requestParams, dateW3C);
		Boolean curationJobExists = true;
		List jobItems = new ArrayList();
		CurationManagerResponse curationManagerResponse = new CurationManagerResponse();
		CurationManagerEV curationManagerEV = new CurationManagerEV();
		Boolean updateCurationJob = false;
		for(JSONObject jsonObject: requestParams){
			def oid = jsonObject.get(CurationManagerConstants.OID);
			def type = jsonObject.get(CurationManagerConstants.TYPE);
			def title = jsonObject.get(CurationManagerConstants.TITLE);
			String reqIdentifiers = jsonObject.get(CurationManagerConstants.REQUIRED_IDENTIFIERS);
			List results = findEntryAndRelationships(oid);
			if(null == results || results.size() == 0){
				List curationsByOid = findCurationsByOid(oid);
				createEntryFromCurations(curationJob, curationsByOid, oid, type, title);
			}			
			List  newFilters = StringUtil.getFilters(reqIdentifiers)
			Map reqIdentifiersNew = new HashMap();
			setRequiredIdentifersMap(reqIdentifiersNew, oid, title, type)
			jobItems.add(reqIdentifiersNew);
			if(results.size() > 0){
				for(Entry entry : results)	 {
					if(oid.equals(entry.getId())){
					   curationManagerEV.validateEntryType(oid, entry,  type)
				       CurationJob.withNewTransaction {
					    Map reqIdentifiersNewMap = createCurationJobItemsFromEntry(entry, curationJob, oid, 
							type, title, jobItems, 
							curationStatusLookup, newFilters, dateW3C);
						jobItems.add(reqIdentifiersNewMap);
				    }
				  }
				}
			}else{
			   Entry entry = createEntryEntity(oid, type, title);
			   Map reqIdentifiersNewMap = createCurationEntities(oid, type, title, jobItems, curationJob,
				  entry, curationStatusLookup,  newFilters,  dateW3C, false);
				  jobItems.add(reqIdentifiersNewMap);
			 }
		}		
		curationJob.save();
    	if(null !=  curationJob && null != curationJob.getId()){
		  setCurationManagerResponse(curationJob, curationManagerResponse, jobItems)
	 	}
		return curationManagerResponse;
	}
	
	
	def void createEntryFromCurations(curationJob, curationsByOid, oid, type, title){
		Entry entryNew = createEntryEntity(oid, type, title)
		for(Curation curationByOid: curationsByOid){
			Entry entry = curationByOid.getEntry();
			List<CurationJobItems> curationJobItems = curationByOid.getCurationJobItems();
			for(CurationJobItems curationJobItem: curationJobItems){
			 if(null == entry){
				entryNew.addToCurations(curationByOid);
				curationByOid.setEntry(entryNew);
				curationJobItem.setCurationJob(curationJob);
				curationJob.addToCurationJobItems(curationJobItem);
			   }
			}
		}
	}
 
	def List findCurationsByOid(oid){
		def results =  Curation.withCriteria(){
			createAlias(CurationManagerConstants.ENTRY, CurationManagerConstants.ENTRY)
			eq(CurationManagerConstants.ENTRY_ID, oid)
		}
		return results;
	}
	
	
	def Map createCurationJobItemsFromEntry(entry, curationJob, oid, type, title, 
		jobItems, curationStatusLookup, newFilters, dateW3C){
	    List curations;
		if (!GrailsHibernateUtil.isInitialized(entry, CurationManagerConstants.CURATIONS) || entry.isAttached()){
		    entry.attach();
		}
	    curations = entry.getCurations();	
		Map requiredIdentifiersNew =  new HashMap();
		List reqIdentifiersNew = new ArrayList();
		for(Curation curationNew : curations){
		   String entryOid = curationNew.getEntry().getId();
		   String identifierType = curationNew.getIdentifierType();
		   if(newFilters.contains(identifierType) && entryOid.equals(oid)){
			   Map requiredIdentifiers =  new HashMap();
			   CurationJobItems curationJobItemsNew = createCurationJobItemsEntity(curationJob);
			   curationJobItemsNew.setCuration(curationNew);
			   curationJobItemsNew.setCurationJob(curationJob);
			   curationJob.addToCurationJobItems(curationJobItemsNew);
			   newFilters.remove(identifierType)
			   requiredIdentifiers.put(CurationManagerConstants.STATUS, curationNew.getCurationStatusLookup().getValue());
			   requiredIdentifiers.put(CurationManagerConstants.IDENTIFIER, curationNew.getIdentifier());
			   requiredIdentifiers.put(CurationManagerConstants.IDENTIFIER_TYPE, curationNew.getIdentifierType());
			   requiredIdentifiers.put(CurationManagerConstants.DATE_CREATED, curationNew.getDateCreated());
			   requiredIdentifiers.put(CurationManagerConstants.DATE_COMPLETED, curationNew.getDateCompleted());
			   reqIdentifiersNew.add(requiredIdentifiers);
			   }
		}
		requiredIdentifiersNew.put(CurationManagerConstants.REQIDENTIFIERS, reqIdentifiersNew)
		if(newFilters.size() > 0 && entry.getId().equals(oid)){
			requiredIdentifiersNew.putAll(createCurationEntities(oid, type, title, jobItems,  curationJob,
			   entry, curationStatusLookup,  newFilters,  dateW3C, false));
		}
		
		return requiredIdentifiersNew;
	}
	
	def void setCurationManagerResponse(curationJob, curationManagerResponse, jobItems){
		curationManagerResponse.setJobId(curationJob.getId());
		curationManagerResponse.setJobStatus(CurationManagerConstants.IN_PROGRESS);
		curationManagerResponse.setDateCreated(curationJob.getDateCreated());
		curationManagerResponse.setDateCompleted(curationJob.getDateCompleted());
		curationManagerResponse.setJobItems(jobItems);
	}
	
	def void setRequiredIdentifersMap(reqIdentifiersNew, oid, title, type){
		reqIdentifiersNew.put(CurationManagerConstants.OID, oid);
		reqIdentifiersNew.put(CurationManagerConstants.TITLE, title);
		reqIdentifiersNew.put(CurationManagerConstants.TYPE, type);
	}
	
	
	def List findEntryAndRelationships(oid){
		def results =  Entry.withCriteria(){
			eq("id", oid)
		}
		return results;
	}
	
	def Map createCurationEntities(oid, type, title, jobItems, curationJob,
		entry, curationStatusLookup,  newFilters,  dateW3C, isEntry){
		Map requiredIdentifiersNew =  new HashMap();
		List reqIdentifiersNew = new ArrayList();
		for(int i=0;i<newFilters.size(); i++){
			    createCurationEntity(entry, newFilters[i],  curationStatusLookup, reqIdentifiersNew,
				  newFilters, curationJob, dateW3C, isEntry)		
		}
		requiredIdentifiersNew.put(CurationManagerConstants.REQIDENTIFIERS, reqIdentifiersNew)
		return requiredIdentifiersNew;
	}
		
	
	def void createCurationEntity(entry, newFilterFiled,  curationStatusLookup, reqIdentifiersNew,
		 newFilters, curationJob, dateW3C, isEntry){
		Map requiredIdentifiers =  new HashMap();
		CurationJobItems curationJobItems = createCurationJobItemsEntity(curationJob);
		Curation curation  = new Curation();
		curation.setDateCreated(dateW3C);
		curation.setIdentifierType(newFilterFiled);
		curation.setCurationStatusLookup(curationStatusLookup);
		curationJobItems.setCuration(curation);
		entry.addToCurations(curation);
		curation.setEntry(entry);
		curation.addToCurationJobItems(curationJobItems);
		curationJob.addToCurationJobItems(curationJobItems);
		requiredIdentifiers.put(CurationManagerConstants.STATUS, curationStatusLookup.getValue());
		requiredIdentifiers.put(CurationManagerConstants.IDENTIFIER, "");
		requiredIdentifiers.put(CurationManagerConstants.IDENTIFIER_TYPE, newFilterFiled);
		requiredIdentifiers.put(CurationManagerConstants.DATE_CREATED, dateW3C);
		requiredIdentifiers.put(CurationManagerConstants.DATE_COMPLETED, "");
		reqIdentifiersNew.add(requiredIdentifiers);
	}
		
   def void copyCurationJobItems(curationJob, curationsOld){
	   CurationJobItems curationJobItemsNew = createCurationJobItemsEntity(curationJob);
	   curationJobItemsNew.setCuration(curationsOld);
	   curationJob.addToCurationJobItems(curationJobItemsNew);
   }
		 
		
	def CurationJob createCurationJobEntity(requestParams, dateW3C){
		CurationJob curationJob = new CurationJob();
		curationJob.setDateCreated(dateW3C);
		curationJob.setCurationStatusLookup(CurationStatusLookup.findByValue(CurationManagerConstants.IN_PROGRESS));
		return curationJob;
	}
	
	def CurationJobItems createCurationJobItemsEntity(curationJob){
		CurationJobItems curationJobItems = new CurationJobItems();
		curationJobItems.setCurationJob(curationJob);
		return curationJobItems;
	}
	
	def Entry createEntryEntity(oid, type, title){
		Entry entry = new Entry();
		entry.setId(oid);
		EntryTypeLookup entryTypeLookup = EntryTypeLookup.findByValue(type);
		entry.setEntryTypeLookup(entryTypeLookup);
		entry.setTitle(title);
		return entry;
	}
	
	def void updateCuration(curation, identifier, curationStatus, dateCompleted,   errorMsg){
		curation.setIdentifier(identifier);
		curation.setError(errorMsg);
		curation.setDateCompleted(dateCompleted);
		curation.setCurationStatusLookup(curationStatus);
		curation.save();
	}
	
	def CurationStatusLookup statusLookup(statusValue){
		return CurationStatusLookup.findByValue(statusValue);
	}
	
	def void updateCurationJob(curationJob, curationStatus){
		curationJob.setCurationStatusLookup(curationStatus);
		curationJob.setDateCompleted(DateUtil.getW3CDate());
		curationJob.save();
    }
}