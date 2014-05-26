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
package org.apache.jetspeed.portlets.sso;

import java.io.IOException;
import java.net.URI;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.security.auth.Subject;

import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.security.JSSubject;
import org.apache.jetspeed.security.PasswordCredential;
import org.apache.jetspeed.sso.SSOManager;
import org.apache.jetspeed.sso.SSOSite;
import org.apache.jetspeed.sso.SSOUser;
import org.apache.portals.applications.webcontent.portlet.IFrameGenericPortlet;
import org.apache.portals.applications.webcontent.proxy.HttpReverseProxyConstants;
import org.apache.portals.applications.webcontent.proxy.SSOSiteCredentials;
import org.apache.portals.applications.webcontent.proxy.impl.DefaultSSOSiteCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SSOReverseProxyIFramePortlet
 * 
 * @version $Id: SSOReverseProxyIFramePortlet.java 823599 2009-10-09 15:55:13Z woonsan $
 */
public class SSOReverseProxyIFramePortlet extends IFrameGenericPortlet
{
    
    public static final String SUBJECT_SSO_SITE_CREDS = "org.apache.jetspeed.portlets.sso.ssoSiteCredsOfSubject";
    
    private static Logger log = LoggerFactory.getLogger(SSOReverseProxyIFramePortlet.class);
    
    private SSOManager ssoManager;
    
    public void init(PortletConfig config) throws PortletException
    {
        super.init(config);
        
        ssoManager = (SSOManager) config.getPortletContext().getAttribute("cps:SSO");
        
        if (null == ssoManager) 
        { 
            throw new PortletException("Failed to find SSO Provider on portlet initialization"); 
        }
    }
    
    @Override
    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException
    {
        List<SSOSiteCredentials> ssoSiteCredsOfSubject = (List<SSOSiteCredentials>) request.getPortletSession().getAttribute(SUBJECT_SSO_SITE_CREDS, PortletSession.APPLICATION_SCOPE);
        
        if (ssoSiteCredsOfSubject == null)
        {
            request.getPortletSession().setAttribute(SUBJECT_SSO_SITE_CREDS, createSSOSiteCredentialsOfSubject(), PortletSession.APPLICATION_SCOPE);
            request.getPortletSession().setAttribute(HttpReverseProxyConstants.SSO_SITE_CREDENTIALS_PROVIDER, new DefaultSSOSiteCredentialsProviderImpl(), PortletSession.APPLICATION_SCOPE);
        }
        
        super.doView(request, response);
    }
    
    private List<SSOSiteCredentials> createSSOSiteCredentialsOfSubject()
    {
        List<SSOSiteCredentials> ssoSiteCredsOfSubject = new ArrayList<SSOSiteCredentials>();
            
        try
        {
            Subject subject = JSSubject.getSubject(AccessController.getContext());
            Collection<SSOSite> ssoSites = ssoManager.getSitesForSubject(subject);
            
            if (ssoSites != null)
            {
                for (SSOSite ssoSite : ssoSites)
                {
                    URI siteURI = URI.create(ssoSite.getURL());
                    Collection<SSOUser> ssoUsers = ssoManager.getRemoteUsers(ssoSite, subject);
                    
                    if (ssoUsers != null)
                    {
                        for (SSOUser ssoUser : ssoUsers)
                        {
                            DefaultSSOSiteCredentials ssoCreds = new DefaultSSOSiteCredentials(ssoSite.getURL(), siteURI.getHost(), siteURI.getPort());
                            
                            if (!StringUtils.isBlank(ssoSite.getRealm()))
                            {
                                ssoCreds.setRealm(ssoSite.getRealm());
                            }
                            
                            PasswordCredential pwc = ssoManager.getCredentials(ssoUser);
                            ssoCreds.setUsername(pwc.getUserName());
                            ssoCreds.setPassword(pwc.getPassword());
                            
                            if (ssoSite.isFormAuthentication())
                            {
                                ssoCreds.setFormAuthentication(true);
                                ssoCreds.setFormUserField(ssoSite.getFormUserField());
                                ssoCreds.setFormPwdField(ssoSite.getFormPwdField());
                            }
                            
                            ssoSiteCredsOfSubject.add(ssoCreds);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            if (log.isWarnEnabled())
            {
                log.warn("Failed to retrieve sso site credentials. {}", e.toString());
            }
        }
        
        return ssoSiteCredsOfSubject;
    }
    
}
