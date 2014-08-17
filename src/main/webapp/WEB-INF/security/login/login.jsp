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

<fmt:setBundle
	basename="org.apache.jetspeed.portlets.security.resources.LoginResources" />

<c_rt:set var="requestContext"
	value="<%=request.getAttribute(RequestContext.REQUEST_PORTALENV)%>" />
<c_rt:set var="loginDestination" value="<%=LoginConstants.DESTINATION%>" />
<c:set var="portalContextPath"
	value="${requestContext.request.contextPath}" />
<c:set var="portalContextPathInUrlTag" value="${portalContextPath}" />
<c:if test="${empty portalContextPathInUrlTag}">
	<c:set var="portalContextPathInUrlTag" value="/" />
</c:if>
<c:set var="encoder"
	value="${requestContext.request.parameterMap.encoder[0]}" />
<c:set var="destLogin"
	value="/login/proxy?${loginDestination}=${requestContext.portalURL.basePath}" />
<c:set var="destLogout"
	value="/login/logout?${loginDestination}=${requestContext.portalURL.basePath}" />
<c:set var="destAccount"
	value="${requestContext.request.servletPath}/my-account.psml" />
<c:if test="${not empty encoder && encoder == 'desktop'}">
	<c:set var="destLogin"
		value="/login/proxy?${loginDestination}=${requestContext.request.contextPath}/desktop" />
	<c:set var="destLogout"
		value="/login/logout?${loginDestination}=${requestContext.request.contextPath}/desktop" />
	<c:set var="destAccount"
		value="/desktop/my-account.psml?${loginDestination}=${requestContext.request.contextPath}/desktop" />
</c:if>
<div class="portlet-section-text">
	<c:choose>
		<c:when test="${pageContext.request.userPrincipal != null}">
			<fmt:message key="login.label.Welcome">
				<fmt:param>
					<c:out value="${pageContext.request.userPrincipal.name}" />
				</fmt:param>
			</fmt:message>
			<br>
			<a
				href='<c:url context="${portalContextPathInUrlTag}" value="${destLogout}"/>'><fmt:message
					key="login.label.Logout" /></a>
			<br>
			<a
				href='<c:url context="${portalContextPathInUrlTag}" value="${destAccount}"/>'><fmt:message
					key="login.label.ChangePassword" /></a>
		</c:when>
		<c:otherwise>
			<%-- backdoor access to the portal session to get the login error count --%>
			<c_rt:set var="errorCode"
				value="<%=((RequestContext) request
							.getAttribute(RequestContext.REQUEST_PORTALENV))
							.getSessionAttribute(LoginConstants.ERRORCODE)%>" />
			<c:choose>
				<c:when test="${not empty errorCode}">
					<br>
					<div class="portlet-msg-alert">
						<fmt:message key="login.label.ErrorCode.${errorCode}" />
					</div>
					<br>
				</c:when>
				<c:otherwise>
					<c_rt:set var="retryCount"
						value="<%=((RequestContext) request
									.getAttribute(RequestContext.REQUEST_PORTALENV))
									.getSessionAttribute(LoginConstants.RETRYCOUNT)%>" />
					<c:if test="${not empty retryCount}">
						<br>
						<div class="portlet-msg-alert">
							<fmt:message key="login.label.InvalidUsernameOrPassword">
								<fmt:param value="${retryCount}" />
							</fmt:message>
						</div>
						<br>
					</c:if>
				</c:otherwise>
			</c:choose>

			<div class="account-container stacked">
				<div class="content clearfix">
					<form
						action="<c:url context="${portalContextPathInUrlTag}" value="${destLogin}"/>"
						method="post">
						<h1>Login</h1>
						<h1>

							<div class="login-fields">
								<p>Sign in using your registered account:</p>

								<div class="field">
									<c_rt:set var="userName"
										value="<%=((RequestContext) request
							.getAttribute(RequestContext.REQUEST_PORTALENV))
							.getSessionAttribute(LoginConstants.USERNAME)%>" />
									<label for="userName"><fmt:message
											key="login.label.Username" /></label> <input type="text"
										value="${userName}"
										class="form-control input-lg username-field"
										placeholder="<fmt:message key="login.label.Username" />"
										name="<%=LoginConstants.USERNAME%>">
								</div>
								<!-- /field -->

								<div class="field">
									<label for="password"><fmt:message
											key="login.label.Password" /></label> <input type="password"
										class="form-control input-lg password-field"
										placeholder="<fmt:message key="login.label.Password" />"
										value="" name="<%=LoginConstants.PASSWORD%>">
								</div>
								<!-- /password -->
							</div>

							<div class="login-actions">

								<span class="login-checkbox"> <input type="checkbox"
									tabindex="4" value="First Choice" class="field login-checkbox"
									name="Field" id="Field"> <label for="Field"
									class="choice"><fmt:message
											key="login.label.KeepMeSignedIn" /></label>
								</span>

								<button class="login-action btn btn-primary" type="submit">
									<fmt:message key="login.label.Login" />
								</button>
							</div>

							<div class="login-social">
								<p>Sign in using social network:</p>

								<div class="twitter">
									<a class="btn_1" href="#">Login with Twitter</a>
								</div>

								<div class="fb">
									<a class="btn_2" href="#">Login with Facebook</a>
								</div>
							</div>
						</h1>
					</form>
				</div>
			</div>
		</c:otherwise>
	</c:choose>
</div>