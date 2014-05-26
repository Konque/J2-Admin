/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.jetspeed.portlets;

import java.io.Serializable;

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;

import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.administration.PortalConfiguration;
import org.apache.jetspeed.audit.AuditActivity;
import org.apache.jetspeed.components.portletregistry.PortletRegistry;
import org.apache.jetspeed.decoration.DecorationFactory;
import org.apache.jetspeed.deployment.DeploymentManager;
import org.apache.jetspeed.factory.PortletFactory;
import org.apache.jetspeed.page.PageManager;
import org.apache.jetspeed.profiler.Profiler;
import org.apache.jetspeed.search.SearchEngine;
import org.apache.jetspeed.security.GroupManager;
import org.apache.jetspeed.security.JetspeedPrincipalManagerProvider;
import org.apache.jetspeed.security.PasswordCredential;
import org.apache.jetspeed.security.RoleManager;
import org.apache.jetspeed.security.SecurityException;
import org.apache.jetspeed.security.User;
import org.apache.jetspeed.security.UserManager;
import org.apache.jetspeed.serializer.JetspeedSerializer;
import org.apache.jetspeed.tools.pamanager.PortletApplicationManagement;
import org.apache.jetspeed.tools.pamanager.servletcontainer.ApplicationServerManager;
import org.apache.wicket.RequestContext;
import org.apache.wicket.protocol.http.portlet.PortletRequestContext;

/**
 * Locates Jetspeed Services either by name or with direct accessors
 * 
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: JetspeedServiceLocator.java 770633 2009-05-01 11:16:20Z woonsan $
 */
public class JetspeedServiceLocator implements Serializable 
{
    private static final long serialVersionUID = 1L;

    public Object getService(String serviceName)
    {
        return getPortletContext().getAttribute(serviceName);        
    }
    
    public PortletRequest getPortletRequest()
    {
        return ((PortletRequestContext) RequestContext.get()).getPortletRequest();
    }

    public PortletContext getPortletContext()
    {
        return ((PortletConfig)getPortletRequest().getAttribute("javax.portlet.config")).getPortletContext();        
    }
    
    public UserManager getUserManager()
    {
        return (UserManager)getPortletContext().getAttribute(CommonPortletServices.CPS_USER_MANAGER_COMPONENT);
    }

    public RoleManager getRoleManager()
    {
        return (RoleManager) getPortletContext().getAttribute(CommonPortletServices.CPS_ROLE_MANAGER_COMPONENT);
    }

    public GroupManager getGroupManager()
    {
        return (GroupManager) getPortletContext().getAttribute(CommonPortletServices.CPS_GROUP_MANAGER_COMPONENT);
    }

    public Profiler getProfiler()
    {
        return (Profiler) getPortletContext().getAttribute(CommonPortletServices.CPS_PROFILER_COMPONENT);
    }

    public AuditActivity getAuditActivity()
    {
        return (AuditActivity) getPortletContext().getAttribute(CommonPortletServices.CPS_AUDIT_ACTIVITY);
    }

    public PageManager getPageManager()
    {
        return (PageManager) getPortletContext().getAttribute(CommonPortletServices.CPS_PAGE_MANAGER_COMPONENT);
    }

    public PortletRegistry getPortletRegistry()
    {
        return (PortletRegistry) getPortletContext().getAttribute(CommonPortletServices.CPS_REGISTRY_COMPONENT);
    }

    public SearchEngine getSearchEngine()
    {
        return (SearchEngine)getPortletContext().getAttribute(CommonPortletServices.CPS_SEARCH_COMPONENT);
    }
    
    public PortalConfiguration getPortalConfiguration()
    {
        return (PortalConfiguration) getPortletContext().getAttribute(CommonPortletServices.CPS_PORTAL_CONFIGURATION);
    }

    public JetspeedPrincipalManagerProvider getJetspeedPrincipalManagerProvider()
    {
        return (JetspeedPrincipalManagerProvider) getPortletContext().getAttribute(CommonPortletServices.CPS_JETSPEED_PRINCIPAL_MANAGER_PROVIDER);
    }

    public DecorationFactory getDecorationFactory()
    {
        return (DecorationFactory) getPortletContext().getAttribute(CommonPortletServices.CPS_DECORATION_FACTORY);
    }
    
    public PageManager getCastorPageManager()
    {
        return (PageManager) getPortletContext().getAttribute(CommonPortletServices.CPS_IMPORTER_MANAGER);
    }

    public PortletFactory getPortletFactory()
    {
        return (PortletFactory) getPortletContext().getAttribute(CommonPortletServices.CPS_PORTLET_FACTORY_COMPONENT);
    }
    
    public ApplicationServerManager getApplicationServerManager()
    {
        return (ApplicationServerManager) getPortletContext().getAttribute(CommonPortletServices.CPS_APPLICATION_SERVER_MANAGER_COMPONENT);
    }

    public PortletApplicationManagement getPortletApplicationManager()
    {
        return (PortletApplicationManagement) getPortletContext().getAttribute(CommonPortletServices.CPS_PORTLET_APPLICATION_MANAGER);
    }
    
    public DeploymentManager getDeploymentManager()
    {
        return (DeploymentManager) getPortletContext().getAttribute(CommonPortletServices.CPS_DEPLOYMENT_MANAGER_COMPONENT);
    }
    
    public PasswordCredential getCredential(User user) throws SecurityException
    {
        return getUserManager().getPasswordCredential(user);
    }
    
    public JetspeedSerializer getJetspeedSerializer()
    {
        return (JetspeedSerializer) getPortletContext().getAttribute(CommonPortletServices.CPS_JETSPEED_SERIALIZER);
    }
    
}
