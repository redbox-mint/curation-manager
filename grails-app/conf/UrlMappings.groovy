class UrlMappings {
	
		static mappings = {
			"/$controller/$action?/$id?(.${format})?"{
				constraints {
					// apply constraints here
				}
			}
			"/job"(controller: "CurationManagerController", action = [GET:"show", POST:"save"], parseRequest: true) //POST
	
			"/"(view:"/index")
			"500"(view:'/error')
		}
	}
	