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
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c_rt"%>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>

<%@page import="org.apache.jetspeed.portlets.openid.OpenIDLoginPortlet"%>

<portlet:defineObjects/>
<c_rt:set var="saveAction" value="<%=OpenIDLoginPortlet.SAVE_ACTION_PARAM_NAME%>"/>
<c_rt:set var="providerLabelsPrefName" value="<%=OpenIDLoginPortlet.PROVIDER_LABELS_PREF_NAME%>"/>
<c_rt:set var="providerLabels" value="<%=renderRequest.getAttribute(OpenIDLoginPortlet.PROVIDER_LABELS_PREF_NAME)%>"/>
<c_rt:set var="providerDomainsPrefName" value="<%=OpenIDLoginPortlet.PROVIDER_DOMAINS_PREF_NAME%>"/>
<c_rt:set var="providerDomains" value="<%=renderRequest.getAttribute(OpenIDLoginPortlet.PROVIDER_DOMAINS_PREF_NAME)%>"/>
<c_rt:set var="enableOpenIDEntryPrefName" value="<%=OpenIDLoginPortlet.ENABLE_OPEN_ID_ENTRY_PREF_NAME%>"/>
<c_rt:set var="enableOpenIDEntry" value="<%=Boolean.parseBoolean((String)renderRequest.getAttribute(OpenIDLoginPortlet.ENABLE_OPEN_ID_ENTRY_PREF_NAME))%>"/>
<c_rt:set var="enableConfigPrefName" value="<%=OpenIDLoginPortlet.ENABLE_REGISTRATION_CONFIG_PREF_NAME%>"/>
<c_rt:set var="enableConfig" value="<%=Boolean.parseBoolean((String)renderRequest.getAttribute(OpenIDLoginPortlet.ENABLE_REGISTRATION_CONFIG_PREF_NAME))%>"/>
<c_rt:set var="enablePrefName" value="<%=OpenIDLoginPortlet.ENABLE_REGISTRATION_PREF_NAME%>"/>
<c_rt:set var="enable" value="<%=Boolean.parseBoolean((String)renderRequest.getAttribute(OpenIDLoginPortlet.ENABLE_REGISTRATION_PREF_NAME))%>"/>
<c_rt:set var="userTemplatePrefName" value="<%=OpenIDLoginPortlet.REGISTRATION_USER_TEMPLATE_PREF_NAME%>"/>
<c_rt:set var="userTemplate" value="<%=renderRequest.getAttribute(OpenIDLoginPortlet.REGISTRATION_USER_TEMPLATE_PREF_NAME)%>"/>
<c_rt:set var="subsiteRootPrefName" value="<%=OpenIDLoginPortlet.REGISTRATION_SUBSITE_ROOT_PREF_NAME%>"/>
<c_rt:set var="subsiteRoot" value="<%=renderRequest.getAttribute(OpenIDLoginPortlet.REGISTRATION_SUBSITE_ROOT_PREF_NAME)%>"/>
<c_rt:set var="rolesPrefName" value="<%=OpenIDLoginPortlet.REGISTRATION_ROLES_PREF_NAME%>"/>
<c_rt:set var="roles" value="<%=renderRequest.getAttribute(OpenIDLoginPortlet.REGISTRATION_ROLES_PREF_NAME)%>"/>
<c_rt:set var="groupsPrefName" value="<%=OpenIDLoginPortlet.REGISTRATION_GROUPS_PREF_NAME%>"/>
<c_rt:set var="groups" value="<%=renderRequest.getAttribute(OpenIDLoginPortlet.REGISTRATION_GROUPS_PREF_NAME)%>"/>
<c_rt:set var="ruleNamesPrefName" value="<%=OpenIDLoginPortlet.REGISTRATION_PROFILER_RULE_NAMES_PREF_NAME%>"/>
<c_rt:set var="ruleNames" value="<%=renderRequest.getAttribute(OpenIDLoginPortlet.REGISTRATION_PROFILER_RULE_NAMES_PREF_NAME)%>"/>
<c_rt:set var="ruleValuesPrefName" value="<%=OpenIDLoginPortlet.REGISTRATION_PROFILER_RULE_VALUES_PREF_NAME%>"/>
<c_rt:set var="ruleValues" value="<%=renderRequest.getAttribute(OpenIDLoginPortlet.REGISTRATION_PROFILER_RULE_VALUES_PREF_NAME)%>"/>

<div class="portlet-section-text">
  <form method="POST" action='<portlet:actionURL/>'>
    <table border="0">
      <tr>
        <td nowrap align="right" class="portlet-section-alternate">OpenId Provider Labels:&nbsp;</td>
        <td align="left" class="portlet-section-body">
          <input type="text" size="40" name="${providerLabelsPrefName}" value="${providerLabels}" class="portlet-form-field-label"/>
        </td>  
      </tr>
      <tr>
        <td nowrap align="right" class="portlet-section-alternate">OpenId Provider Domains:&nbsp;</td>
        <td align="left" class="portlet-section-body">
          <input type="text" size="40" name="${providerDomainsPrefName}" value="${providerDomains}" class="portlet-form-field-label"/>
        </td>  
      </tr>
      <tr>
        <td nowrap align="right" class="portlet-section-alternate">Enable OpenID provider or URL entry:&nbsp;</td>
        <td align="left" class="portlet-section-body">
          <input type="checkbox" name="${enableOpenIDEntryPrefName}" value="true" <c:if test="${enableOpenIDEntry == 'true'}">checked</c:if>/>
        </td>  
      </tr>
      <tr>
        <td nowrap align="right" class="portlet-section-alternate">Enable Registration Preferences:&nbsp;</td>
        <td align="left" class="portlet-section-body">
          <input type="checkbox" name="${enableConfigPrefName}" value="true" <c:if test="${enableConfig == 'true'}">checked</c:if>/>
        </td>  
      </tr>
      <tr>
        <td nowrap align="right" class="portlet-section-alternate">Enable Registration:&nbsp;</td>
        <td align="left" class="portlet-section-body">
          <input type="checkbox" name="${enablePrefName}" value="true" <c:if test="${enable == 'true'}">checked</c:if>/>
        </td>  
      </tr>
      <tr>
        <td nowrap align="right" class="portlet-section-alternate">New User Template Directory:&nbsp;</td>
        <td align="left" class="portlet-section-body">
          <input type="text" size="40" name="${userTemplatePrefName}" value="${userTemplate}" class="portlet-form-field-label"/>
        </td>  
      </tr>
      <tr>
        <td nowrap align="right" class="portlet-section-alternate">Subsite Root Folder:&nbsp;</td>
        <td align="left" class="portlet-section-body">
          <input type="text" size="40" name="${subsiteRootPrefName}" value="${subsiteRoot}" class="portlet-form-field-label"/>
        </td>  
      </tr>
      <tr>
        <td nowrap align="right" class="portlet-section-alternate">Roles List:&nbsp;</td>
        <td align="left" class="portlet-section-body">
          <input type="text" size="40" name="${rolesPrefName}" value="${roles}" class="portlet-form-field-label"/>
        </td>  
      </tr>
      <tr>
        <td nowrap align="right" class="portlet-section-alternate">Groups List:&nbsp;</td>
        <td align="left" class="portlet-section-body">
          <input type="text" size="40" name="${groupsPrefName}" value="${groups}" class="portlet-form-field-label"/>
        </td>  
      </tr>
      <tr>
        <td nowrap align="right" class="portlet-section-alternate">Profiler Rule Names List:&nbsp;</td>
        <td align="left" class="portlet-section-body">
          <input type="text" size="40" name="${ruleNamesPrefName}" value="${ruleNames}" class="portlet-form-field-label"/>
        </td>  
      </tr>
      <tr>
        <td nowrap align="right" class="portlet-section-alternate">Profiler Rule Values List:&nbsp;</td>
        <td align="left" class="portlet-section-body">
          <input type="text" size="40" name="${ruleValuesPrefName}" value="${ruleValues}" class="portlet-form-field-label"/>
        </td>  
      </tr>
      <tr>
        <td colspan="2"><input type="submit" name="${saveAction}" value="Save"/><input type="submit" value="Cancel"/></td>
      </tr>
    </table>
  </form>
</div>
