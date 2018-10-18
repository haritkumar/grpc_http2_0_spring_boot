![alt text](https://res.cloudinary.com/haritkumar/image/upload/v1539849392/github/grpc.png)

## Understand problem it sloved
Microservices are split out into separate codebases, one important issue with microservices, is communication. In a monolith communication is not an issue, as we call code directly from elsewhere in our codebase. However, microservices don't have that ability, as they live in separate places. So we need a way in which these independent services can talk to one another with as little latency as possible.

Here, we could use traditional REST, such as JSON or XML over http. However, the problem with this approach is that service A has to encode its data into JSON/XML, send a large string over the wire, to service B, which then has to decode this message from JSON, back into code. This has potential overhead problems at scale. Whilst we're forced to adopt this form of communication for web browsers, services can just about talk to each other in any format they wish.

## How gRPC works
 gRPC is a light-weight binary based RPC communication protocol brought out by Google.

![alt text](https://res.cloudinary.com/haritkumar/image/upload/v1539849391/github/grpc_flow.jpg)

 gRPC uses binary as its core data format. In RESTful services, using JSON, we would be sending a string over http. Strings contain bulky metadata about its encoding format; about its length, its content format and various other bits and pieces. This is so that a server can inform a traditionally browser based client what to expect. We don't really need all of this when communicating between two services. So we can use cold hard binary, which is much more light-weight. gRPC uses the new HTTP 2.0 spec, which allows for the use of binary data. It even allows for bi-directional streaming, which is pretty cool! HTTP 2 is pretty fundamental to how gRPC works. 

 ## But how can we do anything with binary data? for this gRPC uses Protobuf
 gRPC has an interchange DSL called protobuf. Protobuf allows us to define an interface to service using a developer friendly format.

gRPC services are defined using protocol buffers. These are language-neutral, platform-neutral, extensible mechanism for serializing structured data.

We specify how we want the information we’re serializing to be structured by defining protocol buffer message types in **.proto** files. Each protocol buffer message is a small logical record of information, containing a series of name-value pairs.

![alt text](https://res.cloudinary.com/haritkumar/image/upload/v1539849881/github/Protobuf.png)


## Implement gRPC in spring boot rest project
### Step 1 create a Spring boot project with web dependancies
### Step 2 Add following to pom.xml
```xml
<grpc-spring-boot-starter.version>2.3.2</grpc-spring-boot-starter.version>
<protobuf-maven-plugin.version>0.5.1</protobuf-maven-plugin.version>
<os-maven-plugin.version>1.6.0</os-maven-plugin.version>

<repositories>
    <repository>
      <id>jcenter</id>
      <url>https://jcenter.bintray.com/</url>
    </repository>
</repositories>

<dependency>
	  <groupId>org.lognet</groupId>
	  <artifactId>grpc-spring-boot-starter</artifactId>
	  <version>${grpc-spring-boot-starter.version}</version>
</dependency>

<build>
		<extensions>
			<!-- os-maven-plugin -->
			<extension>
				<groupId>kr.motd.maven</groupId>
				<artifactId>os-maven-plugin</artifactId>
				<version>${os-maven-plugin.version}</version>
			</extension>
		</extensions>
		<plugins>
			<!-- spring-boot-maven-plugin -->
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
			<!-- protobuf-maven-plugin -->
			<plugin>
				<groupId>org.xolstice.maven.plugins</groupId>
				<artifactId>protobuf-maven-plugin</artifactId>
				<version>${protobuf-maven-plugin.version}</version>
				<configuration>
					<protocArtifact>com.google.protobuf:protoc:3.5.1-1:exe:${os.detected.classifier}</protocArtifact>
					<pluginId>grpc-java</pluginId>
					<pluginArtifact>io.grpc:protoc-gen-grpc-java:1.11.0:exe:${os.detected.classifier}</pluginArtifact>
				</configuration>
				<executions>
					<execution>
						<goals>
							<goal>compile</goal>
							<goal>compile-custom</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>

```

**os-maven-plugin** extension that generates various useful platform-dependent project properties. This information is needed as the Protocol Buffer compiler is native code. In other words, the protobuf-maven-plugin needs to fetch the correct compiler for the platform it is running on.

**protobuf-maven-plugin** will generate Java artifacts for the **HelloWorld.proto** file located in **src/main/proto/** (this is the default location the plugin uses).

Execute following Maven command, and the different message and service classes should be generated under **target/generated-sources/protobuf/**

```sh
mvn compile
```




