package de.ndr.teamcity;

import jetbrains.buildServer.controllers.admin.AdminPage;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.PositionConstraint;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

public class JwtBuildFeatureAdminPage extends AdminPage {
    private static final String PAGE = "jwtBuildFeatureSettings.jsp";
    private static final String TAB_TITLE = "JWT build feature";

    public JwtBuildFeatureAdminPage(@NotNull PagePlaces pagePlaces, @NotNull PluginDescriptor descriptor) {
        super(pagePlaces);
        setPluginName("jwtPlugin");
        setIncludeUrl(descriptor.getPluginResourcesPath(PAGE));
        setTabTitle(TAB_TITLE);
        setPosition(PositionConstraint.after("clouds", "email", "jabber"));
        register();
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
