package com.jondejong.api.command

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Created by jondejong on 10/6/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class CreateCommand {
    String username
    String password

    @Override
    String toString() {
        return "${username}/${password}"
    }
}
