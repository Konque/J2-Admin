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
<%@page import="org.apache.jetspeed.portlets.spaces.SpaceBean"%>
<%@page import="org.apache.jetspeed.request.RequestContext"%>

<%@ page contentType="text/html" %>
<%@page import="org.apache.jetspeed.portlets.spaces.SpaceBean"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c_rt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>

<portlet:defineObjects/>
<fmt:setBundle basename="org.apache.jetspeed.portlets.spaces.resources.SpacesResources" />
<c_rt:set var="requestContext" value="<%=request.getAttribute(RequestContext.REQUEST_PORTALENV)%>"/>
<c:set var="portalContextPath" value="${requestContext.request.contextPath}"/>
<c:set var="portalContextPathInUrlTag" value="${portalContextPath}"/>
<c:if test="${empty portalContextPathInUrlTag}">
  <c:set var="portalContextPathInUrlTag" value="/"/>
</c:if>

<c:set var="formDisplayble" value="false" />
<c:choose>
  <c:when test="${spaceCreatable}">
    <c:set var="formDisplayble" value="true" />
  </c:when>
  <c:when test="${spaceEditable and not empty space.name}">
    <c:set var="formDisplayble" value="true" />
  </c:when>
</c:choose>

<c:choose>

<c:when test="${formDisplayble}">

  <form method="POST" action='<portlet:actionURL/>'>
  <input type='hidden' name='spacePersisted' value='${space.persisted}'/>
  <table width="100%">
    <tr>
      <td>
        <table>
          <tr>
            <td class="portlet-section-subheader"><fmt:message key="spaces.label.name"/></td>
            <td class="portlet-section-subheader"><input type="text" <c:if test="${space.persisted}">readonly</c:if> name="spaceName" size="30" value="${space.name}" /></td>
          </tr>
          <c:if test="${space.persisted}">
            <tr>
              <td class="portlet-section-subheader"><fmt:message key="spaces.label.owner"/></td>
              <td class="portlet-section-subheader"><input type="text" name="spaceOwner" size="30" value="${space.owner}" /></td>
            </tr>
          </c:if>
          <tr>
            <td class="portlet-section-subheader"><fmt:message key="spaces.label.title"/></td>
            <td class="portlet-section-subheader"><input type="text" name="spaceTitle" size="30" value="${space.title}" /></td>
          </tr>
          <tr>
            <td class="portlet-section-subheader"><fmt:message key="spaces.label.description"/></td>
            <td class="portlet-section-subheader"><input type="text" name="spaceDescription" size="30" value="${space.description}"/></td>
          </tr>
          <tr>
            <td class="portlet-section-subheader"><fmt:message key="spaces.label.security"/></td>
            <td class="portlet-section-subheader">
              <select name="securityConstraintRef">
                <c:forEach items="${constraints}" var="constraint">
                  <option value="${constraint}" <c:if test="${space.securityConstraint == constraint}"> selected </c:if>>${constraint}</option>
                </c:forEach>
              </select>
            </td>
          </tr>
          <tr>
            <td class="portlet-section-subheader"><fmt:message key="spaces.label.theme"/></td>
            <td class="portlet-section-subheader"></td>
          </tr>
          <tr>
            <td colspan='2'>
              <table>
                <c:forEach var="theme" items="${themes}">
                  <tr>
                    <td><img src='<c:url context="${portalContextPathInUrlTag}" value="/decorations/layout/${theme.name}/${theme.image}"/>'></td>
                    <td style="vertical-align: middle"><input type="radio" <c:if test="${theme.selected}"> checked </c:if> name="theme" value="${theme.name}" /> ${theme.title}</td>
                  </tr>
                </c:forEach>
              </table>
            </td>
          </tr>
        </table>
      </td>
    </tr>
    <tr>
      <td colspan='2' style="text-align: center" class="portlet-section-subheader">
        <input type="submit" name="saveAction" value="<fmt:message key='spaces.label.save'/>" />
        <input type="submit" name="cancelAction" value="<fmt:message key='spaces.label.cancel'/>" />
      </td>
    </tr>
    <tr>
      <th class="portlet-section-header" colspan="2"></th>
    </tr>
  </table>
  </form>

</c:when>

<c:otherwise>

<p><em><fmt:message key='spaces.message.forbidden'/></em></p>

</c:otherwise>

</c:choose>
