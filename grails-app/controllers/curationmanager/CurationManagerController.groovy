package curationmanager

import au.com.redboxresearchdata.curationmanager.utility.MessageResolver
import au.com.redboxresearchdata.curationmanager.constants.CurationManagerConstants
import au.com.redboxresearchdata.curationmanager.response.CurationManagerResponse
import au.com.redboxresearchdata.curationmanager.businesservicexception.CurationManagerBSException
import au.com.redboxresearchdata.curationmanager.businessservice.CurationManagerBusinessService
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray


class CurationManagerController{
  
    def job(){
	    if(request.method == 'GET'){
 	      showJob()
	    } else if(request.method == 'POST'){
	      save();
	   }
    }	
	
	def save() {
		try{		
			JSONArray requestParams = (JSONArray)  request.JSON.records;
			CurationManagerBusinessService curationMgrBS = new CurationManagerBusinessService();
			CurationManagerResponse curationManagerResponse = curationMgrBS.curate(requestParams);
			if(null != curationManagerResponse){
				render curationManagerResponse as JSON;
			}
		 }catch(CurationManagerBSException bex){
		   log.error(bex.getValue() + bex.getMessage());
		   render(status:  bex.getKey(), text:  bex.getValue());
		 }catch(Exception ex){
		   log.error(ex.getMessage() + " " + ex.getCause());
		   def msg = MessageResolver.getMessage(CurationManagerConstants.UNEXPECTED_ERROR);
		   render(status: CurationManagerConstants.STATUS_500, text:  msg)
		 }
	 }	 	
	 
	 def showJob(){
		try{
		    CurationManagerBusinessService curationMgrBS = new CurationManagerBusinessService();
		    CurationManagerResponse curationManagerResponse  = curationMgrBS.retrieveJob(params.id);
		    if(null != curationManagerResponse){
			   render curationManagerResponse as JSON;
		    }
		 }catch(CurationManagerBSException bex){
		   log.error(bex.getValue() + bex.getMessage());
		   render(status:  bex.getKey(), text:  bex.getValue());
		 }catch(Exception ex){
		   log.error(ex.getMessage() + " " + ex.getCause());
		   def msg = MessageResolver.getMessage(CurationManagerConstants.CURATIONMANAGER_JOB_LOOKUP_FAILED);
		   render(status: CurationManagerConstants.STATUS_500, text:  msg)
		 }
	 }	 	 
	 
	 def oid(){
		try{
			CurationManagerBusinessService curationMgrBS = new CurationManagerBusinessService();
			CurationManagerResponse curationManagerResponse  = curationMgrBS.retrieveJobByOid(params.id);
			if(null != curationManagerResponse){
			   render curationManagerResponse.getJobItems() as JSON;
			}
		 }catch(CurationManagerBSException bex){
		   log.error(bex.getValue() + bex.getMessage());
		   render(status:  bex.getKey(), text:  bex.getValue());
		 }catch(Exception ex){
		  log.error(ex.getMessage() + " " + ex.getCause());
		   def msg = MessageResolver.getMessage(CurationManagerConstants.CURATIONMANAGER_IDENTITYSERVICE_FAILED);
		   render(status: CurationManagerConstants.STATUS_500, text:  msg)
		 }
	 }
}