package me.imlc.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerRequest.Headers;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

@Component
@Slf4j
public class WebHookHandler {

  public Mono<ServerResponse> gitee(ServerRequest request) {

    Headers headers = request.headers();
    headers.asHttpHeaders().entrySet().forEach(e -> {
      log.info("{}: {}", e.getKey(), e.getValue());
    });

    return request.bodyToMono(String.class)
        .map((body) -> {
          log.info(body);
          return ServerResponse.ok().contentType(MediaType.TEXT_PLAIN)
              .body(BodyInserters.fromValue(""));
        })
        .flatMap(mono -> mono)
        .single();

  }
}
