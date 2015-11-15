# Akka-typed-eventsourcing
----

An implementation of Eventsourcing with Akka-typed, Spray, Slick & Postgres.

## Summary
----

1. A simple Spray REST API to submit commands to Akka-typed's ActorSystem.
2. Implements "Actor per request" pattern using typed actors.
3. Provides base implementations for AggregateManager and Aggregate.
4. Uses Akka-typed's Total behavior's lifecycle event 'PreStart' to replay all stored events which restores Actor's state.
5. Maps stored JSON events to relevant Case classes.
6. Uses Slick-pg extension.
7. Uses Gson for JSON parsing.

## Running 
---

1. Add your postgres database configuration in application.conf
2. Run Schema.scala's main method to create the database schema
3. Run Boot.scala to start application.

## EVENTS table

PERSISTENT_ID  |   EVENT_JSON  | EVENT_NAME | TAGS | CREATE_TIME
-------------- | ------------- | ---------- | ---- | -----------
String         |    String     |   String   | List[String] | Timestamp


## APIs
---

#### CreateUser
http://localhost:8080/createUser?username=John&name=Smith&password=123

    {
        "username": "John",
        "name": "Smith",
        "password": "123",
        "deleted": false
    }

Running the same URL again returns error message
    
    {
        "message": "Username 'John' with name 'Smith' already exists."
    }

#### UpdateName

http://localhost:8080/updateName?username=John&name=Jhonny

    {
        "username": "John",
        "name": "Jhonny",
        "password": "123",
        "deleted": false
    }
    
Running the same URL again returns error message

    {
        "message": "Name unchanged!",
        "state": {
            "username": "John",
            "name": "Jhonny",
            "password": "123",
            "deleted": false
        }
    }
    
#### ChangePassword

http://localhost:8080/changePassword?username=John&password=PA$$W0RD

    {
        "username": "John",
        "name": "Jhonny",
        "password": "PA$$W0RD",
        "deleted": false
    }

#### DeleteUser

http://localhost:8080/deleteUser?username=John

    {
        "username": "John",
        "name": "Jhonny",
        "password": "PA$$W0RD",
        "deleted": true
    }

Calling CreateUser on a delete users display this message: http://localhost:8080/createUser?username=John&name=Smith&password=123

    {
        "message": "Not a valid request: 'Initialize' for current state: 'deleted'",
        "state": {
            "username": "John",
            "name": "Jhonny",
            "password": "PA$$W0RD",
            "deleted": true
        }
    }
    
### TODOs
- [ ] Test cases
- [ ] Stopping Actors after reaching certain threshold of in-memory Actors.
- [ ] Snapshotting
- [ ] Read/Query side implementation with https://github.com/MfgLabs/akka-stream-extensions
