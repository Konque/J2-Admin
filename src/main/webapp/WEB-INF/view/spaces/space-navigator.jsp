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
<%@ page import="java.util.List"%>
<%@ page import="java.text.DecimalFormat"%>
<%@ page import="java.text.NumberFormat"%>
<%@ page import="org.apache.jetspeed.portlets.spaces.SpaceBean"%>
<%@ page import="org.apache.jetspeed.om.page.Page"%>
<%@ page import="org.apache.jetspeed.CommonPortletServices"%>
<%@ page contentType="text/html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>

<portlet:defineObjects/>
<fmt:setBundle basename="org.apache.jetspeed.portlets.spaces.resources.SpacesResources" />

<div class="subnavbar">
    <div class="subnavbar-inner">
        <div class="container">

            <a data-target=".subnav-collapse" data-toggle="collapse" class="subnav-toggle" href="javascript:;">
                <span class="sr-only">Toggle navigation</span>
                <i class="icon-reorder"></i>
            </a>

            <div class="collapse subnav-collapse">
                <ul class="mainnav">
                    <c:forEach items="${spaces}" var="spaceItem" varStatus="status">
                            <c:if test="${!(status.index > 4 && (status.index+1) < fn:length(spaces))}">
                                <li class="<c:if test="${spaceItem.name  == space.name}">active</c:if>">
                                    <a href="<portlet:actionURL><portlet:param name='space' value='${spaceItem.name}'/></portlet:actionURL>" title="${spaceItem.title}">
                                        <i class="icon-home"></i>
                                        <span>${spaceItem.title}</span>
                                    </a>
                                </li>
                           </c:if>
                    </c:forEach>

                    <c:forEach items="${spaces}" var="spaceItem" varStatus="status">
                            <c:if test="${status.index > 4 && (status.index+1) < fn:length(spaces)}">
                                <c:choose>
                                    <c:when test="${status.index == 5}">
                                        <li class="dropdown">
                                        <a data-toggle="dropdown" class="dropdown-toggle" href="javascript:;">
                                            <i class="icon-th"></i>
                                            <span>Others</span>
                                            <b class="caret"></b>
                                        </a>

                                        <ul class="dropdown-menu">
                                    </c:when>
                                    <c:when test="${status.index > 5}">
                                        <li>
                                            <a href="<portlet:actionURL><portlet:param name='space' value='${spaceItem.name}'/></portlet:actionURL>" title="${spaceItem.title}">
                                                <i class="icon-home"></i>
                                                <span>${spaceItem.title}</span>
                                            </a>
                                        </li>BB${status.last}CC
                                        <c:if test="${status.last}">

                                            <c:if test="${spaceEditable or spaceCreatable}">
                                                <li class="divider"></li>
                                            </c:if>

                                            <c:if test="${spaceEditable}">
                                                <li>
                                                    <a href="<portlet:actionURL><portlet:param name='navAction' value='editSpace'/><portlet:param name='space' value='${space.name}'/></portlet:actionURL>" title="<fmt:message key='spaces.label.edit.current'/>"><fmt:message key='spaces.label.edit.current'/></a>
                                                </li>
                                            </c:if>

                                            <c:if test="${spaceCreatable}">
                                                <li>
                                                    <a href="<portlet:actionURL><portlet:param name='navAction' value='addSpace'/></portlet:actionURL>" title="<fmt:message key='spaces.label.add'/>"><fmt:message key='spaces.label.add'/></a>
                                                </li>
                                            </c:if>

                                            </ul>
                                            </li>
                                        </c:if>
                                    </c:when>
                                </c:choose>
                             </c:if>
                    </c:forEach>
                </ul>
            </div>
        </div>
    </div>
</div>

<ul id="nav-top">
  <li>

    <ul id="spaceList">
      <c:forEach items="${spaces}" var="spaceItem" varStatus="status">
        <c:set var="style" value=""/>
        <c:set var="extStyle" value=""/>
        <c:if test="${status.first}">
          <c:set var="style" value="first" />
        </c:if>
        <c:if test="${status.last}">
          <c:set var="style" value="last" />
        </c:if>
        <c:if test="${spaceItem.name == space.name}">
          <c:set var="extStyle" value="font-weight: bold" />
        </c:if>
        <li class="${style}">
          <a style="${extStyle}" href="<portlet:actionURL><portlet:param name='space' value='${spaceItem.name}'/></portlet:actionURL>" title="${spaceItem.title}">${spaceItem.title}</a>
        </li>
      </c:forEach>

      <c:if test="${spaceEditable or spaceCreatable}">
        <li class="separator"></li>
      </c:if>
      <c:if test="${spaceEditable}">
        <li>
          <a href="<portlet:actionURL><portlet:param name='navAction' value='editSpace'/><portlet:param name='space' value='${space.name}'/></portlet:actionURL>" title="<fmt:message key='spaces.label.edit.current'/>"><fmt:message key='spaces.label.edit.current'/></a>
        </li>
      </c:if>
      <c:if test="${spaceCreatable}">
      <li>
        <a href="<portlet:actionURL><portlet:param name='navAction' value='addSpace'/></portlet:actionURL>" title="<fmt:message key='spaces.label.add'/>"><fmt:message key='spaces.label.add'/></a>
      </li>
      </c:if>
    </ul>
  </li>
</ul>
