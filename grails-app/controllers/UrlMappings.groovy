class UrlMappings {
	
    static mappings = {
		def grailsApplication = grails.util.Holders.grailsApplication
		def version = grailsApplication.config.info.app.version
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

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
