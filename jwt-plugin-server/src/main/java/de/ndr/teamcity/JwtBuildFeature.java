package de.ndr.teamcity;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.serverSide.BuildStartContext;
import jetbrains.buildServer.serverSide.BuildStartContextProcessor;
import jetbrains.buildServer.serverSide.SRunningBuild;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;


public class JwtBuildFeature implements BuildStartContextProcessor {
    private final ExtensionHolder extensionHolder;

    public JwtBuildFeature(@NotNull final ExtensionHolder extensionHolder) {
        this.extensionHolder = extensionHolder;
    }

    public void register() {
        extensionHolder.registerExtension(BuildStartContextProcessor.class, this.getClass().getName(), this);
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
            JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                    .subject(build.getBuildTypeExternalId() + "_" + build.getBuildNumber())
                    .audience("example audience")
                    .issuer("https://teamcity.example.com")
                    .issueTime(now.toDate()) // iat
                    .notBeforeTime(now.toDate()) // nbf
                    .expirationTime(now.plusHours(1).toDate()) // exp
                    .claim("branch", ObjectUtils.defaultIfNull(build.getBranch(), ""))
                    .claim("buildTypeExternalId", build.getBuildTypeExternalId())
                    .claim("triggeredById", build.getTriggeredBy().getUser().getId())
                    .claim("triggeredBy", build.getTriggeredBy().getAsString())
                    .claim("buildNumber", build.getBuildNumber())
                    .build();

            SignedJWT signedJWT = new SignedJWT(jwsHeader, jwtClaimsSet);

            signedJWT.sign(signer);
            String jwtToken = signedJWT.serialize();

            buildStartContext.addSharedParameter("env.JWT", rsaKey.toPublicJWK().toString());
            buildStartContext.addSharedParameter("env.JWT_TOKEN", jwtToken);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    private static RSAKey getRsaKey() throws JOSEException {
        return new RSAKeyGenerator(2048)
                .keyID("teamcity")
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .generate();
    }

}
