package de.ndr.teamcity;

import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.serverSide.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

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
        when(runningBuild.getBuildFeaturesOfType(JwtBuildFeature.PLUGIN_TYPE)).thenReturn(Collections.emptyList());
        jwtBuildStartContext.updateParameters(buildStartContext);
        verify(buildStartContext, never()).addSharedParameter(any(), any());
    }

}
