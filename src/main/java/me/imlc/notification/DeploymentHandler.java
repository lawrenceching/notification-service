package me.imlc.notification;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class DeploymentHandler {

  private static Logger logger = LoggerFactory.getLogger(DeploymentHandler.class);
  private static Gson gson = new GsonBuilder().create();
  private DockerClient dockerClient;

  public DeploymentHandler(@Autowired DockerClient dockerClient) {
    this.dockerClient = dockerClient;
  }

  private Mono<ServerResponse> requireNonBlank(JsonObject json, String ... names) {

    for (String name : names) {
      if( !json.has(name) || StringUtils.isBlank(json.get(name).getAsString()) ) {
        Mono<ServerResponse> resp = ServerResponse.badRequest().body(
            Mono.just("Missing argument \"" + name + "\""),
            String.class
        );
        return resp;
      }
    }

    return null;
  }

  private Mono<ServerResponse> response(int statusCode, String msg) {
    Mono<ServerResponse> resp = ServerResponse.status(statusCode).body(
        Mono.just(new Response(msg).toJson()),
        String.class
    );
    return resp;
  }

  public Mono<ServerResponse> dockerUpgrade(ServerRequest request) {

    Mono<ServerResponse> abc = request.bodyToMono(String.class)
        .map(body -> {
          return gson.fromJson(body, JsonObject.class);
        })
        .map(json -> {

          Mono<ServerResponse> failureResponse = requireNonBlank(json, "name",
              "image");
          if (null != failureResponse) {
            return failureResponse;
          }

          String name = json.get("name").getAsString();
          String image = json.get("image").getAsString();
          List<Container> containers = dockerClient.listContainersCmd()
              .withShowAll(true)
              .withNameFilter(Lists.newArrayList(name)).exec();

          if(containers.size() > 0 ) {
            Container container = containers.get(0);
            String containerId = container.getId();
            String status = container.getStatus();
            logger.info("Found container {} {} {}", name, containerId, status);

            if (status.startsWith("Up ")) {
              logger.info("Stopping container {}", containerId);
              dockerClient.stopContainerCmd(containerId).exec();
            }

            logger.info("Removing container {}", containerId);
            dockerClient.removeContainerCmd(containerId).exec();
          }

          logger.info("Creating new container with name \"{}\"", name);
          CreateContainerResponse createContainerResponse = dockerClient.createContainerCmd(image)
              .withName(name)
              .withExposedPorts(new ExposedPort(8001))
              .withPortSpecs("9000")
              .exec();

          String id = createContainerResponse.getId();
          dockerClient.startContainerCmd(id).exec();
          logger.info("Started container {} {}", name, id);


          return response(200, "Container created successfully with id " + id);
        })
        .flatMap(mono -> mono)
        .single();

    return abc;

  }
}
