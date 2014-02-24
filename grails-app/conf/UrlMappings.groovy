class UrlMappings {
	
		static mappings = {
			"/$controller/$action?/$id?(.${format})?"{
				constraints {
					// apply constraints here
				}
			}		
			
			"/job/$id"(controller:"CurationManagerController", parseRequest:true){
				action = [GET:"showJob"]
			}
			
			"/job"(controller:"CurationManagerController", parseRequest:true){
				action = [POST:"save"]
			}
			
			"/oid/$id"(controller:"CurationManagerController", parseRequest:true){
				action = [GET:"oid"]
			}
			
			"/"(view:"/index")
			"500"(view:'/error')
		}
	}
	