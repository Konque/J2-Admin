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
package org.apache.jetspeed.portlets.security.choosers;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;

import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.security.RoleManager;
import org.apache.portals.applications.gems.browser.BrowserIterator;
import org.apache.portals.applications.gems.browser.BrowserPortlet;
import org.apache.portals.applications.gems.browser.DatabaseBrowserIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RoleChooserPortlet
 * 
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: RoleChooserPortlet.java 771261 2009-05-04 10:18:30Z woonsan $
 */
public class RoleChooserPortlet extends BrowserPortlet
{
    
    static final Logger logger = LoggerFactory.getLogger(RoleChooserPortlet.class);
    
    private RoleManager roleManager;
    
    public void init(PortletConfig config)
    throws PortletException 
    {
        super.init(config);
        roleManager = (RoleManager) 
            getPortletContext().getAttribute(CommonPortletServices.CPS_GROUP_MANAGER_COMPONENT);
        if (null == roleManager)
        {
            throw new PortletException("Failed to find the User Manager on portlet initialization");
        }
    }
           
    public void getRows(RenderRequest request, String sql, int windowSize)
    throws Exception
    {
        List resultSetTitleList = new ArrayList();
        List resultSetTypeList = new ArrayList();
        try
        {
            resultSetTypeList.add(String.valueOf(Types.VARCHAR));
            resultSetTitleList.add("Role");

            List<String> list = roleManager.getRoleNames("");
            
            BrowserIterator iterator = new DatabaseBrowserIterator(
                    list, resultSetTitleList, resultSetTypeList,
                    windowSize);
            setBrowserIterator(request, iterator);
            iterator.sort("Role");
        }
        catch (Exception e)
        {
            logger.error("Exception in getRows: ", e);
            throw e;
        }        
    }
       
}
