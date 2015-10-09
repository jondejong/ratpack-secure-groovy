package com.jondeong.security

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Created by jondejong on 10/9/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class User {
    String key
    String firstName
    String lastName
    String email
    String password
    String salt
}
