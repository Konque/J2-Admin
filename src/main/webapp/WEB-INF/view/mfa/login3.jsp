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
<%@taglib uri="http://java.sun.com/jstl/core" prefix="cr"%>
<%@taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>

<%@ page import="org.apache.jetspeed.security.mfa.MFA" %>
<%@ page import="org.apache.jetspeed.security.mfa.portlets.MFALogin" %>
<%@ page import="org.apache.jetspeed.security.mfa.portlets.StatusMessage" %>
<%@ page import="org.apache.jetspeed.security.mfa.CaptchaBean" %>
<%@ page import="org.apache.jetspeed.security.mfa.impl.CaptchaBeanImpl" %>

<portlet:defineObjects/>
<jsp:useBean id="userBean" scope="session" class="org.apache.jetspeed.security.mfa.portlets.UserBean" />
<portlet:actionURL var="restartAction" >
	<portlet:param name="phase" value="restart"/>
</portlet:actionURL>
<portlet:actionURL var="submitAction" />

<fmt:setBundle basename="org.apache.jetspeed.security.mfa.portlets.resources.MFAResources" />

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

<%
StatusMessage statusMsg = (StatusMessage)renderRequest.getAttribute(MFALogin.STATUS_MESSAGE);
if (statusMsg != null)
{   
%>
<br/>
<table width="100%" cellpadding="0" cellspacing="0" border="0">
<tr>
	<div class="<%= statusMsg.getType() %>"><%= statusMsg.getText() %></div>
</tr>
</table>
<%
}
%>

<%-- backdoor access to the portal session to get the login error count --%>
<c:set var="errorCode" value="<%=((RequestContext)request.getAttribute(RequestContext.REQUEST_PORTALENV)).getSessionAttribute(MFALogin.ERRORCODE)%>"/>
<c:choose>    
  <c:when test="${not empty errorCode}">
    <br>
    <div class="portlet-msg-alert">
      <fmt:message key="login.label.ErrorCode.${errorCode}"/>
    </div>
    <br>
  </c:when>
  <c:otherwise>
    <c:set var="retryCount" value="<%=((RequestContext)request.getAttribute(RequestContext.REQUEST_PORTALENV)).getSessionAttribute(MFALogin.RETRYCOUNT)%>"/>
    <c:if test="${not empty retryCount}">
      <br>
      <div class="portlet-msg-alert">
        <fmt:message key="login.label.InvalidUsernameOrPassword"><fmt:param value="${retryCount}"/></fmt:message>
      </div>
      <br>
    </c:if>
  </c:otherwise>
</c:choose>
<% if ( !userBean.getPassPhrase().equals("") ) {
	CaptchaBean captcha2 = MFA.getInstance().createCaptcha( request, userBean.getPassPhrase() ); %>
<img src="<%= captcha2.getImageURL() %>" />
<p>Our secret passphrase is <strong><%= userBean.getPassPhrase() %></strong>.
If it isn't, stop now and report to your local branch that the server has been breached.</p>
<% } %>

<form method="POST" action='${submitAction}'>
  <table border="0">
  <tr>
    <td><div class="portlet-form-field-label"><fmt:message key="login.label.Password"/></div></td>
    <td><input type="password" class="portlet-form-field" size="30" name="<%=LoginConstants.PASSWORD%>"></td>
  </tr>
  <tr>
    <td colspan="2"><input type="submit" class="portlet-form-button" value="<fmt:message key="login.label.Login"/>"></td>
  </tr>
  </table>
  <input type='hidden' value='<c:url context="${portalContextPathInUrlTag}" value="/"/>' name='redirect' id='redirect'/>
  <input type='hidden' name='phase' id='phase' value='three'/>  
</form>
<br/>

<a href='${restartAction}'>Restart login sequence</a>


