package com.jondejong.demo.datastore

import com.gmongo.GMongo
import com.google.inject.Singleton

import javax.inject.Inject

@Singleton
class MongoConnection {

    GMongo mongo
    MongoConfig config

    @Inject
    MongoConnection(MongoConfig config) {
        this.config = config
        mongo = new GMongo(config.host, config.port)
    }

    def getDatabase() {
        mongo.getDB(config.database)
    }

}
