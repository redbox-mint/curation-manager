dataSource {
	
	pooled = false
	driverClassName = "org.postgresql.Driver"
	dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
	url = "jdbc:postgresql://127.0.0.1:5433/curationmanager"
	//dialect = "org.hibernate.dialect.PostgreSQLDialect"
	username = "postgres"
	password = "123devi"
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
			url = "jdbc:postgresql://127.0.0.1:5433/curationmanager"
			//dialect = "org.hibernate.dialect.PostgreSQLDialect"
			username = "postgres"
			password = "123devi"
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
			url = "jdbc:postgresql://127.0.0.1:5433/curationmanager"
			//dialect = "org.hibernate.dialect.PostgreSQLDialect"
			username = "postgres"
			password = "123devi"
		}
	}
	production {
		dataSource {
			pooled = false
			driverClassName = "org.postgresql.Driver"
			dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
			url = "jdbc:postgresql://127.0.0.1:5433/curationmanager"
			//dialect = "org.hibernate.dialect.PostgreSQLDialect"
			username = "postgres"
			password = "123devi"
		}
	}
}
