import groovy.util.logging.Slf4j

class UrlMappings {
	
    static mappings = {
		def grailsApplication = grails.util.Holders.grailsApplication
		def version = grailsApplication.config.info.app.version
		if(version.indexOf("-SNAPSHOT") != 0) {
			version = version.replaceAll("-SNAPSHOT","")
		}
		//log.info "API accessible on the context /v-"+version
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }
		
		"/v-${version}/jobs/${status}"(controller: "job", action: "index", method: "GET")
		"/v-${version}/job/${id}"(controller: "job", action: "show", method: "GET")
		"/v-${version}/job"(controller: "job", action: "create", method: "POST")
		"/v-${version}/oid/${id}"(controller: "job", action: "showOid", method: "GET")
        "/v-${version}/import"(controller: "import", action: "batchImport", method: "POST")
        "/v-${version}/update"(controller: "import", action: "batchUpdate", method: "POST")

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
