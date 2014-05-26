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
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.jetspeed.JetspeedActions;
import org.apache.jetspeed.PortalReservedParameters;
import org.apache.jetspeed.openid.OpenIDConstants;
import org.apache.jetspeed.openid.OpenIDRegistrationConfiguration;
import org.apache.jetspeed.request.RequestContext;
import org.apache.portals.bridges.common.GenericServletPortlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Portlet to hold new user registration preferences
 * for OpenID login.
 *
 * @author <a href="mailto:rwatler@apache.org">Randy Watler</a>
 * @version $Id$
 */
public class OpenIDLoginPortlet extends GenericServletPortlet
{
    private static final Logger log = LoggerFactory.getLogger(OpenIDLoginPortlet.class);

    public static final String PROVIDER_LABELS_INIT_PARAM_NAME = "providerLabels";
    public static final String PROVIDER_DOMAINS_INIT_PARAM_NAME = "providerDomains";
    public static final String ENABLE_OPEN_ID_ENTRY_INIT_PARAM_NAME = "enableOpenIDEntry";
    
    public static final String PROVIDER_LABELS_PREF_NAME = "providerLabels";
    public static final String PROVIDER_DOMAINS_PREF_NAME = "providerDomains";
    public static final String ENABLE_OPEN_ID_ENTRY_PREF_NAME = "enableOpenIDEntry";

    public static final String ENABLE_REGISTRATION_CONFIG_PREF_NAME = "enableRegistrationConfig";
    public static final String ENABLE_REGISTRATION_PREF_NAME = "enableRegistration";
    public static final String REGISTRATION_USER_TEMPLATE_PREF_NAME = "newUserTemplateDirectory";
    public static final String REGISTRATION_SUBSITE_ROOT_PREF_NAME = "subsiteRootFolder";
    public static final String REGISTRATION_ROLES_PREF_NAME = "roles";
    public static final String REGISTRATION_GROUPS_PREF_NAME = "groups";
    public static final String REGISTRATION_PROFILER_RULE_NAMES_PREF_NAME = "rulesNames";
    public static final String REGISTRATION_PROFILER_RULE_VALUES_PREF_NAME = "rulesValues";
    
    public static final String SAVE_ACTION_PARAM_NAME = "save";
    
    public static final String PROVIDER_BUTTONS_ATTR_NAME = "providerButtons";
    public static final String ENABLE_OPEN_ID_ENTRY_ATTR_NAME = "enableOpenIDEntry";

    private static final List<String> PREF_NAMES = Arrays.asList(new String[]{PROVIDER_LABELS_PREF_NAME,
                                                                              PROVIDER_DOMAINS_PREF_NAME,
                                                                              ENABLE_OPEN_ID_ENTRY_PREF_NAME,
                                                                              ENABLE_REGISTRATION_CONFIG_PREF_NAME,
                                                                              ENABLE_REGISTRATION_PREF_NAME,
                                                                              REGISTRATION_USER_TEMPLATE_PREF_NAME,
                                                                              REGISTRATION_SUBSITE_ROOT_PREF_NAME,
                                                                              REGISTRATION_ROLES_PREF_NAME,
                                                                              REGISTRATION_GROUPS_PREF_NAME,
                                                                              REGISTRATION_PROFILER_RULE_NAMES_PREF_NAME,
                                                                              REGISTRATION_PROFILER_RULE_VALUES_PREF_NAME});
    private static final List<String> BOOLEAN_PREF_NAMES = Arrays.asList(new String[]{ENABLE_OPEN_ID_ENTRY_PREF_NAME,
                                                                                      ENABLE_REGISTRATION_CONFIG_PREF_NAME,
                                                                                      ENABLE_REGISTRATION_PREF_NAME});
    private static final String OPEN_ID_RELAYING_PARTY_SERVLET_MAPPING = "/openid";
    
    private List<String> initProviderLabels;
    private List<String> initProviderDomains;
    private boolean initEnableOpenIDEntry;
    private OpenIDRegistrationConfiguration initRegistrationConfiguration;
    
    /* (non-Javadoc)
     * @see org.apache.portals.bridges.common.GenericServletPortlet#init(javax.portlet.PortletConfig)
     */
    public void init(PortletConfig config) throws PortletException
    {
        super.init(config);

        // registration configuration parameters
        initProviderLabels = OpenIDRegistrationConfiguration.parseParameterList(config.getInitParameter(PROVIDER_LABELS_INIT_PARAM_NAME));
        initProviderDomains = OpenIDRegistrationConfiguration.parseParameterList(config.getInitParameter(PROVIDER_DOMAINS_INIT_PARAM_NAME));
        initEnableOpenIDEntry = Boolean.parseBoolean(config.getInitParameter(ENABLE_OPEN_ID_ENTRY_INIT_PARAM_NAME));
        if (Boolean.parseBoolean(config.getInitParameter(OpenIDConstants.ENABLE_REGISTRATION_CONFIG_INIT_PARAM_NAME)))
        {
            initRegistrationConfiguration = new OpenIDRegistrationConfiguration();
            initRegistrationConfiguration.setEnableRegistration(config.getInitParameter(OpenIDConstants.ENABLE_REGISTRATION_INIT_PARAM_NAME));
            initRegistrationConfiguration.setUserTemplateDirectory(config.getInitParameter(OpenIDConstants.REGISTRATION_USER_TEMPLATE_INIT_PARAM_NAME));
            initRegistrationConfiguration.setSubsiteRootFolder(config.getInitParameter(OpenIDConstants.REGISTRATION_SUBSITE_ROOT_INIT_PARAM_NAME));
            initRegistrationConfiguration.setRoles(config.getInitParameter(OpenIDConstants.REGISTRATION_ROLES_INIT_PARAM_NAME));
            initRegistrationConfiguration.setGroups(config.getInitParameter(OpenIDConstants.REGISTRATION_GROUPS_INIT_PARAM_NAME));
            initRegistrationConfiguration.setProfilerRules(config.getInitParameter(OpenIDConstants.REGISTRATION_PROFILER_RULE_NAMES_INIT_PARAM_NAME), config.getInitParameter(OpenIDConstants.REGISTRATION_PROFILER_RULE_VALUES_INIT_PARAM_NAME));
        }
    }
    
    /* (non-Javadoc)
     * @see javax.portlet.GenericPortlet#destroy()
     */
    public void destroy()
    {
        initRegistrationConfiguration = null;
        super.destroy();
    }

    /* (non-Javadoc)
     * @see javax.portlet.GenericPortlet#doDispatch(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    protected void doDispatch(RenderRequest request, RenderResponse response) throws PortletException, IOException
    {
        // support edit defaults mode
        if (!request.getWindowState().equals(WindowState.MINIMIZED))
        {
            PortletMode curMode = request.getPortletMode();            
            if (JetspeedActions.EDIT_DEFAULTS_MODE.equals(curMode))
            {
                doEdit(request, response);
            }
            else
            {
                super.doDispatch(request, response);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.portals.bridges.common.GenericServletPortlet#doView(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException
    {
        // provider button labels and domains
        PortletPreferences prefs = request.getPreferences();
        List<String> providerButtonLabels = OpenIDRegistrationConfiguration.parseParameterList(prefs.getValue(PROVIDER_LABELS_PREF_NAME, null));
        List<String> providerButtonDomains = OpenIDRegistrationConfiguration.parseParameterList(prefs.getValue(PROVIDER_DOMAINS_PREF_NAME, null));
        if ((providerButtonLabels == null) || providerButtonLabels.isEmpty() || (providerButtonDomains == null) || providerButtonDomains.isEmpty())
        {
            providerButtonLabels = initProviderLabels;
            providerButtonDomains = initProviderDomains;
        }
        if ((providerButtonLabels != null) && !providerButtonLabels.isEmpty() && (providerButtonDomains != null) && !providerButtonDomains.isEmpty())
        {
            Map<String,String> providerButtons = new LinkedHashMap<String,String>();
            Iterator<String> labelsIter = providerButtonLabels.iterator();
            Iterator<String> domainsIter = providerButtonDomains.iterator();
            while (labelsIter.hasNext() && domainsIter.hasNext())
            {
                providerButtons.put(domainsIter.next(), labelsIter.next());
            }
            request.setAttribute(PROVIDER_BUTTONS_ATTR_NAME, providerButtons);
        }
        // enable OpenID entry
        boolean enableOpenIDEntry = initEnableOpenIDEntry;
        String enableOpenIDEntryPref = prefs.getValue(ENABLE_OPEN_ID_ENTRY_PREF_NAME, null);
        if (enableOpenIDEntryPref != null)
        {
            enableOpenIDEntry = Boolean.parseBoolean(enableOpenIDEntryPref);
        }
        request.setAttribute(ENABLE_OPEN_ID_ENTRY_ATTR_NAME, Boolean.toString(enableOpenIDEntry));
        
        // access login errors in session from OpenIDRelayingPartyServlet
        RequestContext requestContext = (RequestContext)request.getAttribute(PortalReservedParameters.REQUEST_CONTEXT_ATTRIBUTE);
        String errorCode = (String)requestContext.getSessionAttribute(OpenIDConstants.OPEN_ID_ERROR);
        if (errorCode != null)
        {
            request.setAttribute(OpenIDConstants.OPEN_ID_ERROR, errorCode);
        }
        
        // render portlet content
        response.setContentType("text/html");
        super.doView(request, response);
    }
    
    /* (non-Javadoc)
     * @see org.apache.portals.bridges.common.GenericServletPortlet#doEdit(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    public void doEdit(RenderRequest request, RenderResponse response) throws PortletException, IOException
    {
        // access provider and registration preferences to edit
        PortletPreferences prefs = request.getPreferences();
        for (String prefName : PREF_NAMES)
        {
            String prefValue = prefs.getValue(prefName, null);
            if (prefValue != null)
            {
                request.setAttribute(prefName, prefValue);
            }
        }
        
        // render portlet content
        response.setContentType("text/html");
        super.doEdit(request, response);
    }
    
    /* (non-Javadoc)
     * @see org.apache.portals.bridges.common.GenericServletPortlet#processAction(javax.portlet.ActionRequest, javax.portlet.ActionResponse)
     */
    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException, IOException
    {
        PortletMode curMode = actionRequest.getPortletMode();
        if (curMode == PortletMode.VIEW)
        {
            String action = actionRequest.getParameter(OpenIDConstants.OPEN_ID_REQUEST);
            if (action != null)
            {
                // redirect action to OpenIDRelayingPartyServlet
                RequestContext requestContext = (RequestContext)actionRequest.getAttribute(PortalReservedParameters.REQUEST_CONTEXT_ATTRIBUTE);
                String servletRedirect = requestContext.getRequest().getContextPath()+OPEN_ID_RELAYING_PARTY_SERVLET_MAPPING+"/"+action+"?"+OpenIDConstants.OPEN_ID_RETURN+"="+requestContext.getPortalURL().getBasePath();
                
                // perform action
                if (action.equals(OpenIDConstants.OPEN_ID_LOGIN_REQUEST))
                {
                    // access new user registration init parameters and preferences
                    // and save in session for consumption by the OpenIDRelayingPartyServlet
                    String logConfiguration = "none";
                    PortletPreferences prefs = actionRequest.getPreferences();
                    if (Boolean.parseBoolean(prefs.getValue(ENABLE_REGISTRATION_CONFIG_PREF_NAME, null)))
                    {
                        OpenIDRegistrationConfiguration registrationConfiguration = new OpenIDRegistrationConfiguration();
                        registrationConfiguration.setEnableRegistration(prefs.getValue(ENABLE_REGISTRATION_PREF_NAME, null));
                        registrationConfiguration.setUserTemplateDirectory(prefs.getValue(REGISTRATION_USER_TEMPLATE_PREF_NAME, null));
                        registrationConfiguration.setSubsiteRootFolder(prefs.getValue(REGISTRATION_SUBSITE_ROOT_PREF_NAME, null));
                        registrationConfiguration.setRoles(prefs.getValue(REGISTRATION_ROLES_PREF_NAME, null));
                        registrationConfiguration.setGroups(prefs.getValue(REGISTRATION_GROUPS_PREF_NAME, null));
                        registrationConfiguration.setProfilerRules(prefs.getValue(REGISTRATION_PROFILER_RULE_NAMES_PREF_NAME, null), prefs.getValue(REGISTRATION_PROFILER_RULE_VALUES_PREF_NAME, null));
                        registrationConfiguration.merge(initRegistrationConfiguration);
                        requestContext.setSessionAttribute(OpenIDConstants.OPEN_ID_REGISTRATION_CONFIGURATION, registrationConfiguration);
                        logConfiguration = "preferences";
                    }
                    else if (initRegistrationConfiguration != null)
                    {
                        requestContext.setSessionAttribute(OpenIDConstants.OPEN_ID_REGISTRATION_CONFIGURATION, initRegistrationConfiguration);
                        logConfiguration = "init params";
                    }

                    // login redirect to OpenIDRelayingPartyServlet
                    String discoveryParam = actionRequest.getParameter(OpenIDConstants.OPEN_ID_DISCOVERY);
                    servletRedirect += (((discoveryParam != null) && (discoveryParam.length() > 0)) ? "&"+OpenIDConstants.OPEN_ID_DISCOVERY+"="+discoveryParam : "");
                    String providerParam = actionRequest.getParameter(OpenIDConstants.OPEN_ID_PROVIDER);
                    servletRedirect += (((providerParam != null) && (providerParam.length() > 0)) ? "&"+OpenIDConstants.OPEN_ID_PROVIDER+"="+providerParam : "");
                    actionResponse.sendRedirect(servletRedirect);

                    // log user login
                    if (log.isDebugEnabled())
                    {
                        log.debug("OpenID login: discovery: "+discoveryParam+", provider: "+providerParam+", configuration: "+logConfiguration);
                    }
                }
                else if (action.equals(OpenIDConstants.OPEN_ID_LOGOUT_REQUEST))
                {
                    // logout redirect to OpenIDRelayingPartyServlet
                    actionResponse.sendRedirect(servletRedirect);

                    // log user logout
                    if (log.isDebugEnabled())
                    {
                        log.debug("OpenID logout");
                    }
                }
            }            
        }
        else if (curMode.equals(JetspeedActions.EDIT_DEFAULTS_MODE))
        {
            // default preferences edit mode
            if (actionRequest.getParameter(SAVE_ACTION_PARAM_NAME) != null)
            {
                // save provider and registration preferences
                PortletPreferences prefs = actionRequest.getPreferences();
                boolean storePrefs = false;
                for (String prefName : PREF_NAMES)
                {
                    String prefValue = actionRequest.getParameter(prefName);
                    if ((prefValue != null) && (prefValue.length() > 0))
                    {
                        if (!prefValue.equals(prefs.getValue(prefName, null)))
                        {
                            prefs.setValue(prefName, prefValue);
                            storePrefs = true;
                        }
                    }
                    else if (BOOLEAN_PREF_NAMES.indexOf(prefName) != -1)
                    {
                        String booleanPrefValue = prefs.getValue(prefName, null);
                        if ((booleanPrefValue == null) || !booleanPrefValue.equals("false"))
                        {
                            prefs.setValue(prefName, "false");
                            storePrefs = true;
                        }
                    }
                    else if (prefs.getValue(prefName, null) != null)
                    {
                        prefs.setValue(prefName, null);
                        storePrefs = true;
                    }
                }
                
                // store preferences
                if (storePrefs)
                {
                    prefs.store();
                }

                // log store preferences
                if (log.isDebugEnabled())
                {
                    log.debug("OpenID preferences saved: stored: "+storePrefs);
                }
            }
            
            // switch to view mode when done
            actionResponse.setPortletMode(PortletMode.VIEW);
        }
    }
}
