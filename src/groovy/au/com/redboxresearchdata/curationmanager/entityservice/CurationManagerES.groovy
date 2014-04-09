package au.com.redboxresearchdata.curationmanager.entityservice

import grails.converters.JSON
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher
import java.util.regex.Pattern

import org.apache.commons.logging.Log;
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil;
import org.codehaus.groovy.grails.web.json.JSONObject

import au.com.redboxresearchdata.curationmanager.utility.JsonUtil;
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
import au.com.redboxresearchdata.curationmanager.utility.MessageResolver;

import org.apache.commons.logging.LogFactory
import org.hibernate.FetchMode
import org.hibernate.PessimisticLockException;

class CurationManagerES {

	private static final Log log = LogFactory.getLog(this)

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
			Map  newFiltersMap = JsonUtil.getFilters(reqIdentifiers);
			Set newFiltersUnique = newFiltersMap.keySet();
			List newFilters = new ArrayList(newFiltersUnique);
			Entry entry = findEntryAndRelationships(oid);
			if(null == entry){
				List curationsByOid = findCurationsByOid(oid);
				createEntryFromCurations(curationJob, curationsByOid, oid, type, title);
			}
			Map reqIdentifiersNew = new HashMap();
			if(null != entry){
				if(oid.equals(entry.getId())){
					curationManagerEV.validateEntryType(oid, entry,  type)
					Map reqIdentifiersNewMap = createCurationJobItemsFromEntry(entry, reqIdentifiersNew,
						curationJob, oid,type, title, jobItems,
						curationStatusLookup, reqIdentifiers ,
						newFilters, newFiltersMap ,dateW3C);
					reqIdentifiersNewMap.put(CurationManagerConstants.OID, oid);
					reqIdentifiersNewMap.put(CurationManagerConstants.TITLE, title);
					reqIdentifiersNewMap.put(CurationManagerConstants.TYPE, type);
					jobItems.add(reqIdentifiersNewMap);
				}
			}else{
				Entry newEntry = createEntryEntity(oid, type, title);
				Map reqIdentifiersNewMap = createCurationEntities(oid, type, title, jobItems, curationJob,
						newEntry, curationStatusLookup, reqIdentifiers,  newFilters, newFiltersMap,  dateW3C);
				reqIdentifiersNewMap.put(CurationManagerConstants.OID, oid);
		        reqIdentifiersNewMap.put(CurationManagerConstants.TITLE, title);
		        reqIdentifiersNewMap.put(CurationManagerConstants.TYPE, type);
				jobItems.add(reqIdentifiersNewMap);
			}
		}
		try{
			if(!jobItems.isEmpty())	{
				curationJob.save(flush:true);
			}
		}catch(org.springframework.dao.OptimisticLockingFailureException oex){
			log.error(oex.getMessage());
			log.error(oex.getCause());
			curationJob.save(flush:true);
		}
		if(null !=  curationJob && null != curationJob.getId()){
			setCurationManagerResponse(curationJob, curationManagerResponse, jobItems,
					CurationManagerConstants.IN_PROGRESS)
		}
		return curationManagerResponse;
	}


	def void createEntryFromCurations(curationJob, curationsByOid, oid, type, title) throws Exception{
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

	def List<Curation> findCurationsByOid(oid) throws Exception {
		List<Curation> curations;
		try{	
		Entry entry = Entry.findById(oid.toString());
		if(null != entry){
			curations = entry.getCurations();
		}
	  }catch(Exception ex){
	    log.error(ex.getMessage());
		log.error(ex.getCause());
		throw ex;
	  }
		return curations;
	}


	def Map createCurationJobItemsFromEntry(entry, reqIdentifiersNew, curationJob, oid, type, title,
			jobItems, curationStatusLookup, reqIdentifiers, newFilters, newFiltersMap, dateW3C) throws Exception{
		Map requiredIdentifiersNew =  new HashMap();
		if (!GrailsHibernateUtil.isInitialized(entry, CurationManagerConstants.CURATIONS) || entry.isAttached()){
			entry.attach();
		}
		List curations = entry.getCurations();
		List previousNewFilters = new ArrayList(newFilters.size());
		previousNewFilters = newFilters.clone();
		List existingIdentifiers = new ArrayList();
		for(Curation curation : curations){
			String entryOid = curation.getEntry().getId();
			String identifierType = curation.getIdentifierType();
			existingIdentifiers.add(identifierType);
			if(newFilters.contains(identifierType) && entryOid.equals(oid)){
				newFilters.removeAll(identifierType);
			}
		}
		if(!newFilters.isEmpty())  {
			setRequiredIdentifersMap(reqIdentifiersNew, oid, title, type)
			Map reqIdent = createCurationJobItemsFromEntry(entry, curationJob, oid, type, title,
				jobItems, curationStatusLookup, reqIdentifiers, existingIdentifiers, dateW3C)
			Map reqIdentNew = createCurationEntities(oid, type, title, jobItems, curationJob,
			entry, curationStatusLookup, reqIdentifiers, newFilters, newFiltersMap, dateW3C);
		   if(!reqIdent.isEmpty()){
			  List reqIdentifers = reqIdent.get(CurationManagerConstants.REQIDENTIFIERS);
			  List reqIdentifersNew =  reqIdentNew.get(CurationManagerConstants.REQIDENTIFIERS);
			  reqIdentifersNew.addAll(reqIdentifers);
			  reqIdentNew.put(CurationManagerConstants.REQIDENTIFIERS, reqIdentifersNew)
			  return reqIdentNew;
		   }else {
			 return reqIdentNew;
		   }
		} else if(newFilters.isEmpty()){
			setRequiredIdentifersMap(reqIdentifiersNew, oid, title, type)
			return createCurationJobItemsFromEntry(entry, curationJob, oid, type, title,
			jobItems, curationStatusLookup, reqIdentifiers, previousNewFilters, newFiltersMap, dateW3C)
		}
		return null;
	}

	def Map createCurationJobItemsFromEntry(entry, curationJob, oid, type, title,
			jobItems, curationStatusLookup, reqIdentifiers, previousNewFilters, newFiltersMap, dateW3C)
		throws CurationManagerEVException, Exception{
		Map requiredIdentifiersNew =  new HashMap();
		List reqIdentifiersNew = new ArrayList();
		try {
		 List curations = entry.getCurations();
		 for(Curation curationNew : curations){
			String entryOid = curationNew.getEntry().getId();
			String identifierType = curationNew.getIdentifierType();
			if(previousNewFilters.contains(identifierType) && entryOid.equals(oid)){
				Map requiredIdentifiers =  new HashMap();
				CurationJobItems curationJobItemsNew = createCurationJobItemsEntity(curationJob);
				curationJobItemsNew.setCuration(curationNew);
				curationJobItemsNew.setCurationJob(curationJob);
				curationJob.addToCurationJobItems(curationJobItemsNew);
				previousNewFilters.remove(identifierType)
				requiredIdentifiers.put(CurationManagerConstants.STATUS, curationNew.getCurationStatusLookup().getValue());
				requiredIdentifiers.put(CurationManagerConstants.IDENTIFIER, curationNew.getIdentifier());
				requiredIdentifiers.put(CurationManagerConstants.IDENTIFIER_TYPE, curationNew.getIdentifierType());
				requiredIdentifiers.put(CurationManagerConstants.DATE_CREATED, curationNew.getDateCreated());
				requiredIdentifiers.put(CurationManagerConstants.DATE_COMPLETED, curationNew.getDateCompleted());
				reqIdentifiersNew.add(requiredIdentifiers);
			}
		  }
		  requiredIdentifiersNew.put(CurationManagerConstants.REQIDENTIFIERS, reqIdentifiersNew)
		  if(previousNewFilters.size() > 0 && entry.getId().equals(oid)){
			requiredIdentifiersNew.putAll(createCurationEntities(oid, type, title, jobItems,  curationJob,
					entry, curationStatusLookup, reqIdentifiers,  previousNewFilters, newFiltersMap,  dateW3C));
		  }
		} catch(Exception ex){
		   throwEntityException(ex)
		}
		return requiredIdentifiersNew;
	}

	def void setCurationManagerResponse(curationJob, curationManagerResponse, jobItems, curationJobStatus){
		if(null != curationJob){
			curationManagerResponse.setJobId(curationJob.getId());
			curationManagerResponse.setDateCreated(curationJob.getDateCreated());
			curationManagerResponse.setDateCompleted(curationJob.getDateCompleted());
		}
		if(null != curationJobStatus){
			curationManagerResponse.setJobStatus(curationJobStatus);
		}
		curationManagerResponse.setJobItems(jobItems);
	}

	def void setRequiredIdentifersMap(requiredIdentifiersNew, oid, title, type){
		requiredIdentifiersNew.put(CurationManagerConstants.OID, oid);
		requiredIdentifiersNew.put(CurationManagerConstants.TITLE, title);
		requiredIdentifiersNew.put(CurationManagerConstants.TYPE, type);
	}

	def Entry findEntryAndRelationships(oid) throws CurationManagerEVException, Exception{
		Entry entry
		try {
		   entry = Entry.findById(oid.toString());
		} catch(Exception ex){
		  throwEntityException(ex)
		}
		return entry;
	}

	def Map createCurationEntities(oid, type, title, jobItems, curationJob,
			entry, curationStatusLookup, reqIdentifiers, newFilters,  newFiltersMap, dateW3C)
		throws CurationManagerEVException, Exception{
		Map requiredIdentifiersNew =  new HashMap();
		Map requiredIdentifiersType = new HashMap();
		for(int i=0; i<newFilters.size(); i++){
			String metaData = newFiltersMap.get(newFilters[i]);
			createCurationEntity(entry, newFilters[i], metaData, curationStatusLookup, requiredIdentifiersType,
					newFilters, curationJob, dateW3C)
		}
		requiredIdentifiersNew.put(CurationManagerConstants.REQIDENTIFIERS, requiredIdentifiersType)
		return requiredIdentifiersNew;
	}

	def Curation createCurationEntity(entry, newFilterFiled,  metaData, curationStatusLookup, requiredIdentifiersType,
			newFilters, curationJob, dateW3C) throws Exception{
		Map requiredIdentifiers =  new HashMap();
	    List reqIdentifiersNew = new ArrayList();
		CurationJobItems curationJobItems = createCurationJobItemsEntity(curationJob);
		Curation curation  = new Curation();
		curation.setMetaData(metaData);
		curation.setDateCreated(dateW3C);
		curation.setIdentifierType(newFilterFiled);
		curation.setCurationStatusLookup(curationStatusLookup);
		curationJobItems.setCuration(curation);
		curation.addToCurationJobItems(curationJobItems);
		curationJob.addToCurationJobItems(curationJobItems);
		curation.setEntry(entry);
		entry.addToCurations(curation);
		requiredIdentifiers.put(CurationManagerConstants.STATUS, curationStatusLookup.getValue());
		requiredIdentifiers.put(CurationManagerConstants.IDENTIFIER, "");
		requiredIdentifiers.put(CurationManagerConstants.IDENTIFIER_TYPE, newFilterFiled);
		requiredIdentifiers.put(CurationManagerConstants.DATE_CREATED, dateW3C);
		requiredIdentifiers.put(CurationManagerConstants.DATE_COMPLETED, "");
		reqIdentifiersNew.add(requiredIdentifiers);
		requiredIdentifiersType.put(newFilterFiled, reqIdentifiersNew);
	
		return curation;
	}

	def void copyCurationJobItems(curationJob, curationsOld){
		CurationJobItems curationJobItemsNew = createCurationJobItemsEntity(curationJob);
		curationJobItemsNew.setCuration(curationsOld);
		curationJob.addToCurationJobItems(curationJobItemsNew);
	}


	def CurationJob createCurationJobEntity(requestParams, dateW3C) throws CurationManagerEVException, Exception{
		CurationJob curationJob = new CurationJob();
		curationJob.setDateCreated(dateW3C);
		try {
		  curationJob.setCurationStatusLookup(CurationStatusLookup.findByValue(CurationManagerConstants.IN_PROGRESS));
		} catch(Exception ex){
		  throwEntityException(ex)
		}
		return curationJob;
	}

	def CurationJobItems createCurationJobItemsEntity(curationJob) throws Exception{
		CurationJobItems curationJobItems = new CurationJobItems();
		curationJobItems.setCurationJob(curationJob);
		return curationJobItems;
	}

	def Entry createEntryEntity(oid, type, title) throws Exception{
		Entry entry = new Entry();
		entry.setId(oid);
		EntryTypeLookup entryTypeLookup = EntryTypeLookup.findByValue(type);
		entry.setEntryTypeLookup(entryTypeLookup);
		entry.setTitle(title);
		return entry;
	}

	def void updateCuration(curation, identifier, curationStatus, dateCompleted,   errorMsg)
	  throws CurationManagerEVException{
	 try{
	  Long curationId = curation.id;
	  Curation.withNewTransaction {
		Curation newCuration = Curation.findById(curationId, [lock: true]);
		newCuration.lock(curationId);
		newCuration.setIdentifier(identifier);
		newCuration.setError(errorMsg);
		newCuration.setDateCompleted(dateCompleted);
		CurationStatusLookup curationStatusLookup = statusLookup(curationStatus);
		newCuration.setCurationStatusLookup(curationStatusLookup);
		newCuration.save(flush:true);
	  }
	  }catch(org.springframework.dao.OptimisticLockingFailureException e){
		updateCuration(curation, identifier, curationStatus, dateCompleted, errorMsg);
	  }  catch(PessimisticLockException pex){
		updateCuration(curation, identifier, curationStatus, dateCompleted, errorMsg);
	  }catch(Exception ex){
		updateCuration(curation, identifier, curationStatus, dateCompleted, errorMsg);
	  }
	 
	}

	def void updateCurationJob(curationJob, curationStatus) throws CurationManagerEVException{
	  try{
		  Long curationJobId = curationJob.id;
			//curationJob.lock();
		  CurationJob.withNewTransaction {
			CurationJob newCurationJob= CurationJob.findById(curationJobId, [lock: true]);
			newCurationJob.lock(curationJobId);
			CurationStatusLookup curationStatusLookup = statusLookup(curationStatus);
			newCurationJob.setCurationStatusLookup(curationStatusLookup);
			newCurationJob.setDateCompleted(DateUtil.getW3CDate());
			newCurationJob.save(flush:true);
		  }
		} catch(org.springframework.dao.OptimisticLockingFailureException e){
		  updateCurationJob(curationJob, curationStatus);
		} catch(PessimisticLockException pex){
		  updateCurationJob(curationJob, curationStatus);
		}catch(Exception ex){
		 log.error(ex.getMessage());
		 log.error(ex.getCause());
		 throwEntityException(ex)
		}
	}

	def CurationStatusLookup statusLookup(statusValue) throws Exception{
		return CurationStatusLookup.findByValue(statusValue);
	}

	def CurationManagerResponse retreiveJobByOID(oid) throws CurationManagerEVException, Exception{
		CurationManagerResponse  curationManagerResponse;
		try{
			Entry.withNewTransaction {
			Entry entry = Entry.findById(oid.toString());
			if(null == entry){
				def msg = MessageResolver.getMessage(CurationManagerConstants.OID_EXISTS);
				log.error(CurationManagerConstants.STATUS_404 + msg + " "+ oid);
				throw new CurationManagerEVException(CurationManagerConstants.STATUS_404, msg + " "+ oid);
			  }
			List jobItems = new ArrayList();
			curationManagerResponse = retreiveJobByEntryAndCurationJob(entry, jobItems, oid)
			}
		 } catch(Exception ex){
		   throwEntityException(ex)
	  }
		return curationManagerResponse;
	}

	def CurationManagerResponse retreiveJobByEntryAndCurationJob(entry, jobItems, previousOid)
		throws CurationManagerEVException, Exception{
		CurationManagerResponse curationManagerResponse = new CurationManagerResponse();
		try {
		   if (!GrailsHibernateUtil.isInitialized(entry, CurationManagerConstants.CURATIONS) ||  entry.isAttached()){
			   entry.attach();
		   }
		   List<Curation> curations = entry.getCurations();
		   Map requiredIdentifiersNew =  new HashMap();
		   Map requiredIdentifiersType =  new HashMap();
		   String oid = entry.getId();
		   String title = entry.getTitle();
		   EntryTypeLookup entryTypeLookup = entry.getEntryTypeLookup();
		   String type = entryTypeLookup.getValue();
		   requiredIdentifiersNew.put(CurationManagerConstants.OID, oid);
		   requiredIdentifiersNew.put(CurationManagerConstants.TITLE, title);
		   requiredIdentifiersNew.put(CurationManagerConstants.TYPE, type);
		   List reqIdentifiersNew = new ArrayList();
		   for(Curation curation : curations){
			  if(previousOid.equals(curation.getEntry().getId())) {
				Map requiredIdentifiers =  new HashMap();
				CurationStatusLookup curationStatusLookup = curation.getCurationStatusLookup();
				String curationStatus = curationStatusLookup.getValue();
				requiredIdentifiers.put(CurationManagerConstants.STATUS, curationStatus);
				requiredIdentifiers.put(CurationManagerConstants.IDENTIFIER, curation.getIdentifier());
				requiredIdentifiers.put(CurationManagerConstants.IDENTIFIER_TYPE, curation.getIdentifierType());
				requiredIdentifiers.put(CurationManagerConstants.DATE_CREATED, curation.getDateCreated());
				requiredIdentifiers.put(CurationManagerConstants.DATE_COMPLETED, curation.getDateCompleted());
				reqIdentifiersNew.add(requiredIdentifiers);
			   }
			}
			requiredIdentifiersNew.put(CurationManagerConstants.REQIDENTIFIERS, reqIdentifiersNew);
			jobItems.add(requiredIdentifiersNew);
			setCurationManagerResponse(null, curationManagerResponse, jobItems, null);
		} catch(Exception ex){
		  throwEntityException(ex)
		}
		return curationManagerResponse;
	}

	def void throwEntityException(Exception ex) throws CurationManagerEVException {
		if(ex instanceof CurationManagerEVException){
			throw ex
		}
		log.error(ex.getMessage() + ex.getCause());
		def msg = MessageResolver.getMessage(CurationManagerConstants.UNEXPECTED_ERROR);
		throw new CurationManagerEVException(CurationManagerConstants.STATUS_404, msg);
		
   }

	def CurationManagerResponse retreiveJobByEntry(entry, jobItems, curationJob) throws CurationManagerEVException, Exception{
		CurationManagerResponse curationManagerResponse = new CurationManagerResponse();
		try{
		  if(null != curationJob)  {
			if (!GrailsHibernateUtil.isInitialized(entry, CurationManagerConstants.CURATIONS) || entry.isAttached()){
				entry.attach();
			}
			List<Curation> curations = entry.getCurations();
			Map requiredIdentifiersNew =  new HashMap();
			CurationJob curationJobNew;
			String oid = entry.getId();
			String title = entry.getTitle();
			EntryTypeLookup entryTypeLookup = entry.getEntryTypeLookup();
			String type = entryTypeLookup.getValue();
			requiredIdentifiersNew.put(CurationManagerConstants.OID, oid);
			requiredIdentifiersNew.put(CurationManagerConstants.TITLE, title);
			requiredIdentifiersNew.put(CurationManagerConstants.TYPE, type);
			List reqIdentifiersNew = new ArrayList();
			for(Curation curation : curations){
				if(null != curation.getEntry() && oid.equals(curation.getEntry().getId())) {
					List results = curationJob.getCurationJobItems();
					Map requiredIdentifiers =  new HashMap();
					for(CurationJobItems curationJobItem : results){
						if(curationJobItem.getCuration().getId().equals(curation.getId())){
							Curation curationNew = curationJobItem.getCuration();
							CurationStatusLookup curationStatusLookup = curation.getCurationStatusLookup();
							String curationStatus = curationStatusLookup.getValue();
							requiredIdentifiers.put(CurationManagerConstants.STATUS, curationStatus);
							requiredIdentifiers.put(CurationManagerConstants.IDENTIFIER, curation.getIdentifier());
							requiredIdentifiers.put(CurationManagerConstants.IDENTIFIER_TYPE, curation.getIdentifierType());
							requiredIdentifiers.put(CurationManagerConstants.DATE_CREATED, curation.getDateCreated());
							requiredIdentifiers.put(CurationManagerConstants.DATE_COMPLETED, curation.getDateCompleted());
							reqIdentifiersNew.add(requiredIdentifiers);
						}
					}
				}
			}		
			requiredIdentifiersNew.put(CurationManagerConstants.REQIDENTIFIERS, reqIdentifiersNew);
			jobItems.add(requiredIdentifiersNew);
			String curationJobStatus = curationJob.getCurationStatusLookup().getValue();
			setCurationManagerResponse(curationJob, curationManagerResponse, jobItems, curationJobStatus);
		}
		} catch(Exception ex){
		  throwEntityException(ex)
		}
		return curationManagerResponse;
	}

	def CurationManagerResponse retreiveJob(jobId) throws CurationManagerEVException, Exception{
		CurationManagerResponse curationManagerResponse;

		 Entry.withNewTransaction {
			CurationJob curationJob = CurationJob.findById(jobId, [lock: true]);
			if(null == curationJob){
				def msg = MessageResolver.getMessage(CurationManagerConstants.JOBID_EXISTS);
				throw new CurationManagerEVException(CurationManagerConstants.STATUS_404, msg + "JobId " + jobId);
			}
			def results =  Entry.withCriteria(){
				createAlias("curations", "curation")
				createAlias("curation.curationJobItems", "curationJobItems")
				createAlias("curationJobItems.curationJob", "curationJob")
				eq("curationJob.id", new Long(jobId))
			}
			List jobItems = new ArrayList();
			if(null != results){
				Set resultsNew = new HashSet(results);
				resultsNew.each{
					Entry entry   = it;
					curationManagerResponse = retreiveJobByEntry(entry, jobItems, curationJob)
				}
			} else if(null == results || results.isEmpty()){
				def msg = MessageResolver.getMessage(CurationManagerConstants.OID_EXISTS_FOR_JOB);
				throw new CurationManagerEVException(CurationManagerConstants.STATUS_404, msg);
			}
		}
		return curationManagerResponse;
	}
	
	def void insertCurationWithDependentIdentifier(metaData, oid, depdentIdentifier, dependentIdentifierName, type, title){
		try {
		  Curation.withNewTransaction {
			 Curation curation  = new Curation();
			 curation.setMetaData(metaData);
			 curation.setIdentifier(depdentIdentifier)
			 Date dateW3C = DateUtil.getW3CDate();
			 curation.setDateCreated(dateW3C);
			 curation.setIdentifierType(dependentIdentifierName);
			 if(null != depdentIdentifier){
			   CurationStatusLookup completedCurationStatusLookup = CurationStatusLookup.findByValue(CurationManagerConstants.COMPLETED);
			   curation.setDateCompleted(dateW3C);
			   curation.setCurationStatusLookup(completedCurationStatusLookup);
			 }else{
			   CurationStatusLookup failedCurationStatusLookup = CurationStatusLookup.findByValue(CurationManagerConstants.FAILED);
			   curation.setCurationStatusLookup(failedCurationStatusLookup);
			 }
			 Entry entry = findEntryAndRelationships(oid);
			 entry.addToCurations(curation);
			 curation.setEntry(entry);
			 curation.save();
			}
		 } catch(org.springframework.dao.OptimisticLockingFailureException e){
		  insertCurationWithDependentIdentifier(metaData, oid, depdentIdentifier, dependentIdentifierName, type, title);
		} catch(PessimisticLockException pex){
		  insertCurationWithDependentIdentifier(metaData, oid, depdentIdentifier, dependentIdentifierName, type, title);
		}catch(Exception ex){
		 throwEntityException(ex)
	  }
	}
	
	def void insertCuration(curation, nlaId){
	  if(null!= curation.id) {
		 Long curationId = curation.id;
		 Curation.withNewTransaction  {
			Curation newCuration = Curation.findById(curationId, [lock: true]);
			if(null != nlaId) {
			  newCuration.setIdentifier(nlaId);
			  Date dateW3C = DateUtil.getW3CDate();
			  newCuration.setDateCompleted(dateW3C);
			  CurationStatusLookup completedCurationStatusLookup = CurationStatusLookup.findByValue(CurationManagerConstants.COMPLETED);
			  newCuration.setCurationStatusLookup(completedCurationStatusLookup);
			}
			Set<CurationJobItems> curationJobItems = newCuration.getCurationJobItems();
			newCuration.lock(curationId);
			newCuration.save(flush:true);
		}
	  }
	}
}
