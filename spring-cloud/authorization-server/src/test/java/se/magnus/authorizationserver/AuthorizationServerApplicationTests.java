package se.magnus.authorizationserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(properties = {"eureka.client.enabled=false", "spring.cloud.config.enabled=false"})
@AutoConfigureMockMvc
class AuthorizationServerApplicationTests {

	@Autowired
	MockMvc mvc;

	@Test
	public void requestTokenWhenUsingPasswordGrantTypeThenOk() throws Exception{
		this.mvc.perform(
			MockMvcRequestBuilders.post("/oauth/token")
				.param("grant_type", "password")
				.param("username", "magnus")
				.param("password", "password")
				.header("Authorization", "Basic cmVhZGVyOnNlY3JldA==")
		).andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void requestJwtSetWhenUsingDefaultsThenOk() throws Exception{
		this.mvc.perform(
			MockMvcRequestBuilders.get("/.well-known/jwks.json")
		).andExpect(MockMvcResultMatchers.status().isOk());
	}
}
