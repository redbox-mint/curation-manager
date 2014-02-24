package au.com.redboxresearchdata.curationmanager.entityservice

import java.util.List;
import java.util.regex.Matcher
import java.util.regex.Pattern

import org.apache.commons.logging.Log;
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
import au.com.redboxresearchdata.curationmanager.utility.MessageResolver;
import org.apache.commons.logging.LogFactory
import org.hibernate.FetchMode


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
			List  newFilters = StringUtil.getFilters(reqIdentifiers)
			Entry entry = findEntryAndRelationships(oid, newFilters);
			if(null == entry){
				List curationsByOid = findCurationsByOid(oid);
				createEntryFromCurations(curationJob, curationsByOid, oid, type, title);
			}
			Map reqIdentifiersNew = new HashMap();
			jobItems.add(reqIdentifiersNew);
			if(null != entry){
				if(oid.equals(entry.getId())){
					curationManagerEV.validateEntryType(oid, entry,  type)
					Map reqIdentifiersNewMap = createCurationJobItemsFromEntry(entry, reqIdentifiersNew, curationJob, oid,
							type, title, jobItems,curationStatusLookup, newFilters, dateW3C);
					if(null != reqIdentifiersNewMap)
						jobItems.add(reqIdentifiersNewMap);
				}
			}else{
				setRequiredIdentifersMap(reqIdentifiersNew, oid, title, type)
				Entry newEntry = createEntryEntity(oid, type, title);
				Map reqIdentifiersNewMap = createCurationEntities(oid, type, title, jobItems, curationJob,
						newEntry, curationStatusLookup,  newFilters,  dateW3C);
				jobItems.add(reqIdentifiersNewMap);
			}
		}
		try{
			if(!jobItems.isEmpty())	{
				curationJob.save(flush:true);
			}
		}catch(org.springframework.dao.OptimisticLockingFailureException oex){
			log.error(oex.getMessage() + oex.getCause());
			curationJob.lock();
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
		Entry entry = Entry.findById(oid.toString());
		if(null != entry){
			curations = entry.getCurations();
		}
		return curations;
	}


	def Map createCurationJobItemsFromEntry(entry, reqIdentifiersNew, curationJob, oid, type, title,
			jobItems, curationStatusLookup, newFilters, dateW3C) throws Exception{
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
				jobItems, curationStatusLookup, existingIdentifiers, dateW3C)
			
			Map reqIdentNew = createCurationEntities(oid, type, title, jobItems, curationJob,
			entry, curationStatusLookup,  newFilters,  dateW3C);
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
			jobItems, curationStatusLookup, previousNewFilters, dateW3C)
		}
		return null;
	}

	def Map createCurationJobItemsFromEntry(entry, curationJob, oid, type, title,
			jobItems, curationStatusLookup, previousNewFilters, dateW3C)throws Exception{
		List curations = entry.getCurations();
		Map requiredIdentifiersNew =  new HashMap();
		List reqIdentifiersNew = new ArrayList();
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
					entry, curationStatusLookup,  previousNewFilters,  dateW3C, false));
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

	def Entry findEntryAndRelationships(oid, newFilters) throws Exception{
		Entry entry = Entry.findById(oid.toString());
		return entry;
	}

	def Map createCurationEntities(oid, type, title, jobItems, curationJob,
			entry, curationStatusLookup,  newFilters,  dateW3C) throws Exception{
		Map requiredIdentifiersNew =  new HashMap();
		List reqIdentifiersNew = new ArrayList();
		for(int i=0; i<newFilters.size(); i++){
			createCurationEntity(entry, newFilters[i],  curationStatusLookup, reqIdentifiersNew,
					newFilters, curationJob, dateW3C)
		}
		requiredIdentifiersNew.put(CurationManagerConstants.REQIDENTIFIERS, reqIdentifiersNew)
		return requiredIdentifiersNew;
	}

	def Curation createCurationEntity(entry, newFilterFiled,  curationStatusLookup, reqIdentifiersNew,
			newFilters, curationJob, dateW3C) throws Exception{
		Map requiredIdentifiers =  new HashMap();
		CurationJobItems curationJobItems = createCurationJobItemsEntity(curationJob);
		Curation curation  = new Curation();
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
		return curation;
	}

	def void copyCurationJobItems(curationJob, curationsOld){
		CurationJobItems curationJobItemsNew = createCurationJobItemsEntity(curationJob);
		curationJobItemsNew.setCuration(curationsOld);
		curationJob.addToCurationJobItems(curationJobItemsNew);
	}


	def CurationJob createCurationJobEntity(requestParams, dateW3C) throws Exception{
		CurationJob curationJob = new CurationJob();
		curationJob.setDateCreated(dateW3C);
		curationJob.setCurationStatusLookup(CurationStatusLookup.findByValue(CurationManagerConstants.IN_PROGRESS));
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

	def void updateCuration(curation, identifier, curationStatus, dateCompleted,   errorMsg) throws Exception{
		curation.setIdentifier(identifier);
		curation.setError(errorMsg);
		curation.setDateCompleted(dateCompleted);
		CurationStatusLookup curationStatusLookup = statusLookup(curationStatus);
		curation.setCurationStatusLookup(curationStatusLookup);
		curation.lock();
		curation.save(flush:true);
	}

	def void updateCurationJob(curationJob, curationStatus) throws Exception{
		curationJob.lock();
		CurationStatusLookup curationStatusLookup = statusLookup(curationStatus);
		curationJob.setCurationStatusLookup(curationStatusLookup);
		curationJob.setDateCompleted(DateUtil.getW3CDate());
		curationJob.lock();
		curationJob.save(flush:true);
	}

	def CurationStatusLookup statusLookup(statusValue) throws Exception{
		return CurationStatusLookup.findByValue(statusValue);
	}

	def CurationManagerResponse retreiveJobByOID(oid) throws CurationManagerEVException{
		CurationManagerResponse  curationManagerResponse;
		Entry.withNewTransaction {
			Entry entry = Entry.findById(oid.toString());
			if(null == entry){
				def msg = MessageResolver.getMessage(CurationManagerConstants.OID_EXISTS);
				log.error(CurationManagerConstants.STATUS_404 + msg);
				throw new CurationManagerEVException(CurationManagerConstants.STATUS_404, msg);
			}
			List jobItems = new ArrayList();
			curationManagerResponse = retreiveJobByEntryAndCurationJob(entry, jobItems, oid)
		}
		return curationManagerResponse;
	}

	def CurationManagerResponse retreiveJobByEntryAndCurationJob(entry, jobItems, previousOid) throws Exception{
		CurationManagerResponse curationManagerResponse = new CurationManagerResponse();
		if (!GrailsHibernateUtil.isInitialized(entry, CurationManagerConstants.CURATIONS) ||  entry.isAttached()){
			entry.attach();
		}
		List<Curation> curations = entry.getCurations();
		List reqIdentifiersNew = new ArrayList();
		Map requiredIdentifiersNew =  new HashMap();
		String oid = entry.getId();
		String title = entry.getTitle();
		EntryTypeLookup entryTypeLookup = entry.getEntryTypeLookup();
		String type = entryTypeLookup.getValue();
		setRequiredIdentifersMap(requiredIdentifiersNew, oid, title, type);
		jobItems.addAll(requiredIdentifiersNew);
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
		Map requiredIdentifiersNew2 =  new HashMap();
		requiredIdentifiersNew2.put(CurationManagerConstants.REQIDENTIFIERS, reqIdentifiersNew);
		jobItems.addAll(requiredIdentifiersNew2);
		setCurationManagerResponse(null, curationManagerResponse, jobItems, null);
		return curationManagerResponse;
	}

	//	def void throwEntityException(ex){
	//		log.error(ex.getMessage() + ex.getCause());
	//		def msg = MessageResolver.getMessage(CurationManagerConstants.UNEXPECTED_ERROR);
	//		throw new CurationManagerEVException(CurationManagerConstants.STATUS_404, msg);
	//  }

	def CurationManagerResponse retreiveJobByEntry(entry, jobItems, curationJob) throws Exception{
		CurationManagerResponse curationManagerResponse = new CurationManagerResponse();
		if(null != curationJob)  {
			if (!GrailsHibernateUtil.isInitialized(entry, CurationManagerConstants.CURATIONS) || entry.isAttached()){
				entry.attach();
			}
			List<Curation> curations = entry.getCurations();
			List reqIdentifiersNew = new ArrayList();
			Map requiredIdentifiersNew =  new HashMap();
			CurationJob curationJobNew;
			String oid = entry.getId();
			String title = entry.getTitle();
			EntryTypeLookup entryTypeLookup = entry.getEntryTypeLookup();
			String type = entryTypeLookup.getValue();
			setRequiredIdentifersMap(requiredIdentifiersNew, oid, title, type);
			jobItems.addAll(requiredIdentifiersNew);
			for(Curation curation : curations){
				if(null != curation.getEntry() && oid.equals(curation.getEntry().getId())) {
					List results = curationJob.getCurationJobItems();
					Map requiredIdentifiers =  new HashMap();
					for(CurationJobItems curationJobItem : results){
						if(curationJobItem.getCuration().getId().equals(curation.getId())){
							Curation curationNew = curationJobItem.getCuration();
							String previousOid = requiredIdentifiersNew.get(CurationManagerConstants.OID);
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
			Map requiredIdentifiersNew2 =  new HashMap();
			requiredIdentifiersNew2.put(CurationManagerConstants.REQIDENTIFIERS, reqIdentifiersNew);
			jobItems.addAll(requiredIdentifiersNew2);
			String curationJobStatus = curationJob.getCurationStatusLookup().getValue();
			setCurationManagerResponse(curationJob, curationManagerResponse, jobItems, curationJobStatus);
		}
		return curationManagerResponse;
	}

	def CurationManagerResponse retreiveJob(jobId) throws CurationManagerEVException{
		CurationManagerResponse curationManagerResponse;

		Entry.withNewTransaction {
			CurationJob curationJob = CurationJob.findById(jobId);
			if(null == curationJob){
				def msg = MessageResolver.getMessage(CurationManagerConstants.JOBID_EXISTS);
				throw new CurationManagerEVException(CurationManagerConstants.STATUS_404, msg);
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
				def msg = MessageResolver.getMessage(CurationManagerConstants.OID_EXISTS);
				throw new CurationManagerEVException(CurationManagerConstants.STATUS_404, msg);
			}
		}
		return curationManagerResponse;
	}
}