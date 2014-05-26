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
package org.apache.jetspeed.portlets.dojo;

import java.io.IOException;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.jetspeed.request.RequestContext;
import org.apache.portals.gems.dojo.AbstractDojoVelocityPortlet;
import org.apache.portals.gems.dojo.DojoPortletHelper;

/**
 * Extending AbstractDojoVelocityPortlet because admin portlets can use jetspeed-api
 * to detect the portal webapp context path.
 * 
 * @version $Id: AbstractAdminDojoVelocityPortlet.java 766113 2009-04-17 18:46:24Z woonsan $
 */
public abstract class AbstractAdminDojoVelocityPortlet extends AbstractDojoVelocityPortlet
{
    
    protected static final String DOJO_JS_RELATIVE_URL_INIT_PARAM = "dojo.js.relative.url";

    protected String dojoJsRelativeUrl;
    
    /*
     * Portlet constructor.
     */
    public AbstractAdminDojoVelocityPortlet() 
    {
        super();
    }
    
    @Override
    public void init(PortletConfig config) throws PortletException
    {
        super.init(config);
        this.dojoJsRelativeUrl = getInitParameter(DOJO_JS_RELATIVE_URL_INIT_PARAM);
    }
    
    @Override
    protected void doHeaders(RenderRequest request, RenderResponse response)
    {
        if (this.dojoJsRelativeUrl != null)
        {
            RequestContext requestContext = (RequestContext) request.getAttribute(RequestContext.REQUEST_PORTALENV);
            String portalContextPath = requestContext.getRequest().getContextPath();
            DojoPortletHelper.enableDojo(response, portalContextPath + this.dojoJsRelativeUrl);
        }
        else
        {
            DojoPortletHelper.enableDojo(response, this.dojoJsUrl);
        }
        
        if (this.headerPage != null)
        {
            try
            {
                this.getPortletContext().getRequestDispatcher(this.headerPage).include(request, response);
            }
            catch (PortletException e)
            {
                throw new RuntimeException("Failed to include header page.", e);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Failed to include header page.", e);
            }
        }

        if (this.dojoRequiresCoreList != null)
        {
            DojoPortletHelper.contributeDojoRequires(response, this.dojoRequiresCoreList);
        }
        
        if (this.dojoRequiresAddOnList != null)
        {
            DojoPortletHelper.contributeDojoRequires(response, this.dojoRequiresAddOnList);
        }
    }

}
