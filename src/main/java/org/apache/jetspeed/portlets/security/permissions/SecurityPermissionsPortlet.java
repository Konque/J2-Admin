/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jetspeed.portlets.security.permissions;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.portlets.dojo.AbstractAdminDojoVelocityPortlet;
import org.apache.jetspeed.security.JetspeedPermission;
import org.apache.jetspeed.security.JetspeedPrincipal;
import org.apache.jetspeed.security.JetspeedPrincipalType;
import org.apache.jetspeed.security.PermissionFactory;
import org.apache.jetspeed.security.PermissionManager;
import org.apache.jetspeed.security.Role;
import org.apache.jetspeed.security.RoleManager;
import org.apache.velocity.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Security Permissions Portlet
 * 
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: $
 */
public class SecurityPermissionsPortlet extends AbstractAdminDojoVelocityPortlet
{
    static final Logger logger = LoggerFactory.getLogger(SecurityPermissionsPortlet.class);
    protected PermissionManager pm = null;
    protected RoleManager rm = null;
    
    // TODO: move to prefs
    static final String CLASSNAMES[] = 
    {
        "org.apache.jetspeed.security.FolderPermission",
        "org.apache.jetspeed.security.PagePermission",
        "org.apache.jetspeed.security.PortletPermission"
    };
    static final String TITLES[] = 
    {
        "Folders",
        "Pages",
        "Portlets"
    };
    
    
    public void init(PortletConfig config) throws PortletException
    {
        super.init(config);
        PortletContext context = getPortletContext();
        pm = (PermissionManager) context
                .getAttribute(CommonPortletServices.CPS_PERMISSION_MANAGER);
        if (pm == null)
                throw new PortletException(
                        "Could not get instance of portal permission manager component");
        rm = (RoleManager) context
                .getAttribute(CommonPortletServices.CPS_ROLE_MANAGER_COMPONENT);
        if (rm == null)
            throw new PortletException(
                "Could not get instance of portal role manager component");        
    }

   
    public void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException
    {
        retrievePermissions(request.getPortletSession(), getContext(request));
        super.doView(request, response);
    }

    public void retrievePermissions(PortletSession session, Context context)
    {
        // TODO: don't use session, since this is a client-side portlet
        Iterator folderPermissions = (Iterator)session.getAttribute("folderPermissions", PortletSession.PORTLET_SCOPE);
        Iterator pagePermissions = (Iterator)session.getAttribute("pagePermissions", PortletSession.PORTLET_SCOPE);
        Iterator portletPermissions = (Iterator)session.getAttribute("portletPermissions", PortletSession.PORTLET_SCOPE);
        List<Role> roles = (List<Role>)session.getAttribute("roles", PortletSession.PORTLET_SCOPE);
        if (portletPermissions == null)
        {
            List folders = new LinkedList();
            List pages = new LinkedList();
            List portlets = new LinkedList();
            Iterator<JetspeedPermission> all = pm.getPermissions().iterator();
            while (all.hasNext())
            {
                JetspeedPermission permission = all.next();                
                if (permission.getType().equals(PermissionFactory.FOLDER_PERMISSION))
                {
                    folders.add(new PermissionData(permission));                    
                }
                else if (permission.getType().equals(PermissionFactory.PAGE_PERMISSION))
                {
                    pages.add(new PermissionData(permission));
                }
                else if (permission.getType().equals(PermissionFactory.PORTLET_PERMISSION))
                {
                    portlets.add(new PermissionData(permission));
                }                
            }
            folderPermissions = folders.iterator();
            pagePermissions = pages.iterator();
            portletPermissions = portlets.iterator();
            try
            {
                roles = rm.getRoles("");
            }
            catch(Exception e)
            {
                logger.error(e.getMessage(),e);
            }
        }        
        context.put("folderPermissions", folderPermissions);
        context.put("pagePermissions", pagePermissions);
        context.put("portletPermissions", portletPermissions);
        context.put("roles", roles);
    }
    
    public void processAction(ActionRequest request,
            ActionResponse actionResponse) throws PortletException, IOException
    {
    }

    public class PermissionData
    {
        public PermissionData(JetspeedPermission permission)
        {
            this.permission = permission;
            this.roles = "";
            List<JetspeedPrincipal> principals = null;
            principals = pm.getPrincipals(permission, JetspeedPrincipalType.ROLE);
            int size = principals.size();
            if (size == 0)
            {
                return;
            }
            int count = 0;
            StringBuffer result = new StringBuffer();
            for (JetspeedPrincipal principal : principals)
            {
                count++;
                result.append(principal.getName());
                if (count < size)
                {
                    result.append(",");
                }
            }
            this.roles = result.toString();
        }
        
        JetspeedPermission permission;
        String roles;
        
        public JetspeedPermission getPermission()
        {
            return permission;
        }
        
        public void setPermission(JetspeedPermission permission)
        {
            this.permission = permission;
        }
        
        public String getRoles()
        {
            return roles;
        }
        
        public void setRoles(String roles)
        {
            this.roles = roles;
        }
    }
}
