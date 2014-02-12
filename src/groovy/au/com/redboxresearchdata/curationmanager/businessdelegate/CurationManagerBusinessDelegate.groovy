package au.com.redboxresearchdata.curationmanager.businessdelegate

import au.com.redboxresearchdata.curationmanager.identityProviderResult.BaseIdentityResult
import au.com.redboxresearchdata.curationmanager.identityProviderResult.IdentifierResult
import au.com.redboxresearchdata.curationmanager.identityProviderResult.PromiseResult
import au.com.redboxresearchdata.curationmanager.identityProviderService.IdentityProviderService

import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.context.ApplicationContext

import au.com.redboxresearchdata.curationmanager.entityservice.CurationManagerES
import au.com.redboxresearchdata.curationmanager.entityvalidationexception.CurationManagerEVException;
import au.com.redboxresearchdata.curationmanager.utility.ApplicationContextHolder
import au.com.redboxresearchdata.curationmanager.utility.DateUtil
import au.com.redboxresearchdata.curationmanager.constants.CurationManagerConstants
import au.com.redboxresearchdata.curationmanager.domain.Curation
import au.com.redboxresearchdata.curationmanager.domain.CurationJob
import au.com.redboxresearchdata.curationmanager.domain.CurationJobItems
import au.com.redboxresearchdata.curationmanager.domain.CurationStatusLookup

class CurationManagerBusinessDelegate {

	def void doProcess(jobId) throws InterruptedException,  CurationManagerEVException, Exception {
		ApplicationContext ctx = ApplicationContextHolder.getApplicationContext();
		System.out.println("I am sleeping..."
				+ Thread.currentThread().getName());
		Thread.sleep(3000);
		System.out.println("I am done sleeping..."
				+ Thread.currentThread().getName());
		def results =  CurationJob.withCriteria(){
			createAlias("curationJobItems", "curJobItems")
			createAlias("curJobItems.curation", "cur")
			createAlias("cur.curationStatusLookup","curStatslookup")
			eq("curStatslookup.id", new Long(1))
			eq("curationStatusLookup.id", new Long(1))
			maxResults(1)
			order("dateCreated", "asc")
		}
		
		CurationManagerES curationManagerES = new CurationManagerES();
		CurationStatusLookup curationStatusCurating = curationManagerES.statusLookup(CurationManagerConstants.CURATING);
		CurationStatusLookup curationStatusCompleted = curationManagerES.statusLookup(CurationManagerConstants.COMPLETED);
		CurationStatusLookup curationStatusFailed = curationManagerES.statusLookup(CurationManagerConstants.FAILED);
		
	
		results.each{
			List curationJobItems = it.getCurationJobItems();
			updateCurationAndIdentityService(it, curationJobItems, curationManagerES, ctx, curationStatusCurating,
					curationStatusCompleted, curationStatusFailed)
		}
		
		if(null != jobId && (null == results || results.size() == 0)){
			def resultsNew =  CurationJob.withCriteria(){
//				createAlias("curationJobItems", "curJobItems")
//				createAlias("curJobItems.curation", "cur")
//				createAlias("cur.curationStatusLookup","curStatslookup")
//				ne("curationStatusLookup.id", new Long(1))
//				ne("curationStatusLookup.id", new Long(2))
//				ne("curationStatusLookup.id", new Long(4))
			    eq("id", jobId)
				maxResults(1)
			}
			resultsNew.each{
				List curationJobItems = it.getCurationJobItems();
				updateCurationJob(it, curationJobItems, curationManagerES, curationStatusCompleted, curationStatusFailed);
			}
		}
	  }
	
	def void updateCurationJob(curationJob, curationJobItems, curationManagerES, curationStatusCompleted, curationStatusFailed){
		Boolean curationCompleted = Boolean.FALSE;
		Boolean curationFailed = Boolean.FALSE;	
		for(CurationJobItems curationJobItem : curationJobItems){
			Curation curation = curationJobItem.getCuration();
			CurationStatusLookup curationStatusLookup = curation.getCurationStatusLookup();
			if(curationStatusLookup.getId()  == 4){
				curationCompleted = Boolean.TRUE;
			 }else if(curationStatusLookup.getId()  == 3) {
				curationFailed = Boolean.TRUE;
			 }else{
				curationCompleted = Boolean.FALSE;
				curationFailed = Boolean.FALSE;
			 }
	    }
		if(curationCompleted && curationFailed){
			curationManagerES.updateCurationJob(curationJob, curationStatusFailed)		
	   }else if(curationFailed){
			curationManagerES.updateCurationJob(curationJob, curationStatusFailed)
	   }else if(curationStatusCompleted){
	        curationManagerES.updateCurationJob(curationJob, curationStatusCompleted)
	   }
	}
	
	
	def void updateCurationAndIdentityService(curationJob, curationJobItems, curationManagerES, ctx, curationStatusCurating,
			curationStatusCompleted, curationStatusFailed) throws NoSuchBeanDefinitionException{
		Boolean curationCompleted = Boolean.FALSE;
		Boolean curationFailed = Boolean.FALSE;
		for(CurationJobItems curationJobItem : curationJobItems){
			    Curation curation = curationJobItem.getCuration();
				//CurationJob curationJob  = curationJobItem.getCurationJob();
				String identifierType = curation.getIdentifierType();
				CurationStatusLookup curationStatusLookup = curation.getCurationStatusLookup();
				if(curationStatusLookup.getId() != 2 && null != curation && null == curation.getIdentifier()) {
					curationManagerES.updateCuration(curation, null, curationStatusCurating, null, "");
					IdentityProviderService identityProviderService =  ctx.getBean(identifierType);
					try{
						if(null != identityProviderService){
							String oid = curation.getEntry().getId();
							BaseIdentityResult baseIdentifier = identityProviderService.curate(oid, null);
							IdentifierResult identifierResult = (IdentifierResult) baseIdentifier;
							if(null != baseIdentifier && baseIdentifier instanceof IdentifierResult){
								if(null != identifierResult && null != identifierResult.getIdentifier()){
									curationManagerES.updateCuration(curation, identifierResult.getIdentifier(),
											curationStatusCompleted, DateUtil.getW3CDate(), "");
									//curationManagerES.updateCurationJob(curationJob, curationStatusCompleted)
									curationCompleted = Boolean.TRUE;
								}else {
									curationManagerES.updateCuration(curation, null, curationStatusFailed,
											null, CurationManagerConstants.ERROR_MESSAGE_SERVICE_NOT_AVAILABLE)
								    //curationManagerES.updateCurationJob(curationJob, curationStatusFailed)
									curationCompleted = Boolean.FALSE;
								}
							} else if(null != baseIdentifier && baseIdentifier instanceof PromiseResult){
								////////// Do the Asynchronous here.
							}else{
								curationManagerES.updateCuration(curation, null, curationStatusFailed,
										null, CurationManagerConstants.ERROR_MESSAGE_SERVICE_NOT_AVAILABLE)
								//curationManagerES.updateCurationJob(curationJob, curationStatusFailed)
								curationCompleted = Boolean.FALSE;
							}
						}else{
							curationManagerES.updateCuration(curation, null, curationStatusFailed,
									null, CurationManagerConstants.ERROR_MESSAGE_SERVICE_NOT_AVAILABLE)
						   //curationManagerES.updateCurationJob(curationJob, curationStatusFailed)
							curationCompleted = Boolean.FALSE;
						}
					}catch(Exception ex){
						curationManagerES.updateCuration(curation, null, curationStatusFailed,
								null, CurationManagerConstants.ERROR_MESSAGE_CONTACT_SYSTEM_ADMININSTRATOR)
						//curationManagerES.updateCurationJob(curationJob, curationStatusFailed)
						curationCompleted = Boolean.FALSE;
					}
				}
		     }
		
		     if(curationCompleted){
				 curationManagerES.updateCurationJob(curationJob, curationStatusCompleted)
		     }else {
			     curationManagerES.updateCurationJob(curationJob, curationStatusFailed)
		     }
		}
}