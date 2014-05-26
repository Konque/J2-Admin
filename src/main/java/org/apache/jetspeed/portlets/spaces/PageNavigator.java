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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.MimeResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.Jetspeed;
import org.apache.jetspeed.PortalReservedParameters;
import org.apache.jetspeed.administration.PortalAdministration;
import org.apache.jetspeed.container.url.BasePortalURL;
import org.apache.jetspeed.om.folder.Folder;
import org.apache.jetspeed.om.page.ContentPage;
import org.apache.jetspeed.om.page.Link;
import org.apache.jetspeed.om.page.Page;
import org.apache.jetspeed.page.PageManager;
import org.apache.jetspeed.portalsite.Menu;
import org.apache.jetspeed.portalsite.MenuElement;
import org.apache.jetspeed.portalsite.PortalSiteRequestContext;
import org.apache.jetspeed.portlet.HeaderPhaseSupportConstants;
import org.apache.jetspeed.request.RequestContext;
import org.apache.jetspeed.spaces.Spaces;
import org.apache.portals.bridges.common.GenericServletPortlet;
import org.apache.portals.messaging.PortletMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * Jetspeed Navigator
 *
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: PageNavigator.java 929556 2010-03-31 14:48:29Z woonsan $
 */
public class PageNavigator extends GenericServletPortlet
{
    
    /**
     * PORTAL_SITE_REQUEST_CONTEXT_ATTR_KEY - session portal site context attribute key
     */
    private static String PORTAL_SITE_SESSION_CONTEXT_ATTR_KEY = PortalReservedParameters.PORTAL_SITE_SESSION_CONTEXT_ATTR_KEY;

    /**
     * PORTAL_SITE_REQUEST_CONTEXT_ATTR_KEY - request portal site context attribute key
     */
    private static String PORTAL_SITE_REQUEST_CONTEXT_ATTR_KEY = PortalReservedParameters.PORTAL_SITE_REQUEST_CONTEXT_ATTR_KEY;
    
    public static final String DEFAULT_PAGES_MENU = "pages";
    public static final String DEFAULT_LINKS_MENU = "links";
    public static final String DEFAULT_SPACE_NAVS_MENU = "space-navigations";
    public static final String DEFAULT_SPACE_LINKS_MENU = "space-links";
    public static final String DEFAULT_TEMPLATE_PAGE = "/_template/new-user/min.psml";
    public static final String [] DEFAULT_MANAGEABLE_NODE_TYPES = { ".psml", "folder", ".link" }; 
    public static final String FOLDER_SPACE_FLAGS = "folderSpaceFlags";
    public static final String TEMPLATE_PAGE_NODES = "templatePageNodes";
    
    private static Logger log = LoggerFactory.getLogger(PageNavigator.class);
    
    private Spaces spacesService;
    private PortalAdministration admin;
    protected PageManager pageManager;
    private BasePortalURL baseUrlAccess = null;
    private String defaultMenu = DEFAULT_SPACE_NAVS_MENU;
    private String defaultLinksMenu = DEFAULT_SPACE_LINKS_MENU;
    
    protected String yuiScriptPath = "/javascript/yui/build/yui/yui-min.js";
    
    @Override
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
        pageManager = (PageManager)context.getAttribute(CommonPortletServices.CPS_PAGE_MANAGER_COMPONENT);
        if (null == pageManager)
        {
            throw new PortletException("Failed to find the Page Manager on portlet initialization");
        }
        try
        {
            baseUrlAccess = (BasePortalURL) Jetspeed.getComponentManager().getComponent("BasePortalURL");
        }
        catch (Exception e)
        {
            baseUrlAccess = null; // optional
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
    
    @Override
    public void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException
    {    
        String newSpace = (String)PortletMessaging.consume(request, SpacesManager.MSG_TOPIC_PAGE_NAV, SpacesManager.MSG_SPACE_CHANGE);
        if (newSpace != null)
        {
            request.getPortletSession().removeAttribute(SpaceNavigator.ATTRIBUTE_SPACES, PortletSession.APPLICATION_SCOPE);
            request.getPortletSession().removeAttribute(SpaceNavigator.ATTRIBUTE_SPACE, PortletSession.APPLICATION_SCOPE);
        }
        
        SpaceChangeContext scc = SpaceNavigator.changeSpace(request, spacesService, newSpace);
        SpaceBean spaceBean = scc.getSpace();
        List<SpaceBean> spaceBeans = scc.getSpaces();
        request.setAttribute(SpaceNavigator.ATTRIBUTE_SPACE, spaceBean);
        request.setAttribute(SpaceNavigator.ATTRIBUTE_SPACES, spaceBeans);        
        request.setAttribute(SpacesManager.MSG_TOPIC_PAGE_NAV, this);        
        request.setAttribute("spaceMenuElements", getSpaceMenuElements(spaceBean, request));
        request.setAttribute("spaceLinkElements", getSpaceLinkMenuElements(spaceBean, request));
        request.setAttribute("templatePages", getTemplatePageNodes(request));
        
        String [] manageableNodeTypes = DEFAULT_MANAGEABLE_NODE_TYPES;
        String manageableNodeTypesPref = request.getPreferences().getValue("manageableNodeTypes", null);
        if (!StringUtils.isBlank(manageableNodeTypesPref))
        {
            manageableNodeTypes = StringUtils.split(manageableNodeTypesPref, ", \t\r\n");
        }
        request.setAttribute("manageableNodeTypes", manageableNodeTypes);
        
        if (SpaceAdminUtils.isUserSpaceOwner(spaceBean, request) || SpaceAdminUtils.isUserSpaceAdmin(spaceBean, admin, request))
        {
            request.setAttribute("pageEditable", Boolean.TRUE);
        }
        
        super.doView(request, response);
    }
    
    @Override
    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException, IOException
    {
        String name = actionRequest.getParameter("name");
        String type = actionRequest.getParameter("type");
        String templatePage = StringUtils.defaultString(actionRequest.getParameter("templatePage"), null);
        
        SpaceBean space = (SpaceBean) actionRequest.getPortletSession().getAttribute(SpaceNavigator.ATTRIBUTE_SPACE, PortletSession.APPLICATION_SCOPE);
        
        if (space == null)
        {
            log.warn("Space not found in session.");
            return;
        }
        
        if (StringUtils.isBlank(name))
        {
            log.warn("Blank name to create a node of type " + type);
            return;
        }
        
        if (StringUtils.isBlank(type))
        {
            throw new PortletException("Blank node type: " + type);
        }
        
        if ((Page.DOCUMENT_TYPE.equals(type) || (Folder.FOLDER_TYPE.equals(type))) && StringUtils.isBlank(templatePage))
        {
            templatePage = actionRequest.getPreferences().getValue("defaultTemplatePage", null);
            
            if (StringUtils.isBlank(templatePage))
            {
                throw new PortletException("Invalid template page: " + templatePage);
            }
        }
        
        try
        {
            RequestContext requestContext = (RequestContext) actionRequest.getAttribute(RequestContext.REQUEST_PORTALENV);
            ContentPage contentPage = requestContext.getPage();
            
            String spacePath = space.getPath();
            String contentPagePath = contentPage.getPath();
            String contentFolderPath = StringUtils.defaultIfEmpty(StringUtils.substringBeforeLast(contentPagePath, "/"), "/");
            String nodeName = name.replace(' ', '_');
            String nodePath = null;
            
            if (contentFolderPath.startsWith(spacePath))
            {
                nodePath = StringUtils.removeEnd(contentFolderPath, "/") + "/" + StringUtils.removeStart(nodeName, "/");
            }
            else
            {
                nodePath = StringUtils.removeEnd(spacePath, "/") + "/" + StringUtils.removeStart(nodeName, "/");
            }
            
            if (Page.DOCUMENT_TYPE.equals(type))
            {
                String path = nodePath + Page.DOCUMENT_TYPE;
                Page source = pageManager.getPage(templatePage);
                Page newPage = pageManager.copyPage(source, path, false);
                newPage.setTitle(name);
                pageManager.updatePage(newPage);
                
                requestContext.setSessionAttribute(PORTAL_SITE_SESSION_CONTEXT_ATTR_KEY, null);
                
                String redirect = admin.getPortalURL(actionRequest, actionResponse, path);
                actionResponse.sendRedirect(redirect);
            }
            else if (Folder.FOLDER_TYPE.equals(type))
            {
                String path = nodePath;
                Folder folder = pageManager.newFolder(path);
                folder.setTitle(name);
                pageManager.updateFolder(folder);
                
                String defaultPagePath = folder.getPath() + "/" + Folder.FALLBACK_DEFAULT_PAGE;
                Page source = pageManager.getPage(templatePage);
                Page newPage = pageManager.copyPage(source, defaultPagePath, false);
                pageManager.updatePage(newPage);
                
                requestContext.setSessionAttribute(PORTAL_SITE_SESSION_CONTEXT_ATTR_KEY, null);
            }
            else if (Link.DOCUMENT_TYPE.equals(type))
            {
                String path = nodePath + Link.DOCUMENT_TYPE;
                Link link = pageManager.newLink(path);
                link.setTitle(name);
                pageManager.updateLink(link);
                
                requestContext.setSessionAttribute(PORTAL_SITE_SESSION_CONTEXT_ATTR_KEY, null);
            }
        }
        catch (Exception e)
        {
            log.error("Failed to update page.", e);
        }
    }
    
    public String getAbsoluteUrl(String relativePath, RenderResponse renderResponse, RequestContext rc)
    {
        // only rewrite a non-absolute url
        if (relativePath != null && relativePath.indexOf("://") == -1 && relativePath.indexOf("mailto:") == -1)            
        {
            HttpServletRequest request = rc.getRequest();
            StringBuffer path = new StringBuffer();
            
            if ( !rc.getPortalURL().isRelativeOnly() )
            {
                if (this.baseUrlAccess == null)
                {
                    path.append(request.getScheme()).append("://").append(request.getServerName()).append(":").append(request.getServerPort());
                }
                else
                {
                    path.append(baseUrlAccess.getServerScheme()).append("://").append(baseUrlAccess.getServerName()).append(":").append(baseUrlAccess.getServerPort());
                }
            }
            
            return renderResponse.encodeURL(path.append(request.getContextPath()).append(request.getServletPath()).append(relativePath).toString());
        }
        else
        {
            return relativePath;
        }
    }
    
    private List<MenuElement> getSpaceMenuElements(SpaceBean space, PortletRequest request)
    {
        List<MenuElement> spaceMenuElements = null;
        
        try
        {
            RequestContext rc = (RequestContext) request.getAttribute(RequestContext.REQUEST_PORTALENV);
            PortalSiteRequestContext psrc = (PortalSiteRequestContext) rc.getAttribute(PORTAL_SITE_REQUEST_CONTEXT_ATTR_KEY);

            Menu spaceMenu = null;
            
            String menuName = request.getPreferences().getValue("Menu", defaultMenu);
            
            try
            {
                spaceMenu = psrc.getMenu(menuName);
            }
            catch (Exception e)
            {
                log.error("Failed to retrieve menu.", e);
            }
            
            if (spaceMenu == null)
            {
                if (!DEFAULT_PAGES_MENU.equals(menuName))
                {
                    spaceMenu = psrc.getMenu(DEFAULT_PAGES_MENU);
                    
                    if (spaceMenu != null)
                    {
                        defaultMenu = DEFAULT_PAGES_MENU;
                    }
                }
            }
            
            if (spaceMenu != null)
            {
                spaceMenuElements = spaceMenu.getElements();
            }
        }
        catch (Exception e)
        {
            log.error("Failed to retrieve space menu elements.", e);
        }
        
        if (spaceMenuElements == null)
        {
            spaceMenuElements = Collections.emptyList();
        }
        
        return spaceMenuElements;
    }
    
    private List<MenuElement> getSpaceLinkMenuElements(SpaceBean space, PortletRequest request)
    {
        List<MenuElement> spaceLinkMenuElements = null;
        
        try
        {
            RequestContext rc = (RequestContext) request.getAttribute(RequestContext.REQUEST_PORTALENV);
            PortalSiteRequestContext psrc = (PortalSiteRequestContext) rc.getAttribute(PORTAL_SITE_REQUEST_CONTEXT_ATTR_KEY);

            Menu spaceLinksMenu = null;
            
            String linksMenuName = request.getPreferences().getValue("LinkMenu", defaultLinksMenu);
            
            try
            {
                spaceLinksMenu = psrc.getMenu(linksMenuName);
            }
            catch (Exception e)
            {
                log.error("Failed to retrieve menu.", e);
            }
            
            if (spaceLinksMenu == null)
            {
                if (!DEFAULT_LINKS_MENU.equals(linksMenuName))
                {
                    spaceLinksMenu = psrc.getMenu(DEFAULT_LINKS_MENU);
                    
                    if (spaceLinksMenu != null)
                    {
                        defaultLinksMenu = DEFAULT_LINKS_MENU;
                    }
                }
            }
            
            if (spaceLinksMenu != null)
            {
                spaceLinkMenuElements = spaceLinksMenu.getElements();
            }
        }
        catch (Exception e)
        {
            log.error("Failed to retrieve space menu elements.", e);
        }
        
        if (spaceLinkMenuElements == null)
        {
            spaceLinkMenuElements = Collections.emptyList();
        }
        
        return spaceLinkMenuElements;
    }
    
    private List<NodeBean> getTemplatePageNodes(PortletRequest request)
    {
        List<NodeBean> templatePageNodes = (List<NodeBean>) request.getPortletSession(true).getAttribute(TEMPLATE_PAGE_NODES);

        if (templatePageNodes == null)
        {
            templatePageNodes = new ArrayList<NodeBean>();
            
            String templatePages = request.getPreferences().getValue("templatePages", null);
            
            if (!StringUtils.isBlank(templatePages))
            {
                String [] templatePagePaths = StringUtils.split(templatePages, ", \t\r\n");
                
                for (String templatePagePath : templatePagePaths)
                {
                    try
                    {
                        Page templatePage = pageManager.getPage(templatePagePath);
                        templatePageNodes.add(new NodeBean(templatePage));
                    }
                    catch (SecurityException e)
                    {
                        // ignore security exception when retrieving template pages.
                    }
                    catch (Exception e)
                    {
                        log.warn("Invalid template page path: " + templatePagePath + ". {}", e.toString());
                    }
                }
            }
            
            request.getPortletSession().setAttribute(TEMPLATE_PAGE_NODES, templatePageNodes);
        }
        
        return templatePageNodes;
    }
    
}
