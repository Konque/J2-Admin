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
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>

<c:set var="requestContext" value="<%=request.getAttribute(RequestContext.REQUEST_PORTALENV)%>"/>
<c:set var="loginDestination" value="<%=LoginConstants.DESTINATION%>"/>
<c:set var="portalContextPath" value="${requestContext.request.contextPath}"/>
<c:set var="portalContextPathInUrlTag" value="${portalContextPath}"/>
<c:if test="${empty portalContextPathInUrlTag}">
  <c:set var="portalContextPathInUrlTag" value="/"/>
</c:if>
<c:set var="encoder" value="${requestContext.request.parameterMap.encoder[0]}"/>
<c:set var="destLogin" value="/login/proxy"/>
<c:set var="destLogout" value="/login/logout"/>
<c:set var="destAccount" value="/portal/my-account.psml"/>
<c:if test="${not empty encoder && encoder == 'desktop'}">
  <c:set var="destLogin" value="${destLogin}?${loginDestination}=${requestContext.request.contextPath}/desktop"/>
  <c:set var="destLogout" value="${destLogout}?${loginDestination}=${requestContext.request.contextPath}/desktop"/>
  <c:set var="destAccount" value="/desktop/my-account.psml?${loginDestination}=${requestContext.request.contextPath}/desktop"/>
</c:if>
<div class="portlet-section-text">
    Welcome <c:out value="${pageContext.request.userPrincipal.name}"/><br>
    <a href='<c:url context="${portalContextPathInUrlTag}" value="${destLogout}"/>'>Logout</a>
    <br>
    <a href='<c:url context="${portalContextPathInUrlTag}" value="${destAccount}"/>'>Change Password</a>
</div>
