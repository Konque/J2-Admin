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
package org.apache.jetspeed.portlets.localeselector;

import java.io.IOException;
import java.util.Locale;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.PortalReservedParameters;
import org.apache.jetspeed.request.RequestContext;
import org.apache.jetspeed.security.SecurityAttributes;
import org.apache.jetspeed.security.SecurityException;
import org.apache.jetspeed.security.User;
import org.apache.jetspeed.security.UserManager;
import org.apache.jetspeed.util.JetspeedLocale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the portlet to select user's preferred locale.
 * 
 * @author <a href="mailto:shinsuke@yahoo.co.jp">Shinsuke Sugaya</a>
 * @version $Id: LocaleSelectorPortlet.java 348264 2005-11-22 22:06:45Z taylor $
 */
public class LocaleSelectorPortlet extends GenericPortlet
{
    public static final String PREFERED_LOCALE_SESSION_KEY = "prefered_locale";
    
    static final Logger logger = LoggerFactory.getLogger(LocaleSelectorPortlet.class);

    private UserManager userManager;
    
    /* (non-Javadoc)
     * @see javax.portlet.Portlet#init(javax.portlet.PortletConfig)
     */
    public void init(PortletConfig config) throws PortletException
    {
        super.init(config);
        userManager = (UserManager)getPortletContext().getAttribute(CommonPortletServices.CPS_USER_MANAGER_COMPONENT);
        if (null == userManager)
        {
            throw new PortletException("Failed to find the User Manager on portlet initialization");
        }
    }

    /* (non-Javadoc)
     * @see javax.portlet.GenericPortlet#doView(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException
    {
        PortletContext context = getPortletContext();

        Locale locale = request.getLocale();
        if (locale == null)
        {
            locale = Locale.getDefault();
        }
        request.setAttribute("currentLocale", locale.toString());

        PortletRequestDispatcher rd = context.getRequestDispatcher("/WEB-INF/view/locale-list.jsp");
        rd.include(request, response);
    }

    /* (non-Javadoc)
     * @see javax.portlet.Portlet#processAction(javax.portlet.ActionRequest, javax.portlet.ActionResponse)
     */
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException
    {

        PortletSession session = request.getPortletSession();
        String language = request.getParameter(PREFERED_LOCALE_SESSION_KEY);

        if (language != null)
        {
            String[] localeArray = language.split("[-|_]");
            String country = "";
            String variant = "";
            for (int i = 0; i < localeArray.length; i++)
            {
                if (i == 0)
                {
                    language = localeArray[i];
                }
                else if (i == 1)
                {
                    country = localeArray[i];
                }
                else if (i == 2)
                {
                    variant = localeArray[i];
                }
            }

            Locale preferedLocale = new Locale(language, country, variant);

            if (request.getRemoteUser() != null)
            {
                // Set the prefered locale to user's perferences(persistent storage) if not anon user
                try
                {
                    User user = userManager.getUser(request.getRemoteUser());
                    // TODO if preferred lang or locale is defined in PLT.D, it's better to use it
                    SecurityAttributes secAttrs = user.getSecurityAttributes();
                    secAttrs.getAttribute(PortalReservedParameters.PREFERED_LOCALE_ATTRIBUTE, true).setStringValue(JetspeedLocale.convertLocaleToString(preferedLocale));
                    userManager.updateUser(user);
                }
                catch (SecurityException e)
                {
                    logger.error("Failed to update security attributes with preferred locale.", e);
                }
            }

            session.setAttribute(PortalReservedParameters.PREFERED_LOCALE_ATTRIBUTE, preferedLocale,
                    PortletSession.APPLICATION_SCOPE);
            RequestContext requestContext = (RequestContext) request
                    .getAttribute(PortalReservedParameters.REQUEST_CONTEXT_ATTRIBUTE);
            requestContext.setLocale(preferedLocale);
            requestContext.setSessionAttribute(PortalReservedParameters.PREFERED_LOCALE_ATTRIBUTE, preferedLocale);
        }

        return;
    }

}
