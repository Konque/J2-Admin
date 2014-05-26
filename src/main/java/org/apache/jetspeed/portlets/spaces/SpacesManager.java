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
package org.apache.jetspeed.portlets.spaces;

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
import org.apache.jetspeed.administration.PortalAdministration;
import org.apache.jetspeed.decoration.DecorationFactory;
import org.apache.jetspeed.om.folder.Folder;
import org.apache.jetspeed.om.page.SecurityConstraintsDef;
import org.apache.jetspeed.page.PageManager;
import org.apache.jetspeed.portlets.toolbox.ThemeBean;
import org.apache.jetspeed.security.UserManager;
import org.apache.jetspeed.spaces.Space;
import org.apache.jetspeed.spaces.Spaces;
import org.apache.portals.bridges.common.GenericServletPortlet;
import org.apache.portals.messaging.PortletMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Spaces Manager
 *
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: SpacesManager.java 926730 2010-03-23 18:55:35Z woonsan $
 */
public class SpacesManager extends GenericServletPortlet 
{
    public static final String MSG_TOPIC_SPACE_LIST = "SpaceList";
    public static final String MSG_TOPIC_SPACE_NAV =  "SpaceNavigator";
    public static final String MSG_TOPIC_PAGE_NAV =  "PageNavigator";
    public static final String MSG_SPACE_CHANGE = "spaceChange";
    
    private static final String DEFAULT_SPACE_TEMPLATE = "/_template/space";
    private static final String SPACE_TEMPLATE = "spaceTemplate";

    private static Logger log = LoggerFactory.getLogger(SpacesManager.class);
    
    protected PageManager pageManager;
    private PortalAdministration admin;
    private UserManager userManager;
    
    private Spaces spacesService;
    protected DecorationFactory decorationFactory;    
    
    public void init(PortletConfig config) throws PortletException
    {
        super.init(config);
        
        PortletContext context = getPortletContext();
        
        spacesService = (Spaces) context.getAttribute(CommonPortletServices.CPS_SPACES_SERVICE);
        if (spacesService == null)
        {
            throw new PortletException("Could not get instance of portal spaces service component");
        }
        
        admin = (PortalAdministration) context.getAttribute(CommonPortletServices.CPS_PORTAL_ADMINISTRATION);
        if (admin == null) 
        { 
            throw new PortletException("Failed to find the Portal Administration on portlet initialization"); 
        }
        
        pageManager = (PageManager)context.getAttribute(CommonPortletServices.CPS_PAGE_MANAGER_COMPONENT);
        if (pageManager == null)
        {
            throw new PortletException("Failed to find the Page Manager on portlet initialization");
        }
        
        userManager = (UserManager) context.getAttribute(CommonPortletServices.CPS_USER_MANAGER_COMPONENT);
        if (null == userManager)
        {
            throw new PortletException("Failed to find the user manager on portlet initialization");
        }                
        
        decorationFactory = (DecorationFactory)context.getAttribute(CommonPortletServices.CPS_DECORATION_FACTORY);
        if (null == decorationFactory)
        {
            throw new PortletException("Failed to find the Decoration Factory on portlet initialization");
        }                
    }

    @SuppressWarnings("unchecked")
    public void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException
    {
    	Space space = null;
    	SpaceBean spaceBean = null;
        String current = (String)PortletMessaging.receive(request, SpacesManager.MSG_TOPIC_SPACE_LIST, SpacesManager.MSG_SPACE_CHANGE);                
        if (current != null)
        {
            // FIXME: lookupSpace() can find system spaces only, not user space.
            //        So, what if a system space name is as same as a user space name?
        	space = spacesService.lookupSpace(current);
        	
        	if (space == null)
        	{
        	    space = spacesService.lookupUserSpace(current);
        	}
        }
        
        if (space != null)
        {
            spaceBean = new SpaceBean(space);
        }
        else
        {
        	spaceBean = new SpaceBean("", "");
    		spaceBean.setDescription("");
    		spaceBean.setTitle("");
    		spaceBean.setSecurityConstraint("");
    		spaceBean.setTheme(ThemeBean.getDefaultTheme(request, decorationFactory));
        }
        
        request.setAttribute("constraints", retrieveConstraints(request));
        request.setAttribute("themes", ThemeBean.retrieveThemes(request, decorationFactory, spaceBean.getTheme()));
        request.setAttribute(SpaceNavigator.ATTRIBUTE_SPACE, spaceBean);
        
        if (SpaceAdminUtils.isUserSpaceOwner(spaceBean, request) || SpaceAdminUtils.isUserSpaceAdmin(spaceBean, admin, request))
        {
            request.setAttribute("spaceEditable", Boolean.TRUE);
        }
        
        if (SpaceAdminUtils.isUserSpaceAdmin(spaceBean, admin, request))
        {
            request.setAttribute("spaceCreatable", Boolean.TRUE);
        }
        
        super.doView(request, response);        
    }
    
    protected List<String> retrieveConstraints(RenderRequest request) throws PortletException
    {
    	
    	List<String> result = (List<String>)request.getPortletSession().getAttribute("constraints");
    	if (result != null)
    		return result;    	
    	result = new LinkedList<String>();
    	result.add("");
        Iterator i;
		try 
		{			
			i = this.pageManager.getPageSecurity().getSecurityConstraintsDefs().iterator();
		} catch (Exception e) 
		{
			throw new PortletException(e);
		}
        while (i.hasNext())
        {
        	SecurityConstraintsDef def = (SecurityConstraintsDef)i.next();
        	result.add(def.getName());
        }
        request.getPortletSession().setAttribute("constraints", result);
    	return result;
    }
    
    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException,
    IOException
    {
        boolean isCanceled = (actionRequest.getParameter("cancelAction") != null);
    	String persisted = scrapeParameter(actionRequest, "spacePersisted");
    	boolean isNew = (persisted == null || persisted.equals("0") || persisted.equalsIgnoreCase("false"));
        String name = scrapeParameter(actionRequest, "spaceName");
        String title = scrapeParameter(actionRequest, "spaceTitle");
        if (title == null || "".equals(title.trim()))
        {
            title = name;
        }
        String description = scrapeParameter(actionRequest, "spaceDescription");
        String theme = scrapeParameter(actionRequest, "theme");
        String constraint = scrapeParameter(actionRequest, "securityConstraintRef");
        
        if (isCanceled)
        {
            try
            {
                Space space = null;
                
                if (!"".equals(name))
                {
                    space = spacesService.lookupSpace(name);
                    
                    if (space == null)
                    {
                        space = spacesService.lookupUserSpace(name);
                    }
                }
                
                String path = admin.getPortalURL(actionRequest, actionResponse, (space != null ? space.getPath() : "/"));
                actionResponse.sendRedirect(path);
                return;
            }
            catch (Exception e)
            {
                log.error("Failed to retrieve space.", e);
            }
        }
        
        if ("".equals(name))
        {
            return;
        }
        
        try
        {
            if (isNew)
        	{
        		// TODO: better validation
        		String spaceName = name.replace(' ', '_');                
                String templateFolderPath = actionRequest.getPreferences().getValue(SPACE_TEMPLATE, DEFAULT_SPACE_TEMPLATE);               
                if (!pageManager.folderExists(templateFolderPath))
                {
                    throw new PortletException("Space template folder does not exist: " + templateFolderPath);
                }
                
                Folder templateFolder = pageManager.getFolder(templateFolderPath);
                String owner = actionRequest.getUserPrincipal().getName();
                try
                {
                    userManager.getUser(owner);
                }
                catch (SecurityException notFoundEx)
                {
                    throw new PortletException("Space owner is not found: " + owner);
                }
                
                Space space = spacesService.createSpace(spaceName, owner, templateFolder, title, title, description, theme, constraint);
                // redirect
                String path = admin.getPortalURL(actionRequest, actionResponse, space.getPath());
                actionResponse.sendRedirect(path);
        	}
        	else
        	{
                String owner = scrapeParameter(actionRequest, "spaceOwner");
                // FIXME: lookupSpace() can find system spaces only, not user space.
                //        So, what if a system space name is as same as a user space name?
                Space space = spacesService.lookupSpace(name);
                
                if (space == null)
                {
                    space = spacesService.lookupUserSpace(name);
                }
                
                if (space != null)
                {
            		space.setDescription(description);
            		space.setTitle(title);
            		space.setShortTitle(title);
	                space.setTheme(theme);
	                
	                if (constraint.equals(""))
	                {
	                	String old = space.getSecurityConstraint();
	                	if (old != null)
	                	{
	                		if (!"".equals(old))
			                	space.setSecurityConstraint(constraint);		                		
	                	}
	                		
	                }
	                else
	                {
	                	space.setSecurityConstraint(constraint);
	                }
	                
	                if (owner != null && !owner.equals(space.getOwner()))
	                {
                        try
                        {
                            userManager.getUser(owner);
                        }
                        catch (SecurityException notFoundEx)
                        {
                            throw new PortletException("Space owner is not found: " + owner);
                        }
                        space.setOwner(owner);
	                }
	                
	                spacesService.storeSpace(space);
                }
                
                // redirect
                String path = admin.getPortalURL(actionRequest, actionResponse, space.getPath());
                actionResponse.sendRedirect(path);
        	}
            
            PortletMessaging.publish(actionRequest, SpacesManager.MSG_TOPIC_SPACE_LIST, SpacesManager.MSG_SPACE_CHANGE, name);                
            PortletMessaging.publish(actionRequest, SpacesManager.MSG_TOPIC_SPACE_NAV, SpacesManager.MSG_SPACE_CHANGE, name);
            PortletMessaging.publish(actionRequest, SpacesManager.MSG_TOPIC_PAGE_NAV, SpacesManager.MSG_SPACE_CHANGE, name);
            actionRequest.getPortletSession().removeAttribute(SpaceNavigator.ATTRIBUTE_SPACES, PortletSession.APPLICATION_SCOPE);
            actionRequest.getPortletSession().removeAttribute(SpaceNavigator.ATTRIBUTE_SPACE, PortletSession.APPLICATION_SCOPE);            	
        }
        catch (Exception e)
        {
            log.error("Failed to save space.", e);
        }
    }        
    
    private String scrapeParameter(ActionRequest request, String paramName)
    {
        String param = request.getParameter(paramName);
        if (param == null)
            param = "";
        param = param.trim();
        return param;
    }
    
}