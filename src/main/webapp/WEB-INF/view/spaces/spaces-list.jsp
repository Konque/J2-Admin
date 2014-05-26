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
<%@page contentType="text/html" %>
<%@page import="java.util.List"%>
<%@page import="org.apache.jetspeed.portlets.spaces.SpaceBean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<fmt:setBundle basename="org.apache.jetspeed.portlets.spaces.resources.SpacesResources" />

<form id='jsSpacesList' method="POST" action='<portlet:actionURL/>'>
<table width='100%'>
  <tr>
    <th class="portlet-section-subheader"><fmt:message key="spaces.label.title"/></th>
    <th class="portlet-section-subheader"><fmt:message key="spaces.label.owner"/></th>
    <th class="portlet-section-subheader" colspan="3"></th>
  </tr>
  <c:forEach var="spaceItem" items="${spaces}">
    <c:choose>
      <c:when test="${spaceItem.name == space.name}">
        <c:set var="styleClass" value="'portlet-section-alternate'" />
      </c:when>
      <c:otherwise>
        <c:set var="styleClass" value="'portlet-section-body'" />
      </c:otherwise>
    </c:choose>
    <tr>
      <td class="${styleClass}">
        <a href="<portlet:actionURL><portlet:param name='edit' value='${spaceItem.name}'/></portlet:actionURL>">${spaceItem.title}</a>
      </td>
      <td class="${styleClass}">${spaceItem.owner}</td>
      <td class="${styleClass}">&nbsp;</td>
      <td class="${styleClass}">&nbsp;</td>
      <c:choose>
        <c:when test="${spaceItem.path == '/' or spaceItem.path == '/Administrative'}">
          <td class="${styleClass}"></td>
        </c:when>
        <c:otherwise>
          <td class="${styleClass}">
            <a class="delete" href0="<portlet:actionURL><portlet:param name='delete' value='${spaceItem.name}'/></portlet:actionURL>"><fmt:message key="spaces.label.delete"/></a>
          </td>
        </c:otherwise>
      </c:choose>
    </tr>
  </c:forEach>
  <tr>
    <th class="portlet-section-header" colspan="5"></th>
  </tr>
  <tr>
    <td colspan="5"><input name='addspace' type='submit' value='<fmt:message key="spaces.label.add"/>'/></td>
  </tr>
</table>
</form>

<script language="javascript">
YUI().use('jetui-portal', 'node', function(Y) {
    var onDeleteClick = function(e) {
        var cf = confirm('<fmt:message key="spaces.message.confirm.delete"/>');
        if (!cf) {
            e.halt();
        }
    };
    
    Y.Node.all("A.delete").each(function(v, k) {
        v.on("click", onDeleteClick);
        v.set("href", v.getAttribute("href0"));
    });
});
</script>
