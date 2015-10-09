package com.jondejong.demo.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import org.bson.types.ObjectId

/**
 * Created by jondejong on 10/9/15.
 */
class ObjectIdObjectMapper extends ObjectMapper {

    public ObjectIdObjectMapper() {
        SimpleModule module = new SimpleModule("ObjectIdModule")
        module.addSerializer(ObjectId.class, new ObjectIdSerializer())
        this.registerModule(module)
    }

}