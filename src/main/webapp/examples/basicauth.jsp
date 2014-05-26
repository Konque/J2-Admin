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

<%--
WARNING:

  This example is provided to demonstrate the SSO feature of SSOReverseProxyIFrame portlet.
  This is not for production use!
  You should consult with other examples if you want to implement an authentication.

--%>

<%@ page language="java"%>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="javax.servlet.http.HttpServletRequest" %>
<%@ page import="javax.servlet.http.HttpServletResponse" %>
<%@ page import="org.apache.commons.codec.binary.Base64" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<%!
private static final String REALM = "ExampleBasicAuthJSP";

private Map userInfoMap = new HashMap();

public void jspInit()
{
    userInfoMap.put("manager", "manager");
    userInfoMap.put("admin", "admin");
}

private boolean authenticate(HttpServletRequest req)
{
    try
    {
		String authorization = req.getHeader("Authorization");
		
		if (authorization != null)
		{
		    Base64 base64 = new Base64();
			String userInfo = new String(base64.decode(authorization.substring(6).getBytes()));
			String [] userInfoArray = StringUtils.split(userInfo, ":");
			String username = userInfoArray[0];
			String password = userInfoArray[1];
			
			if (password.equals(userInfoMap.get(username)))
			{
			    req.getSession().setAttribute("examples.basicauth.username", username);
			    return true;
			}
		}
    }
    catch (Exception e)
    {
        e.printStackTrace();
    }
	
	return false;
}
%>
<%
if (authenticate(request))
{
	response.sendRedirect("basicauth_success.jsp");
}
else
{
	response.setHeader("WWW-Authenticate", "Basic realm=\"" + REALM + "\"");
	response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed! You can access by manager/manager or admin/admin.");
}
%>
