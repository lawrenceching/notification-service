package me.imlc.notification;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

@Component
public class WebHookHandler {

  public Mono<ServerResponse> gitee(ServerRequest request) {

    return request.bodyToMono(String.class)
        .log()
        .map((body) -> {
          return ServerResponse.ok().contentType(MediaType.TEXT_PLAIN)
              .body(BodyInserters.fromValue(""));
        })
        .flatMap(mono -> mono)
        .single();

  }
}
