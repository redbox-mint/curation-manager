package au.com.redboxresearchdata.curationmanager.domain

class CurationJobItems {

	static constraints = {
	}

	CurationJob curationJob;
	static belongsTo = [curationJob: CurationJob];
	Curation  curation;

	static mapping = {
		curation lazy: false
   }
}