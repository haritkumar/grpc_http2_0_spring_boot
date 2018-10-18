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
		<pluginManagement>
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
		</pluginManagement>
	</build>

```

**os-maven-plugin** extension that generates various useful platform-dependent project properties. This information is needed as the Protocol Buffer compiler is native code. In other words, the protobuf-maven-plugin needs to fetch the correct compiler for the platform it is running on.

**protobuf-maven-plugin** will generate Java artifacts for the **HelloWorld.proto** file located in **src/main/proto/** (this is the default location the plugin uses).

```java
syntax = "proto3";

option java_multiple_files = true;
package com.grpc.boot.pojo;

message Person {
  string first_name = 1;
  string last_name = 2;
}

message Greeting {
  string message = 1;
}

service HelloWorldService {
  rpc sayHello (Person) returns (Greeting);
}
```

Execute following Maven command, and the different message and service classes should be generated under **target/generated-sources/protobuf/**

```sh
mvn compile
```

![alt text](https://res.cloudinary.com/haritkumar/image/upload/v1539852339/github/proto.png)

### Step 3 Copy all generated classes to **com.grpc.boot.pojo**

### Step 4 Add server and client
- Server **HelloWorldServiceImpl**
  HelloWorldServiceImpl POJO that implements the HelloWorldServiceImplBase class that was generated from the **HelloWorld.proto** file. We override the sayHello() method and generate a Greeting response based on the first and last name of the Person passed in the request.

  response is a StreamObserver object. In other words, the service is by default asynchronous and whether you want to block or not when receiving the response(s) is the decision of the client as we will see further below.

  We use the response observer’s onNext() method to return the Greeting and then call the response observer’s onCompleted() method to tell gRPC that we’ve finished writing responses

  The HelloWorldServiceImpl POJO is annotated with @GRpcService which auto-configures the specified gRPC service to be exposed on port **6565**

- Client **HelloWorldClient**
  To call gRPC service methods, we first need to create a stub. There are two types of stubs available: a blocking/synchronous stub that will wait for the server to respond and a non-blocking/asynchronous stub that makes non-blocking calls to the server, where the response is returned asynchronously.

  In order to transport messages, gRPC uses http/2 and some abstraction layers in between. This complexity is hidden behind a MessageChannel that handles the connectivity. The general recommendation is to use one channel per application and share it among service stubs.

  We use an init() method annotated with @PostConstruct in order to build a new MessageChannel right after the after the bean has been initialized. The channel is then used to create the helloWorldServiceBlockingStub stub.

  gRPC by default uses a secure connection mechanism such as TLS. As this is a simple development test will use usePlaintext() in order to avoid having to setup the different security artifacts such as key/trust stores.

  The sayHello() method creates a Person object using the Builder pattern on which we set the 'firstname' and 'lastname' input parameters.

  The helloWorldServiceBlockingStub is then used to send the request towards the Hello World gRPC service. The result is a Greeting object from which we return the containing message.


### Step 5 Test Application
- Create a test class
```java
package com.grpc.boot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import static org.assertj.core.api.Assertions.assertThat;
import com.grpc.boot.client.HelloWorldClient;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GrpcApplicationTests {

	@Autowired
	  private HelloWorldClient helloWorldClient;

	  @Test
	  public void testSayHello() {
	    assertThat(helloWorldClient.sayHello("Harit", "Kumar"))
	        .isEqualTo("Hello Harit Kumar!");
	  }

}

```

- Run test
  ```bash
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.0.6.RELEASE)

2018-10-18 14:32:45.092  INFO 37139 --- [           main] com.grpc.boot.GrpcApplicationTests       : Starting GrpcApplicationTests on Harits-MacBook-Air.local with PID 37139 (started by haritkumar in /Users/haritkumar/Documents/zed_i/grpc_http2_0)
2018-10-18 14:32:45.096  INFO 37139 --- [           main] com.grpc.boot.GrpcApplicationTests       : No active profile set, falling back to default profiles: default
2018-10-18 14:32:45.191  INFO 37139 --- [           main] o.s.w.c.s.GenericWebApplicationContext   : Refreshing org.springframework.web.context.support.GenericWebApplicationContext@543295b0: startup date [Thu Oct 18 14:32:45 IST 2018]; root of context hierarchy
2018-10-18 14:32:47.988  INFO 37139 --- [           main] o.s.w.s.handler.SimpleUrlHandlerMapping  : Mapped URL path [/**/favicon.ico] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
2018-10-18 14:32:48.359  INFO 37139 --- [           main] s.w.s.m.m.a.RequestMappingHandlerAdapter : Looking for @ControllerAdvice: org.springframework.web.context.support.GenericWebApplicationContext@543295b0: startup date [Thu Oct 18 14:32:45 IST 2018]; root of context hierarchy
2018-10-18 14:32:48.516  INFO 37139 --- [           main] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped "{[/error]}" onto public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController.error(javax.servlet.http.HttpServletRequest)
2018-10-18 14:32:48.520  INFO 37139 --- [           main] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped "{[/error],produces=[text/html]}" onto public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)
2018-10-18 14:32:48.588  INFO 37139 --- [           main] o.s.w.s.handler.SimpleUrlHandlerMapping  : Mapped URL path [/webjars/**] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
2018-10-18 14:32:48.590  INFO 37139 --- [           main] o.s.w.s.handler.SimpleUrlHandlerMapping  : Mapped URL path [/**] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
2018-10-18 14:32:49.134  INFO 37139 --- [           main] com.grpc.boot.GrpcApplicationTests       : Started GrpcApplicationTests in 4.838 seconds (JVM running for 6.77)
2018-10-18 14:32:49.140  INFO 37139 --- [           main] o.l.springboot.grpc.GRpcServerRunner     : Starting gRPC Server ...
2018-10-18 14:32:49.198  INFO 37139 --- [           main] o.l.springboot.grpc.GRpcServerRunner     : 'com.grpc.boot.server.HelloWorldServiceImpl' service has been registered.
2018-10-18 14:32:49.399  INFO 37139 --- [           main] o.l.springboot.grpc.GRpcServerRunner     : gRPC Server started, listening on port 6565.
2018-10-18 14:32:49.726  INFO 37139 --- [           main] com.grpc.boot.client.HelloWorldClient    : client sending first_name: "Harit"
last_name: "Kumar"

2018-10-18 14:32:50.266  INFO 37139 --- [ault-executor-0] c.g.boot.server.HelloWorldServiceImpl    : server received first_name: "Harit"
last_name: "Kumar"

2018-10-18 14:32:50.273  INFO 37139 --- [ault-executor-0] c.g.boot.server.HelloWorldServiceImpl    : server responded message: "Hello Harit Kumar!"

2018-10-18 14:32:50.290  INFO 37139 --- [           main] com.grpc.boot.client.HelloWorldClient    : client received message: "Hello Harit Kumar!"

[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 7.432 s - in com.grpc.boot.GrpcApplicationTests
2018-10-18 14:32:50.441  INFO 37139 --- [       Thread-3] o.s.w.c.s.GenericWebApplicationContext   : Closing org.springframework.web.context.support.GenericWebApplicationContext@543295b0: startup date [Thu Oct 18 14:32:45 IST 2018]; root of context hierarchy
2018-10-18 14:32:50.443  INFO 37139 --- [       Thread-3] o.l.springboot.grpc.GRpcServerRunner     : Shutting down gRPC server ...
2018-10-18 14:32:50.446  INFO 37139 --- [       Thread-3] o.l.springboot.grpc.GRpcServerRunner     : gRPC server stopped.
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 15.480 s
[INFO] Finished at: 2018-10-18T14:32:50+05:30
[INFO] Final Memory: 32M/262M
[INFO] ------------------------------------------------------------------------
  ```





