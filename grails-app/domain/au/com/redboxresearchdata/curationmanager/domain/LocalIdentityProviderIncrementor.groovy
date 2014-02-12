package au.com.redboxresearchdata.curationmanager.domain

class LocalIdentityProviderIncrementor {

    static constraints = {
    }
	
	static mapping = {
		id generator: 'hilo',
		params: [table: 'local_incrementor', column: 'next_value', max_lo: 100]
	}
}
