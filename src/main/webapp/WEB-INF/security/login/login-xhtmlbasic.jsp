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
<%@page import="org.apache.jetspeed.login.LoginConstants"%>
<%@page import="org.apache.jetspeed.request.RequestContext"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c_rt"%>
<fmt:setBundle basename="org.apache.jetspeed.portlets.security.resources.LoginResources" />
<c_rt:set var="requestContext" value="<%=request.getAttribute(RequestContext.REQUEST_PORTALENV)%>"/>
<c:set var="portalContextPath" value="${requestContext.request.contextPath}"/>
<c:set var="portalContextPathInUrlTag" value="${portalContextPath}"/>
<c:if test="${empty portalContextPathInUrlTag}">
  <c:set var="portalContextPathInUrlTag" value="/"/>
</c:if>
<c:choose>
  <c:when test="${pageContext.request.userPrincipal != null}">
    <fmt:message key="login.label.Welcome"><fmt:param><c:out value="${pageContext.request.userPrincipal.name}"/></fmt:param></fmt:message><br/>
    <a href='<c:url context="${portalContextPathInUrlTag}" value="/login/logout?${loginDestination}=${requestContext.portalURL.basePath}"/>'><fmt:message key="login.label.Logout"/></a>
    <br/>
    <a href='<c:url context="${portalContextPathInUrlTag}" value="${requestContext.request.servletPath}/my-account.psml"/>'><fmt:message key="login.label.ChangePassword"/></a>
  </c:when>
  <c:otherwise>
    <%-- backdoor access to the portal session to get the login error count --%>
    <c_rt:set var="errorCode" value="<%=((RequestContext)request.getAttribute(RequestContext.REQUEST_PORTALENV)).getSessionAttribute(LoginConstants.ERRORCODE)%>"/>
    <c:choose>    
      <c:when test="${not empty errorCode}">
        <br/><fmt:message key="login.label.ErrorCode.${errorCode}"/><br/>
      </c:when>
      <c:otherwise>
        <c_rt:set var="retryCount" value="<%=((RequestContext)request.getAttribute(RequestContext.REQUEST_PORTALENV)).getSessionAttribute(LoginConstants.RETRYCOUNT)%>"/>
        <c:if test="${not empty retryCount}">
          <br/>
          <fmt:message key="login.label.InvalidUsernameOrPassword"><fmt:param value="${retryCount}"/></fmt:message><br/>
        </c:if>
      </c:otherwise>
    </c:choose>   
    <form method="post" action='<c:url context="${portalContextPathInUrlTag}" value="/login/proxy?${loginDestination}=${requestContext.portalURL.basePath}"/>'>
      <div>
        <fmt:message key="login.label.Username"/>
        <c_rt:set var="userName" value="<%=((RequestContext)request.getAttribute(RequestContext.REQUEST_PORTALENV)).getSessionAttribute(LoginConstants.USERNAME)%>"/>
        <input type="text" size="30" name="<%=LoginConstants.USERNAME%>" value="<c:out value="${userName}"/>"/>
      </div>
      <div>
        <fmt:message key="login.label.Password"/>
        <input type="password" size="30" name="<%=LoginConstants.PASSWORD%>"/>
      </div>
      <div>
        <input type="submit" value="<fmt:message key="login.label.Login"/>"/>
      </div>
    </form>
  </c:otherwise>
</c:choose>
