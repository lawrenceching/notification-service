package me.imlc.notification;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports.Binding;
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
  private Authenticator authenticator;
  private Docker docker;

  public DeploymentHandler(@Autowired DockerClient dockerClient,
      @Autowired Authenticator authenticator) {
    this.dockerClient = dockerClient;
    this.authenticator = authenticator;
    this.docker = new Docker(this.dockerClient);
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
    List<String> authorization = request.headers().header("TOKEN");
    if(authorization.isEmpty() || !authenticator.authenticate(authorization.get(0))) {
      return response(401, "Unauthenticated");
    }

    Mono<ServerResponse> respMono = request.bodyToMono(String.class)
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

          List<Image> images = dockerClient.listImagesCmd()
              .withImageNameFilter(image)
              .withShowAll(true)
              .exec();

          String repoTag = toRepoTag(image);

          boolean found = images.stream()
              .map(i -> i.getRepoTags())
              .map(i -> i[0])
              .anyMatch(i -> repoTag.equals(i));

          if(!found) {
            docker.pull(image).join();
          }

          logger.info("Creating new container with name \"{}\"", name);
          CreateContainerResponse createContainerResponse = dockerClient.createContainerCmd(image)
              .withName(name)
              .withHostConfig(
                  new HostConfig().withPortBindings(
                      new PortBinding(
                          new Binding("127.0.0.1", "8001"),
                          new ExposedPort(9000)
                      )
                  )
              )
              .exec();

          String id = createContainerResponse.getId();
          dockerClient.startContainerCmd(id).exec();
          logger.info("Started container {} {}", name, id);


          return response(200, "Container created successfully with id " + id);
        })
        .flatMap(mono -> mono)
        .single();

    return respMono;

  }

  private String toRepoTag(String image) {
    String[] words = image.split(":");
    String imageName = words[0];
    String tag = (words.length < 2 || "".equals(words[1]))? "latest" : words[1];
    return imageName + ":"  + tag;
  }
}
