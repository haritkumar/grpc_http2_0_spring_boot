![alt text](https://res.cloudinary.com/haritkumar/image/upload/v1539849392/github/grpc.png)

## Understand problem it sloved
Microservices are split out into separate codebases, one important issue with microservices, is communication. In a monolith communication is not an issue, as you call code directly from elsewhere in your codebase. However, microservices don't have that ability, as they live in separate places. So you need a way in which these independent services can talk to one another with as little latency as possible.

Here, you could use traditional REST, such as JSON or XML over http. However, the problem with this approach is that service A has to encode its data into JSON/XML, send a large string over the wire, to service B, which then has to decode this message from JSON, back into code. This has potential overhead problems at scale. Whilst you're forced to adopt this form of communication for web browsers, services can just about talk to each other in any format they wish.

## How gRPC works
 gRPC is a light-weight binary based RPC communication protocol brought out by Google.

![alt text](https://res.cloudinary.com/haritkumar/image/upload/v1539849391/github/grpc_flow.jpg)

 gRPC uses binary as its core data format. In RESTful services, using JSON, we would be sending a string over http. Strings contain bulky metadata about its encoding format; about its length, its content format and various other bits and pieces. This is so that a server can inform a traditionally browser based client what to expect. We don't really need all of this when communicating between two services. So we can use cold hard binary, which is much more light-weight. gRPC uses the new HTTP 2.0 spec, which allows for the use of binary data. It even allows for bi-directional streaming, which is pretty cool! HTTP 2 is pretty fundamental to how gRPC works. 

 ## But how can we do anything with binary data? for this gRPC uses Protobuf
 gRPC has an interchange DSL called protobuf. Protobuf allows us to define an interface to service using a developer friendly format.

![alt text](https://res.cloudinary.com/haritkumar/image/upload/v1539849881/github/Protobuf.png)




