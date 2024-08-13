package de.ndr.teamcity;

import com.nimbusds.jose.jwk.RSAKey;
import jetbrains.buildServer.serverSide.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.security.*;
import java.text.ParseException;


public class JwtBuildFeature extends BuildFeature {

    public static final String PLUGIN_TYPE = "JWT-Plugin";
    public static final String DISPLAY_NAME = "JWT";
    private final RSAKey rsaKey;

    public JwtBuildFeature(@NotNull ServerPaths serverPaths) throws NoSuchAlgorithmException, IOException, ParseException {
        File directory = new File(serverPaths.getPluginDataDirectory() + File.separator + "JwtBuildFeature");
        directory.mkdirs();
        File keyFile = new File(directory + File.separator + "key.json");

        rsaKey = new RsaKeyGenerator(keyFile).generateRSAKey();
    }

    public RSAKey getRsaKey() {
        return rsaKey;
    }

    @NotNull
    @Override
    public String getType() {
        return PLUGIN_TYPE;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
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
}
