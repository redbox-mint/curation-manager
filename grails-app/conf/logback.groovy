import grails.util.BuildSettings
import grails.util.Environment


// See http://logback.qos.ch/manual/groovy.html for details on configuration
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} |- %level %logger  - %msg%n"
    }
}

root(ERROR, ['STDOUT'])

if(Environment.current == Environment.DEVELOPMENT) {
	println "Logback is in Dev environment."
    def targetDir = BuildSettings.TARGET_DIR
    if(targetDir) {
        appender("FULL_STACKTRACE", FileAppender) {

            file = "${targetDir}/stacktrace.log"
            append = true
            encoder(PatternLayoutEncoder) {
                pattern = "%d{HH:mm:ss.SSS} |- %level %logger - %msg%n"
            }
        }
		appender("MAIN_LOG", FileAppender) {

			file = "${targetDir}/main.log"
			append = true
			encoder(PatternLayoutEncoder) {
				pattern = "%d{HH:mm:ss.SSS} |- %level %logger - %msg%n"
			}
		}
        logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false )
		logger "curation.manager.Application", DEBUG, ['STDOUT','MAIN_LOG'], false
		logger "au.com.redboxresearchdata.cm.controller.JobController", DEBUG, ['STDOUT', 'MAIN_LOG'], false
		logger "grails.app.init.BootStrap", DEBUG, ['STDOUT','MAIN_LOG'], false
    }
}
