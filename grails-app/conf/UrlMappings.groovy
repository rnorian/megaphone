import com.norian.megaphone.domain.ShoutController

class UrlMappings {

	static mappings = {
        "/shouts"(resources: 'shout') {

        }

        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(view:"/index")
        "500"(view:'/error')
	}
}
