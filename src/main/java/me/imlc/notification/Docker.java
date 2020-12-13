package me.imlc.notification;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
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

  public CompletableFuture pull(String image) {
    String[] words = image.split(":");
    log.info("Pulling image {}:{}", words[0], words[1]);

    CompletableFuture<Void> f = new CompletableFuture<>();
    dockerClient.pullImageCmd(words[0])
        .withRegistry("https://hub.docker.com/")
        .withTag(words[1])
        .exec(new ResultCallback<PullResponseItem>() {

          @Override
          public void close() throws IOException {
            log.debug("close()");
          }

          @Override
          public void onStart(Closeable closeable) {
            log.debug("onStart()");
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
            f.complete(null);
          }
        });
    return f;
  }
}
