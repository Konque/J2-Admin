<%--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c_rt"%>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>

<%@page import="org.apache.jetspeed.openid.OpenIDConstants"%>
<%@page import="org.apache.jetspeed.portlets.openid.OpenIDLoginPortlet"%>

<fmt:setBundle basename="org.apache.jetspeed.portlets.security.resources.OpenIDLoginResources"/>

<portlet:defineObjects/>
<c_rt:set var="openIDReturn" value="<%=OpenIDConstants.OPEN_ID_RETURN%>"/>
<c_rt:set var="openIDDiscovery" value="<%=OpenIDConstants.OPEN_ID_DISCOVERY%>"/>
<c_rt:set var="openIDProvider" value="<%=OpenIDConstants.OPEN_ID_PROVIDER%>"/>
<c_rt:set var="openIDRequest" value="<%=OpenIDConstants.OPEN_ID_REQUEST%>"/>
<c_rt:set var="openIDLoginRequest" value="<%=OpenIDConstants.OPEN_ID_LOGIN_REQUEST%>"/>
<c_rt:set var="openIDLogoutRequest" value="<%=OpenIDConstants.OPEN_ID_LOGOUT_REQUEST%>"/>
<c_rt:set var="openIDError" value="<%=renderRequest.getAttribute(OpenIDConstants.OPEN_ID_ERROR)%>"/>

<c_rt:set var="providerButtons" value="<%=renderRequest.getAttribute(OpenIDLoginPortlet.PROVIDER_BUTTONS_ATTR_NAME)%>"/>
<c_rt:set var="enableOpenIDEntry" value="<%=renderRequest.getAttribute(OpenIDLoginPortlet.ENABLE_OPEN_ID_ENTRY_ATTR_NAME)%>"/>

<div class="portlet-section-text">
  <c:choose>
    <c:when test="${pageContext.request.userPrincipal != null}">
      <fmt:message key="openid-login.label.Welcome"><fmt:param><c:out value="${pageContext.request.userPrincipal.name}"/></fmt:param></fmt:message>
      <form method="POST" action='<portlet:actionURL/>'>
        <input type="submit" value='<fmt:message key="openid-login.label.Logout"/>'/>
        <input type="hidden" name="${openIDRequest}" value="${openIDLogoutRequest}"/>
      </form>
    </c:when>
    <c:otherwise>
      <c:if test="${openIDError != null}"><div class="portlet-msg-alert"><fmt:message key="openid-login.label.${openIDError}"/></div><br/></c:if>
      <c:if test="${not empty providerButtons}">
        <form method="POST" action='<portlet:actionURL/>'>
          <input type="hidden" name="${openIDProvider}"/>
          <c:forEach var="providerButton" items="${providerButtons}">
            <input type="submit" value='<fmt:message key="openid-login.label.ProviderLogin"><fmt:param><c:out value="${providerButton.value}"/></fmt:param></fmt:message>' onclick="this.form.elements[0].value='${providerButton.key}'; true"/>
          </c:forEach>
          <input type="hidden" name="${openIDRequest}" value="${openIDLoginRequest}"/>
        </form>
      </c:if>
      <c:if test="${enableOpenIDEntry == 'true'}">
        <form method="POST" action='<portlet:actionURL/>'>
          <span class="portlet-form-field-label"><fmt:message key="openid-login.label.OpenID"/></span>
          <input type="text" class="portlet-form-field" size="30" name="${openIDDiscovery}"/>
          <input type="submit" class="portlet-form-button" value='<fmt:message key="openid-login.label.Login"/>'/>
          <input type="hidden" name="${openIDRequest}" value="${openIDLoginRequest}"/>
        </form>
      </c:if>
    </c:otherwise>
  </c:choose>
</div>
