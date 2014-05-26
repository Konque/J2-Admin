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
<%@ page import="org.apache.jetspeed.security.mfa.portlets.MFALogin" %>
<%@ page import="org.apache.jetspeed.security.mfa.portlets.StatusMessage" %>

<%@ page import="java.util.List" %>
<%@ page import="org.apache.jetspeed.security.mfa.util.QuestionFactory" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>

<portlet:defineObjects/>
<portlet:actionURL var="submitAction" />
<portlet:actionURL var="restartAction" >
	<portlet:param name="phase" value="restart"/>
</portlet:actionURL>

<jsp:useBean id="userBean" scope="session" class="org.apache.jetspeed.security.mfa.portlets.UserBean" />
<jsp:useBean id="securityQuestion" scope="session" class="org.apache.jetspeed.security.mfa.SecurityQuestionBean" />

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

<form name="enroll" method="post" action="${submitAction}">

<%= userBean.getQuestion() %><br/>
<select name="question1">
<%
   QuestionFactory questions = (QuestionFactory)renderRequest.getAttribute(MFALogin.QUESTION_FACTORY);
   List list1 = questions.getAllQuestionsInRandomOrder();
   for (int i=0; i<list1.size(); i++)
   {
%>
<option value="<%= list1.get(i) %>"><%= list1.get(i) %></option>
<% } %>
</select>
<input type="text" id="answer1" name="answer1"/><br/>
<select name="question2">
<% List list2 = questions.getAllQuestionsInRandomOrder();
   for (int i=0; i<list2.size(); i++)
   {
%>
 <option value="<%= list2.get(i) %>"><%= list2.get(i) %></option>
<% } %>
</select>
<input type="text" id="answer2" name="answer2"/><br/>
<select name="question3">
<% List list3 = questions.getAllQuestionsInRandomOrder();
   for (int i=0; i<list3.size(); i++)
   {
%>
 <option value="<%= list3.get(i) %>"><%= list3.get(i) %></option>
 <% } %>
</select>
<input type="text" id="answer3" name="answer3"/><br/>
<br/>
<p>Please enter a pass phrase to be displayed when you login. Everytime you login, make sure you see this unique passphrase.
This is not a password, but simply a method for you to be sure you are logging into the correct system.</p>
<p>Passphrase: <input type="text" id="passphrase" name="passphrase"/></p>
<input type='hidden' id='phase' name='phase' value='enroll'/>
<input type='hidden' value='<c:url context="${portalContextPathInUrlTag}" value="/"/>' name='redirect' id='redirect'/>
<input type="submit" />
</form>
<br/>

<a href='${restartAction}'>Restart login sequence</a>

