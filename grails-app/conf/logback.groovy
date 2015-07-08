import grails.util.BuildSettings
import grails.util.Environment


// See http://logback.qos.ch/manual/groovy.html for details on configuration
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} |- %level %logger  - %msg%n"
    }
}

root(ERROR, ['STDOUT'])
def targetDir = BuildSettings.TARGET_DIR
println "CM is in Environment: ${Environment.current.name}"
if(Environment.current == Environment.DEVELOPMENT) {
	// do nothing :)
} else {
	def baseDir = System.getProperty("catalina.base") ? System.getProperty("catalina.base") : "build" 
	targetDir = "${baseDir}/logs"
}
if(targetDir) {
	println "Using CM Log directory: ${targetDir}"
	appender("FULL_STACKTRACE", FileAppender) {

		file = "${targetDir}/cm_stacktrace.log"
		append = true
		encoder(PatternLayoutEncoder) {
			pattern = "%d{HH:mm:ss.SSS} |- %level %logger - %msg%n"
		}
	}
	appender("MAIN_LOG", FileAppender) {

		file = "${targetDir}/cm_main.log"
		append = true
		encoder(PatternLayoutEncoder) {
			pattern = "%d{HH:mm:ss.SSS} |- %level %logger - %msg%n"
		}
	}
	logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false )
	logger "curation.manager", DEBUG, ['STDOUT','MAIN_LOG'], false
	logger "au.com.redboxresearchdata.cm", DEBUG, ['STDOUT', 'MAIN_LOG'], false
	logger "grails.app.init.BootStrap", DEBUG, ['STDOUT','MAIN_LOG'], false
}
