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
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.portlets.security.SecurityUtil;
import org.apache.jetspeed.security.GroupManager;
import org.apache.jetspeed.security.JetspeedPrincipal;
import org.apache.jetspeed.security.JetspeedPrincipalType;
import org.apache.jetspeed.security.SecurityException;
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
 * SSODetails
 * 
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: SSODetails.java 348264 2005-11-22 22:06:45Z taylor $
 */
public class SSODetails extends BrowserPortlet
{
    
    static final Logger logger = LoggerFactory.getLogger(SSODetails.class);
    
    private SSOManager sso;
    private UserManager userManager;
    private GroupManager groupManager;
        
    public void init(PortletConfig config)
    throws PortletException 
    {
        super.init(config);
        sso = (SSOManager)getPortletContext().getAttribute(CommonPortletServices.CPS_SSO_COMPONENT);
        if (null == sso)
        {
            throw new PortletException("Failed to find the SSO Manager on portlet initialization");
        }
        userManager = (UserManager) getPortletContext().getAttribute(CommonPortletServices.CPS_USER_MANAGER_COMPONENT);
        if (null == userManager)
        {
            throw new PortletException("Failed to find the User Manager on portlet initialization");
        }
        groupManager = (GroupManager) getPortletContext().getAttribute(CommonPortletServices.CPS_GROUP_MANAGER_COMPONENT);
        if (null == groupManager)
        {
            throw new PortletException("Failed to find the Group Manager on portlet initialization");
        }        
    }
       
    
    public void getRows(RenderRequest request, String sql, int windowSize)
    throws Exception
    {
        List resultSetTitleList = new ArrayList();
        List resultSetTypeList = new ArrayList();
        try
        {
            SSOSite site = null;
            
            List<RemoteAndLocalPrincipalPair> list = new ArrayList<RemoteAndLocalPrincipalPair>();

            resultSetTypeList.add(String.valueOf(Types.VARCHAR));
            resultSetTitleList.add("Principal");
            resultSetTypeList.add(String.valueOf(Types.VARCHAR));
            resultSetTitleList.add("Remote");
            
            String selectedSite = (String)PortletMessaging.receive(request, "site", "selectedName");
            if (selectedSite != null)
            {
                site = sso.getSiteByName(selectedSite);
                Collection<SSOUser> ssoUsers = sso.getUsersForSite(site);
                for (SSOUser user : ssoUsers) {
                	Collection<JetspeedPrincipal> principals = sso.getPortalPrincipals(user);
                	// keep 1-on-n relation for now. Later portlet needs to be refactored to support n-to-n
                	if (principals.size() == 1){
                		list.add(new RemoteAndLocalPrincipalPair(user,principals.iterator().next()));
                	}                	
				}
            }           
            BrowserIterator iterator = new DatabaseBrowserIterator(
                    list, resultSetTitleList, resultSetTypeList,
                    windowSize);
            setBrowserIterator(request, iterator);
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
        String change = (String)PortletMessaging.consume(request, "site", "change");
        if (change != null)
        { 
            this.clearBrowserIterator(request);
        }
        Context context = this.getContext(request);        
        String selectedSite = (String)PortletMessaging.receive(request, "site", "selectedName");
        if (selectedSite != null)
        {        
            context.put("currentSite", selectedSite);
            String title = this.getTitle(request);
            if (title != null)
            {
                int pos = title.indexOf("-");
                if (pos > 1)
                {
                    title = title.substring(0, pos) + "- " + selectedSite;
                }
                else
                    title = title + " - "  + selectedSite;
            }
            else
                title = selectedSite;
            response.setTitle(title);
        }        
        
        // get relative link, TOremoveCredentialsForSiteDO: encapsulate Jetspeed links access into component
        String userChooser = SecurityUtil.getAbsoluteUrl(request, "/Administrative/choosers/users.psml");
        String groupChooser = SecurityUtil.getAbsoluteUrl(request, "/Administrative/choosers/groups.psml");
        
        context.put("userChooser", userChooser);
        context.put("groupChooser", groupChooser);
        
        StatusMessage msg = (StatusMessage)PortletMessaging.consume(request, "SSODetails", "status");
        if (msg != null)
        {
            this.getContext(request).put("statusMsg", msg);            
        }
        
        super.doView(request, response);
    }
        
    protected JetspeedPrincipal getJetspeedPrincipal(String principalType, String principalName) throws SecurityException {
    	JetspeedPrincipal foundPrincipal = null;
    	if (principalType.equals(JetspeedPrincipalType.USER)){
    		foundPrincipal = userManager.getUser(principalName);	
    	} else if (principalType.equals(JetspeedPrincipalType.GROUP)){
    		foundPrincipal = groupManager.getGroup(principalName);
    	}
    	return foundPrincipal;
    }
    
    public void processAction(ActionRequest request, ActionResponse response)
    throws PortletException, IOException
    {
        if (request.getPortletMode() == PortletMode.VIEW)
        {
            String refresh = request.getParameter("sso.refresh");
            String add = request.getParameter("sso.add");
            String deleteUser = request.getParameter("ssoDelete.user");
            String deleteGroup = request.getParameter("ssoDelete.group");
            
            if (refresh != null)
            {
                this.clearBrowserIterator(request);
            }
            else if (StringUtils.isNotEmpty(deleteUser) || StringUtils.isNotEmpty(deleteGroup) )
            {
            	String principalType = null;
            	String principalName = null;
            	if (StringUtils.isNotEmpty(deleteUser)){
            		principalType = JetspeedPrincipalType.USER;
                	principalName=deleteUser;
                } else {                    
                    if (StringUtils.isNotEmpty(deleteGroup)){                	
                    	principalName = deleteGroup;
                    	principalType = JetspeedPrincipalType.GROUP;
                    }
                }
            	try
                {
                    String siteName = (String)PortletMessaging.receive(request, "site", "selectedName");                                            
                    SSOSite site = sso.getSiteByName(siteName);
                    
                    if ( site != null )
                    {
                        
                    	JetspeedPrincipal principal = getJetspeedPrincipal(principalType,principalName);
                    	if (principal != null){
                    		Collection<SSOUser> ssoUsers = sso.getRemoteUsers(site, principal);
                    		if (ssoUsers != null && ssoUsers.size() == 1){
                    			sso.removeUser(ssoUsers.iterator().next());
                    		} else {
                        		// TODO: provide feedback, sso user not found
                        	}		
                    	} else {
                    		// TODO: provide feedback, user not found
                    	}	
                    	
                        this.clearBrowserIterator(request);
	                 }
                }
                catch (SSOException e)
                {
                    publishStatusMessage(request, "SSODetails", "status", e, "Could not remove credentials");
                }
                catch (SecurityException e)
                {
                    publishStatusMessage(request, "SSODetails", "status", e, "Could not remove credentials");
                }
            }
            else if (add != null)
            {
                // Roger: here is the principal type
                String principalType = request.getParameter("principal.type");  //group user
                String portalPrincipal = request.getParameter("portal.principal");                
                String remotePrincipal = request.getParameter("remote.principal");
                String remoteCredential = request.getParameter("remote.credential");
                
                // The principal type can benull if the user just typed the name instead of
                // using the choosers.
                
                if (principalType == null || principalType.length() == 0 )
                    principalType = "user";
                
                if (!(StringUtils.isEmpty(remotePrincipal) || StringUtils.isEmpty(remotePrincipal) || StringUtils.isEmpty(remoteCredential)))
                {
                    try
                    {
                        String siteName = (String)PortletMessaging.receive(request, "site", "selectedName");                        
                        SSOSite site = sso.getSiteByName(siteName);

                        JetspeedPrincipal localPrincipal = getJetspeedPrincipal(principalType, portalPrincipal);
                        
                        if (site != null && localPrincipal != null )
                        {
                        	if (sso.getRemoteUsers(site, localPrincipal).size() > 0)
                        	{
                                try
                                {
                                    // TODO: fixme, bug in Pluto on portlet session
                                    PortletMessaging.publish(request, "SSODetails", "status", new StatusMessage("Could not add remote user: portal principal "+localPrincipal.getName()+" is already associated with a remote user for this site!", StatusMessage.ERROR));
                                }
                                catch (Exception e)
                                {
                                    logger.error("Failed to publish message: {}", e);
                                }
                        	} else {
                            	sso.addUser(site, localPrincipal, remotePrincipal, remoteCredential);                            
                                this.clearBrowserIterator(request);
                        	}
                        }
                    }
                    catch (SSOException e)
                    {
                        publishStatusMessage(request, "SSODetails", "status", e, "Could not add credentials");
                    }
                    catch (SecurityException se)
                    {
                        publishStatusMessage(request, "SSODetails", "status", se, "Could not add credentials");
                    }                    
                }
            }            
        }
        super.processAction(request, response);
            
    }
    
    public static class RemoteAndLocalPrincipalPair {
    	SSOUser remotePrincipal;
    	JetspeedPrincipal portalPrincipal;
    	
    	RemoteAndLocalPrincipalPair(SSOUser remoteUser, JetspeedPrincipal portalPrincipal){
    		this.remotePrincipal=remoteUser;
    		this.portalPrincipal=portalPrincipal;
    	}

		public SSOUser getRemotePrincipal() {
			return remotePrincipal;
		}

		public JetspeedPrincipal getPortalPrincipal() {			
			return portalPrincipal;
		}
    	
    }

}
