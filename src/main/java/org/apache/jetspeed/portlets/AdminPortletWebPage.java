/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.jetspeed.portlets;

import java.util.Enumeration;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.apache.commons.lang.BooleanUtils;
import org.apache.jetspeed.portlets.wicket.AbstractAdminWebApplication;
import org.apache.jetspeed.request.RequestContext;
import org.apache.wicket.Component;
import org.apache.wicket.IPageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;


/**
 * General Portlet functionality provided to Admin Portlets via inheritance in an abstract class.
 * <P>
 * <em>Note that some utility methods for portlet environment defined here 
 * can be unavailable in a sub component.</em> 
 * </P>
 * <P>
 * For example, if a panel in a web page has an event handler which uses the following example code:
 * <CODE><PRE>
 * String param = ((AdminPortletWebPage) getWebPage()).getInitParam("ViewPage");
 * </PRE></CODE>
 * The above code will work in this case. However, if the above code is included in the constructor of
 * the panel and the page should contain the panel at the initial time, the above code will fail.
 * It is simply because the web page object is not accessible during constructing the web page itself.
 * For that reason, developers should pay caution to use the utility methods defined here.
 * </P>
 * <P>
 * <EM>Note that it is desirable to use the utility methods defined in {@link AbstractAdminWebApplication}
 * instead of using this class. For example, you can code like the following example:
 * <CODE><PRE>
 * String param = ((AbstractAdminWebApplication) getApplication()).getInitParam("ViewPage");
 * </PRE></CODE>
 * The above code will work in the case mentioned earlier.
 * </P>
 * 
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: AdminPortletWebPage.java 929549 2010-03-31 14:25:45Z woonsan $
 */
public abstract class AdminPortletWebPage extends WebPage
{
    public static final String USER_ADMINISTRATION = AbstractAdminWebApplication.USER_ADMINISTRATION;
    
    protected AdminPortletWebPage()
    {
        super();
    }

    protected AdminPortletWebPage(final IModel<?> model)
    {
        super(model);
    }

    protected AdminPortletWebPage(final IPageMap pageMap)
    {
        super(pageMap);
    }

    protected AdminPortletWebPage(final IPageMap pageMap, final IModel<?> model)
    {
        super(pageMap, model);
    }

    protected AdminPortletWebPage(final PageParameters parameters)
    {
        super(parameters);
    }

    protected AdminPortletWebPage(final IPageMap pageMap, final PageParameters parameters)
    {
        super(pageMap, parameters);
    }

    /**
     * Delegates the invocation to {@link AbstractAdminWebApplication#getServiceLocator()}.
     * Please use the corresponding method of {@link AbstractAdminWebApplication} directly.
     */
    public JetspeedServiceLocator getServiceLocator()
    {
        return ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
    }
    
    /**
     * Delegates the invocation to {@link AbstractAdminWebApplication#getInitParam(String)}.
     * Please use the corresponding method of {@link AbstractAdminWebApplication} directly.
     */
    public String getInitParam(String paramKey)
    {
        return ((AbstractAdminWebApplication) getApplication()).getInitParam(paramKey);
    }

    /**
     * Delegates the invocation to {@link AbstractAdminWebApplication#getInitParamAsInteger(String)}.
     * Please use the corresponding method of {@link AbstractAdminWebApplication} directly.
     */
    public int getInitParamAsInteger(String paramKey)
    {
        return ((AbstractAdminWebApplication) getApplication()).getInitParamAsInteger(paramKey);
    }

    /**
     * Delegates the invocation to {@link AbstractAdminWebApplication#getInitParamAsBoolean(String)}.
     * Please use the corresponding method of {@link AbstractAdminWebApplication} directly.
     */
    public boolean getInitParamAsBoolean(String paramKey)
    {
        return ((AbstractAdminWebApplication) getApplication()).getInitParamAsBoolean(paramKey);
    }
    
    /**
     * Delegates the invocation to {@link AbstractAdminWebApplication#getPreferenceValue(String)}.
     * Please use the corresponding method of {@link AbstractAdminWebApplication} directly.
     */
    public String getPreference(String key)
    {
        return ((AbstractAdminWebApplication) getApplication()).getPreferenceValue(key);
    }
    
    /**
     * Delegates the invocation to {@link AbstractAdminWebApplication#getPreferenceValueAsInteger(String)}.
     * Please use the corresponding method of {@link AbstractAdminWebApplication} directly.
     */
    public int getPreferenceAsInteger(String key)
    {
        return ((AbstractAdminWebApplication) getApplication()).getPreferenceValueAsInteger(key);
    }

    /**
     * Delegates the invocation to {@link AbstractAdminWebApplication#getPreferenceValueAsBoolean(String)}.
     * Please use the corresponding method of {@link AbstractAdminWebApplication} directly.
     */
    public boolean getPreferenceAsBoolean(String key)
    {
        return ((AbstractAdminWebApplication) getApplication()).getPreferenceValueAsBoolean(key);
    }
    
    /**
     * Delegates the invocation to {@link AbstractAdminWebApplication#getPreferenceValue(String, String)}.
     * Please use the corresponding method of {@link AbstractAdminWebApplication} directly.
     */
    public String getPreference(String key,String defaultValue)
    {
        return ((AbstractAdminWebApplication) getApplication()).getPreferenceValue(key, defaultValue);
    }
    
    /**
     * Delegates the invocation to {@link AbstractAdminWebApplication#getPreferenceValues(String, String[])}.
     * Please use the corresponding method of {@link AbstractAdminWebApplication} directly.
     */
    public String [] getPreference(String key,String [] values)
    {
        return ((AbstractAdminWebApplication) getApplication()).getPreferenceValues(key, values);
    }
    
    /**
     * Delegates the invocation to {@link AbstractAdminWebApplication#getPortletRequest()}.
     * Please use the corresponding method of {@link AbstractAdminWebApplication} directly.
     */
    public PortletRequest getPortletRequest()
    {
        return ((AbstractAdminWebApplication) getApplication()).getPortletRequest();
    }
    
    /**
     * Delegates the invocation to {@link AbstractAdminWebApplication#getPortalRequestContext()}.
     * Please use the corresponding method of {@link AbstractAdminWebApplication} directly.
     */
    public RequestContext getPortalRequestContext()
    {
        return ((AbstractAdminWebApplication) getApplication()).getPortalRequestContext();
    }
    
    /**
     * Delegates the invocation to {@link AbstractAdminWebApplication#setTitle(String)}.
     * Please use the corresponding method of {@link AbstractAdminWebApplication} directly.
     */
    public void setTitle(String title)
    {
        ((AbstractAdminWebApplication) getApplication()).setTitle(title);
    }
    
    /**
     * Delegates the invocation to {@link AbstractAdminWebApplication#getTitle()}.
     * Please use the corresponding method of {@link AbstractAdminWebApplication} directly.
     */
    public String getTitle()
    {
        return ((AbstractAdminWebApplication) getApplication()).getTitle();
    }
    
    /**
     * Delegates the invocation to {@link AbstractAdminWebApplication#getPortletName()}.
     * Please use the corresponding method of {@link AbstractAdminWebApplication} directly.
     */
    public String getPortletName()
    {
        return ((AbstractAdminWebApplication) getApplication()).getPortletName();
    }
    
    /**
     * Delegates the invocation to {@link AbstractAdminWebApplication#getPortletResponse()}.
     * Please use the corresponding method of {@link AbstractAdminWebApplication} directly.
     */
    public PortletResponse getPortletResponse()
    {
        return ((AbstractAdminWebApplication) getApplication()).getPortletResponse();
    }
    
    /**
     * Delegates the invocation to {@link AbstractAdminWebApplication#getIPAddress()}.
     * Please use the corresponding method of {@link AbstractAdminWebApplication} directly.
     */
    public String getIPAddress()
    {
        return ((AbstractAdminWebApplication) getApplication()).getIPAddress();
    }
 
    /**
     * Delegates the invocation to {@link AbstractAdminWebApplication#getUserPrincipalName()}.
     * Please use the corresponding method of {@link AbstractAdminWebApplication} directly.
     */
    public String getAuthenticatedUsername()
    {
       return ((AbstractAdminWebApplication) getApplication()).getUserPrincipalName();
    }
    
    /**
     * Set visibilities of components in a page based on preferences.
     * The preference name for a component is prepended by the specified preferenceNamePrefix.
     * @param prefix
     * @param defaultVisibility
     */
    public void setVisibilitiesOfChildComponentsByPreferences(String prefix, boolean defaultVisibility)
    {
        PortletRequest request = ((AbstractAdminWebApplication) getApplication()).getPortletRequest();
        PortletPreferences prefs = request.getPreferences();
        
        for (Enumeration<String> prefNames = prefs.getNames(); prefNames.hasMoreElements(); )
        {
            String prefName = prefNames.nextElement();
            
            if (prefName.startsWith(prefix))
            {
                String componentName = prefName.substring(prefix.length());
                String prefValue = prefs.getValue(prefName, defaultVisibility ? "true" : "false");
                Component component = get(componentName);
                
                if (component != null)
                {
                    component.setVisibilityAllowed(true).setVisible(BooleanUtils.toBoolean(prefValue));
                }
            }
        }
    }
    
}