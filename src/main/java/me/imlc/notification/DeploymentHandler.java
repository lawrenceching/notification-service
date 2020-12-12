package me.imlc.notification;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class DeploymentHandler {

  private static Logger logger = LoggerFactory.getLogger(DeploymentHandler.class);
  private static Gson gson = new GsonBuilder().create();


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

  public Mono<ServerResponse> dockerUpgrade(ServerRequest request) {

    Mono<ServerResponse> abc = request.bodyToMono(String.class)
        .map(body -> {
          return gson.fromJson(body, JsonObject.class);
        })
        .map(json -> {

          Mono<ServerResponse> failureResponse = requireNonBlank(json, "type", "containerId",
              "image");
          if (null != failureResponse) {
            return failureResponse;
          }

          String type = json.get("type").getAsString();
          String containerId = json.get("containerId").getAsString();
          String image = json.get("image").getAsString();

          Mono<ServerResponse> resp = ServerResponse.ok().body(
              Mono.just("succeeded"),
              String.class
          );
          return resp;
        })
        .flatMap(mono -> mono)
        .single();

    return abc;

  }
}
