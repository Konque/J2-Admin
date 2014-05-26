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
package org.apache.jetspeed.portlets.custom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.PortalReservedParameters;
import org.apache.jetspeed.layout.PageLayoutComponent;
import org.apache.jetspeed.om.common.SecurityConstraint;
import org.apache.jetspeed.om.common.SecurityConstraints;
import org.apache.jetspeed.om.page.ContentFragment;
import org.apache.jetspeed.om.page.ContentPage;
import org.apache.jetspeed.page.PageManager;
import org.apache.jetspeed.request.RequestContext;
import org.apache.portals.bridges.velocity.GenericVelocityPortlet;

/**
 * Common Custom Config Mode Portlet
 * 
 * @version $Id$
 */
public class CustomConfigModePortlet extends GenericVelocityPortlet
{
    private static final PortletMode CONFIG_MODE = new PortletMode("config");
    private static final String DELIMITERS = "[],; \t\r\n";
    
    private PageManager pageManager;
    private PageLayoutComponent pageLayoutComponent;
    private String configPage;
    
    @Override
    public void init(PortletConfig config) throws PortletException
    {
        super.init(config);
        
        configPage = config.getInitParameter("ConfigPage");
        
        PortletContext context = getPortletContext();
        
        pageManager = (PageManager) context.getAttribute(CommonPortletServices.CPS_PAGE_MANAGER_COMPONENT);
        
        if (pageManager == null)
        {
            throw new PortletException("Could not get instance of pageManager component");
        }
        
        pageLayoutComponent = (PageLayoutComponent) context.getAttribute(CommonPortletServices.CPS_PAGE_LAYOUT_COMPONENT);
        
        if (pageLayoutComponent == null)
        {
            throw new PortletException("Could not get instance of pageLayoutComponent");
        }
    }
    
    @Override
    protected void doDispatch(RenderRequest request, RenderResponse response) throws PortletException, IOException
    {
        if (!request.getWindowState().equals(WindowState.MINIMIZED))
        {
            PortletMode curMode = request.getPortletMode();
            
            if (CONFIG_MODE.equals(curMode))
            {
                ContentFragment curFragment = (ContentFragment) request.getAttribute(PortalReservedParameters.FRAGMENT_ATTRIBUTE);
                
                if (curFragment == null)
                {
                    throw new PortletException("Cannot retrieve current fragment from the request.");
                }
                
                List securityContraintRefList = null;
                
                try
                {
                    securityContraintRefList = this.pageManager.getPageSecurity().getSecurityConstraintsDefs();
                }
                catch (Exception e)
                {
                    throw new PortletException("Cannot find page security constraint definitions.", e);
                }
                
                if (securityContraintRefList != null)
                {
                    request.setAttribute("securityContraintRefList", securityContraintRefList);
                }
                
                request.setAttribute("fragmentId", curFragment.getId());
                request.setAttribute("securityConstraints", new TransientSecurityConstraints(curFragment.getSecurityConstraints()));
                
                request.setAttribute(PARAM_EDIT_PAGE, this.configPage);
                
                doEdit(request, response);
            }
            else
            {
                super.doDispatch(request, response);
            }
        }
    }

    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException
    {
        String action = request.getParameter("action");
        
        if ("addConstraint".equals(action))
        {
            addSecurityConstraint(request, response);
        }
        else if ("removeConstraint".equals(action))
        {
            removeSecurityConstraint(request, response);
        }
        else if ("updateConstraintRefs".equals(action))
        {
            updateSecurityConstraintRefs(request, response);
        }
    }
    
    @Override
    protected String getTitle(RenderRequest request)
    {
        String title = null;
        
        try
        {
            ContentFragment curFragment = (ContentFragment) request.getAttribute(PortalReservedParameters.FRAGMENT_ATTRIBUTE);
            title = curFragment.getPortletContent().getTitle();
        }
        catch (Exception ignore)
        {
        }
        
        if (title != null)
        {
            return title;
        }
        
        return super.getTitle(request);
    }
    
    private void addSecurityConstraint(ActionRequest request, ActionResponse response) throws PortletException, IOException
    {
        try
        {
            RequestContext requestContext = (RequestContext) request.getAttribute(RequestContext.REQUEST_PORTALENV);
            ContentPage page = requestContext.getPage();
            String fragmentId = request.getParameter("fragment");
            
            ContentFragment fragment = page.getFragmentById(fragmentId);
            
            if (fragment == null)
            {
                throw new PortletException("Cannot find fragment: " + fragmentId);
            }
            
            SecurityConstraints constraints = new TransientSecurityConstraints(fragment.getSecurityConstraints());
            SecurityConstraint constraint = new TransientSecurityConstraint(fragment.newSecurityConstraint());
            String [] rolesArray = StringUtils.split(request.getParameter("roles"), DELIMITERS);
            String [] groupsArray = StringUtils.split(request.getParameter("groups"), DELIMITERS);
            String [] usersArray = StringUtils.split(request.getParameter("users"), DELIMITERS);
            
            if (!ArrayUtils.isEmpty(rolesArray))
            {
                constraint.setRoles(Arrays.asList(rolesArray));
            }
            
            if (!ArrayUtils.isEmpty(groupsArray))
            {
                constraint.setGroups(Arrays.asList(groupsArray));
            }
            
            if (!ArrayUtils.isEmpty(usersArray))
            {
                constraint.setUsers(Arrays.asList(usersArray));
            }
            
            String [] permissionArray = StringUtils.split(StringUtils.defaultString(request.getParameter("permissions")), DELIMITERS);
            
            if (!ArrayUtils.isEmpty(permissionArray))
            {
                constraint.setPermissions(Arrays.asList(permissionArray));
            }
            
            List<SecurityConstraint> constraintList = constraints.getSecurityConstraints();
            
            if (constraintList == null)
            {
                constraintList = new ArrayList<SecurityConstraint>();
            }
            
            constraintList.add(constraint);
            constraints.setSecurityConstraints(constraintList);
            
            pageLayoutComponent.updateSecurityConstraints(fragment, constraints);
        }
        catch (Exception e)
        {
            throw new PortletException("Failed to add security constraint.", e);
        }
    }
    
    private void removeSecurityConstraint(ActionRequest request, ActionResponse response) throws PortletException, IOException
    {
        try
        {
            RequestContext requestContext = (RequestContext) request.getAttribute(RequestContext.REQUEST_PORTALENV);
            ContentPage page = requestContext.getPage();
            String fragmentId = request.getParameter("fragment");
            
            ContentFragment fragment = page.getFragmentById(fragmentId);
            
            if (fragment == null)
            {
                throw new PortletException("Cannot find fragment: " + fragmentId);
            }

            String roles = request.getParameter("roles");
            String groups = request.getParameter("groups");
            String users = request.getParameter("users");
            String permissions = request.getParameter("permissions");
            
            SecurityConstraints constraints = new TransientSecurityConstraints(fragment.getSecurityConstraints());
            
            List<SecurityConstraint> constraintList = null;
            
            if (constraints != null)
            {
                constraintList = constraints.getSecurityConstraints();
                
                if (constraintList != null)
                {
                    for (Iterator it = constraintList.iterator(); it.hasNext(); )
                    {
                        SecurityConstraint constraint = (SecurityConstraint) it.next();
                        
                        String [] removeRoleArray = StringUtils.split(roles, DELIMITERS);
                        String [] removeGroupArray = StringUtils.split(groups, DELIMITERS);
                        String [] removeUserArray = StringUtils.split(users, DELIMITERS);
                        
                        List<String> roleList = constraint.getRoles();
                        List<String> groupList = constraint.getGroups();
                        List<String> userList = constraint.getUsers();
                        
                        if (hasEqualItems(removeRoleArray, roleList) &&
                            hasEqualItems(removeGroupArray, groupList) &&
                            hasEqualItems(removeUserArray, userList))
                        {
                            it.remove();
                            break;
                        }
                    }
                }
            }
            
            if (constraints != null && constraintList != null)
            {
                constraints.setSecurityConstraints(constraintList);
            }
            
            pageLayoutComponent.updateSecurityConstraints(fragment, constraints);
        }
        catch (Exception e)
        {
            throw new PortletException("Failed to remove security constraint.", e);
        }
    }

    private void updateSecurityConstraintRefs(ActionRequest request, ActionResponse response) throws PortletException, IOException
    {
        try
        {
            RequestContext requestContext = (RequestContext) request.getAttribute(RequestContext.REQUEST_PORTALENV);
            ContentPage page = requestContext.getPage();
            String fragmentId = request.getParameter("fragment");
            
            ContentFragment fragment = page.getFragmentById(fragmentId);
            
            if (fragment == null)
            {
                throw new PortletException("Cannot find fragment: " + fragmentId);
            }
            
            String [] securityConstraintRefs = request.getParameterValues("securityConstraintRef");
            
            SecurityConstraints constraints = new TransientSecurityConstraints(fragment.getSecurityConstraints());
            
            Set<String> constraintRefSet = new HashSet<String>();
            
            if (securityConstraintRefs != null)
            {
                for (int i = 0; i < securityConstraintRefs.length; i++)
                {
                    if (!"".equals(securityConstraintRefs[i]))
                    {
                        constraintRefSet.add(securityConstraintRefs[i]);
                    }
                }
            }
            
            constraints.setSecurityConstraintsRefs(constraintRefSet.isEmpty() ? null : new ArrayList<String>(constraintRefSet));
            
            pageLayoutComponent.updateSecurityConstraints(fragment, constraints);
        }
        catch (Exception e)
        {
            throw new PortletException("Failed to remove security constraint.", e);
        }
    }
    
    private boolean hasEqualItems(String [] array, List<String> list)
    {
        if (ArrayUtils.isEmpty(array))
        {
            return (list == null || list.isEmpty());
        }
        else if (list == null)
        {
            return ArrayUtils.isEmpty(array);
        }
        else if (ArrayUtils.getLength(array) == list.size())
        {
            for (String item : array)
            {
                if (!list.contains(item))
                {
                    return false;
                }
            }
            
            return true;
        }
        
        return false;
    }
    
    private class TransientSecurityConstraints implements SecurityConstraints
    {
        private String owner;
        private List<SecurityConstraint> securityConstraints;
        private List<String> securityConstraintsRefs;
        
        public TransientSecurityConstraints(SecurityConstraints sourceSecurityConstraints)
        {
            if (sourceSecurityConstraints != null)
            {
                this.owner = sourceSecurityConstraints.getOwner();
                
                List<SecurityConstraint> sourceConstraintList = sourceSecurityConstraints.getSecurityConstraints();
                
                if (sourceConstraintList != null)
                {
                    this.securityConstraints = new ArrayList<SecurityConstraint>();
                    
                    for (SecurityConstraint constraint : sourceConstraintList)
                    {
                        this.securityConstraints.add(new TransientSecurityConstraint(constraint));
                    }
                }
                
                List<String> constraintsRefs = sourceSecurityConstraints.getSecurityConstraintsRefs();
                
                if (constraintsRefs != null)
                {
                    securityConstraintsRefs = new ArrayList<String>(constraintsRefs);
                }
            }
        }
        
        public String getOwner()
        {
            return owner;
        }

        public List getSecurityConstraints()
        {
            return securityConstraints;
        }

        public List getSecurityConstraintsRefs()
        {
            return securityConstraintsRefs;
        }

        public boolean isEmpty()
        {
            return (this.securityConstraints == null || this.securityConstraints.isEmpty()) && (this.securityConstraintsRefs == null || this.securityConstraintsRefs.isEmpty());
        }

        public void setOwner(String owner)
        {
            this.owner = owner;
        }

        public void setSecurityConstraints(List constraints)
        {
            this.securityConstraints = constraints;
        }

        public void setSecurityConstraintsRefs(List constraintsRefs)
        {
            this.securityConstraintsRefs = constraintsRefs;
        }
    }
    
    private class TransientSecurityConstraint implements SecurityConstraint
    {
        private List<String> roles;
        private List<String> groups;
        private List<String> users;
        private List<String> permissions;
        
        public TransientSecurityConstraint(SecurityConstraint sourceSecurityConstraint)
        {
            if (sourceSecurityConstraint.getRoles() != null)
            {
                this.roles = new ArrayList<String>(sourceSecurityConstraint.getRoles());
            }
            
            if (sourceSecurityConstraint.getGroups() != null)
            {
                this.groups = new ArrayList<String>(sourceSecurityConstraint.getGroups());
            }
            
            if (sourceSecurityConstraint.getUsers() != null)
            {
                this.users = new ArrayList<String>(sourceSecurityConstraint.getUsers());
            }
            
            if (sourceSecurityConstraint.getPermissions() != null)
            {
                this.permissions = new ArrayList<String>(sourceSecurityConstraint.getPermissions());
            }
        }
        
        public List getGroups()
        {
            return groups;
        }

        public List getPermissions()
        {
            return permissions;
        }

        public List getRoles()
        {
            return roles;
        }

        public List getUsers()
        {
            return users;
        }

        public void setGroups(List groups)
        {
            this.groups = groups;
        }

        public void setPermissions(List permissions)
        {
            this.permissions = permissions;
        }

        public void setRoles(List roles)
        {
            this.roles = roles;
        }

        public void setUsers(List users)
        {
            this.users = users;
        }
    }
    
}
