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
package org.apache.jetspeed.portlets.prm;

import java.util.Locale;

import org.apache.jetspeed.om.portlet.DisplayName;
import org.apache.jetspeed.om.portlet.PortletDefinition;

public class PortletDefinitionBean extends PortletApplicationNodeBean
{
    private static final long serialVersionUID = 1L;
    
    protected String uniqueName;
    protected String displayName;
    protected boolean cloned;
        
    public PortletDefinitionBean(PortletDefinition portlet, String appName, Locale locale)
    {
        super(appName, portlet.getPortletName());
        this.uniqueName = portlet.getUniqueName();
        DisplayName dn = portlet.getDisplayName(locale);
        
        if (dn == null)
        {
            this.displayName = this.name;
        }
        else
        {
            this.displayName = dn.getDisplayName();
        }
        
        this.cloned = portlet.isClone();
    }
    
    public String getUniqueName()
    {
        return uniqueName;
    }
    
    public String toString()
    {
        return getDisplayName();
    }
        
    public String getDisplayName()
    {
        return displayName;
    }

    public boolean isCloned()
    {
        return cloned;
    }

    public void setCloned(boolean cloned)
    {
        this.cloned = cloned;
    }
    
}
