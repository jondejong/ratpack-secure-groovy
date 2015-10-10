package com.jondejong.demo

import com.gmongo.GMongo
import com.jondejong.demo.datastore.MongoConfig
import com.jondejong.demo.datastore.MongoConnection
import com.jondejong.demo.user.User
import com.jondejong.demo.user.UserModule
import com.jondejong.demo.user.UserService
import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodProcess
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import ratpack.groovy.test.GroovyRatpackMainApplicationUnderTest
import ratpack.guice.Guice
import ratpack.registry.Registry
import ratpack.test.ApplicationUnderTest
import ratpack.test.http.TestHttpClient
import ratpack.test.remote.RemoteControl
import spock.lang.Shared
import spock.lang.Specification

class ApiFunctionalSpec extends Specification {

    final static MongodStarter starter = MongodStarter.defaultInstance

    @Shared
    ApplicationUnderTest aut = new GroovyRatpackMainApplicationUnderTest() {
        @Override
        protected Registry createOverrides(Registry serverRegistry) throws Exception {
            Guice.registry {
                it.bindInstance ratpack.remote.RemoteControl.handlerDecorator()

                it.bindInstance MongoConfig, new MongoConfig(host: 'localhost', port: 1337, database: 'test')
                it.bind MongoConnection
                it.module UserModule
            }.apply(serverRegistry)
        }
    }

    @Delegate
    TestHttpClient client = aut.httpClient
    RemoteControl remote = new RemoteControl(aut)

    @Shared
    GMongo mongo

    @Shared
    MongodExecutable exe

    @Shared
    MongodProcess process

    def setupSpec() {
        exe = starter
                .prepare(new MongodConfigBuilder()
                    .version(Version.Main.PRODUCTION)
                    .net(new Net(1337, Network.localhostIsIPv6()))
                    .build())

        process = exe.start()

        mongo = new GMongo('localhost', 1337)
    }

    def "Can create a user"() {
        given:
        requestSpec { requestSpec ->
            requestSpec.body { body ->
                body
                    .type('application/json')
                    .text JsonOutput.toJson([
                        firstName: 'Pizza',
                        lastName: 'Party',
                        email: 'pizza@party.edu',
                        password: 'notaburger'
                ])
            }
        }

        expect:
        mongo.getDB('test').user.find().count() == 0

        when:
        post('create')

        then:
        response.body.text == '{"message":"user created"}'

        and:
        mongo.getDB('test').user.find().count() == 1
        def user = mongo.getDB('test').user.findOne()
        user.firstName == 'Pizza'
    }

    def "Can login as user"() {
        given:
        remote.exec {
            get(UserService).createNewUser(new User(
                    firstName: 'Pizza',
                    lastName: 'Party',
                    email: 'pizza@party.edu',
                    password: 'notaburger')).then()
        }

        and:
        requestSpec {
            it.body {
                it.type('application/json')
                .text '{"username": "pizza@party.edu", "password":"notaburger"}'
            }
        }

        when:
        post('login')

        then:
        response.statusCode == 200
        new JsonSlurper().parseText(response.body.text).auth
    }

    def "Can't access the API without valid X-Auth-Token"() {
        when:
        get('api/users')

        then:
        response.statusCode == 401
        response.body.text == 'Nein'

        when:
        requestSpec {
            it.headers {
                it.set 'X-Auth-Token', 'invalid'
            }
        }

        and:
        get('api/users')

        then:
        response.statusCode == 401
        response.body.text == /{"message":"You're not authorized to do this"}/
    }

    def cleanup() {
        mongo.getDB('test').user.remove([:])
    }

    def cleanupSpec() {
        process.stop()
        exe.stop()
    }
}
