package au.com.redboxresearchdata.curationmanager.domain

import java.util.List;

class Entry {

	static constraints = {
	}
	List curations;
	String id;
	String title;
	EntryTypeLookup entryTypeLookup;
	static belongsTo = [curations: Curation];
	static hasMany = [curations: Curation];

	static mapping = {
		//curations cascade: 'all-delete-orphan'
		table 'entry'
		id column: "oid", generator: 'assigned', name: 'id', type: 'string'
		entryTypeLookup column : 'type_id'
		entryTypeLookup lazy: false
		curations batchSize: 30
	}
}
