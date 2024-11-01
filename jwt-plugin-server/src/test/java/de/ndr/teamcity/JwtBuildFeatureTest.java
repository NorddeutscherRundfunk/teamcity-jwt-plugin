package de.ndr.teamcity;

import com.nimbusds.jose.jwk.RSAKey;
import jetbrains.buildServer.serverSide.ServerPaths;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtBuildFeatureTest {

    @Mock
    private ServerPaths serverPaths;

    @TempDir
    private File tempDir;

    @Mock
    private RsaKeyGenerator rsaKeyGenerator;

    @Test
    public void testGetter() throws NoSuchAlgorithmException, IOException, ParseException {
        JwtBuildFeature jwtBuildFeature = new JwtBuildFeature(serverPaths);

        assertEquals(jwtBuildFeature.getType(), JwtBuildFeature.PLUGIN_TYPE);
        assertEquals(jwtBuildFeature.getDisplayName(), JwtBuildFeature.DISPLAY_NAME);
        assertEquals(jwtBuildFeature.getEditParametersUrl(), StringUtils.EMPTY);
        assertFalse(jwtBuildFeature.isRequiresAgent());
        assertFalse(jwtBuildFeature.isMultipleFeaturesPerBuildTypeAllowed());
    }

    @Test
    public void createsRsaKeyInKeyFile() throws NoSuchAlgorithmException, IOException, ParseException {
        when(serverPaths.getPluginDataDirectory()).thenReturn(tempDir);
        File keyFile = new File(tempDir + File.separator + "JwtBuildFeature" + File.separator + "key.json");

        RSAKey rsaKey = new JwtBuildFeature(serverPaths).getRsaKey();
        assertNotNull(rsaKey);
        assertTrue(keyFile.exists());
        assertEquals(FileUtils.readFileToString(keyFile, Charset.defaultCharset()), rsaKey.toString());
    }

}
