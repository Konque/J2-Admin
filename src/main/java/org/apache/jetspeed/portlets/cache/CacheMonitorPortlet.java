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
package org.apache.jetspeed.portlets.cache;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;

import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.cache.JetspeedCacheMonitor;
import org.apache.portals.bridges.common.GenericServletPortlet;

/**
 * Jetspeed Cache Monitor Portlet
 *
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: CacheMonitorPortlet.java 773302 2009-05-10 03:34:06Z taylor $
 */
public class CacheMonitorPortlet extends GenericServletPortlet
{
    private JetspeedCacheMonitor cacheMonitor;
    public static final String ALL = "(all)";
    
    public void init(PortletConfig config)
    throws PortletException 
    {
        super.init(config);
        cacheMonitor = (JetspeedCacheMonitor) getPortletContext().getAttribute(CommonPortletServices.CPS_CACHE_MONITOR);
        if (null == cacheMonitor)
        {
            throw new PortletException("Failed to find the Cache Monitor on portlet initialization");
        }        
    }
        
    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException,
    IOException
    {
        String cacheName = actionRequest.getParameter("cacheNameCalc");
        if (cacheName != null)
        {
            if (cacheName.equals(ALL))
            {
                cacheMonitor.calculateStatistics();
            }
            else
            {
                cacheMonitor.calculateStatistics(cacheName);
            }
        }
        else
        {
            cacheName = actionRequest.getParameter("cacheNameReset");
            if (cacheName != null)
            {
                if (cacheName.equals(ALL))
                {
                    cacheMonitor.resetStatistics();
                }
                else
                {
                    cacheMonitor.resetStatistics(cacheName);
                }                
            }            
        }
    }    
}
