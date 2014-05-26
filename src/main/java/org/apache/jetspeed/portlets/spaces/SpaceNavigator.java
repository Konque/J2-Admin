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
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.administration.PortalAdministration;
import org.apache.jetspeed.om.folder.Folder;
import org.apache.jetspeed.request.RequestContext;
import org.apache.jetspeed.spaces.Space;
import org.apache.jetspeed.spaces.Spaces;
import org.apache.jetspeed.spaces.SpacesException;
import org.apache.portals.bridges.common.GenericServletPortlet;
import org.apache.portals.messaging.PortletMessaging;

/**
 * Jetspeed Navigator
 *
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: SpaceNavigator.java 926730 2010-03-23 18:55:35Z woonsan $
 */
public class SpaceNavigator extends GenericServletPortlet
{
    public static final String ATTRIBUTE_SPACES = "spaces";
    public static final String ATTRIBUTE_SPACE = "space";
    public static final String ATTRIBUTE_IS_SPACES_ADMIN = "isSpacesAdmin";
    public static final String SPACE_EDIT_PATH_PREF = "spaceEditPath";
    public static final String SPACE_EDIT_PATH_DEFAULT = "/spaces.psml";
    
	private Spaces spacesService;
    private PortalAdministration admin;
    
    @Override
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
    }

    @Override
    public void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException
    {    
        String spaceName = (String)PortletMessaging.consume(request, SpacesManager.MSG_TOPIC_SPACE_NAV, SpacesManager.MSG_SPACE_CHANGE);
        if (spaceName != null)
        {
            request.getPortletSession().removeAttribute(SpaceNavigator.ATTRIBUTE_SPACES, PortletSession.APPLICATION_SCOPE);
            request.getPortletSession().removeAttribute(SpaceNavigator.ATTRIBUTE_SPACE, PortletSession.APPLICATION_SCOPE);
        }
        SpaceChangeContext scc = changeSpace(request, spacesService, spaceName);
        SpaceBean spaceBean = scc.getSpace();
        List<SpaceBean> spaceBeans = scc.getSpaces();
        request.setAttribute(SpaceNavigator.ATTRIBUTE_SPACE, spaceBean);
        request.setAttribute(SpaceNavigator.ATTRIBUTE_SPACES, spaceBeans);
        
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
    
    @Override
    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException, IOException
    {
        String navAction = actionRequest.getParameter("navAction");
        Space space = null;
        String spaceName = actionRequest.getParameter(SpaceNavigator.ATTRIBUTE_SPACE);
        if (spaceName != null)
        {
            space = getSpaceFromName(spaceName, actionRequest);
        }
        
        if ("addSpace".equals(navAction))
        {
            // TODO: make this link configurable. We need JetspeedLinks like in Jetspeed-1
            PortletMessaging.cancel(actionRequest, SpacesManager.MSG_TOPIC_SPACE_LIST, SpacesManager.MSG_SPACE_CHANGE);
            String spaceEditPath = actionRequest.getPreferences().getValue(SPACE_EDIT_PATH_PREF, SPACE_EDIT_PATH_DEFAULT);                           
            String path = admin.getPortalURL(actionRequest, actionResponse, spaceEditPath);
            actionResponse.sendRedirect(path);
            return;
        }
        else if ("editSpace".equals(navAction) && space != null)
        {
            if (space != null)
            {
                PortletMessaging.publish(actionRequest, SpacesManager.MSG_TOPIC_SPACE_LIST, SpacesManager.MSG_SPACE_CHANGE, spaceName);
                String spaceEditPath = actionRequest.getPreferences().getValue(SPACE_EDIT_PATH_PREF, SPACE_EDIT_PATH_DEFAULT);                           
                String path = admin.getPortalURL(actionRequest, actionResponse, spaceEditPath);
                actionResponse.sendRedirect(path);
                return;
            }
        }
        else if (space != null)
        {
            if (space != null)
            {
                String path = admin.getPortalURL(actionRequest, actionResponse, space.getPath());
                actionRequest.getPortletSession().setAttribute(SpaceNavigator.ATTRIBUTE_SPACE, new SpaceBean(space));     
                PortletMessaging.publish(actionRequest, SpacesManager.MSG_TOPIC_PAGE_NAV, SpacesManager.MSG_SPACE_CHANGE, spaceName);
                actionResponse.sendRedirect(path);
                return;
            }
        }
    }    
    
    protected Space getSpaceFromName(String spaceName, PortletRequest request)
    {
        List<Space> spaces = spacesService.listSpaces();
        if (request.getUserPrincipal() != null) 
        {
            Space home = spacesService.lookupUserSpace(request.getUserPrincipal().getName());
        	if (home != null)
        	{
        		if (home.getName().equals(spaceName)) // FIXME: name unique
        			return home;
        	}
        }
        Space space = null;
        for (Space sp: spaces)
        {
            if (sp.getName().equals(spaceName))
            {
                space = sp;
                break;
            }
        }
        return space;
    }
    
    protected static SpaceBean findSpaceByName(List<SpaceBean> spaces, String spaceName)
    {
        for (SpaceBean space : spaces)
        {
        	if (space.getName().equals(spaceName))
        	{
        		return space;
        	}
        }
        
        return null;
    }
    
    protected static UserSpaceBeanList createSpaceBeanList(Spaces spacesService, String userName, String env)
    {
        // TODO: use environment
        List<Space> sl = spacesService.listSpaces();
        UserSpaceBeanList spaces = new UserSpaceBeanList(userName);
        
        for (Space s : sl)
        {
            spaces.add(new SpaceBean(s));
        }
        
        return spaces;
    }
    
    @SuppressWarnings("unchecked")
    protected static SpaceChangeContext changeSpace(RenderRequest request, Spaces spacesService, String spaceName) throws PortletException
    {
        String userName = (request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : null);
        UserSpaceBeanList spaces = (UserSpaceBeanList) request.getPortletSession().getAttribute(SpaceNavigator.ATTRIBUTE_SPACES, PortletSession.APPLICATION_SCOPE);
        
        // Validating the spaces cache in session if it was for the same user
        if (spaces != null && !StringUtils.equals(spaces.getUserName(), userName))
        {
            request.getPortletSession().removeAttribute(SpaceNavigator.ATTRIBUTE_SPACES, PortletSession.APPLICATION_SCOPE);
            spaces = null;
        }
        
        if (spaces == null)
        {
            // TODO: use environment
            spaces = createSpaceBeanList(spacesService, userName, null);
            
            if (request.getUserPrincipal() != null)
            {
            	String username = request.getUserPrincipal().getName();
            	Space home = spacesService.lookupUserSpace(username);
            	
            	if (home != null)
            	{
            		if (home.getOwner() == null)
            		{
	            		try
	            		{
		            		home.setOwner(username);
		            		spacesService.storeSpace(home);
	            		}
	            		catch (SpacesException e)
	            		{
	            			throw new PortletException(e);
	            		}
            		}
            		
            		SpaceBean userHome = new SpaceBean(home);
            		userHome.setDescription(home.getDescription());
            		userHome.setTitle(home.getTitle());
            		userHome.setUserHomePath(home.getPath());
            		userHome.setUserHomeName(home.getName());
                	spaces.add(userHome);                		
            	}
            }
            
            request.getPortletSession().setAttribute(SpaceNavigator.ATTRIBUTE_SPACES, spaces, PortletSession.APPLICATION_SCOPE);        
        }
        
        boolean changed = false;
        SpaceBean space = (SpaceBean) request.getPortletSession().getAttribute(SpaceNavigator.ATTRIBUTE_SPACE, PortletSession.APPLICATION_SCOPE);
        
        if (space == null && spaceName != null)
        {
        	space = findSpaceByName(spaces, spaceName);
        	changed = (space != null);
        }
        
        // check if this space matches the current portal page path.
        RequestContext rc = (RequestContext) request.getAttribute(RequestContext.REQUEST_PORTALENV);
        String portalPagePath = rc.getPage().getPath();
        String portalPageFolderPath = StringUtils.substringBeforeLast(portalPagePath, Folder.PATH_SEPARATOR);
        boolean isRootSpace = StringUtils.isEmpty(portalPageFolderPath);
        
        if (isRootSpace)
        {
            for (SpaceBean spaceBean : spaces)
            {
                if (Folder.PATH_SEPARATOR.equals(spaceBean.getPath()))
                {
                    if (!spaceBean.equals(space))
                    {
                        space = spaceBean;
                        changed = true;
                    }
                    break;
                }
            }
        }
        else
        {
            for (SpaceBean spaceBean : spaces)
            {
                if (Folder.PATH_SEPARATOR.equals(spaceBean.getPath()))
                {
                    continue;
                }
                
                if (portalPageFolderPath.equals(spaceBean.getPath()) || portalPageFolderPath.startsWith(spaceBean.getPath() + "/"))
                {
                    if (!spaceBean.equals(space))
                    {
                        space = spaceBean;
                        changed = true;
                    }
                    break;
                }
            }
        }
        
        if (space == null && !spaces.isEmpty())
        {
            space = spaces.get(0);
        	changed = true;            
        }
        
        if (changed)
        {
            request.getPortletSession().setAttribute(SpaceNavigator.ATTRIBUTE_SPACE, space, PortletSession.APPLICATION_SCOPE);
        }
        
    	return new SpaceChangeContext(space, spaces);
    }
    
}