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

import java.security.Principal;

import javax.portlet.PortletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.administration.PortalAdministration;

/**
 * SpaceAdminUtils
 * 
 * @version $Id: SpaceAdminUtils.java 925391 2010-03-19 19:30:06Z woonsan $
 */
public class SpaceAdminUtils
{
    public static final String SPACE_ADMIN_ROLES_PARAM_NAME = "spaceAdminRoles";

    private SpaceAdminUtils()
    {
        
    }
    
    public static boolean isUserSpaceOwner(SpaceBean spaceBean, PortletRequest request) 
    {
        Principal principal = request.getUserPrincipal();
        
        if (principal != null && principal.getName().equals(spaceBean.getOwner()))
        {
            return true;
        }
        
        return false;
    }
    
    public static boolean isUserSpaceAdmin(SpaceBean spaceBean, PortalAdministration portalAdmin, PortletRequest request) 
    {
        String spaceAdminRolesPref = request.getPreferences().getValue(SPACE_ADMIN_ROLES_PARAM_NAME, null);
        
        if (!StringUtils.isBlank(spaceAdminRolesPref))
        {
            String [] spaceAdminRoles = StringUtils.split(spaceAdminRolesPref, ", \t\r\n");
            
            for (String role : spaceAdminRoles)
            {
                if (request.isUserInRole(role))
                {
                    return true;
                }
            }
        }
        
        return portalAdmin.isAdminUser(request) || portalAdmin.isUserInAdminRole(request);
    }
    
}
