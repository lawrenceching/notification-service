package me.imlc.notification;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.command.VersionCmd;
import com.github.dockerjava.api.model.Version;
import com.github.dockerjava.core.command.VersionCmdImpl;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.TestPropertySource;

@Configuration
@Profile("test")
public class TestConfig {

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
    when(mockListContainersCmd.exec()).thenReturn(Collections.emptyList());

    when(mockDockerClient.listContainersCmd()).thenReturn(mockListContainersCmd);

    return mockDockerClient;

  }

}
