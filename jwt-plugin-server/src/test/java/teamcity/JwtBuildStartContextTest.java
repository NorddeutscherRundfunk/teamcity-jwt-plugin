package teamcity;

import de.ndr.teamcity.JwtBuildFeature;
import de.ndr.teamcity.JwtBuildStartContext;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.users.SUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtBuildStartContextTest {

    @Mock
    ExtensionHolder extensionHolder;

    @Mock
    SBuildServer buildServer;

    @Mock
    SRunningBuild runningBuild;

    @Mock
    BuildStartContext buildStartContext;

    @Mock
    SBuildFeatureDescriptor jwtBuildFeatureBuildFeatureDescriptor;

    @Mock
    private ServerPaths serverPaths;

    @TempDir
    private File tempDir;

    @Test
    public void testRegister() {
        JwtBuildStartContext jwtBuildStartContext = new JwtBuildStartContext(extensionHolder, buildServer);
        jwtBuildStartContext.register();

        verify(extensionHolder, times(1)).registerExtension(any(), any(), any());
    }

    @Test
    public void doNotUpdateParametersWhenBuildFeatureDisabled() {
        JwtBuildStartContext jwtBuildStartContext = new JwtBuildStartContext(extensionHolder, buildServer);
        when(buildStartContext.getBuild()).thenReturn(runningBuild);
        when(runningBuild.getBuildFeaturesOfType("JWT-Plugin")).thenReturn(Collections.emptyList());
        jwtBuildStartContext.updateParameters(buildStartContext);
        verify(buildStartContext, never()).addSharedParameter(any(), any());
    }

    @Test
    public void updateParametersWhenBuildFeatureEnabled() throws NoSuchAlgorithmException, IOException, ParseException {
        when(serverPaths.getPluginDataDirectory()).thenReturn(tempDir);
        JwtBuildFeature jwtBuildFeature = new JwtBuildFeature(serverPaths);

        when(buildServer.getRootUrl()).thenReturn("http://localhost:8111");
        JwtBuildStartContext jwtBuildStartContext = new JwtBuildStartContext(extensionHolder, buildServer);

        when(buildStartContext.getBuild()).thenReturn(runningBuild);
        when(runningBuild.getBuildFeaturesOfType("JWT-Plugin")).thenReturn(List.of(jwtBuildFeatureBuildFeatureDescriptor));
        when(jwtBuildFeatureBuildFeatureDescriptor.getBuildFeature()).thenReturn(jwtBuildFeature);

        TriggeredBy triggeredByMock = mock(TriggeredBy.class);
        SUser userMock = mock(SUser.class);
        when(runningBuild.getTriggeredBy()).thenReturn(triggeredByMock);
        when(triggeredByMock.getUser()).thenReturn(userMock);

        jwtBuildStartContext.updateParameters(buildStartContext);
        verify(buildStartContext, times(1)).addSharedParameter(eq("env.JWT"), any());
    }

}
