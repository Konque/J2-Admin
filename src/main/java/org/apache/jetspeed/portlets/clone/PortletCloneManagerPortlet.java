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
package org.apache.jetspeed.portlets.clone;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.components.portletpreferences.PortletPreferencesProvider;
import org.apache.jetspeed.components.portletregistry.PortletRegistry;
import org.apache.jetspeed.om.portlet.DisplayName;
import org.apache.jetspeed.om.portlet.PortletDefinition;
import org.apache.jetspeed.om.portlet.Preference;
import org.apache.jetspeed.om.portlet.Preferences;
import org.apache.jetspeed.request.RequestContext;
import org.apache.portals.bridges.common.GenericServletPortlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Portlet Clone Manager Portlet
 * 
 * @version $Id$
 */
public class PortletCloneManagerPortlet extends GenericServletPortlet
{
    private static Logger log = LoggerFactory.getLogger(PortletCloneManagerPortlet.class);
    
    private PortletRegistry registry;
    private PortletPreferencesProvider prefProvider;
    
    @Override
    public void init(PortletConfig config) throws PortletException
    {
        super.init(config);
        registry = (PortletRegistry) config.getPortletContext().getAttribute(CommonPortletServices.CPS_REGISTRY_COMPONENT);
        prefProvider = (PortletPreferencesProvider) config.getPortletContext().getAttribute(CommonPortletServices.CPS_PORTLET_PREFERENCES_PROVIDER);

    }
    
    @Override
    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException
    {
        RequestContext rc = (RequestContext) request.getAttribute(RequestContext.REQUEST_PORTALENV);
        String portletUniqueName = rc.getRequestParameter("portlet");
        
        if (portletUniqueName == null)
        {
            portletUniqueName = (String) request.getPortletSession(true).getAttribute("originalPortletUniqueName");
        }
        
        PortletDefinition def = null;
        
        if (!StringUtils.isBlank(portletUniqueName))
        {
            request.getPortletSession(true).setAttribute("originalPortletUniqueName", portletUniqueName);
            def = registry.getPortletDefinitionByUniqueName(portletUniqueName);
        }
        
        if (def == null)
        {
            log.error("Cannot find the portlet or clone: {}", portletUniqueName);
        }
        else
        {
            ClonePortletInfo clonePortletInfo = new ClonePortletInfo();
            clonePortletInfo.setOriginalPortletUniqueName(portletUniqueName);
            clonePortletInfo.setPortletName(def.getPortletName());
            clonePortletInfo.setPortletDisplayName(def.getDisplayNameText(Locale.getDefault()));
            clonePortletInfo.setPortletTitle(def.getPortletInfo().getTitle());
            clonePortletInfo.setPortletShortTitle(def.getPortletInfo().getShortTitle());
            clonePortletInfo.setPortletKeywords(def.getPortletInfo().getKeywords());
            Map<String, List<String>> prefsMap = new HashMap<String, List<String>>();
            
            for (Preference pref : def.getPortletPreferences().getPortletPreferences())
            {
                String prefName = pref.getName();
                List<String> prefValues = new ArrayList<String>(pref.getValues());
                prefsMap.put(prefName, prefValues);
            }
            
            clonePortletInfo.setPortletPreferences(prefsMap);
            request.setAttribute("clonePortletInfo", clonePortletInfo);
        }
        
        super.doView(request, response);
    }
    
    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException
    {
        String status = "fail";
        ClonePortletInfo clonePortletInfo = readClonePortletInfoFromRequest(request);
        PortletDefinition def = registry.getPortletDefinitionByUniqueName(clonePortletInfo.getOriginalPortletUniqueName());
        
        try
        {
            if (def == null)
            {
                throw new IllegalArgumentException("Cannot find the portlet or clone: " + clonePortletInfo.getOriginalPortletUniqueName());
            }
            
            if (StringUtils.isBlank(clonePortletInfo.getPortletName()))
            {
                throw new IllegalArgumentException("Invalid clone name: " + clonePortletInfo.getPortletName());
            }

            PortletDefinition clone = registry.clonePortletDefinition(def, StringUtils.trim(clonePortletInfo.getPortletName()));
            clone.getPortletInfo().setTitle(StringUtils.defaultString(clonePortletInfo.getPortletTitle()));
            clone.getPortletInfo().setShortTitle(StringUtils.defaultString(clonePortletInfo.getPortletShortTitle()));
            clone.getPortletInfo().setKeywords(StringUtils.defaultString(clonePortletInfo.getPortletKeywords()));
            
            Locale defaultLocale = Locale.getDefault();
            DisplayName defaultDisplayName = null;
            
            for (DisplayName displayName : clone.getDisplayNames())
            {
                if (displayName.getLocale().equals(defaultLocale))
                {
                    defaultDisplayName = displayName;
                    break;
                }
            }
            
            if (defaultDisplayName == null)
            {
                defaultDisplayName = clone.addDisplayName(defaultLocale.toString());
            }
            
            defaultDisplayName.setDisplayName(StringUtils.defaultString(clonePortletInfo.getPortletDisplayName()));
            
            for (Map.Entry<String, List<String>> entry : clonePortletInfo.getPortletPreferences().entrySet())
            {
                String prefName = entry.getKey();
                List<String> prefValues = entry.getValue();
                Preferences prefs = clone.getPortletPreferences();
                Preference pref = prefs.getPortletPreference(prefName);
                
                if (pref == null)
                {
                    pref = prefs.addPreference(prefName);
                }
                
                List<String> values = pref.getValues();
                values.clear();
                values.addAll(prefValues);
                
                prefProvider.storeDefaults(clone, pref);
            }
            
            registry.savePortletDefinition(clone);
            status = "success";
        }
        catch (Exception e) 
        {
            request.getPortletSession(true).setAttribute("errorMessage", e.toString());
            log.error("Failed to clone portlet from " + clonePortletInfo.getOriginalPortletUniqueName() + " to " + clonePortletInfo.getPortletName(), e);
        }
        
        request.getPortletSession(true).setAttribute("status", status);
    }
    
    private ClonePortletInfo readClonePortletInfoFromRequest(ActionRequest request)
    {
        ClonePortletInfo clonePortletInfo = new ClonePortletInfo();
        clonePortletInfo.setOriginalPortletUniqueName(request.getParameter("originalPortletUniqueName"));
        clonePortletInfo.setPortletName(request.getParameter("portlet_name"));
        clonePortletInfo.setPortletDisplayName(request.getParameter("portlet_displayName"));
        clonePortletInfo.setPortletTitle(request.getParameter("portlet_title"));
        clonePortletInfo.setPortletShortTitle(request.getParameter("portlet_shortTitle"));
        clonePortletInfo.setPortletKeywords(request.getParameter("portlet_keywords"));
        
        Map<String, List<String>> prefsMap = new HashMap<String, List<String>>();
        
        for (Enumeration<String> paramNames = request.getParameterNames(); paramNames.hasMoreElements(); )
        {
            String paramName = paramNames.nextElement();
            
            if (paramName.startsWith("prefs_"))
            {
                String prefName = paramName.substring("prefs_".length());
                String [] prefValues = request.getParameterValues(paramName);
                List<String> prefValueList = null;
                if (prefValues == null)
                {
                    prefValueList = Collections.emptyList();
                }
                else
                {
                    prefValueList = Arrays.asList(prefValues);
                }
                prefsMap.put(prefName, prefValueList);
            }
        }
        
        clonePortletInfo.setPortletPreferences(prefsMap);
        
        return clonePortletInfo;
    }
    
}

