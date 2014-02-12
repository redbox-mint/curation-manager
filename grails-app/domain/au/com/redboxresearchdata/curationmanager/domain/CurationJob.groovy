//package curationmanagernew
//
//class CurationJob{
//
//    static constraints = {
//		dateCompleted blank: true, nullable: true;
//    }
//	//List<CurationJobItems> curationJobItems;
//	Date dateCreated;
//	Date dateCompleted
//	CurationStatusLookup curationStatusLookup;
//
//	static hasMany = [curationJobItems: CurationJobItems]
//
//	//static fetchMode = [curationJobItems: 'eager']
//
//	static mapping ={
//		 id column : 'job_id'
//		 curationStatusLookup column : 'status_id'
//		// curationJobItems lazy: false
//		 curationJobItems fetch:'join'
//		// curationJobItems joinTable:[name:'curation_job_items', column:'job_id']
//	}
//}

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
		 //curationJobItems joinTable:[name:'curation_job_items', column:'job_id']
	}
}