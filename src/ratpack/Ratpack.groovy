import com.jondejong.api.command.CreateCommand

import static ratpack.groovy.Groovy.ratpack
import static ratpack.jackson.Jackson.json

ratpack {
    bindings {
    }

    handlers {
        post('create') {
            parse(CreateCommand).then { createCommand ->
                println "${createCommand}"
                render json([message: 'User created'])
            }
        }
        get {
            render json([message: 'Hit /create to create a new user'])
        }

        files { dir "public" }
    }
}
