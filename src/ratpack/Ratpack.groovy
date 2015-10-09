import com.jondejong.api.command.LoginCommand
import com.jondeong.security.User

import java.security.MessageDigest

import static ratpack.groovy.Groovy.ratpack
import static ratpack.jackson.Jackson.json

def sha256Hash(text) {
    MessageDigest.getInstance("SHA-256")
            .digest(text.getBytes("UTF-8")).encodeBase64().toString()
}

def generatePassword(user, password) {
    sha256Hash("${user.salt}${password}")
}

ratpack {

    def userStore = [:]
    def keyMap = [:]

    bindings {
    }

    handlers {

        post('create') {
            parse(User).then { user ->
                user.key = UUID.randomUUID()
                userStore[user.email] = user
                user.salt = UUID.randomUUID().toString()

                user.password = generatePassword(user, user.password)

                render json([message: 'User created'])
            }
        }

        post('login') {
            parse(LoginCommand).then { command ->
                User user = userStore[command.username]
                if (user.password == generatePassword(user, command.password)) {
                    def key = UUID.randomUUID().toString()
                    keyMap[key] = user
                    render json([auth: key])
                } else {
                    clientError(401)
                }
            }

        }

        prefix('api') {
            all {
                def token = request.headers.get('X-Auth-Token')
                def user = keyMap[token]

                if(!user) {
                    clientError(401)
                } else {
                    next()
                }
            }
            get('users') {
                def users = userStore.values()
                render json(users)
            }
        }
        get {
            render json([message: 'Hit /create to create a new user'])
        }

        files { dir "public" }
    }
}
