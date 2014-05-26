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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ClonePortletInfo implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private String originalPortletUniqueName;
    private String portletName;
    private String portletDisplayName;
    private String portletTitle;
    private String portletShortTitle;
    private String portletKeywords;
    private Map<String, List<String>> portletPreferences;
    
    public String getOriginalPortletUniqueName()
    {
        return originalPortletUniqueName;
    }

    public void setOriginalPortletUniqueName(String originalPortletUniqueName)
    {
        this.originalPortletUniqueName = originalPortletUniqueName;
    }

    public String getPortletName()
    {
        return portletName;
    }
    
    public void setPortletName(String portletName)
    {
        this.portletName = portletName;
    }
    
    public String getPortletDisplayName()
    {
        return portletDisplayName;
    }
    
    public void setPortletDisplayName(String portletDisplayName)
    {
        this.portletDisplayName = portletDisplayName;
    }
    
    public String getPortletTitle()
    {
        return portletTitle;
    }
    
    public void setPortletTitle(String portletTitle)
    {
        this.portletTitle = portletTitle;
    }
    
    public String getPortletShortTitle()
    {
        return portletShortTitle;
    }
    
    public void setPortletShortTitle(String portletShortTitle)
    {
        this.portletShortTitle = portletShortTitle;
    }
    
    public String getPortletKeywords()
    {
        return portletKeywords;
    }
    
    public void setPortletKeywords(String portletKeywords)
    {
        this.portletKeywords = portletKeywords;
    }
    
    public Map<String, List<String>> getPortletPreferences()
    {
        return portletPreferences;
    }
    
    public void setPortletPreferences(Map<String, List<String>> portletPreferences)
    {
        this.portletPreferences = portletPreferences;
    }
}
