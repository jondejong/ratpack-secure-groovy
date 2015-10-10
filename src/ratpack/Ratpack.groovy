import com.fasterxml.jackson.databind.ObjectMapper
import com.jondejong.demo.api.command.LoginCommand
import com.jondejong.demo.datastore.MongoConfig
import com.jondejong.demo.datastore.MongoConnection
import com.jondejong.demo.handlers.ErrorHandler
import com.jondejong.demo.jackson.ObjectIdObjectMapper
import com.jondejong.demo.user.User
import com.jondejong.demo.user.UserModule
import com.jondejong.demo.user.UserService
import ratpack.config.ConfigData
import ratpack.error.ServerErrorHandler
import ratpack.func.Pair

import static ratpack.groovy.Groovy.ratpack
import static ratpack.jackson.Jackson.json

ratpack {

    bindings {
        ConfigData configData = ConfigData.of { c ->
            c.props("$serverConfig.baseDir.file/application.properties")
            c.env()
            c.sysProps()
        }

        bindInstance(ServerErrorHandler, new ErrorHandler())

        bindInstance(MongoConfig, configData.get("/mongo", MongoConfig))
        bind MongoConnection

        module UserModule
        add(ObjectMapper, new ObjectIdObjectMapper())
    }

    handlers {
        all {
            response.headers.add 'Access-Control-Allow-Origin', '*'
            response.headers.add 'Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE'
            response.headers.add 'Access-Control-Allow-Headers', 'X-Auth-Token, Content-Type,X-Requested-With'
            next()
        }

        post('create') { UserService userService ->
            parse(User)
                .flatMap { user ->
                    userService.createNewUser(user).promise()
                }
                .map { json([message: 'user created']) }
                .then(context.&render)
        }

        post('login') { UserService userService ->
            parse(LoginCommand)
                .flatMap { command ->
                    userService
                        .getUserByEmail(command.username)
                        .route(
                            { user -> !user },
                            { user ->
                                response.status(401)
                                render 'No bueno'
                            }
                        )
                        .map { user ->
                            Pair.of(user, command.password)
                        }
                }.map { pair ->
                    Pair.of(pair.left, userService.generatePassword(pair.left, pair.right))
                }.route(
                    { pair -> pair.left.password != pair.right },
                    { pair ->
                        response.status(401)
                        render 'No bueno'
                    }
                ).map { pair -> pair.right }
                .map { key -> json([auth: key]) }
                .then(context.&render)
        }

        prefix('api') {
            when ({ !request.headers.get('X-Auth-Token') }, {
                all {
                    response.status(401)
                    render 'Nein'
                }
            })

            all { UserService userService ->
                def tokenString = request.headers.get('X-Auth-Token')

                userService
                    .getUserByToken(tokenString)
                    .route({ user -> !user }, {
                        response.status(401)
                        render 'Nein'
                    })
                    .operation()
                    .then(context.&next)
            }

            get('users') { UserService userService ->
                userService
                    .list()
                    .map { users -> json(users) }
                    .then(context.&render)
            }
        }

    }
}
