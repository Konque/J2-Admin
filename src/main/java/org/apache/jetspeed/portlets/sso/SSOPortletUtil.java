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
package org.apache.jetspeed.portlets.sso;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.util.Collection;

import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.security.auth.Subject;

import org.apache.jetspeed.security.JSSubject;
import org.apache.jetspeed.security.JetspeedPrincipal;
import org.apache.jetspeed.security.PasswordCredential;
import org.apache.jetspeed.sso.SSOException;
import org.apache.jetspeed.sso.SSOManager;
import org.apache.jetspeed.sso.SSOSite;
import org.apache.jetspeed.sso.SSOUser;
import org.apache.portals.applications.gems.browser.StatusMessage;
import org.apache.portals.messaging.PortletMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author <a href="mailto:ddam@apache.org">Dennis Dam</a>
 * @version $Id: SSOPortletUtil.java 771261 2009-05-04 10:18:30Z woonsan $
 */
public abstract class SSOPortletUtil
{

    static final Logger logger = LoggerFactory.getLogger(SSOPortletUtil.class);
    
    public static Subject getSubject()
    {
        AccessControlContext context = AccessController.getContext();
        return JSSubject.getSubject(context);         
    }
    
    public static SSOUser getRemoteUser(SSOManager sso, PortletRequest request, SSOSite site) throws SSOException
    {
        Subject subject = getSubject();
        if (subject != null)
        {
            Collection<SSOUser> remoteUsers = sso.getRemoteUsers(site, subject);
            // keep backwards compatibility : enforce a relationship (ssouser :
            // user) of 1-to-n.
            // TODO: support multiple SSO users and select 1 that is used for
            // browsing.
            if (remoteUsers.size() == 1) 
            { 
                return remoteUsers.iterator().next(); 
            }
        }
        return null;
    }
    
    public static void updateUser(SSOManager sso, PortletRequest request, SSOSite site, String newPrincipal, String newPassword) throws SSOException
    {
        SSOUser remoteUser = getRemoteUser(sso, request, site);
        if (remoteUser != null)
        {
            if (!remoteUser.getName().equals(newPrincipal))
            {
                // rename SSO user and update
                remoteUser.setName(newPrincipal);
                sso.updateUser(remoteUser);
            }
            sso.setPassword(remoteUser, newPassword);
        }
        else
        {
            Subject subject = getSubject();
            Principal ownerPrincipal = null;
            for (Principal p : subject.getPrincipals())
            {
                if (p instanceof JetspeedPrincipal)
                {            
                    ownerPrincipal = p;
                    break;
                }
            }
            sso.addUser(site, (JetspeedPrincipal)ownerPrincipal, newPrincipal, newPassword);            
        }
        
    }
    
    public static PasswordCredential getCredentialsForSite(SSOManager sso, String siteUrl, RenderRequest request)
    {
        PasswordCredential pwc = null;
        SSOSite site = sso.getSiteByUrl(siteUrl);
        if (site != null) 
        { 
            return getCredentialsForSite(sso, site, request); 
        }
        return pwc;
    }

    public static PasswordCredential getCredentialsForSite(SSOManager sso, SSOSite site, RenderRequest request)
    {
        PasswordCredential pwc = null;
        try
        {
            SSOUser remoteUser = getRemoteUser(sso, request, site);
            if (remoteUser != null)
            {
                pwc = sso.getCredentials(remoteUser);
            }

        }
        catch (SSOException sx)
        {

        }
        return pwc;
    }
        
    public static void publishStatusMessage(PortletRequest request, String portlet, String topic, Throwable e, String message)
    {
        String msg = message + ": " + e.toString();
        Throwable cause = e.getCause();
        if (cause != null)
        {
            msg = msg + ", " + cause.getMessage();
        }
        StatusMessage sm = new StatusMessage(msg, StatusMessage.ERROR);
        try
        {
            // TODO: fixme, bug in Pluto on portlet session
            PortletMessaging.publish(request, portlet, topic, sm);
        }
        catch (Exception ee)
        {
            logger.error("Failed to publish message.", e);
        }
    }
    
}
