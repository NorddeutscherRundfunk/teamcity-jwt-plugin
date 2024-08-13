package teamcity;

import de.ndr.teamcity.JwtBuildFeature;
import jetbrains.buildServer.serverSide.ServerPaths;
import org.apache.commons.io.FileUtils;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtBuildFeatureTest {

    @Mock
    private ServerPaths serverPaths;

    @TempDir
    private File tempDir;

    @Test
    public void testGetRsaKeyCreatesFile() throws NoSuchAlgorithmException, IOException, ParseException {
        File pluginDirectory = new File(tempDir + File.separator + new File("foobar"));
        pluginDirectory.mkdirs();
        when(serverPaths.getPluginDataDirectory()).thenReturn(pluginDirectory);
        File keyFile = new File(pluginDirectory + File.separator + "JwtBuildFeature" + File.separator + "key.json");
        JwtBuildFeature jwtBuildFeature = new JwtBuildFeature(serverPaths);
        assertTrue(keyFile.exists());
        assertEquals(jwtBuildFeature.getRsaKey().toString(), FileUtils.readFileToString(keyFile, Charset.defaultCharset()));
    }

    @Test
    public void testGetRsaKeyReusesFile() throws NoSuchAlgorithmException, IOException, ParseException {

        File pluginDirectory = new File(tempDir + File.separator + new File("foobar"));
        pluginDirectory.mkdirs();
        when(serverPaths.getPluginDataDirectory()).thenReturn(pluginDirectory);
        File keyFile = new File(pluginDirectory + File.separator + "JwtBuildFeature" + File.separator + "key.json");

        new JwtBuildFeature(serverPaths);
        String keyFileContents = FileUtils.readFileToString(keyFile, Charset.defaultCharset());

        new JwtBuildFeature(serverPaths);
        String keyFileContents2 = FileUtils.readFileToString(keyFile, Charset.defaultCharset());
        assertEquals(keyFileContents, keyFileContents2);
    }

}
