dataSource {
	pooled = false
	driverClassName = "org.apache.derby.jdbc.ClientDriver"
	dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
	url = "jdbc:derby://127.0.0.1:1527/CURATIONMANAGER;create=TRUE"
}
hibernate {
	cache.use_second_level_cache = true
	cache.use_query_cache = false
	cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory' // Hibernate 3
//    cache.region.factory_class = 'org.hibernate.cache.ehcache.EhCacheRegionFactory' // Hibernate 4
}

// environment specific settings
environments {
	development {
		dataSource {
		   pooled = false
		   driverClassName = "org.apache.derby.jdbc.ClientDriver"
		   dbCreate = "create" // one of 'create', 'create-drop', 'update', 'validate', ''
		   url = "jdbc:derby://127.0.0.1:1527/CURATIONMANAGER;create=TRUE"
		}
	}
	test {
		dataSource {
			dbCreate = "update"
			url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE"
		}
	}
	production {
		dataSource {
			dbCreate = "update"
			url = "jdbc:h2:prodDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE"
			properties {
			   maxActive = -1
			   minEvictableIdleTimeMillis=1800000
			   timeBetweenEvictionRunsMillis=1800000
			   numTestsPerEvictionRun=3
			   testOnBorrow=true
			   testWhileIdle=true
			   testOnReturn=false
			   validationQuery="SELECT 1"
			   jdbcInterceptors="ConnectionState"
			}
		}
	}
}
