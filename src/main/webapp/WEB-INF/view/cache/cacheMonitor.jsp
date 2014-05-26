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
<%@page import="java.text.DecimalFormat"%>
<%@page import="java.text.NumberFormat"%>
<%@page import="org.apache.jetspeed.cache.JetspeedCacheMonitor"%>
<%@page import="org.apache.jetspeed.cache.CacheMonitorState"%>
<%@page import="org.apache.jetspeed.CommonPortletServices"%>
<%@page import="org.apache.jetspeed.portlets.cache.CacheMonitorPortlet"%>
<%@ page contentType="text/html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>

<portlet:defineObjects/>
<fmt:setBundle basename="org.apache.jetspeed.portlets.cache.resources.CacheMonitorResources" />

   <p>   
 	<%
 	JetspeedCacheMonitor cacheMonitor = (JetspeedCacheMonitor)portletConfig.getPortletContext().getAttribute(CommonPortletServices.CPS_CACHE_MONITOR);
 	List<CacheMonitorState> states = cacheMonitor.snapshotStatistics();
  	%>
<table style="border-collapse: collapse; width: 350px; margin-top: 8px; float: left;">
	<tr>
		<th class="portlet-section-header" colspan="17"><fmt:message key="cachemonitor.table.title"/></th>
	</tr>

     <tr>
      <th class="portlet-section-subheader"><fmt:message key="cachemonitor.label.name"/></th>
      <th class="portlet-section-subheader" style="text-align: right"><fmt:message key="cachemonitor.label.maxmemory"/></th>
      <th class="portlet-section-subheader" style="text-align: right"><fmt:message key="cachemonitor.label.maxdisk"/></th>
      <th class="portlet-section-subheader" style="text-align: right"><fmt:message key="cachemonitor.label.idle"/></th>
      <th class="portlet-section-subheader" style="text-align: right"><fmt:message key="cachemonitor.label.live"/></th>
      <th class="portlet-section-subheader">&nbsp;</th>
      <th class="portlet-section-subheader" style="text-align: right"><fmt:message key="cachemonitor.label.memsize"/></th>
      <th class="portlet-section-subheader" style="text-align: right"><fmt:message key="cachemonitor.label.disksize"/></th>
      <th class="portlet-section-subheader" style="text-align: right"><fmt:message key="cachemonitor.label.avgget"/></th>
      <th class="portlet-section-subheader" style="text-align: right"><fmt:message key="cachemonitor.label.hits"/></th>
      <th class="portlet-section-subheader"style="text-align: right"><fmt:message key="cachemonitor.label.misses"/></th>
      <th class="portlet-section-subheader" style="text-align: right"><fmt:message key="cachemonitor.label.evictions"/></th>
      <th class="portlet-section-subheader">&nbsp;</th>
      <th class="portlet-section-subheader" style="text-align: right"><fmt:message key="cachemonitor.label.inmemsize"/></th>
      <th class="portlet-section-subheader">&nbsp;</th>
      <th class="portlet-section-subheader">&nbsp;</th>
      <th class="portlet-section-subheader">&nbsp;</thd>
     </tr>
<%
NumberFormat pf = new DecimalFormat("##0.000");
NumberFormat nf = new DecimalFormat("###,###,##0");

for (CacheMonitorState state : states)
{
%>     
	
     <tr>
       <td class="portlet-section-body"><%=state.getCacheName()%></td>
	   <td class="portlet-section-body" style="text-align: right"><%=nf.format(state.getMaxElementsInMemory())%></td>
	   <td class="portlet-section-body" style="text-align: right"><%=nf.format(state.getMaxElementsOnDisk())%></td>
	   <td class="portlet-section-body" style="text-align: right"><%=nf.format(state.getTimeToIdle())%></td>
	   <td class="portlet-section-body" style="text-align: right"><%=nf.format(state.getTimeToLive())%></td>
       <td class="portlet-section-body">&nbsp;</td>
	   <td class="portlet-section-body" style="text-align: right"><%=nf.format(state.getMemoryStoreSize())%></td>
	   <td class="portlet-section-body" style="text-align: right"><%=nf.format(state.getDiskStoreSize())%></td>
	   <td class="portlet-section-body" style="text-align: right"><%=pf.format(state.getAverageGetTime())%></td>
	   <td class="portlet-section-body" style="text-align: right"><%=nf.format(state.getCacheHits())%></td>
	   <td class="portlet-section-body" style="text-align: right"><%=nf.format(state.getCacheMisses())%></td>
	   <td class="portlet-section-body" style="text-align: right"><%=nf.format(state.getEvictionCount())%></td>
       <td class="portlet-section-body">&nbsp;</td>
       <td class="portlet-section-body" style="text-align: right"><%=nf.format(state.getInMemorySize())%></td>
	   <td class="portlet-section-body"><a href="<portlet:actionURL><portlet:param name='cacheNameCalc' value='<%=state.getCacheName()%>'/></portlet:actionURL>"><fmt:message key="cachemonitor.action.calculate"/></a></td>
       <td class="portlet-section-body">&nbsp;</td>
	   <td class="portlet-section-body"><a href="<portlet:actionURL><portlet:param name='cacheNameReset' value='<%=state.getCacheName()%>'/></portlet:actionURL>"><fmt:message key="cachemonitor.action.reset"/></a></td>
     </tr>
<%
}
%>
	<tr>
		<th class="portlet-section-header" colspan="17"></th>
	</tr>
	<tr>
	<td class="portlet-section-body' colspan="17">
	<a href="<portlet:actionURL><portlet:param name='cacheNameCalc' value='<%=CacheMonitorPortlet.ALL%>'/></portlet:actionURL>"><fmt:message key="cachemonitor.action.calculate.all"/></a>
	<a href="<portlet:actionURL><portlet:param name='cacheNameReset' value='<%=CacheMonitorPortlet.ALL%>'/></portlet:actionURL>"><fmt:message key="cachemonitor.action.reset.all"/></a>
	</td>
    </tr>
</table>
    
