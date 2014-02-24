package au.com.redboxresearchdata.curationmanager.domain

class CurationStatusLookup {

	static constraints = {
	}
	static hasMany = [curationJobs: CurationJob, curations: Curation]
	static mapping = {
		id column: 'status_id'
		curationJobs joinTable:[name:'curation_job', column:'status_id']
		curations joinTable:[name:'curation', column:'status_id']
	}
	
	String value;
}