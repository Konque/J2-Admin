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
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.PortalReservedParameters;
import org.apache.jetspeed.administration.PortalAdministration;
import org.apache.jetspeed.portalsite.Menu;
import org.apache.jetspeed.portalsite.MenuElement;
import org.apache.jetspeed.portalsite.MenuOption;
import org.apache.jetspeed.portalsite.PortalSiteRequestContext;
import org.apache.jetspeed.request.RequestContext;
import org.apache.jetspeed.spaces.Spaces;
import org.apache.portals.bridges.common.GenericServletPortlet;

/**
 * Jetspeed Breadcrumb Menu
 *
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: SiteBreadcrumbMenu.java 929549 2010-03-31 14:25:45Z woonsan $
 */
public class SiteBreadcrumbMenu extends GenericServletPortlet
{
    public static final String PORTAL_SITE_REQUEST_CONTEXT_ATTR_KEY = PortalReservedParameters.PORTAL_SITE_REQUEST_CONTEXT_ATTR_KEY;
    
    private Spaces spacesService;
    private PortalAdministration admin;

    public void init(PortletConfig config) throws PortletException
    {
        super.init(config);
        PortletContext context = getPortletContext();
        spacesService = (Spaces) context.getAttribute(CommonPortletServices.CPS_SPACES_SERVICE);
        if (spacesService == null)
                throw new PortletException(
                        "Could not get instance of portal spaces service component");
        admin = (PortalAdministration) getPortletContext().getAttribute(
                CommonPortletServices.CPS_PORTAL_ADMINISTRATION);
        if (null == admin) { throw new PortletException(
                "Failed to find the Portal Administration on portlet initialization"); }
    }

    public void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException
    {    
        RequestContext rc = (RequestContext) request.getAttribute(RequestContext.REQUEST_PORTALENV);

        List<BreadcrumbMenuItem> breadcrumbs = new LinkedList<BreadcrumbMenuItem>();
        try
        {
            Locale preferredLocale = rc.getLocale();
            PortalSiteRequestContext siteRequestContext = (PortalSiteRequestContext)rc.getAttribute(PORTAL_SITE_REQUEST_CONTEXT_ATTR_KEY);
            Menu breadcrumbsMenu = siteRequestContext.getMenu("breadcrumbs");
            if (breadcrumbsMenu != null)
            {
                List<MenuElement> breadcrumbsMenuElements = breadcrumbsMenu.getElements();
                if (breadcrumbsMenuElements != null)
                {
	                for (MenuElement breadcrumbMenuElement : breadcrumbsMenuElements)
	                {
	                    if (breadcrumbMenuElement instanceof MenuOption)
	                    {
	                        MenuOption breadcrumbMenuOption = (MenuOption)breadcrumbMenuElement;
	                        String title = breadcrumbMenuOption.getTitle(preferredLocale);
	                        String url = admin.getPortalURL(request, response, breadcrumbMenuOption.getUrl());
	                        breadcrumbs.add(new BreadcrumbMenuItem(title, url));
	                    }
	                }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();            
        }
        request.setAttribute("breadcrumbs", breadcrumbs);
        
        try
        {
            super.doView(request, response);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }

    public class BreadcrumbMenuItem implements Serializable
    {
        private static final long serialVersionUID = 1L;
        private String title;
        private String path;
        
        public BreadcrumbMenuItem(String title, String path)
        {
            this.title = title;
            this.path = path;
        }
        
        public String getTitle()
        {
            return title;
        }
        
        public String getPath()
        {
            return path;
        }
    }
}