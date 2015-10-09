## Securing a REST API in Ratpack with Groovy


### This is a simple implementation of token based security for rest services with Ratpack in Groovy

#### Mongo
Users and tokens are backed by MongoDB. Set your MongoDB connection information in application.properties

#### To Use

To run this, just use the Gradle wrapper:

    ./gradlew run

Using the REST client of your choice (I use Postman), create a user for yourself by POSTing to:

    http://localhost:5050/create

With a payload similar to:

    {
      "firstName": "Jon",
      "lastName": "DeJong",
      "email": "email@provider.com",
      "password": "Password1"
    }

Then, you can login by POSTing to:

    http://localhost:5050/login

With a payload similar to:

    {
      "username": "email@provider.com",
      "password": "Password1"
    }

You should get back a response that looks something like this:

    {
      "auth": "56182d6577c864e068c0d4f2"
    }

That is your new token. In your next request set a header with the name "X-Auth-Token" to this value. Try to hit the secured API by calling get on:

    http://localhost:5050/api/users

You should see a response similar to:

    [
      {
          "id": "56182d6277c864e068c0d4f1",
          "firstName": "Jon",
          "lastName": "DeJong",
          "email": "email@provider.com",
          "password": "VUvwJV/c+GV4a7ssGASvcUeD4OvyAUuNJKyXVp1W+MM=",
          "salt": "1fea0666-10b5-4294-af10-44549b5269d8"
      }
    ]
