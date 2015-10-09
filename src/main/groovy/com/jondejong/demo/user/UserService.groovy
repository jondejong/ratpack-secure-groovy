package com.jondejong.demo.user

import javax.inject.Inject
import java.security.MessageDigest

class UserService {

    UserRepository userRepository
    TokenRepository tokenRepository

    @Inject
    def UserService(UserRepository userRepository, TokenRepository tokenRepository) {
        this.userRepository = userRepository
        this.tokenRepository = tokenRepository
    }

    def list() {
        userRepository.getUsers()
    }

    def createNewUser(User user) {
        user.salt = UUID.randomUUID().toString()
        user.password = generatePassword(user, user.password)
        userRepository.saveUser(user)
    }

    def getUser(id) {
        userRepository.getUser(id)
    }

    def getUserByEmail(email) {
        userRepository.getUserByEmail(email)
    }

    def createToken(User user) {
        def token = tokenRepository.saveToken(user.id)
        token._id
    }

    def getUserByToken(tokenString) {
        def token = tokenRepository.find(tokenString)
        if (!token) {
            throw new IllegalAccessException()
        }
        User user = userRepository.getUser(token.userKey)
        if (!user) {
            throw new IllegalAccessException()
        }
        user
    }

    def generatePassword(user, password) {
        sha256Hash("${user.salt}${password}")
    }

    protected sha256Hash(text) {
        MessageDigest.getInstance("SHA-256")
                .digest(text.getBytes("UTF-8")).encodeBase64().toString()
    }


}
