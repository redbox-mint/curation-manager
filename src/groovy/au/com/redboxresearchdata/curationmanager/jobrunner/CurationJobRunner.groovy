package au.com.redboxresearchdata.curationmanager.jobrunner

import au.com.redboxresearchdata.curationmanager.domain.Curation;

class CurationJobRunner {

	def void executeJob(){		
		def results =  Curation.withCriteria(uniqueResult: true){
			createAlias("curationStatusLookup","curStatslookup")
			eq("curStatslookup.value", "CURATING")
			order("dateCreated", "asc")
		}		
		results.each {
			Curation curations = it;
			println "-----I am here--in the curations---"
			println curations;
		  }
	}
}
