package au.com.redboxresearchdata.curationmanager.domain

import java.util.Date;

class Curation {

	static constraints = {
		dateCompleted blank: true, nullable: true;
		identifier nullable: true;
		error nullable: true;
	}
	CurationStatusLookup curationStatusLookup;
	static hasMany = [curationJobItems: CurationJobItems]
	Entry entry;
	static belongsTo = [curationJobItems: CurationJobItems];
    static mapping = {
		id column: 'curation_id'
		entry column: 'oid'
		curationStatusLookup column : 'status_id'
		curationStatusLookup lazy: false
		curationJobItems lazy: false
		entry lazy: false
	}
	String error;
	Date dateCreated;
	Date dateCompleted;
	String identifierType;
	String identifier;
}