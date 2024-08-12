package de.ndr.teamcity;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.shaded.gson.*;
import jetbrains.buildServer.controllers.admin.AdminPage;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.PositionConstraint;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class JwtBuildFeatureAdminPage extends AdminPage {
    private static final String PAGE = "jwtBuildFeatureSettings.jsp";
    private static final String TAB_TITLE = "JWT build feature";
    @NotNull
    private final JwtBuildFeature jwtBuildFeature;

    public JwtBuildFeatureAdminPage(@NotNull PagePlaces pagePlaces, @NotNull PluginDescriptor descriptor, @NotNull JwtBuildFeature jwtBuildFeature) {
        super(pagePlaces);
        this.jwtBuildFeature = jwtBuildFeature;
        setPluginName("jwtPlugin");
        setIncludeUrl(descriptor.getPluginResourcesPath(PAGE));
        setTabTitle(TAB_TITLE);
        setPosition(PositionConstraint.after("clouds", "email", "jabber"));
        register();
    }

    @Override
    public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
        super.fillModel(model, request);

        JWKSet jwks = new JWKSet(jwtBuildFeature.getRsaKey().toPublicJWK());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String formattedJwks = gson.toJson(JsonParser.parseString(jwks.toString()).getAsJsonObject());
        model.put("jwks", formattedJwks);
    }

    @Override
    public boolean isAvailable(@NotNull HttpServletRequest request) {
        return super.isAvailable(request) && checkHasGlobalPermission(request, Permission.CHANGE_SERVER_SETTINGS);
    }

    @NotNull
    @Override
    public String getGroup() {
        return INTEGRATIONS_GROUP;
    }
}
