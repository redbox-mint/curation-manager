package curationmanager

import org.springframework.beans.factory.annotation.Autowired

import au.com.redboxresearchdata.curationmanager.constants.CurationManagerConstants
import au.com.redboxresearchdata.curationmanager.response.CurationManagerResponse
import au.com.redboxresearchdata.curationmanager.businesservicexception.CurationManagerBSException
import au.com.redboxresearchdata.curationmanager.businessservice.CurationManagerBusinessService

import org.codehaus.groovy.grails.web.json.JSONArray

class CurationManagerController{
   
	 def job() {
		try{
			JSONArray requestParams = (JSONArray)  request.JSON.records;
			CurationManagerBusinessService curationMgrBS = new CurationManagerBusinessService();
			CurationManagerResponse curationManagerResponse = curationMgrBS.curate(requestParams);
			if(null != curationManagerResponse){
				render(contentType: "text/json") {
					  curationManagerResponse;
				 }
		    }
		 }catch(CurationManagerBSException bex){
		   render(status:  bex.getKey(), text:  bex.getValue());
		 }catch(Exception bex){
		   render(status: CurationManagerConstants.STATUS_500, text:  CurationManagerConstants.FAILED);
		 }	
	}
}
