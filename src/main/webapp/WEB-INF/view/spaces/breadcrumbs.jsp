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
<%@page import="java.util.List"%>
<%@page import="org.apache.jetspeed.page.document.Node"%>
<%@page import="org.apache.jetspeed.portlets.spaces.BreadcrumbMenu.BreadcrumbMenuItem" %>
<%@ page contentType="text/html" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<fmt:setBundle basename="org.apache.jetspeed.portlets.spaces.resources.SpacesResources" />
<%
List<BreadcrumbMenuItem> menus = (List<BreadcrumbMenuItem>)renderRequest.getAttribute("breadcrumbs");
int count = 0;

String separator = "";
for (BreadcrumbMenuItem item : menus)
{
	if (count == 1)
	    separator = "> ";
%>     
<span style='display: inline; color: #808080'><%=separator%></span><a href="<%=item.getPath()%>"><%=item.getTitle() %></a>
<%
	count++;
}
%>
