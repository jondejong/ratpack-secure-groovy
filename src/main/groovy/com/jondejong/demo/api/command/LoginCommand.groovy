package com.jondejong.demo.api.command

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Created by jondejong on 10/6/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class LoginCommand {
    String username
    String password

    @Override
    String toString() {
        return "${username}/${password}"
    }
}
