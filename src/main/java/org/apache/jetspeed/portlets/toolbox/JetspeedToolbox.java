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
package org.apache.jetspeed.portlets.toolbox;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.MimeResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.JetspeedActions;
import org.apache.jetspeed.administration.PortalAdministration;
import org.apache.jetspeed.decoration.DecorationFactory;
import org.apache.jetspeed.layout.PageLayoutComponent;
import org.apache.jetspeed.om.page.ContentFragment;
import org.apache.jetspeed.om.page.ContentPage;
import org.apache.jetspeed.portlet.HeaderPhaseSupportConstants;
import org.apache.jetspeed.request.RequestContext;
import org.apache.portals.bridges.common.GenericServletPortlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * Jetspeed Toolbox
 *
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: JetspeedToolbox.java 1093937 2011-04-16 06:37:16Z woonsan $
 */
public class JetspeedToolbox extends GenericServletPortlet
{
    static Logger log = LoggerFactory.getLogger(JetspeedToolbox.class);
    
    protected PortalAdministration portalAdministration;
    protected PageLayoutComponent pageLayoutComponent;
    protected DecorationFactory decorationFactory;
    protected String yuiScriptPath = "/javascript/yui/build/yui/yui-min.js";
    
    public void init(PortletConfig config) throws PortletException
    {
        super.init(config);
        
        PortletContext context = getPortletContext();
        
        portalAdministration = (PortalAdministration) getPortletContext().getAttribute(CommonPortletServices.CPS_PORTAL_ADMINISTRATION);
        
        pageLayoutComponent = (PageLayoutComponent) context.getAttribute(CommonPortletServices.CPS_PAGE_LAYOUT_COMPONENT);
        
        if (pageLayoutComponent == null)
        {
            throw new PortletException("Failed to find the Page Layout Component on portlet initialization");
        }        
        
        decorationFactory = (DecorationFactory)context.getAttribute(CommonPortletServices.CPS_DECORATION_FACTORY);
        
        if (decorationFactory == null)
        {
            throw new PortletException("Failed to find the Decoration Factory on portlet initialization");
        }        
        
        String param = config.getInitParameter("yuiScriptPath");
        
        if (param != null) {
            yuiScriptPath = param;
        }
    }
    
    @Override
    protected void doHeaders(RenderRequest request, RenderResponse response) {
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
    
    @Override
    public void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException
    {
        request.setAttribute("defaultCategory", getDefaultCategory(request, ""));
        List<String> categories = retrieveCategories(request);
        request.setAttribute("categories", categories);
        Map<String, String> categoryKeywordsMap = retrieveCategoryKeywordsMap(categories, request);
        request.setAttribute("categoryKeywords", categoryKeywordsMap);
        
        request.setAttribute("layouts", LayoutBean.retrieveLayouts(request, decorationFactory));
        request.setAttribute("themes", ThemeBean.retrieveThemes(request, decorationFactory));
        PortletPreferences prefs = request.getPreferences();
        request.setAttribute("prefs", prefs.getMap());
        
        boolean userInAdminRole = portalAdministration.isUserInAdminRole(request);
        request.setAttribute("userInAdminRole", userInAdminRole ? Boolean.TRUE : Boolean.FALSE);
        
        boolean hasEditAccess = false;
        
        try
        {
            RequestContext requestContext = (RequestContext) request.getAttribute(RequestContext.REQUEST_PORTALENV);
            ContentPage contentPage = requestContext.getPage();
            contentPage.checkAccess(JetspeedActions.EDIT);
            hasEditAccess = true;
        }
        catch(Exception ignore)
        {
        }
        
        request.setAttribute("editAccess", hasEditAccess ? Boolean.TRUE : Boolean.FALSE);
        
        super.doView(request, response);        
    }
    
    @Override
    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException,
    IOException
    {
        String theme =  actionRequest.getParameter("theme");
        String layout =  actionRequest.getParameter("layout");
        RequestContext requestContext = (RequestContext) actionRequest.getAttribute(RequestContext.REQUEST_PORTALENV);
        
        if (theme != null)
        {
            try
            {
                ContentPage page = requestContext.getPage();
                pageLayoutComponent.updateDefaultDecorator(page, theme, ContentFragment.LAYOUT);
                pageLayoutComponent.updateDefaultDecorator(page, theme, ContentFragment.LAYOUT);
                actionRequest.getPortletSession().removeAttribute("themes");
            }
            catch (Exception e)
            {
                log.error("Page has not been updated.", e);
            }            
        }
        
        if (layout != null)
        {
            try
            {
                ContentFragment layoutFragment = requestContext.getPage().getNonTemplateRootFragment();
                pageLayoutComponent.updateName(layoutFragment, layout);
                actionRequest.getPortletSession().removeAttribute("layouts");
            }
            catch (Exception e)
            {
                log.error("Page has not been updated.", e);
            }            
        }
    }
    
    protected String getDefaultCategory(PortletRequest request, String defaultValue) throws PortletException
    {
        return request.getPreferences().getValue("DefaultCategory", defaultValue);
    }
    
    @SuppressWarnings("unchecked")
    protected List<String> retrieveCategories(PortletRequest request) throws PortletException
    {
        List<String> categories = (List) request.getPortletSession().getAttribute("categories");
        
        if (categories != null)
        {
            return categories;
        }
        
        String cats = request.getPreferences().getValue("Categories", null);
        
        if (cats == null)
        {
            throw new PortletException("No categories defined, please add categories via edit mode.");
        }
        
        categories = Arrays.asList(StringUtils.split(cats, ", \t\r\n"));
        
        request.getPortletSession().setAttribute("categories", categories);
        
        return categories;
    }
    
    protected Map<String, String> retrieveCategoryKeywordsMap(List<String> categories, PortletRequest request) throws PortletException
    {
        Map<String, String> keywordsMap = new HashMap<String, String>();
        PortletPreferences prefs = request.getPreferences();
        
        for (String category : categories)
        {
            String keywords = prefs.getValue("Keywords:" + category, "");
            keywordsMap.put(category, keywords);
        }
        
        return keywordsMap;
    }
    
}
