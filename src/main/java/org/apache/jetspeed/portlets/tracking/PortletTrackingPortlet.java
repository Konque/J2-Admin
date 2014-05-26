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
package org.apache.jetspeed.portlets.tracking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.aggregator.PortletTrackingManager;
import org.apache.jetspeed.container.PortletWindow;
import org.apache.jetspeed.om.portlet.PortletDefinition;
import org.apache.portals.bridges.velocity.GenericVelocityPortlet;
import org.apache.velocity.context.Context;

public class PortletTrackingPortlet extends GenericVelocityPortlet
{
    private PortletTrackingManager trackingManager;
    

     /* (non-Javadoc)
     * @see org.apache.portals.bridges.velocity.GenericVelocityPortlet#init(javax.portlet.PortletConfig)
     */
    public void init(PortletConfig config) throws PortletException
    {
        super.init(config);
        PortletContext context = getPortletContext();
        trackingManager = (PortletTrackingManager)context.getAttribute(CommonPortletServices.CPS_PORTLET_TRACKING_MANAGER);
    }

    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException
    {
        List outOfService = createList();
        Context context = getContext(request);
        context.put("outOfService", outOfService);
        context.put("count", new Integer(outOfService.size()));
        super.doView(request, response);
    }
    
    protected List createList()
    {
        List result = new ArrayList();
        Iterator outOfService = trackingManager.getOutOfServiceList().iterator();
        Map portlets = new HashMap();
        while (outOfService.hasNext())
        {
            PortletWindow window = (PortletWindow)outOfService.next();
            String id = window.getId().toString();
            PortletDefinition pd = window.getPortletDefinition();
            String uniqueName = pd.getUniqueName();
            if (!portlets.containsKey(uniqueName))
            {
                portlets.put(uniqueName, id);
                result.add(pd);              
            }
        }
        return result;
    }
    
    public void processAction(ActionRequest request, ActionResponse actionResponse)
    throws PortletException, IOException
    {
        List result = new ArrayList();
        Enumeration e = request.getParameterNames();
        while (e.hasMoreElements())
        {
            String param = (String)e.nextElement();
            if (param.indexOf("::") > 0)
            {
                String[] values = request.getParameterValues(param);
                if (values[0] != null)
                {
                    result.add(param);
                }
            }
        }
        if (result.size() > 0)
        {            
            trackingManager.putIntoService(result);
        }
        
    }
}
