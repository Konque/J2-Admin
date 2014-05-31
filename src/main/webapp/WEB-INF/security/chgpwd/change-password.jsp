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
<%@page import="org.apache.jetspeed.request.RequestContext"%>
<%@page import="org.apache.jetspeed.portlets.security.ChangePasswordPortlet"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c_rt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<portlet:defineObjects/>
<fmt:setBundle basename="org.apache.jetspeed.portlets.security.resources.ChgPwdResources" />
<c_rt:set var="requestContext" value="<%=request.getAttribute(RequestContext.REQUEST_PORTALENV)%>"/>
<c:set var="portalContextPath" value="${requestContext.request.contextPath}"/>
<c:set var="portalContextPathInUrlTag" value="${portalContextPath}"/>
<c:if test="${empty portalContextPathInUrlTag}">
  <c:set var="portalContextPathInUrlTag" value="/"/>
</c:if>
<div class="portlet-section-text">
<c:choose>
  <c:when test="${pageContext.request.userPrincipal != null}">

    <c:set var="whyKey"><%=ChangePasswordPortlet.WHY%></c:set>
    <c:set var="why" value="${requestScope[whyKey]}"/>
    <c:set var="requiredKey"><%=ChangePasswordPortlet.REQUIRED%></c:set>
    <c:set var="required" value="${requestScope[requiredKey]}"/>
    <c:set var="errorMessagesKey"><%=ChangePasswordPortlet.ERROR_MESSAGES%></c:set>
    <c:set var="errorMessages" value="${requestScope[errorMessagesKey]}"/>
    
    <c:if test="${why != null}">
      <i><c:out value="${why}"/></i>
      <br/>
    </c:if>

	<!--custom error msg start-->
	<c:if test="${errorMessages != null}">
		<c:forEach items="${errorMessages}" var="error">
			 <div class="alert alert-danger alert-dismissable">
				<button class="close" aria-hidden="true" data-dismiss="alert" type="button">x</button>
				<strong><c:out value="${error}"/></strong>
			 </div>
		 </c:forEach>
	 </c:if>
	<!-- end -->


	<!--custom success msg .start-->
    <c_rt:set var="passwordChangedKey" value="<%=ChangePasswordPortlet.PASSWORD_CHANGED%>"/>
    <c:set var="p" value="${requestScope[passwordChangedKey]}"/>
    <c:if test="${requestScope[passwordChangedKey] != null}">
		<div class="alert alert-success alert-dismissable">
			<button class="close" aria-hidden="true" data-dismiss="alert" type="button">x</button>
			<strong><fmt:message key="chgpwd.message.passwordChanged"/></strong>
		</div>      
    </c:if>
    <!-- end -->


	<!-- custome start-->

	<form method="POST"  action="<portlet:actionURL/>" class="form-horizontal col-md-24">
		<fieldset>

			<div class="form-group">
					<label class="col-lg-4" for='<%=ChangePasswordPortlet.CURRENT_PASSWORD%>'><fmt:message key="chgpwd.label.currentPassword"/></label>
					<div class="col-lg-8">
					   <input type="password" name='<%=ChangePasswordPortlet.CURRENT_PASSWORD%>'  class="form-control"/>
					</div>
			</div>

			<div class="form-group">
					<label class="col-lg-4" for='<%=ChangePasswordPortlet.NEW_PASSWORD%>'><fmt:message key="chgpwd.label.newPassword"/></label>
					<div class="col-lg-8">
					   <input type="password" name='<%=ChangePasswordPortlet.NEW_PASSWORD%>'  class="form-control"/>
					</div>
			</div>

			<div class="form-group">
					<label class="col-lg-4" for='<%=ChangePasswordPortlet.NEW_PASSWORD_AGAIN%>'><fmt:message key="chgpwd.label.newPasswordAgain"/></label>
					<div class="col-lg-8">
					   <input type="password" name='<%=ChangePasswordPortlet.NEW_PASSWORD_AGAIN%>'  class="form-control"/>
					</div>
			</div>

			<div class="form-group">
					<input type="submit" value='<fmt:message key="chgpwd.label.save"/>'  class="btn btn-primary"/>

					 <c:if test="${why != null}">
						<c:choose>
						  <c:when test="${required == null}">
							&nbsp;&nbsp;
							<c_rt:set var="cancelItem" value="<%=ChangePasswordPortlet.CANCELLED%>"/>
							<input type="checkbox" style="display:none" name="<c:out value="${cancelItem}"/>">
							<input type="submit"  class="login-action btn btn-primary"
								   value="<fmt:message key="chgpwd.label.cancel"/>"
								   onClick="this.form.<c:out value="${cancelItem}"/>.checked=true">
						  </c:when>
						  <c:otherwise>
							<br/><br/>
							<a href='<c:url context="${portalContextPathInUrlTag}" value="/login/logout"/>'><fmt:message key="chgpwd.label.Logout"/></a>
						  </c:otherwise>
						</c:choose>
					 </c:if>
			</div>



		</fieldset>
	</form>

	<!-- end -->

  </c:when>
  <c:otherwise>
    <fmt:message key="chgpwd.error.notLoggedOn"/><br>
  </c:otherwise>
</c:choose>
</div>
