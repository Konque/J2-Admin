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
<%@ page import="java.util.*" %>
<%@ page import="javax.portlet.*" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.apache.jetspeed.security.PasswordCredential" %>
<%@ page import="org.apache.jetspeed.sso.SSOException" %>
<%@ page import="org.apache.jetspeed.sso.SSOManager" %>
<%@ page import="org.apache.jetspeed.sso.SSOSite" %>
<%@ page import="org.apache.jetspeed.sso.SSOUser" %>
<%@ page import="org.apache.jetspeed.portlets.sso.SSOIFramePortlet" %>
<%@ page import="org.apache.jetspeed.portlets.sso.SSOPortletUtil" %>

<%!
private PasswordCredential getRemotePasswordCredential(PortletRequest portletRequest)
{
    PortletSession portletSession = portletRequest.getPortletSession();
    
    if (portletSession != null)
    {
	    SSOManager sso = (SSOManager)portletSession.getPortletContext().getAttribute("cps:SSO");
	    String siteUrl = portletRequest.getPreferences().getValue("SRC", "");
	    SSOSite site = sso.getSiteByUrl(siteUrl);
	    
	    if (site != null)
	    {
	        try
	        {
	            SSOUser remoteUser = SSOPortletUtil.getRemoteUser(sso, portletRequest, site);
	            
	            if (remoteUser != null)
	            {
	                PasswordCredential pwc = sso.getCredentials(remoteUser);
	                return pwc;
	            }
	        }
	        catch (SSOException e)
	        {
	        }
	    }
    }
    
    return null;
}
%>

<%
ResourceRequest resourceRequest = (ResourceRequest) request.getAttribute("javax.portlet.request");

PortletPreferences prefs = resourceRequest.getPreferences();

String userNameParam = prefs.getValue(SSOIFramePortlet.SSO_TYPE_FORM_USERNAME, "user");
String passwordParam = prefs.getValue(SSOIFramePortlet.SSO_TYPE_FORM_PASSWORD, "password");

PasswordCredential remotePwc = getRemotePasswordCredential(resourceRequest);
String userName = (remotePwc != null ? remotePwc.getUserName() : "");
String password = (remotePwc != null ? remotePwc.getPassword() : "");

String formArgs = prefs.getValue(SSOIFramePortlet.SSO_TYPE_FORM_ARGS, "");

String formAction = prefs.getValue(SSOIFramePortlet.SSO_TYPE_FORM_ACTION, "");
String matrixArgs = StringUtils.substringAfter(formArgs, ";");
if (!StringUtils.isBlank(matrixArgs))
{
    formAction += (";" + matrixArgs);
}

String ssoType = prefs.getValue(SSOIFramePortlet.SSO_TYPE, "");
String formMethod = ("form.get".equals(ssoType) ? "GET" : "POST");

Map<String, String> formArgMap = new HashMap<String, String>();

String [] args = StringUtils.split(StringUtils.substringBefore(formArgs, ";"), "&");

for (String arg : args)
{
    String name = "";
    String value = "";
    String [] pair = StringUtils.split(arg, "=");
    
    if (pair.length > 0)
    {
        name = StringUtils.trim(StringUtils.defaultString(pair[0], ""));
    }
    
    if (pair.length > 1)
    {
        value = StringUtils.trim(StringUtils.defaultString(pair[1], ""));
    }
    
    formArgMap.put(name, value);
}
%>
<html>
<head>
</head>
<body onload="return document.getElementById('loginForm').submit();">
<form id="loginForm" method="<%=formMethod%>" action="<%=formAction%>">
    <input type="hidden" name="<%=userNameParam%>" value="<%=userName%>" />
    <input type="hidden" name="<%=passwordParam%>" value="<%=password%>" />
<% for (Map.Entry<String, String> entry : formArgMap.entrySet()) { %>
    <input type="hidden" name="<%=entry.getKey()%>" value="<%=entry.getValue()%>" />
<% } %>
</form>
</body>
</html>

<%
resourceRequest.getPortletSession(true).setAttribute(SSOIFramePortlet.SSO_TYPE_FORM_AUTH_FLAG, Boolean.TRUE);
%>
