package me.imlc.notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.command.VersionCmd;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.Version;
import com.github.dockerjava.core.command.VersionCmdImpl;
import java.util.Collections;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.TestPropertySource;

@Configuration
@Profile("test")
public class TestConfig {

  public static final String TEST_TOKEN = "TEST_TOKEN";

  @Bean
  public DockerClient dockerClient() {

    Version mockVersion = mock(Version.class);
    when(mockVersion.getVersion()).thenReturn("docker-version-in-test");

    VersionCmd mockVersionCmd = mock(VersionCmd.class);
    when(mockVersionCmd.exec()).thenReturn(mockVersion);

    DockerClient mockDockerClient = mock(DockerClient.class);
    when(mockDockerClient.versionCmd()).thenReturn(mockVersionCmd);

    ListContainersCmd mockListContainersCmd = mock(ListContainersCmd.class);
    when(mockListContainersCmd.withNameFilter(anyCollection())).thenReturn(mockListContainersCmd);
    when(mockListContainersCmd.withShowAll(anyBoolean())).thenReturn(mockListContainersCmd);
    when(mockListContainersCmd.exec()).thenReturn(Collections.emptyList());

    when(mockDockerClient.listContainersCmd()).thenReturn(mockListContainersCmd);

    ListImagesCmd mockListImagesCmd = mock(ListImagesCmd.class);
    when(mockListImagesCmd.withImageNameFilter(anyString())).thenReturn(mockListImagesCmd);
    when(mockListImagesCmd.withShowAll(anyBoolean())).thenReturn(mockListImagesCmd);
    when(mockListImagesCmd.exec()).thenReturn(Collections.emptyList());

    when(mockDockerClient.listImagesCmd()).thenReturn(mockListImagesCmd);

    when(mockDockerClient.authConfig()).thenReturn(new AuthConfig());

    when(mockDockerClient.pullImageCmd(anyString())).thenReturn(new MockPullImageCmd());

    CreateContainerResponse mockCreateContainerResponse = mock(CreateContainerResponse.class);
    when(mockCreateContainerResponse.getId()).thenReturn("mock-docker-container-id");

    CreateContainerCmd mockCreateContainerCmd = mock(CreateContainerCmd.class);
    when(mockCreateContainerCmd.withName(anyString())).thenReturn(mockCreateContainerCmd);
    when(mockCreateContainerCmd.withHostConfig(any())).thenReturn(mockCreateContainerCmd);
    when(mockCreateContainerCmd.exec()).thenReturn(mockCreateContainerResponse);

    when(mockDockerClient.createContainerCmd(anyString())).thenReturn(mockCreateContainerCmd);

    StartContainerCmd mockStartContainerCmd = mock(StartContainerCmd.class);
    doNothing().when(mockStartContainerCmd).exec();
    when(mockDockerClient.startContainerCmd(anyString())).thenReturn(mockStartContainerCmd);
    return mockDockerClient;

  }

  @Bean
  public Authenticator authenticator() {
    return new Authenticator(TEST_TOKEN);
  }

  public static class MockPullImageCmd implements PullImageCmd {

    @Override
    public String getRepository() {
      return null;
    }

    @Override
    public String getTag() {
      return null;
    }

    @Override
    public String getPlatform() {
      return null;
    }

    @Override
    public String getRegistry() {
      return null;
    }

    @Override
    public AuthConfig getAuthConfig() {
      return null;
    }

    @Override
    public PullImageCmd withRepository(String repository) {
      return this;
    }

    @Override
    public PullImageCmd withTag(String tag) {
      return this;
    }

    @Override
    public PullImageCmd withPlatform(String tag) {
      return this;
    }

    @Override
    public PullImageCmd withRegistry(String registry) {
      return this;
    }

    @Override
    public PullImageCmd withAuthConfig(AuthConfig authConfig) {
      return this;
    }

    @Override
    public <T extends ResultCallback<PullResponseItem>> T exec(T resultCallback) {
      resultCallback.onComplete();
      return null;
    }

    @Override
    public void close() {

    }
  }
}
