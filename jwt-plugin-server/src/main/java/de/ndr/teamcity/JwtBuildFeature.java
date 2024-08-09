package de.ndr.teamcity;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.serverSide.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.List;

public class JwtBuildFeature extends BuildFeature implements BuildStartContextProcessor {
    private final ExtensionHolder extensionHolder;

    private final SBuildServer buildServer;
    private final File keyFile;

    public JwtBuildFeature(@NotNull final ExtensionHolder extensionHolder, @NotNull ServerPaths serverPaths, @NotNull SBuildServer buildServer) {
        this.extensionHolder = extensionHolder;
        this.buildServer = buildServer;

        File directory = new File(serverPaths.getPluginDataDirectory() + File.separator + "JwtBuildFeature");
        directory.mkdirs();
        this.keyFile = new File(directory + File.separator + "key.json");
    }

    public void register() {
        extensionHolder.registerExtension(BuildStartContextProcessor.class, this.getClass().getName(), this);
    }

    @NotNull
    @Override
    public String getType() {
        return "";
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
    public void updateParameters(@NotNull final BuildStartContext buildStartContext) {

        try {
            RSAKey rsaKey = getRsaKey();

            JWSSigner signer = new RSASSASigner(rsaKey);

            SRunningBuild build = buildStartContext.getBuild();

            JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(JOSEObjectType.JWT)
                    .keyID(rsaKey.getKeyID())
                    .build();

            DateTime now = new DateTime();
            // Must start with https:// in order to be accepted for OIDC
            String buildServerRootUrl = buildServer.getRootUrl().replaceFirst("http://", "https://");
            JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                    .subject(build.getBuildTypeExternalId())
                    .audience(List.of(buildServerRootUrl))
                    .issuer(buildServerRootUrl)
                    .issueTime(now.toDate()) // iat
                    .notBeforeTime(now.toDate()) // nbf
                    .expirationTime(now.plusHours(1).toDate()) // exp
                    .claim("branch", ObjectUtils.defaultIfNull(build.getBranch(), ""))
                    .claim("build_type_external_id", build.getBuildTypeExternalId())
                    .claim("triggered_by_id", build.getTriggeredBy().getUser().getId())
                    .claim("triggered_by", build.getTriggeredBy().getAsString())
                    .claim("build_number", build.getBuildNumber())
                    .build();

            SignedJWT signedJWT = new SignedJWT(jwsHeader, jwtClaimsSet);

            signedJWT.sign(signer);
            String jwtToken = signedJWT.serialize();

            buildStartContext.addSharedParameter("env.JWK", rsaKey.toPublicJWK().toString());
            buildStartContext.addSharedParameter("env.JWT", jwtToken);
        } catch (JOSEException | UnrecoverableKeyException | CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private RSAKey getRsaKey() throws JOSEException, CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException, ParseException {
        JWK key;
        if (this.keyFile.exists()) {
            key = JWK.parse(FileUtils.readFileToString(this.keyFile, Charset.defaultCharset()));
        } else {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair keyPair = gen.generateKeyPair();

            key = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                    .privateKey((RSAPrivateKey) keyPair.getPrivate())
                    .keyUse(KeyUse.SIGNATURE)
                    .keyID("teamcity")
                    .algorithm(JWSAlgorithm.RS256)
                    .build();
            FileUtils.writeStringToFile(this.keyFile, key.toString(), Charset.defaultCharset());
        }
        return key.toRSAKey();
    }

}
