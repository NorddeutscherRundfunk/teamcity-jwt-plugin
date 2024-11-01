package de.ndr.teamcity;

import com.nimbusds.jose.jwk.RSAKey;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class RsaKeyGeneratorTest {

    @TempDir
    private File tempDir;

    @Test
    public void testGetRsaKeyCreatesFile() throws NoSuchAlgorithmException, IOException, ParseException {
        File keyFile = new File(tempDir + File.separator + "JwtBuildFeature" + File.separator + "key.json");

        RsaKeyGenerator keyGenerator = new RsaKeyGenerator(keyFile);
        RSAKey rsaKey = keyGenerator.generateRSAKey();
        assertTrue(keyFile.exists());
        assertEquals(rsaKey.toString(), FileUtils.readFileToString(keyFile, Charset.defaultCharset()));
    }

    @Test
    public void testGetRsaKeyReusesFile() throws NoSuchAlgorithmException, IOException, ParseException {
        File keyFile = new File(tempDir + File.separator + "JwtBuildFeature" + File.separator + "key.json");

        RsaKeyGenerator keyGenerator = new RsaKeyGenerator(keyFile);

        keyGenerator.generateRSAKey();
        String keyFileContents = FileUtils.readFileToString(keyFile, Charset.defaultCharset());

        keyGenerator.generateRSAKey();
        String keyFileContents2 = FileUtils.readFileToString(keyFile, Charset.defaultCharset());
        assertEquals(keyFileContents, keyFileContents2);
    }

}
