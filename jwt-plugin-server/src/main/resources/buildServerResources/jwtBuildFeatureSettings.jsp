<%@ page import="com.nimbusds.jose.util.Base64" %>
<%@ include file="/include-internal.jsp"%>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

JWKS:
<pre>
<c:out value="${jwks}" />
</pre>
<a href="data:application/json;charset=utf-8;base64,${Base64.encode(jwks)}" download="jwks.json">download</a>
