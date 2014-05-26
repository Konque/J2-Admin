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
<%@ page import="javax.servlet.http.HttpServletRequest" %>
<%@ page import="org.apache.commons.codec.binary.Base64" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<%!
private String getUsername(HttpServletRequest req)
{
    try
    {
        String authorization = req.getHeader("Authorization");
        
        if (authorization != null)
        {
            Base64 base64 = new Base64();
            String userInfo = new String(base64.decode(authorization.substring(6).getBytes()));
            String [] userInfoArray = StringUtils.split(userInfo, ":");
            return userInfoArray[0];
        }
    }
    catch (Exception e)
    {
        e.printStackTrace();
    }
    
    return null;
}
%>
<html>
<head>
<title>Authorized by Basic authentication!</title>
</head>
<body>
<p>Hello, <%=session.getAttribute("examples.basicauth.username")%>. You have been authorized by Basic authentication !!!</p>

<hr/>
<p>
This example is provided to demonstrate the SSO feature of SSOReverseProxyIFrame portlet.<br/>
<strong>This is not for production use! You should consult with other examples <br/>if you want to implement an authentication.</strong> 
</p>

</body>
</html>
