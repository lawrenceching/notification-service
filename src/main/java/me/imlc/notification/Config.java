package me.imlc.notification;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.okhttp.OkDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prd")
public class Config {
  private static DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
  private static DockerHttpClient httpClient = new OkDockerHttpClient.Builder()
      .dockerHost(config.getDockerHost())
      .sslConfig(config.getSSLConfig())
      .build();
  private static DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);

  @Bean
  public DockerClient dockerClient() {
    return dockerClient;
  }

  @Bean
  public Authenticator authenticator(@Value("${token}") String token) {
    return new Authenticator(token);
  }
}
