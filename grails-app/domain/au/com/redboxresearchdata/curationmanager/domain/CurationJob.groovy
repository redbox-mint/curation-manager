package au.com.redboxresearchdata.curationmanager.domain

class CurationJob {

	static constraints = {
		dateCompleted blank: true, nullable: true;
	}

	List curationJobItems;
	Date dateCreated;
	Date dateCompleted
	CurationStatusLookup curationStatusLookup;

	static hasMany = [curationJobItems: CurationJobItems]
	
	static mapping ={
		 id column : 'job_id'
		 curationStatusLookup column : 'status_id'
		 curationJobItems lazy: false
	}
}