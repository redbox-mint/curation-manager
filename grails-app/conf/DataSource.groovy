dataSource {
	
	pooled = false
	driverClassName = "org.postgresql.Driver"
	dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
	url = "jdbc:postgresql://localhost/curationmanager"
	dialect = "org.hibernate.dialect.PostgreSQLDialect"
	username = "curationmanager"
	password = "curationmanager"
 // Derby database
//	pooled = false
//	driverClassName = "org.apache.derby.jdbc.ClientDriver"
//	dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
//	url = "jdbc:derby://127.0.0.1:1527/CURATIONMANAGER;create=TRUE"
}

hibernate {
	cache.use_second_level_cache = true
	cache.use_query_cache = true
	cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory' // Hibernate 3
//    cache.region.factory_class = 'org.hibernate.cache.ehcache.EhCacheRegionFactory' // Hibernate 4
}

// environment specific settings
environments {
	development {
		dataSource {
			pooled = false
			driverClassName = "org.postgresql.Driver"
			dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
			url = "jdbc:postgresql://localhost/curationmanager"
			dialect = "org.hibernate.dialect.PostgreSQLDialect"
			username = "curationmanager"
			password = "curationmanager"
		// Derby datasource
//		   pooled = false
//		   driverClassName = "org.apache.derby.jdbc.ClientDriver"
//		   dbCreate = "create" // one of 'create', 'create-drop', 'update', 'validate', ''
//		   url = "jdbc:derby://127.0.0.1:1527/CURATIONMANAGER;create=TRUE"
		}
	}
	test {
		dataSource {
		  pooled = false
		  driverClassName = "org.postgresql.Driver"
		  dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
		  url = "jdbc:postgresql://localhost/curationmanager"
		  dialect = "org.hibernate.dialect.PostgreSQLDialect"
		  username = "curationmanager"
		  password = "curationmanager"
		}
	}
	production {
		dataSource {
		  pooled = false
		  driverClassName = "org.postgresql.Driver"
		  dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
		  url = "jdbc:postgresql://localhost/curationmanager"
		  dialect = "org.hibernate.dialect.PostgreSQLDialect"
		  username = "curationmanager"
		  password = "curationmanager"
		}
	}
}
