package com.jondejong.demo.handlers

import groovy.util.logging.Slf4j
import io.netty.handler.codec.http.HttpResponseStatus
import ratpack.error.ServerErrorHandler
import ratpack.handling.Context
import static ratpack.jackson.Jackson.json

@Slf4j
class ErrorHandler implements ServerErrorHandler {
    @Override
    void error(Context context, Throwable throwable) {
        log.error('error processing request', throwable)
        def message = 'some thing bad happened'
        switch (throwable.class) {
            case IllegalAccessException:
                context.response.status(HttpResponseStatus.UNAUTHORIZED.code())
                message = "You're not authorized to do this"
                break

            default:
                context.response.status(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                break
        }

        context.with {
            render json([message: message])
        }

    }
}
