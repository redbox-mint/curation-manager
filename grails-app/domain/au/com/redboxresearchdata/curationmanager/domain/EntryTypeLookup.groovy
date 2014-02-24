package au.com.redboxresearchdata.curationmanager.domain

class EntryTypeLookup{
	static constraints = {
	}

	static hasMany = [entries: Entry]
	static mapping = {
		id column: 'type_id'
		entries joinTable:[name:'entry', column:'oid']
		entries lazy: false
	}
	String value;
}