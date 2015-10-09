package com.jondejong.demo.user

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class User {
    String id
    String firstName
    String lastName
    String email
    String password
    String salt

    @Override
    String toString() {
        "${firstName} ${lastName}: ${email}"
    }
}
