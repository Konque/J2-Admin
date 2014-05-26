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
package org.apache.jetspeed.portlets.openid;

import java.io.IOException;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.security.auth.Subject;

import org.apache.jetspeed.PortalReservedParameters;
import org.apache.jetspeed.request.RequestContext;
import org.apache.portals.applications.webcontent.portlet.IFrameGenericPortlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IFrame portlet variant that loads content only when the portal
 * has an active OpenID login.
 *
 * @author <a href="mailto:rwatler@apache.org">Randy Watler</a>
 * @version $Id$
 */
public class OpenIDIFramePortlet extends IFrameGenericPortlet
{
    private static final Logger log = LoggerFactory.getLogger(OpenIDLoginPortlet.class);

    public static final String SESSION_OPEN_ID_PROVIDER_ATTR_NAME = "sessionOpenIDProvider";
    public static final String REQUIRED_OPEN_ID_PROVIDER_ATTR_NAME = "requiredOpenIDProvider";
    public static final String REQUIRED_OPEN_ID_PROVIDER_LABEL_ATTR_NAME = "requiredOpenIDProviderLabel";

    private static final String REQUIRED_OPEN_ID_PROVIDER_LABEL_PREF_NAME = "REQUIREDOPENIDPROVIDERLABEL";
    private static final String REQUIRED_OPEN_ID_PROVIDER_PREF_NAME = "REQUIREDOPENIDPROVIDER";
    
    private static final String NOT_AVAILABLE_VIEW_PAGE_INIT_PARAM = "NotAvailableViewPage";

    private String notAvailableViewPage;
    
    /* (non-Javadoc)
     * @see org.apache.portals.applications.webcontent.portlet.IFrameGenericPortlet#init(javax.portlet.PortletConfig)
     */
    public void init(PortletConfig config) throws PortletException
    {
        super.init(config);
        notAvailableViewPage = config.getInitParameter(NOT_AVAILABLE_VIEW_PAGE_INIT_PARAM);
    }
    
    /* (non-Javadoc)
     * @see javax.portlet.GenericPortlet#destroy()
     */
    public void destroy()
    {
        notAvailableViewPage = null;
        super.destroy();
    }

    /* (non-Javadoc)
     * @see org.apache.portals.applications.webcontent.portlet.IFrameGenericPortlet#doView(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException
    {
        // get current session OpenID login state
        RequestContext requestContext = (RequestContext)request.getAttribute(PortalReservedParameters.REQUEST_CONTEXT_ATTRIBUTE);
        Subject sessionSubject = (Subject)requestContext.getSessionAttribute(PortalReservedParameters.SESSION_KEY_SUBJECT);
        String sessionOpenIDProvider = (String)requestContext.getSessionAttribute(PortalReservedParameters.SESSION_OPEN_ID_PROVIDER);
        PortletPreferences prefs = request.getPreferences();
        String requiredOpenIDProvider = prefs.getValue(REQUIRED_OPEN_ID_PROVIDER_PREF_NAME, null);
        String requiredOpenIDProviderLabel = prefs.getValue(REQUIRED_OPEN_ID_PROVIDER_LABEL_PREF_NAME, null);

        // check against configured requirements
        if ((sessionSubject == null) || ((requiredOpenIDProvider != null) && !requiredOpenIDProvider.equals(sessionOpenIDProvider)))
        {
            if (notAvailableViewPage != null)
            {
                // render not available page
                request.setAttribute(PARAM_VIEW_PAGE, notAvailableViewPage);
                request.setAttribute(SESSION_OPEN_ID_PROVIDER_ATTR_NAME, sessionOpenIDProvider);
                request.setAttribute(REQUIRED_OPEN_ID_PROVIDER_ATTR_NAME, requiredOpenIDProvider);
                request.setAttribute(REQUIRED_OPEN_ID_PROVIDER_LABEL_ATTR_NAME, requiredOpenIDProviderLabel);
                super.doView(request, response);
            }
            return;
        }
        
        // continue with IFrame generation
        super.doView(request, response);
    }
}
