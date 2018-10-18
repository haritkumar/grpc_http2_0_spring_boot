# gRPC
![alt text](https://res.cloudinary.com/haritkumar/image/upload/v1539849392/github/grpc.png)

## Understand problem statement it sloved
Microservices are split out into separate codebases, one important issue with microservices, is communication. In a monolith communication is not an issue, as you call code directly from elsewhere in your codebase. However, microservices don't have that ability, as they live in separate places. So you need a way in which these independent services can talk to one another with as little latency as possible.

Here, you could use traditional REST, such as JSON or XML over http. However, the problem with this approach is that service A has to encode its data into JSON/XML, send a large string over the wire, to service B, which then has to decode this message from JSON, back into code. This has potential overhead problems at scale. Whilst you're forced to adopt this form of communication for web browsers, services can just about talk to each other in any format they wish.