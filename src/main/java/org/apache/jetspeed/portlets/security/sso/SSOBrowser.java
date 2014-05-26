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
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.sso.SSOException;
import org.apache.jetspeed.sso.SSOManager;
import org.apache.jetspeed.sso.SSOSite;
import org.apache.portals.applications.gems.browser.BrowserIterator;
import org.apache.portals.applications.gems.browser.BrowserPortlet;
import org.apache.portals.applications.gems.browser.DatabaseBrowserIterator;
import org.apache.portals.applications.gems.browser.StatusMessage;
import org.apache.portals.messaging.PortletMessaging;
import org.apache.velocity.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SSOBrowser
 * 
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: SSOBrowser.java 348264 2005-11-22 22:06:45Z taylor $
 */
public class SSOBrowser extends BrowserPortlet
{
    
    static final Logger logger = LoggerFactory.getLogger(SSOBrowser.class);
    
    private SSOManager sso;
    
    public void init(PortletConfig config)
    throws PortletException 
    {
        super.init(config);
        sso = (SSOManager)getPortletContext().getAttribute(CommonPortletServices.CPS_SSO_COMPONENT);
        if (null == sso)
        {
            throw new PortletException("Failed to find the SSO Provider on portlet initialization");
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
            resultSetTitleList.add(0, "Url");
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
        String selectedSiteName = (String)PortletMessaging.receive(request, "site", "selectedName");
        if (selectedSiteName != null)
        {        
            Context context = this.getContext(request);
            context.put("currentName", selectedSiteName);  
            context.put("currentUrl", (String)PortletMessaging.receive(request, "site", "selectedUrl"));
            
            String realm = (String)PortletMessaging.receive(request, "site", "realm");
            context.put("currentRealm", realm);  
            String userField = (String)PortletMessaging.receive(request, "site", "idField");
            context.put("currentFFID", userField);  
            String pwdFiled = (String)PortletMessaging.receive(request, "site", "pwdField");
            context.put("currentFFPWD", pwdFiled);

            
            
            
        }
        StatusMessage msg = (StatusMessage)PortletMessaging.consume(request, "SSOBrowser", "status");
        if (msg != null)
        {
            this.getContext(request).put("statusMsg", msg);            
        }
        
        super.doView(request, response);
    }
    
    protected void clearPortletMessages(PortletRequest request, PortletResponse response){
    	PortletMessaging.cancel(request, "site", "selectedName");
        PortletMessaging.cancel(request, "site", "selectedUrl");      
        PortletMessaging.cancel(request, "site", "realm");
        PortletMessaging.cancel(request, "site", "idField");
        PortletMessaging.cancel(request, "site", "pwdField");	
    }
    
    protected void setPortletMessage(PortletRequest request, String key, String msg) throws IOException{
    	if (StringUtils.isNotEmpty(msg)){
    		PortletMessaging.publish(request, "site", key, msg);
    	} else {
    		 PortletMessaging.cancel(request, "site", key);	
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
                    setPortletMessage(request, "realm", site.getRealm());
                    setPortletMessage(request, "idField", site.getFormUserField());
                    setPortletMessage(request, "pwdField", site.getFormPwdField());
                }
            }
            String refresh = request.getParameter("sso.refresh");
            String save = request.getParameter("sso.save");
            String neue = request.getParameter("sso.new");
            String delete = request.getParameter("ssoDelete");
            
            if (refresh != null)
            {
                this.clearBrowserIterator(request);
            }
            else if (neue != null)
            {
            	clearPortletMessages(request,response);
            }
            else if (delete != null && (!(isEmpty(delete))))
            {
            	try
                {
                    SSOSite site = null;
                    site = sso.getSiteByName(delete);
                    if (site != null)
                    {
                        sso.removeSite(site);
                        this.clearBrowserIterator(request);
                        clearPortletMessages(request,response);
                    }
                }
                catch (SSOException e)
                {
                    publishStatusMessage(request, "SSOBrowser", "status", e, "Could not remove site");
                }
            	
            }
            else if (save != null)
            {
                String siteName = request.getParameter("site.name");                
                String siteUrl = request.getParameter("site.url");
                
                String siteRealm = request.getParameter("site.realm");                
                String siteFormID = request.getParameter("site.form_field_ID");
                String siteFormPWD = request.getParameter("site.form_field_PWD");
                 
                if (!(isEmpty(siteName) || isEmpty(siteUrl)))
                {
                    try
                    {
                        SSOSite site = null;
                        String old = (String)PortletMessaging.receive(request, "site", "selectedName");
                        if (old != null)
                        {
                            site = sso.getSiteByName(old);
                        }
                        else
                        {
                            site = sso.getSiteByName(siteName);
                        } 
                        boolean isNewSite = false;
                        if (site == null)
                        {
                        	isNewSite = true;
                        	site = sso.newSite(siteName, siteUrl);
                        }
                        
                        site.setName(siteName);
                        site.setURL(siteUrl);
                        site.setRealm(siteRealm);
                        site.setFormUserField(siteFormID);
                    	site.setFormPwdField(siteFormPWD);
                    	site.setFormAuthentication(StringUtils.isNotEmpty(siteFormID) && StringUtils.isNotEmpty(siteFormPWD));
                    	site.setChallengeResponseAuthentication(!site.isFormAuthentication());
                    	
                    	if (isNewSite)
                    	{
                    		sso.addSite(site);
                    	} else 
                    	{
                    		sso.updateSite(site);
                    	}
                    	this.clearBrowserIterator(request);
                        PortletMessaging.publish(request, "site", "selectedName", siteName);
                        PortletMessaging.publish(request, "site", "selectedUrl", siteUrl);    
                        PortletMessaging.publish(request, "site", "realm", siteRealm);
                        PortletMessaging.publish(request, "site", "idField",siteFormID);
                        PortletMessaging.publish(request, "site", "pwdField", siteFormPWD);
                    }
                    catch (SSOException e)
                    {
                        publishStatusMessage(request, "SSOBrowser", "status", e, "Could not store site");
                    }
                }
            }   
        }
        super.processAction(request, response);
            
    }

    private boolean isEmpty(String s)
    {
        if (s == null) return true;
        
        if (s.trim().equals("")) return true;
        
        return false;
    }
    
}
