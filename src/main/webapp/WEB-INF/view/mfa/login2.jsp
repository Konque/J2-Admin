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

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>

<portlet:defineObjects/>
<portlet:actionURL var="submitAction" />
<portlet:actionURL var="restartAction" >
	<portlet:param name="phase" value="restart"/>
</portlet:actionURL>

<jsp:useBean id="userBean" scope="session" class="org.apache.jetspeed.security.mfa.portlets.UserBean" />

<p>Your computer is not recognized.</p>

<form name="login2" method="post" action="${submitAction}">

<%= userBean.getQuestion() %><br/>
<input type="text" id="answer" name="answer"/><br/>
<input type="checkbox" id="publicTerminal" name="publicTerminal"/>
Don't save any credentials on this computer (choose this option e.g. if you're using a public terminal)<br/>
<input type='hidden' id='phase' name='phase' value='two'/>
<input type="submit" />
</form>
<br/>

<a href='${restartAction}'>Restart login sequence</a>

