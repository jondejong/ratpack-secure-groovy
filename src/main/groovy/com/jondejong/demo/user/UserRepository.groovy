package com.jondejong.demo.user

import com.google.inject.Singleton
import com.jondejong.demo.datastore.MongoConnection
import org.bson.types.ObjectId

import javax.inject.Inject

@Singleton
class UserRepository {

    def database

    @Inject
    UserRepository(MongoConnection mongoConnection) {
        this.database = mongoConnection.database
    }

    def getUsers() {
        database.user.find().collect(this.&documentToUser)
    }

    def getUser(id) {
        def document = database.user.findOne([_id: new ObjectId(id)])
        documentToUser(document)
    }

    def getUserByEmail(email) {
        def document = database.user.findOne([email: email])
        documentToUser(document)
    }

    def saveUser(user) {
        database.user << userToDocument(user)
    }

    protected User documentToUser(user) {
        if(!user) {
            return null
        }

        new User(
                id: user._id,
                firstName: user.firstName,
                lastName: user.lastName,
                email: user.email,
                salt: user.salt,
                password: user.password)
    }

    protected userToDocument(User user) {
        [
                firstName: user.firstName,
                lastName: user.lastName,
                email: user.email,
                password: user.password,
                salt: user.salt
        ]
    }

}