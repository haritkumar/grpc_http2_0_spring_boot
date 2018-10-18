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
