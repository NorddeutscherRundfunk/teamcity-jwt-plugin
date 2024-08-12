<%@ page import="com.nimbusds.jose.jwk.JWKSet" %>
<%@ include file="/include-internal.jsp"%>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="jwtBuildFeature" class="de.ndr.teamcity.JwtBuildFeature"/>

<%

new JWKSet(jwtBuildFeature.getRsaKey().toPublicJWK()).toString();
%>
