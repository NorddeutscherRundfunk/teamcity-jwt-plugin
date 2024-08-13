package de.ndr.teamcity;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import jetbrains.buildServer.log.Loggers;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;

public class RsaKeyGenerator {
    private final File keyFile;

    public RsaKeyGenerator(File keyFile) {
        this.keyFile = keyFile;
    }

    public RSAKey generateRSAKey() throws IOException, NoSuchAlgorithmException, ParseException {
        JWK jwk;
        if (keyFile.exists()) {
            Loggers.SERVER.info("Read existing key from: " + keyFile);
            jwk = JWK.parse(FileUtils.readFileToString(keyFile, Charset.defaultCharset()));
        } else {
            Loggers.SERVER.info("Generate new key to: " + keyFile);
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair keyPair = gen.generateKeyPair();

            jwk = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                    .privateKey((RSAPrivateKey) keyPair.getPrivate())
                    .keyUse(KeyUse.SIGNATURE)
                    .keyID("teamcity")
                    .algorithm(JWSAlgorithm.RS256)
                    .build();
            FileUtils.writeStringToFile(keyFile, jwk.toString(), Charset.defaultCharset());
        }
        return jwk.toRSAKey();
    }
}
