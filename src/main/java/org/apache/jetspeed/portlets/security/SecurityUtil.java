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
package org.apache.jetspeed.portlets.security;

import java.io.NotSerializableException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;

import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;

import org.apache.jetspeed.PortalReservedParameters;
import org.apache.jetspeed.request.RequestContext;
import org.apache.portals.applications.gems.browser.BrowserPortlet;
import org.apache.portals.messaging.PortletMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Abstract Security Browser - factored out common functionality for security browsers 
 * 
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: SecurityUtil.java 348264 2005-11-22 22:06:45Z taylor $
 */
public abstract class SecurityUtil extends BrowserPortlet
{
    
    static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);
        
    public static Principal getPrincipal(Subject subject, Class classe)
    {
        Principal principal = null;
        Iterator principals = subject.getPrincipals().iterator();
        while (principals.hasNext())
        {
            Principal p = (Principal) principals.next();
            if (classe.isInstance(p))
            {
                principal = p;
                break;
            }
        }
        return principal;
    }

    public static boolean isEmpty(String s)
    {
        if (s == null) return true;
        
        if (s.trim().equals("")) return true;
        
        return false;
    }

    public static String getAbsoluteUrl(RenderRequest renderRequest, String relativePath)
    {
        RequestContext requestContext = (RequestContext) renderRequest.getAttribute(PortalReservedParameters.REQUEST_CONTEXT_ATTRIBUTE);
        HttpServletRequest request = requestContext.getRequest();
        StringBuffer path = new StringBuffer();
        if ( !requestContext.getPortalURL().isRelativeOnly() )
        {
            path.append(request.getScheme()).append("://").append(request.getServerName()).append(":").append(request.getServerPort());
        }
        return requestContext.getResponse().encodeURL(path.append(request.getContextPath()).append(request.getServletPath()).append(
                relativePath).toString());
    }
    
    public static void publishErrorMessage(PortletRequest request, String message)
    {
        try
        {
            ArrayList errors = (ArrayList)PortletMessaging.receive(request,SecurityResources.ERROR_MESSAGES);
            if ( errors == null )
            {
                errors = new ArrayList();
            }
            errors.add(message);
            PortletMessaging.publish(request, SecurityResources.ERROR_MESSAGES, errors);
        }
        catch (NotSerializableException e)
        {
            logger.error("Message to publish is not serializable.", e);
        }                
    }
    
    public static void publishErrorMessage(PortletRequest request, String topic, String message)
    {
        try
        {
            ArrayList errors = (ArrayList)PortletMessaging.receive(request,topic,SecurityResources.ERROR_MESSAGES);
            if ( errors == null )
            {
                errors = new ArrayList();
            }
            errors.add(message);
            PortletMessaging.publish(request, topic, SecurityResources.ERROR_MESSAGES, errors);
        }
        catch (NotSerializableException e)
        {
            logger.error("Message to publish is not serializable.", e);
        }                
    }
}
