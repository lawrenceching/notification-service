package me.imlc.notification;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.ResponseItem.ProgressDetail;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Docker {

  private DockerClient dockerClient;

  public Docker(DockerClient dockerClient) {
    this.dockerClient = dockerClient;
  }

  private AuthConfig authConfig() {

    String username = System.getenv("DOCKER_USERNAME");
    String password = System.getenv("DOCKER_PASSWORD");
    String registry = System.getenv("DOCKER_REGISTRY");

    AuthConfig authConfig = dockerClient.authConfig()
        .withUsername(username)
        .withPassword(password)
        .withRegistryAddress(registry);

    return authConfig;
  }

  public CompletableFuture pull(String image) {

    CompletableFuture<Void> f = new CompletableFuture<>();
    dockerClient.pullImageCmd(image)
        .withAuthConfig(authConfig())
        .exec(new ResultCallback<PullResponseItem>() {

          @Override
          public void close() throws IOException {
            log.debug("close()");
          }

          @Override
          public void onStart(Closeable closeable) {
            log.info("Pulling image {}", image);
          }

          @Override
          public void onNext(PullResponseItem object) {
            ProgressDetail detail = object.getProgressDetail();
            if(null != detail) {
              log.debug("{} {} {}/{}", object.getId(), object.getStatus(), detail.getCurrent(), detail.getTotal());
            } else {
              log.debug("{} {}", object.getId(), object.getStatus());
            }
          }

          @Override
          public void onError(Throwable throwable) {
            f.completeExceptionally(throwable);
          }

          @Override
          public void onComplete() {
            log.info("Pulled image {}", image);
            f.complete(null);
          }
        });
    return f;
  }
}
