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
<%@ page import="org.apache.jetspeed.security.mfa.portlets.MFALogin" %>
<%@ page import="org.apache.jetspeed.security.mfa.portlets.StatusMessage" %>

<%@ page import="org.apache.jetspeed.security.mfa.CaptchaBean" %>
<%@ page import="org.apache.jetspeed.security.mfa.MultiFacetedAuthentication" %>
<%@ page import="org.apache.jetspeed.security.mfa.MFA" %>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>

<portlet:defineObjects/>
<portlet:actionURL var="submitAction" />
<jsp:useBean id="userBean" scope="session" class="org.apache.jetspeed.security.mfa.portlets.UserBean" />

<%
userBean.reset();
CaptchaBean captcha = MFA.getInstance().createCaptcha(request);
StatusMessage statusMsg = (StatusMessage)renderRequest.getAttribute(MFALogin.STATUS_MESSAGE);
if (statusMsg != null)
{   
%>
<br/>
<table width="100%" cellpadding="0" cellspacing="0" border="0">
<tr>
	<div class="<%= statusMsg.getType() %>">
	Your request has resulted in an error.
    </div>
    <p>
    <ul>
    <li>Did you enter an incorrect User ID, Password or mistype the Random Code?</li>
    <li>If you have completed the security update and you have entered correct information but are not able to log in please contact us. Your account may have been locked out for security reasons.</li>
    </ul>
    </p>	
</tr>
</table>
<%
}
%>

<jsp:setProperty name="userBean" property="captcha" value="<%= captcha.getChallengeId() %>" />
<jsp:setProperty name="userBean" property="invalidUser" value="<%= false %>" />

<form name="login1_captcha" method="post" action="${submitAction}">
<p>Username: <input type="text" id="username" name="username"/></p>
<p>Type in the text shown in this image:<br/>
<img src="<%= captcha.getImageURL() %>" /> <input type="text" id="captcha" name="captcha"/></p>
<input type='hidden' name='phase' id='phase' value='one'/>
<input type="submit" />
</form>
<%-- 
<p>If you cannot read the text in the image, you may reload to get a new image<% if ( tts.getAudioURL().length() > 0 ) { %>
	, or <a href="<%= tts.getAudioURL() %>">hear it read aloud</a>
<% } %>.</p>
--%>