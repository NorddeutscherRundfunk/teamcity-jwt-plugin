package de.ndr.teamcity;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import jetbrains.buildServer.serverSide.*;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;


public class JwtBuildFeature extends BuildFeature {

    private final ServerPaths serverPaths;
    private final RSAKey rsaKey;

    public JwtBuildFeature(@NotNull ServerPaths serverPaths) {
        this.serverPaths = serverPaths;
        try {
            rsaKey = this.generateRSAKey();
        } catch (IOException | NoSuchAlgorithmException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public RSAKey getRsaKey() {
        return rsaKey;
    }

    @NotNull
    @Override
    public String getType() {
        return "JWT-Plugin";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "JWT";
    }

    @Nullable
    @Override
    public String getEditParametersUrl() {
        return "";
    }

    @Override
    public boolean isRequiresAgent() {
        return false;
    }

    @Override
    public boolean isMultipleFeaturesPerBuildTypeAllowed() {
        return false;
    }

    private RSAKey generateRSAKey() throws IOException, NoSuchAlgorithmException, ParseException {
        File directory = new File(serverPaths.getPluginDataDirectory() + File.separator + "JwtBuildFeature");
        directory.mkdirs();
        File keyFile = new File(directory + File.separator + "key.json");
        JWK jwk;
        if (!keyFile.exists()) {
            jwk = JWK.parse(FileUtils.readFileToString(keyFile, Charset.defaultCharset()));
        } else {
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
