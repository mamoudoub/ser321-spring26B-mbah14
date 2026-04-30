# GRPC Services and Registry

The following folder contains a Registry.jar which includes a Registering service where Nodes can register to allow clients to find them and use their implemented GRPC services. 

Some more detailed explanations will follow and please also check the build.gradle file

## Run things locally without registry
To run see also video. To run locally and without Registry which you should do for the beginning

First Terminal

    gradle runNode

Second Terminal

    gradle runClient

## Run things locally with registry

First terminal

    gradle runRegistryServer

Second terminal

    gradle runNode -PregOn=true 

Third Terminal

    gradle runClient -PregOn=true

### gradle runRegistryServer
Will run the Registry node on localhost (arguments are possible see gradle). This node will run and allows nodes to register themselves. 

The Server allows Protobuf, JSON and gRPC. We will only be using gRPC

### gradle runNode
Will run a node with services. The starter code includes Echo and Joke services as examples. You will need to implement and add the Converter and Library services.

For the Library service: A books.txt file is provided with initial book data (format: title|author|isbn, one per line). Your server should load this on first run and create library_data.json for persistence.

The node registers itself on the Registry. You can change the host and port the node runs on and this will register accordingly with the Registry

### gradle runClient
Will run a client which will call the services from the node, it talks to the node directly not through the registry. At the end the client does some calls to the Registry to pull the services, this will be needed later.

### gradle runDiscovery
Will create a couple of threads with each running a node with services in JSON and Protobuf. This is just an example and not needed for assignment 6. 

### gradle testProtobufRegistration
Registers the protobuf nodes from runDiscovery and do some calls. 

### gradle testJSONRegistration
Registers the json nodes from runDiscovery and do some calls. 

### gradle test
Runs the test cases. The starter code includes example tests for Joke and Echo in ServerTest.java. You need to add your own tests for Converter and Library services in the same file.

IMPORTANT: Tests expect the server to be running first!
First run in one terminal:
    gradle runNode
Then in second terminal:
    gradle test

The tests connect to localhost:8000 by default.

To run in IDE:
- go about it like in the ProtoBuf assignment to get rid of errors
- all mains expect input, so if you want to run them in your IDE you need to provide the inputs for them, see build.gradle

The RPS service demonstrates stateful communication, persistent server-side data storage, and multi-request interaction between client and server.

---

## Service: Rock-Paper-Scissors (RPS)

The RPS service allows players to join a match, play rounds against a server-controlled opponent, and track match progress.

### Supported RPC Methods

- `joinMatch(JoinReq) ==> JoinRes`
- `playMove(MoveReq) ==> MoveRes`
- `getStatus(StatusReq) ==> StatusRes`

---

## Features

### Multiple Requests Supported

The service includes three RPC calls:

- Join a match
- Play a move
- Retrieve match status

---

### Input Requirements

Each request requires input:

- `joinMatch` ==> player name
- `playMove` ==> match ID, player name, move (ROCK / PAPER / SCISSORS)
- `getStatus` ==> match ID

---

### Different Responses Per Request

Each RPC returns different structured responses:

- `JoinRes` ==> match ID + confirmation message
- `MoveRes` ==> round result, score updates, and game status
- `StatusRes` ==> current match state and scores

---

### Repeated Field Usage (Requirement Fulfilled)

The server maintains a persistent history of game rounds using:

```proto
repeated string round_summary = 6;

```
## Project Description

This project is a gRPC-based distributed system that implements multiple services including Echo, Joke, 
Converter, Library, and Rock-Paper-Scissors (RPS).  
It demonstrates client-server communication, service discovery, and stateful server-side behavior.


## How to Use the Program

The program works through a client that sends requests to the server node.

Echo Service: send a message string and receive it back  
Joke Service: send a request number and receive jokes  
Converter Service: convert values between units (e.g., kilometers INTO miles)  

## Library Service:
- Load initial books from books.txt  
- Borrow, return, and search books using title or author  

## RPS Service:
- Join a match using a player name  
- Play moves (ROCK, PAPER, SCISSORS)  
- Retrieve match status using match ID  

All services communicate over gRPC using structured request/response messages.

---

## Requirements Fulfilled

The following requirements were implemented:

- Unary RPC services (Echo, Joke, Converter, Library operations)  
- Stateful server-side service (RPS match tracking system)  
- Persistent storage using file I/O (Library service)  
- Service discovery using Registry server  
- Multiple service registration and lookup  
- Bidirectional interaction between client and server   

---

## Screencast

A screencast is included demonstrating:

- Running the Registry server  
- Starting the node with services  
- Running the client  
- Using each service (Echo, Joke, Converter, Library, RPS)   