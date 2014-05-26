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

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.MimeResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.portlet.HeaderPhaseSupportConstants;
import org.apache.jetspeed.portlets.toolbox.ThemeBean;
import org.apache.jetspeed.request.RequestContext;
import org.apache.jetspeed.spaces.Space;
import org.apache.jetspeed.spaces.Spaces;
import org.apache.jetspeed.spaces.SpacesException;
import org.apache.portals.bridges.common.GenericServletPortlet;
import org.apache.portals.messaging.PortletMessaging;
import org.w3c.dom.Element;

/**
 * Spaces List 
 *
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: $
 */
public class SpacesList extends GenericServletPortlet
{
    private Spaces spacesService;
    
    protected String yuiScriptPath = "/javascript/yui/build/yui/yui-min.js";
    
    public void init(PortletConfig config) throws PortletException
    {
        super.init(config);
        PortletContext context = getPortletContext();
        spacesService = (Spaces) context.getAttribute(CommonPortletServices.CPS_SPACES_SERVICE);
        if (spacesService == null)
        {
                throw new PortletException(
                        "Could not get instance of portal spaces service component");
        }
        
        String param = config.getInitParameter("yuiScriptPath");
        
        if (param != null) 
        {
            yuiScriptPath = param;
        }
    }
    
    @Override
    protected void doHeaders(RenderRequest request, RenderResponse response) 
    {
        super.doHeaders(request, response);
        RequestContext rc = (RequestContext) request.getAttribute(RequestContext.REQUEST_PORTALENV);
        Element headElem = response.createElement("script");
        headElem.setAttribute("language", "javascript");
        String scriptPath = rc.getRequest().getContextPath() + yuiScriptPath;
        headElem.setAttribute("id", HeaderPhaseSupportConstants.HEAD_ELEMENT_CONTRIBUTION_ELEMENT_ID_YUI_LIBRARY_INCLUDE);
        headElem.setAttribute("src", scriptPath);
        headElem.setAttribute("type", "text/javascript");
        response.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, headElem);
    }
    
    @SuppressWarnings("unchecked")
    public void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException
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
            spaces = SpaceNavigator.createSpaceBeanList(spacesService, userName, null);
            request.getPortletSession().setAttribute(SpaceNavigator.ATTRIBUTE_SPACES, spaces);        
        }       
        request.setAttribute(SpaceNavigator.ATTRIBUTE_SPACES, spaces);
        SpaceBean space = (SpaceBean) request.getPortletSession().getAttribute(SpaceNavigator.ATTRIBUTE_SPACE, PortletSession.APPLICATION_SCOPE);        
        request.setAttribute(SpaceNavigator.ATTRIBUTE_SPACE, space);
        try
        {
            super.doView(request, response);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }

	@Override
	public void processAction(ActionRequest request, ActionResponse response)
			throws PortletException, IOException 
	{
		String spaceName = request.getParameter("edit");
		if (spaceName != null)
		{
            PortletMessaging.publish(request, SpacesManager.MSG_TOPIC_SPACE_LIST, SpacesManager.MSG_SPACE_CHANGE, spaceName);
        	ThemeBean.clearThemesSession(request);            
		}
		else
		{
			spaceName = request.getParameter("delete");
			if (spaceName != null)
			{
				Space space = spacesService.lookupSpace(spaceName);
				
				if (space != null)
				{
					try
					{
						spacesService.deleteSpace(space);
						request.getPortletSession().removeAttribute(SpaceNavigator.ATTRIBUTE_SPACES, PortletSession.APPLICATION_SCOPE);
			            PortletMessaging.cancel(request, SpacesManager.MSG_TOPIC_SPACE_LIST, SpacesManager.MSG_SPACE_CHANGE);
		                PortletMessaging.cancel(request, SpacesManager.MSG_TOPIC_SPACE_NAV, SpacesManager.MSG_SPACE_CHANGE);
		                PortletMessaging.cancel(request, SpacesManager.MSG_TOPIC_PAGE_NAV, SpacesManager.MSG_SPACE_CHANGE);
		                request.getPortletSession().removeAttribute(SpaceNavigator.ATTRIBUTE_SPACES, PortletSession.APPLICATION_SCOPE);
		                request.getPortletSession().removeAttribute(SpaceNavigator.ATTRIBUTE_SPACE, PortletSession.APPLICATION_SCOPE);
		            	ThemeBean.clearThemesSession(request);		                
					}
					catch (SpacesException e)
					{
						throw new PortletException(e);
					}
				}
			}
			else
			{
				String add = request.getParameter("addspace");
				if (add != null)
				{
		            PortletMessaging.cancel(request, SpacesManager.MSG_TOPIC_SPACE_LIST, SpacesManager.MSG_SPACE_CHANGE);
		        	ThemeBean.clearThemesSession(request);		            
				}
			}
		}
	}
    
}