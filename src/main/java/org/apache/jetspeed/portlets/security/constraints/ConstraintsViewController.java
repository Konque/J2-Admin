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
package org.apache.jetspeed.portlets.security.constraints;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.om.page.PageSecurity;
import org.apache.jetspeed.page.PageManager;
import org.apache.jetspeed.security.Group;
import org.apache.jetspeed.security.GroupManager;
import org.apache.jetspeed.security.Role;
import org.apache.jetspeed.security.RoleManager;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.context.PortletConfigAware;
import org.springframework.web.portlet.mvc.AbstractController;

public class ConstraintsViewController extends AbstractController implements PortletConfigAware
{	
    private static final String     ROLES_CACHE_SESSION_ATTRIBUTE_NAME = "j2Roles";
    private static final String     GROUPS_CACHE_SESSION_ATTRIBUTE_NAME = "j2Groups";

    private PortletConfig portletConfig ;
    protected PageManager pageManager;

    protected RoleManager rm = null;
    protected GroupManager gm = null;
    
	public ModelAndView handleRenderRequestInternal(RenderRequest request, RenderResponse response) throws Exception
	{
		Map model = new HashMap();
		model.put( "messages", portletConfig.getResourceBundle( request.getLocale() ) );
		model.put( "greeting", "Hello");
        boolean constraintsEnabled = pageManager.getConstraintsEnabled();
        model.put("constraintsEnabled", new Boolean(constraintsEnabled));
        PageSecurity constraints = pageManager.getPageSecurity();        
        model.put("defs", constraints.getSecurityConstraintsDefs());
        model.put("globals", constraints.getGlobalSecurityConstraintsRefs());

        PortletSession session = request.getPortletSession();
        List<Role> roles = (List<Role>) session.getAttribute(ROLES_CACHE_SESSION_ATTRIBUTE_NAME, PortletSession.PORTLET_SCOPE);
        if ( roles == null )
        {
            try
            {
                roles = rm.getRoles("");
                session.setAttribute(ROLES_CACHE_SESSION_ATTRIBUTE_NAME, roles, PortletSession.PORTLET_SCOPE);
            }
            catch(Exception e)
            {
                logger.error( "Could not get list of roles from RoleManager.", e);
            }
        }
        model.put("roles", roles);

        List<Group> groups = (List<Group>) session.getAttribute(GROUPS_CACHE_SESSION_ATTRIBUTE_NAME, PortletSession.PORTLET_SCOPE);
        if ( groups == null )
        {
            try
            {
                groups = gm.getGroups("");
                session.setAttribute(GROUPS_CACHE_SESSION_ATTRIBUTE_NAME, groups, PortletSession.PORTLET_SCOPE);
            }
            catch(Exception e)
            {
                logger.error( "Could not get list of groups from GroupManager.", e);
            }
        }
        model.put("groups", groups);
        
        model.put("renderRequest", request);

        return new ModelAndView("constraintsView", "model", model);
	}	
	       
    public void setPortletConfig(PortletConfig portletConfig)
    {   
        this.portletConfig = portletConfig;
        pageManager = (PageManager) getPortletContext().getAttribute(CommonPortletServices.CPS_PAGE_MANAGER_COMPONENT);        
        if (null == pageManager) 
        { 
            PortletException pe = new PortletException("Failed to find the Page Manager on portlet initialization");
            throw new RuntimeException(pe); 
        }

        rm = (RoleManager) getPortletContext().getAttribute(CommonPortletServices.CPS_ROLE_MANAGER_COMPONENT);
        if (rm == null)
            throw new RuntimeException("Could not get instance of portal role manager component");

        gm = (GroupManager) getPortletContext().getAttribute(CommonPortletServices.CPS_GROUP_MANAGER_COMPONENT);
        if (gm == null)
            throw new RuntimeException("Could not get instance of portal group manager component");

    }


}
