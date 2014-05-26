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
package org.apache.jetspeed.portlets.security.sso;

import java.io.IOException;
import java.security.AccessController;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.security.auth.Subject;

import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.security.JSSubject;
import org.apache.jetspeed.security.SecurityException;
import org.apache.jetspeed.security.User;
import org.apache.jetspeed.security.UserManager;
import org.apache.jetspeed.sso.SSOException;
import org.apache.jetspeed.sso.SSOManager;
import org.apache.jetspeed.sso.SSOSite;
import org.apache.jetspeed.sso.SSOUser;
import org.apache.portals.applications.gems.browser.BrowserIterator;
import org.apache.portals.applications.gems.browser.BrowserPortlet;
import org.apache.portals.applications.gems.browser.DatabaseBrowserIterator;
import org.apache.portals.applications.gems.browser.StatusMessage;
import org.apache.portals.messaging.PortletMessaging;
import org.apache.velocity.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SSOChangePasswordPortlet
 * 
 * @version $Id: SSOChangePasswordPortlet.java 823599 2009-10-09 15:55:13Z woonsan $
 */
public class SSOChangePasswordPortlet extends BrowserPortlet
{
    
    static final Logger logger = LoggerFactory.getLogger(SSOChangePasswordPortlet.class);
    
    private SSOManager sso;
    private UserManager userManager;
    
    public void init(PortletConfig config)
    throws PortletException 
    {
        super.init(config);
        sso = (SSOManager)getPortletContext().getAttribute(CommonPortletServices.CPS_SSO_COMPONENT);
        if (null == sso)
        {
            throw new PortletException("Failed to find the SSO Provider on portlet initialization");
        }
        userManager = (UserManager) getPortletContext().getAttribute(CommonPortletServices.CPS_USER_MANAGER_COMPONENT);
        if (null == userManager)
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
            Collection<SSOSite> sites = sso.getSites("");
            
            resultSetTypeList.add(String.valueOf(Types.VARCHAR));
            resultSetTypeList.add(String.valueOf(Types.VARCHAR));
            resultSetTitleList.add(0, "URL");
            resultSetTitleList.add(1, "Site");

            //subPopulate(rundata, qResult, repo, folder, null);
            List list = new ArrayList();
            if (sites != null){
                for (SSOSite site : sites ){
                	List row = new ArrayList(2);
                    row.add(0, site.getURL());                     
                    row.add(1, site.getName());
                    list.add(row);
                }
            }
            BrowserIterator iterator = new DatabaseBrowserIterator(
                    list, resultSetTitleList, resultSetTypeList,
                    windowSize);
            setBrowserIterator(request, iterator);
            iterator.sort("Site");
        }
        catch (Exception e)
        {
            logger.error("Exception in getRows: ", e);
            throw e;
        }        
    }
   
    public void doView(RenderRequest request, RenderResponse response)
    throws PortletException, IOException
    {
        String selectedSiteName = (String)PortletMessaging.receive(request, "SSOChangePassword", "selectedName");
        if (selectedSiteName != null)
        {        
            Context context = this.getContext(request);
            context.put("currentName", selectedSiteName);  
            context.put("currentUrl", (String)PortletMessaging.receive(request, "SSOChangePassword", "selectedUrl"));
            String ssoUsername = (String)PortletMessaging.receive(request, "SSOChangePassword", "ssoUsername");
            context.put("ssoUsername", ssoUsername);
        }
        StatusMessage msg = (StatusMessage)PortletMessaging.consume(request, "SSOChangePassword", "status");
        if (msg != null)
        {
            this.getContext(request).put("statusMsg", msg);            
        }
        
        super.doView(request, response);
    }
    
    protected void clearPortletMessages(PortletRequest request, PortletResponse response){
    	PortletMessaging.cancel(request, "SSOChangePassword", "selectedName");
        PortletMessaging.cancel(request, "SSOChangePassword", "selectedUrl");
        PortletMessaging.cancel(request, "SSOChangePassword", "ssoUsername");
    }
    
    protected void setPortletMessage(PortletRequest request, String key, String msg) throws IOException{
    	if (StringUtils.isNotEmpty(msg)){
    		PortletMessaging.publish(request, "SSOChangePassword", key, msg);
    	} else {
    		PortletMessaging.cancel(request, "SSOChangePassword", key);	
    	}
    }
    
    public void processAction(ActionRequest request, ActionResponse response)
    throws PortletException, IOException
    {
        if (request.getPortletMode() == PortletMode.VIEW)
        {
            String selectedSite = request.getParameter("ssoSite");
            if (selectedSite != null)
            {
            	SSOSite site = sso.getSiteByName(selectedSite);
                if (site != null)
                {
                    setPortletMessage(request, "selectedUrl", site.getURL());
                    setPortletMessage(request, "selectedName", site.getName());
                    setPortletMessage(request, "change", selectedSite);
                    setPortletMessage(request, "ssoUsername", getRemoteUserName(request, site));
                }
            }
            String refresh = request.getParameter("sso.refresh");
            String save = request.getParameter("sso.save");
            String delete = request.getParameter("sso.delete");
            
            if (refresh != null)
            {
                this.clearBrowserIterator(request);
            }
            else if (delete != null)
            {
                String siteName = request.getParameter("site.name");
                
                if (!StringUtils.isEmpty(siteName))
                {
                    SSOSite site = sso.getSiteByName(siteName);
                    
                    if (site != null)
                    {
                        removeRemoteUser(request, site);
                        this.clearBrowserIterator(request);
                        clearPortletMessages(request,response);
                    }
                }
            }
            else if (save != null)
            {
                String siteName = request.getParameter("site.name");
                String siteUrl = request.getParameter("site.url");
                String ssoUsername = request.getParameter("ssoUsername");
                String ssoPassword = request.getParameter("ssoPassword");
                 
                if (!StringUtils.isEmpty(siteName) && !StringUtils.isEmpty(siteUrl) && !StringUtils.isEmpty(ssoUsername) && !StringUtils.isEmpty(ssoPassword))
                {
                    SSOSite site = null;
                    String old = (String)PortletMessaging.receive(request, "site", "selectedName");
                    site = sso.getSiteByName(old != null ? old : siteName);
                    
                    addOrUpdateRemoteUser(request, site, ssoUsername, ssoPassword);
                    
                	this.clearBrowserIterator(request);
                    PortletMessaging.publish(request, "SSOChangePassword", "selectedName", siteName);
                    PortletMessaging.publish(request, "SSOChangePassword", "selectedUrl", siteUrl);    
                    PortletMessaging.publish(request, "SSOChangePassword", "ssoUsername", ssoUsername);
                }
            }   
        }
        
        request.getPortletSession().removeAttribute("org.apache.jetspeed.portlets.sso.ssoSiteCredsOfSubject", PortletSession.APPLICATION_SCOPE);
        
        super.processAction(request, response);
            
    }
    
    private String getRemoteUserName(PortletRequest request, SSOSite site)
    {
        String remoteUsername = null;
        
        try
        {
            Subject subject = JSSubject.getSubject(AccessController.getContext());
            Collection<SSOUser> remoteUsers = sso.getRemoteUsers(site, subject);
            
            if (remoteUsers != null && !remoteUsers.isEmpty())
            {
                remoteUsername = remoteUsers.iterator().next().getName();
            }
        }
        catch (SSOException e)
        {
            publishStatusMessage(request, "SSOChangePassword", "status", e, "Could not retrieve sso user name");
        }
        
        return remoteUsername;
    }
    
    private void removeRemoteUser(PortletRequest request, SSOSite site)
    {
        try
        {
            Subject subject = JSSubject.getSubject(AccessController.getContext());
            Collection<SSOUser> remoteUsers = sso.getRemoteUsers(site, subject);
            
            if (remoteUsers != null && !remoteUsers.isEmpty())
            {
                sso.removeUser(remoteUsers.iterator().next());
            }
        }
        catch (SSOException e)
        {
            publishStatusMessage(request, "SSOChangePassword", "status", e, "Could not remove sso user");
        }
    }
    
    private void addOrUpdateRemoteUser(PortletRequest request, SSOSite site, String username, String password)
    {
        try
        {
            Subject subject = JSSubject.getSubject(AccessController.getContext());
            Collection<SSOUser> remoteUsers = sso.getRemoteUsers(site, subject);
            
            if (remoteUsers != null && !remoteUsers.isEmpty())
            {
                SSOUser ssoUser = remoteUsers.iterator().next();
                
                if (!StringUtils.equals(ssoUser.getName(), username))
                {
                    ssoUser.setName(username);
                    sso.updateUser(ssoUser);
                }
                
                sso.setPassword(ssoUser, password);
            }
            else
            {
                User user = userManager.getUser(request.getUserPrincipal().getName());
                sso.addUser(site, user, username, password);
            }
        }
        catch (SSOException e)
        {
            publishStatusMessage(request, "SSOChangePassword", "status", e, "Could not remove sso user");
        }
        catch (SecurityException e)
        {
            publishStatusMessage(request, "SSOChangePassword", "status", e, "Could not retrieve jetspeed user");
        }
    }
    
}
