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
import java.util.Collection;

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.jetspeed.PortalReservedParameters;
import org.apache.jetspeed.om.portlet.LocalizedField;
import org.apache.jetspeed.om.portlet.PortletDefinition;
import org.apache.jetspeed.request.RequestContext;
import org.apache.portals.bridges.common.GenericServletPortlet;

/**
 * Common Custom Preview Mode Portlet
 * 
 * @version $Id: CustomPreviewModePortlet.java 927332 2010-03-25 10:34:20Z woonsan $
 */
public class CustomPreviewModePortlet extends GenericServletPortlet
{
    
    public static final String PARAM_PREVIEW_PAGE = "PreviewPage";
    
    private static final PortletMode PREVIEW_MODE = new PortletMode("preview");
    
    private String defaultPreviewPage;
    
    private boolean allowPreferences;
    
    public void init(PortletConfig config) throws PortletException
    {
        super.init(config);
        
        this.defaultPreviewPage = config.getInitParameter(PARAM_PREVIEW_PAGE);
        
        String allowPreferencesString = config.getInitParameter(PARAM_ALLOW_PREFERENCES);
        
        if (allowPreferencesString != null)
        {
            allowPreferences = new Boolean(allowPreferencesString).booleanValue();
        }
    }
    
    protected void doDispatch(RenderRequest request, RenderResponse response) throws PortletException, IOException
    {
        if (!request.getWindowState().equals(WindowState.MINIMIZED))
        {
            PortletMode curMode = request.getPortletMode();
            
            if (PREVIEW_MODE.equals(curMode))
            {
                doPreview(request, response);
            }
            else
            {
                super.doDispatch(request, response);
            }
        }
    }
    
    protected void doPreview(RenderRequest request, RenderResponse response) throws PortletException, IOException 
    {
        String previewImage = "";
        
        RequestContext rc = (RequestContext) request.getAttribute(RequestContext.REQUEST_PORTALENV);
        PortletDefinition def = rc.getCurrentPortletWindow().getPortletDefinition();
        Collection<LocalizedField> colMetaData = null;
        
        if (def != null)
        {
            colMetaData = def.getMetadata().getFields(PortalReservedParameters.PORTLET_EXTENDED_DESCRIPTOR_PREVIEW_IMAGE);
        }
        
        if (colMetaData != null && !colMetaData.isEmpty())
        {
            previewImage = def.getApplication().getContextPath() + colMetaData.iterator().next().getValue();
        }
        
        request.setAttribute("previewImage", previewImage);
        
        String previewPage = this.defaultPreviewPage;
        
        // allow PreviewPage override by the request
        String reqPreviewPage = (String) request.getAttribute(PARAM_PREVIEW_PAGE);
        
        if (reqPreviewPage != null)
        {
            previewPage = reqPreviewPage;
        }
        
        if (allowPreferences == true)
        {
            PortletPreferences prefs = request.getPreferences();
            
            if (prefs != null && reqPreviewPage == null)
            {
                previewPage = prefs.getValue(PARAM_PREVIEW_PAGE, defaultPreviewPage);
            }
        }
        
        if (previewPage != null)
        {
            PortletContext context = getPortletContext();
            PortletRequestDispatcher rd = context.getRequestDispatcher(previewPage);
            rd.include(request, response);
        }
        
        return;
    }
    
}