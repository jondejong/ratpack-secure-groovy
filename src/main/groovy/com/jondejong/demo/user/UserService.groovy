package com.jondejong.demo.user

import ratpack.exec.Blocking
import ratpack.exec.Operation
import ratpack.exec.Promise

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

    Promise<List<User>> list() {
        Blocking.get {
            userRepository.users
        }
    }

    Operation createNewUser(User user) {
        user.salt = UUID.randomUUID().toString()
        user.password = generatePassword(user, user.password)
        Blocking.op {
            userRepository.saveUser(user)
        }
    }

    Promise<User> getUser(id) {
        Blocking.get {
            userRepository.getUser(id)
        }
    }

    Promise<User> getUserByEmail(email) {
        Blocking.get {
            userRepository.getUserByEmail(email)
        }
    }

    Promise createToken(User user) {
        Blocking.get {
            tokenRepository.saveToken(user.id)
        }.map { token ->
            token._id
        }
    }

    Promise<User> getUserByToken(tokenString) {
        Blocking.get {
            tokenRepository.find(tokenString)
        }.map { token ->
            if (!token) {
                throw new IllegalAccessException()
            }
            def user = userRepository.getUser(token.userKey)
            if (!user) {
                throw new IllegalAccessException()
            }
            user
        }
    }

    def generatePassword(user, password) {
        sha256Hash("${user.salt}${password}")
    }

    protected sha256Hash(text) {
        MessageDigest.getInstance("SHA-256")
                .digest(text.getBytes("UTF-8")).encodeBase64().toString()
    }


}
