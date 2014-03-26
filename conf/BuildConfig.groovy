grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.work.dir = "target/work"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.fork = [
	// configure settings for compilation JVM, note that if you alter the Groovy version forked compilation is required
	//  compile: [maxMemory: 256, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
	// configure settings for the test-app JVM, uses the daemon by default
	test: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
	// configure settings for the run-app JVM
	run: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
	// configure settings for the run-war JVM
	war: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
	// configure settings for the Console UI JVM
	console: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256]
]
def activemqVersion = '5.7.0'
grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
	// inherit Grails' default dependencies
    
	inherits("global") {
	}
	
	log "info" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
	checksums true // Whether to verify checksums on resolve
	legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility
	
	repositories {
		inherits true // Whether to inherit repository definitions from plugins
		grailsPlugins()
		grailsHome()
		mavenLocal()
		grailsCentral()
		mavenCentral()
		mavenRepo "http://dev.redboxresearchdata.com.au/nexus/content/repositories/central-snapshots/"
		mavenRepo "http://download.java.net/maven/2/"
		mavenRepo "http://repository.jboss.org/nexus/content/groups/public-jboss/"
	}

	dependencies {
		compile "org.quartz-scheduler:quartz:2.1.5" 
		compile 'org.dspace:handle:6.2.5.02'
		compile 'org.apache.geronimo.specs:geronimo-jms_1.1_spec:1.1.1'
	    compile('org.apache.activemq:activemq-core:5.3.0',
			'org.apache.activemq:activeio-core:3.1.2',
			'org.apache.xbean:xbean-spring:3.7') {
			excludes 'activemq-openwire-generator'
			excludes 'commons-logging'
			excludes 'xalan'
			excludes 'xml-apis'
			excludes 'spring-context'
			exported = false
		}
		compile 'org.apache.commons:commons-io:1.3.2'
		compile 'commons-httpclient:commons-httpclient:3.1'	
		compile 'org.springframework:spring-context:3.2.5.RELEASE'
		compile 'org.springframework:spring-aop:3.2.5.RELEASE'
		compile 'org.springframework:spring-beans:3.2.5.RELEASE'
		compile 'org.springframework:spring-core:3.2.5.RELEASE'
		compile 'org.springframework:spring-tx:3.2.5.RELEASE'
		compile 'org.springframework:spring-jms:3.2.5.RELEASE'
		
		compile 'org.grails:grails-datastore-core:2.0.6.RELEASE'
		compile 'org.grails:grails-datastore-gorm:2.0.6.RELEASE'
		compile 'org.springframework:spring-jdbc:3.2.5.RELEASE'
		compile 'org.springframework:spring-aspects:3.2.5.RELEASE'
		compile 'org.codehaus.groovy:groovy-all:2.1.9'
		compile 'org.springframework:spring-expression:3.2.5.RELEASE'
		compile 'org.slf4j:jcl-over-slf4j:1.7.5'
		compile 'org.slf4j:slf4j-api:1.7.5'
		compile 'org.springframework:spring-web:3.2.5.RELEASE'
		compile 'org.springframework:spring-context-support:3.2.5.RELEASE'
		compile 'org.aspectj:aspectjweaver:1.7.2'
		compile 'asm:asm:3.3.1'
		compile 'org.aspectj:aspectjrt:1.7.2'
		compile 'cglib:cglib:2.2.2'
		compile 'org.grails:grails-datastore-simple:2.0.6.RELEASE'
		compile 'org.springframework:spring-webmvc:3.2.5.RELEASE'	
		runtime 'org.apache.derby:derby:10.8.2.2'
		runtime 'org.apache.derby:derbynet:10.8.2.2'
		runtime 'org.apache.derby:derbyclient:10.8.2.2'		
	}

	plugins {
		compile ":quartz:1.0.1"
		compile ":jms:1.2"
		build ":tomcat:7.0.47"
		compile ":scaffolding:2.0.1"
		compile ':cache:1.1.1'
		runtime ":hibernate:3.6.10.6" 
		runtime ":database-migration:1.3.8"
		runtime ":jquery:1.10.2.2"
		runtime ":resources:1.2.1"
	}
}