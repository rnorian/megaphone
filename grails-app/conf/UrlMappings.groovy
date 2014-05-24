class UrlMappings {

	static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }
/*
        "/contactLists"(resources:'contactList') {
        }
*/
        "/"(view:"/index")
        "500"(view:'/error')
	}
}
