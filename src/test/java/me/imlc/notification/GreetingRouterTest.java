package me.imlc.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import me.imlc.notification.model.DockerDeployment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class GreetingRouterTest {

	@Autowired
	private WebTestClient webTestClient;

	@Test
	public void return400BadRequestWithMissingArgument() {
		webTestClient
				.post()
					.uri("/api/v1/deployment/docker")
				.bodyValue(
						new DockerDeployment(
								"",
								"jdk"
						)
				)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isBadRequest()
				.expectBody(String.class).isEqualTo("Missing argument \"name\"");
	}

	@Test
	public void return404NotFoundIfContainerNotFound() {
		webTestClient
				.post()
					.uri("/api/v1/deployment/docker")
				.bodyValue(
						new DockerDeployment(
								"anyName",
								"jdk"
						)
				)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isNotFound()
				.expectBody(String.class)
				.value(text -> {
					Response resp = new Gson().fromJson(text, Response.class);
					assertThat(resp.getMessage()).isEqualTo("Container \"anyName\" not found");
				});
	}

}
