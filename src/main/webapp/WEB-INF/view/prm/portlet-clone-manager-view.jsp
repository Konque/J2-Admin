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

<fmt:setBundle basename="org.apache.jetspeed.portlets.clone.resources.PortletCloneManagerResources" />

<br/>

<%
String status = (String) renderRequest.getPortletSession(true).getAttribute("status");
if (status != null) 
{
    renderRequest.getPortletSession(true).removeAttribute("status");
}
%>

<% if (status != null) { %>
    <% if ("fail".equals(status)) { %>
        <div>Error: <%=renderRequest.getPortletSession(true).getAttribute("errorMessage")%></div>
    <% } else { %>
        <script type="text/javascript" language="javascript">
            var el = parent.document.getElementsByClassName('modal-window-close-script')[0];
            eval(el.textContent ? el.textContent : el.innerText);
        </script>
    <% } %>
<% } %>

<form method="POST" action="<portlet:actionURL/>">
  <table cellpadding="0" cellspacing="1" border="0" width="100%">
    <tbody>
      <tr>
        <td class="portlet-section-header" colspan="2">
          <h3><fmt:message key="portlet.clone.label.portlet_info"/></h3>
        </td>
      </tr>
      <tr>
        <td class="portlet-section-alternate" width="20%" nowrap="true">
          <fmt:message key="portlet.clone.label.name"/>
        </td>
        <td class="portlet-section-body">
          <input type="text" name="portlet_name" size="40" value="<c:out value='${clonePortletInfo.portletName}'/>"/>
        </td>
      </tr>
      <tr>
        <td class="portlet-section-alternate" width="20%" nowrap="true">
          <fmt:message key="portlet.clone.label.display_name"/>
        </td>
        <td class="portlet-section-body">
          <input type="text" name="portlet_displayName" size="40" value="<c:out value='${clonePortletInfo.portletDisplayName}'/>"/>
        </td>
      </tr>
      <tr>
        <td class="portlet-section-alternate" width="20%" nowrap="true">
          <fmt:message key="portlet.clone.label.title"/>
        </td>
        <td class="portlet-section-body">
          <input type="text" name="portlet_title" size="40" value="<c:out value='${clonePortletInfo.portletTitle}'/>"/>
        </td>
      </tr>
      <tr>
        <td class="portlet-section-alternate" width="20%" nowrap="true">
          <fmt:message key="portlet.clone.label.short_title"/>
        </td>
        <td class="portlet-section-body">
          <input type="text" name="portlet_shortTitle" size="40" value="<c:out value='${clonePortletInfo.portletShortTitle}'/>"/>
        </td>
      </tr>
      <tr>
        <td class="portlet-section-alternate" width="20%" nowrap="true">
          <fmt:message key="portlet.clone.label.keywords"/>
        </td>
        <td class="portlet-section-body">
          <input type="text" name="portlet_keywords" size="40" value="<c:out value='${clonePortletInfo.portletKeywords}'/>"/>
        </td>
      </tr>
    </tbody>
  </table>
  <c:if test="${not empty clonePortletInfo.portletPreferences}">
    <table cellpadding="0" cellspacing="1" border="0" width="100%">
      <tbody>
        <tr>
          <td class="portlet-section-header" colspan="2">
            <h3><fmt:message key="portlet.clone.label.preferences"/></h3>
          </td>
        </tr>
        <c:forEach var="item" items="${clonePortletInfo.portletPreferences}">
          <tr>
            <td class="portlet-section-alternate" width="20%" nowrap="true">
              <c:out value='${item.key}'/>
            </td>
            <td class="portlet-section-body">
              <c:forEach var="valItem" items="${item.value}">
                <input type="text" name="prefs_<c:out value='${item.key}'/>" size="40" value="<c:out value='${valItem}'/>"/>
              </c:forEach>
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </c:if>
  <table cellpadding="0" cellspacing="1" border="0" width="100%">
    <tbody>
      <tr>
        <th class="portlet-section-alternate" colspan="2">
          <input type="submit" value="<fmt:message key='portlet.clone.label.action.clone'/>" />
          <input type="button" value="<fmt:message key='portlet.clone.label.action.cancel'/>"
                 onclick="var el = parent.document.getElementsByClassName('modal-window-close-script')[0]; eval(el.textContent ? el.textContent : el.innerText); return false;" />
          <input type="hidden" name="originalPortletUniqueName" value="<c:out value='${clonePortletInfo.originalPortletUniqueName}'/>" />
        </th>
      </tr>
    </tbody>
  </table>
</form>

<br/>
