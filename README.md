# Akka-typed-eventsourcing
----

An implementation of Eventsourcing with Akka-typed, Spray, Slick & Postgres.

## Summary
----

1. A simple Spray REST API to submit commands to Akka-typed's ActorSystem.
2. Implements "Actor per request" pattern using typed actors.
3. Provides base implementations for [AggregateManager](https://github.com/simerplaha/akka-typed-eventsourcing/blob/master/src/main/scala/aggregate/base/AggregateManager.scala) and [Aggregate](https://github.com/simerplaha/akka-typed-eventsourcing/blob/master/src/main/scala/aggregate/base/Aggregate.scala).
4. Uses Akka-typed's Total behavior's lifecycle event 'PreStart' to replay all stored events which restores Actor's state.
5. Implements an example of CQRS's read side using Akka's EventBus (Check [UserListener](https://github.com/simerplaha/akka-typed-eventsourcing/blob/master/src/main/scala/read/UserListener.scala)).
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

#### Command: CreateUser
##### Event - UserCreated

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

#### Command: UpdateName
##### Event - UserNameUpdated

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
    
#### Command: ChangePassword
##### Event - UserPasswordChanged

http://localhost:8080/changePassword?username=John&password=PA$$W0RD

    {
        "username": "John",
        "name": "Jhonny",
        "password": "PA$$W0RD",
        "deleted": false
    }

#### Command: DeleteUser
##### Event - UserDeleted

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
- Test cases
- Stopping Actors after reaching certain threshold of in-memory Actors.
- Snapshotting
- Use Kafka as eventstore instead.
