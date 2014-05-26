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
<%@ page language="java" import="javax.portlet.*, java.util.List, java.util.Iterator" session="true" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="currentLocale"><%= request.getAttribute("currentLocale") %></c:set>
<fmt:setBundle basename="org.apache.jetspeed.portlets.localeselector.resources.LocaleSelectorResources" />

<portlet:actionURL var="changeLocaleAction">
</portlet:actionURL>
<script>
function setLanguage(lang)
{
	document.langChng.prefered_locale.value=lang;
	document.langChng.submit();
}
</script>
<form name="langChng" action="<%=changeLocaleAction%>" method="POST">
<table border="0">
	<tr>
		<td align="left"><fmt:message key="localeselector.label.language"/></td>
	</tr>
	<tr>
		<td align="left">
			<c:set var="locales"><fmt:message key="localeselector.locales"/></c:set>
			<c:forTokens var="l" items="${locales}" delims=",">
				<a href="javascript:setLanguage('<c:out value="${l}"/>')" title="<fmt:message key="localeselector.locale.${l}"/>""><img border="0" src="<%=request.getContextPath()%><c:out value="/images/${l}.gif"/>" /></a>
			</c:forTokens>
		</td>
	</tr>
</table>
<input type="hidden" name="prefered_locale" value=""/>
</form>