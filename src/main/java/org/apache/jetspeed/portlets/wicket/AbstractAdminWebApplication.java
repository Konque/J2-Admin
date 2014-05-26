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
package org.apache.jetspeed.portlets.wicket;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;

import javax.portlet.PortletConfig;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderResponse;

import org.apache.jetspeed.portlets.JetspeedServiceLocator;
import org.apache.wicket.RequestContext;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.portlet.PortletRequestContext;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.UrlResourceStream;
import org.apache.wicket.util.resource.locator.ResourceStreamLocator;

/**
 * Abstract Admin Wicket Application
 * <P>
 * This abstract class loads customized templates from /WEB-INF/templates/.
 * Also, this class contains many useful unitlity methods for portlet-based applications.
 * </P>
 * 
 * @author <a href="mailto:woonsan@apache.org">Woonsan Ko</a>
 * @version $Id$
 */
public abstract class AbstractAdminWebApplication extends WebApplication
{
    public static final String USER_ADMINISTRATION = "J2 User Administration";
    
    private static final String [] EMPTY_STRING_ARRAY = {};
    
    protected JetspeedServiceLocator serviceLocator;

	protected void init()
	{
        super.init();

		// instruct the application to use our custom resource stream locator
		getResourceSettings().setResourceStreamLocator(new TemplatesResourceStreamLocator());
	}
	
    private class TemplatesResourceStreamLocator extends ResourceStreamLocator
    {
        
        protected String templatesPath = "/WEB-INF/templates/";

        public void setTemplatesPath(String templatesPath)
        {
            this.templatesPath = templatesPath;
        }
        
        public String getTemplatesPath()
        {
            return this.templatesPath;
        }

        /**
         * @see org.apache.wicket.util.resource.locator.ResourceStreamLocator#locate(java.lang.Class,
         *      java.lang.String)
         */
        public IResourceStream locate(Class<?> clazz, String path)
        {
            String location = this.templatesPath + path;
            
            try
            {
                // try to load the resource from the web context
                URL url = getServletContext().getResource(location);
                
                if (url != null)
                {
                    return new UrlResourceStream(url);
                }
            }
            catch (MalformedURLException e)
            {
                throw new WicketRuntimeException(e);
            }

            // resource not found; fall back on class loading
            return super.locate(clazz, path);
        }

    }

    // Utility methods for Portlet APIs
    
    public PortletConfig getPortletConfig()
    {
        return ((PortletRequestContext) RequestContext.get()).getPortletConfig();
    }
    
    public PortletRequest getPortletRequest()
    {
        return ((PortletRequestContext) RequestContext.get()).getPortletRequest();
    }
    
    public org.apache.jetspeed.request.RequestContext getPortalRequestContext()
    {
        return (org.apache.jetspeed.request.RequestContext) getPortletRequest().getAttribute(org.apache.jetspeed.request.RequestContext.REQUEST_PORTALENV);
    }
    
    public PortletResponse getPortletResponse()
    {
        return ((PortletRequestContext) RequestContext.get()).getPortletResponse();
    }

    public String getPortletName()
    {
        return getPortletConfig().getPortletName();
    }
    
    public void setTitle(String title)
    {
        PortletResponse response = getPortletResponse();
        
        if (response instanceof RenderResponse) 
        {
            RenderResponse renderResponse = (RenderResponse) response;
            renderResponse.setTitle(title);
        }
    }

    public String getTitle()
    {
        PortletRequest request = ((PortletRequestContext) RequestContext.get()).getPortletRequest();
        String title = getPortletConfig().getResourceBundle(request.getLocale()).getString("javax.portlet.title");
        
        if (title == null)
        {
            title = getPortletName();
        }
        
        return title;
    }
    
    public String getInitParam(String paramKey)
    {
        return getPortletConfig().getInitParameter(paramKey);
    }

    public int getInitParamAsInteger(String paramKey)
    {
        String paramValue = getInitParam(paramKey);
        return Integer.parseInt(paramValue);
    }

    public boolean getInitParamAsBoolean(String paramKey)
    {
        String paramValue = getInitParam(paramKey);
        return Boolean.parseBoolean(paramValue);
    }
    
    public String getPreferenceValue(String key, String defaultValue)
    {
        String preferenceValue = null;
        PortletPreferences prefs = getPortletRequest().getPreferences();
        
        if (prefs.getMap().containsKey(key))
        {
            preferenceValue = prefs.getValue(key, defaultValue);
        }
        
        return preferenceValue;
    }
    
    public String getPreferenceValue(String key)
    {
        return getPreferenceValue(key, (String) null);
    }
    
    public String [] getPreferenceValues(String key, String [] defaultValues)
    {
        String [] preferenceValues = null;
        PortletPreferences prefs = getPortletRequest().getPreferences();
        
        if (prefs.getMap().containsKey(key))
        {
            preferenceValues = prefs.getValues(key, defaultValues);
        }
        
        return preferenceValues;
    }
    
    public String [] getPreferenceValues(String key)
    {
        return getPreferenceValues(key, EMPTY_STRING_ARRAY);
    }
    
    public int getPreferenceValueAsInteger(String key)
    {
        String preferenceValue = getPreferenceValue(key);
        return (preferenceValue != null ? Integer.parseInt(preferenceValue) : 0);
    }

    public boolean getPreferenceValueAsBoolean(String key)
    {
        String preferenceValue = getPreferenceValue(key);
        return (preferenceValue != null ? Boolean.parseBoolean(preferenceValue) : false);
    }
    
    public Principal getUserPrincipal()
    {
        return getPortletRequest().getUserPrincipal();
    }
    
    public String getUserPrincipalName()
    {
        Principal p = getUserPrincipal();
        return (p == null ? "" : p.getName());
    }
    
    // Utility methods for Jetspeed APIs
    
    public JetspeedServiceLocator getServiceLocator()
    {
        if (serviceLocator == null)
        {
            serviceLocator = new JetspeedServiceLocator();
        }
        
        return serviceLocator;
    }
    
    public void setServiceLocator(JetspeedServiceLocator serviceLocator)
    {
        this.serviceLocator = serviceLocator;
    }

    public String getIPAddress()
    {
        org.apache.jetspeed.request.RequestContext context = 
            (org.apache.jetspeed.request.RequestContext) getPortletRequest().getAttribute(org.apache.jetspeed.request.RequestContext.REQUEST_PORTALENV);
        return (context == null ? "" : context.getRequest().getRemoteAddr());
    }
 
}
