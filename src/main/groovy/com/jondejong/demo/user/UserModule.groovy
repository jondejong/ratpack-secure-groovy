package com.jondejong.demo.user

import com.google.inject.AbstractModule
import com.google.inject.Scopes

/**
 * Created by jondejong on 10/9/15.
 */
class UserModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UserService).in(Scopes.SINGLETON);
        bind(UserRepository).in(Scopes.SINGLETON);
        bind(TokenRepository).in(Scopes.SINGLETON);
    }

}