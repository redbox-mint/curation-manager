package au.com.redboxresearchdata.curationmanager.identityprovider.domain

class IdentityProviderIncrementor {

    static constraints = {
    }
		
	static mapping = {
		id generator: 'hilo',
		params: [table: 'local_incrementor', column: 'next_value', max_lo: 100]
	}
}
