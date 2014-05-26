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

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceURL;

import org.apache.commons.codec.binary.Base64;
import org.apache.jetspeed.security.PasswordCredential;
import org.apache.jetspeed.security.mfa.util.SecurityHelper;
import org.apache.jetspeed.sso.SSOException;
import org.apache.jetspeed.sso.SSOManager;
import org.apache.jetspeed.sso.SSOSite;
import org.apache.jetspeed.sso.SSOUser;
import org.apache.portals.applications.gems.browser.StatusMessage;
import org.apache.portals.applications.webcontent.portlet.IFrameGenericPortlet;
import org.apache.portals.messaging.PortletMessaging;

/**
 * SSOIFramePortlet
 * 
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: SSOIFramePortlet.java 991049 2010-08-31 00:30:29Z woonsan $
 */
public class SSOIFramePortlet extends IFrameGenericPortlet
{
    public static final String SSO_TYPE = "sso.type";
    public static final String SSO_TYPE_URL = "url";
    public static final String SSO_TYPE_URL_BASE64 = "url.base64";
    public static final String SSO_TYPE_HTTP = "http";
    public static final String SSO_TYPE_CERTIFICATE = "certificate";
    public static final String SSO_TYPE_FORM = "form";
    public static final String SSO_TYPE_FORM_GET = "form.get";
    public static final String SSO_TYPE_FORM_POST = "form.post";
    
    public static final String SSO_TYPE_URL_USERNAME = "sso.url.Principal";
    public static final String SSO_TYPE_URL_PASSWORD = "sso.url.Credential";
    
    public static final String SSO_TYPE_FORM_USERNAME = "sso.form.Principal";
    public static final String SSO_TYPE_FORM_PASSWORD = "sso.form.Credential";
    public static final String SSO_TYPE_FORM_ACTION = "sso.form.Action";
    public static final String SSO_TYPE_FORM_ARGS = "sso.form.Args";
    
    public static final String SSO_TYPE_FORM_AUTH_FLAG = SSOIFramePortlet.class.getName() + ".authFlag";
    
    public static final String SSO_REQUEST_ATTRIBUTE_USERNAME = "sso.ra.username";
    public static final String SSO_REQUEST_ATTRIBUTE_PASSWORD = "sso.ra.password";
    
    /*
     * The constants must be used in your HTML form for the SSO principal and
     * credential
     */
    public static final String SSO_FORM_PRINCIPAL = "ssoPrincipal";
    public static final String SSO_FORM_CREDENTIAL = "ssoCredential";
    
    private PortletContext context;
    private SSOManager sso;

    public void init(PortletConfig config) throws PortletException
    {
        super.init(config);
        context = getPortletContext();
        sso = (SSOManager) context.getAttribute("cps:SSO");
        if (null == sso) { throw new PortletException("Failed to find SSO Provider on portlet initialization"); }
    }

    public void doEdit(RenderRequest request, RenderResponse response) throws PortletException, IOException
    {
        String siteUrl = request.getPreferences().getValue("SRC", "");
        SSOSite site = sso.getSiteByUrl(siteUrl);
        if (site != null)
        {
            try
            {
                SSOUser remoteUser = SSOPortletUtil.getRemoteUser(sso, request, site);
                if (remoteUser != null)
                {
                    PasswordCredential pwc = sso.getCredentials(remoteUser);
                    getContext(request).put(SSO_FORM_PRINCIPAL, pwc.getUserName());
                    getContext(request).put(SSO_FORM_CREDENTIAL, pwc.getPassword());
                }
                else
                {
                    getContext(request).put(SSO_FORM_PRINCIPAL, "");
                    getContext(request).put(SSO_FORM_CREDENTIAL, "");
                }
            }
            catch (SSOException e)
            {
                if (e.getMessage().equals(SSOException.NO_CREDENTIALS_FOR_SITE))
                {
                    // no credentials configured in SSO store
                    // switch to SSO Configure View
                    getContext(request).put(SSO_FORM_PRINCIPAL, "");
                    getContext(request).put(SSO_FORM_CREDENTIAL, "");
                }
                else
                {
                    SSOPortletUtil.publishStatusMessage(request, "SSOIFrame", "status", e, "Could not load Site info for user");
                }                
            }
        }
        StatusMessage msg = (StatusMessage)PortletMessaging.consume(request, "SSOIFrame", "status");
        if (msg != null)
        {
            this.getContext(request).put("statusMsg", msg);            
        }                
        this.getContext(request).put("ssoTypes", SSOWebContentPortlet.SSO_TYPES);
        this.getContext(request).put("ssoTypeSelected", request.getPreferences().getValue("sso.type", SSOWebContentPortlet.SSO_TYPE_BASIC));        
        super.doEdit(request, response);
    }

    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException
    {
        String siteUrl = request.getPreferences().getValue("SRC", null);
        SSOSite site = null;
        if (siteUrl != null)
        {
            site = sso.getSiteByUrl(siteUrl);
        }
        if (site == null)
        {
            response.getWriter().print(SSOWebContentPortlet.NO_CREDENTIALS);
            return;
        }
        try
        {
            SSOUser remoteUser = SSOPortletUtil.getRemoteUser(sso, request, site);
            if (remoteUser != null)
            {
                PasswordCredential pwc = sso.getCredentials(remoteUser);
                request.setAttribute(SSO_REQUEST_ATTRIBUTE_USERNAME, pwc.getUserName());
                request.setAttribute(SSO_REQUEST_ATTRIBUTE_PASSWORD, pwc.getPassword());
            }
            else
            {
                response.getWriter().print(SSOWebContentPortlet.NO_CREDENTIALS);
                return;
            }
        }
        catch (SSOException e)
        {
            if (e.getMessage().equals(SSOException.NO_CREDENTIALS_FOR_SITE))
            {
                response.getWriter().print(SSOWebContentPortlet.NO_CREDENTIALS);
                return;
            }
            else
            {
                SSOPortletUtil.publishStatusMessage(request, "SSOIFrame", "status", e, "Could not load Site info for user");
            }
        }
        StatusMessage msg = (StatusMessage)PortletMessaging.consume(request, "SSOIFrame", "status");
        if (msg != null)
        {
            this.getContext(request).put("statusMsg", msg);            
        }        
        super.doView(request, response);
    }

    public void processAction(ActionRequest request, ActionResponse actionResponse) throws PortletException, IOException
    {
        // save the prefs
        super.processAction(request, actionResponse);

        // get the POST params -- requires HTML post params named
        // ssoUserName
        String ssoPrincipal = request.getParameter(SSO_FORM_PRINCIPAL);
        String ssoCredential = request.getParameter(SSO_FORM_CREDENTIAL);
        if (ssoPrincipal == null || ssoCredential == null)
        {
            actionResponse.setPortletMode(PortletMode.EDIT); // stay on edit
        }
        String siteUrl = request.getPreferences().getValue("SRC", "");
        SSOSite site = sso.getSiteByUrl(siteUrl);
        try
        {
            if (!SecurityHelper.isEmpty(siteUrl) && !SecurityHelper.isEmpty(ssoPrincipal) && !SecurityHelper.isEmpty(ssoCredential))
            {
                if (site == null)
                {
                    site = sso.newSite(siteUrl, siteUrl);
                    sso.addSite(site);
                    SSOPortletUtil.updateUser(sso, request, site, ssoPrincipal, ssoCredential);
                }
                else
                {
                    SSOPortletUtil.updateUser(sso, request, site, ssoPrincipal, ssoCredential);
                }
            }
        }
        catch (SSOException e)
        {
            String errorMessage = "Failed to add remote user for the portal principal, " + request.getUserPrincipal().getName() + ".";
            if (e.getCause() != null)
            {
                errorMessage += " (" + e.getCause() + ")";
            }
            StatusMessage statusMessage = new StatusMessage(errorMessage, StatusMessage.ERROR);
            PortletMessaging.publish(request, "SSOIFrame", "status", statusMessage);
            actionResponse.setPortletMode(PortletMode.EDIT); // stay on edit            
        }

    }

    public String getURLSource(RenderRequest request, RenderResponse response, PortletPreferences prefs)
    {
        String baseSource = super.getURLSource(request, response, prefs);
        String type = prefs.getValue(SSO_TYPE, SSO_TYPE_URL);
        if (type.equals(SSO_TYPE_URL) || type.equals(SSO_TYPE_URL_BASE64))
        {
            String userNameParam = prefs.getValue(SSO_TYPE_URL_USERNAME, "user");
            String passwordParam = prefs.getValue(SSO_TYPE_URL_PASSWORD, "password");
            StringBuffer source = new StringBuffer(baseSource);
            if (baseSource.indexOf("?") == -1)
            {
                source.append("?");
            }
            else
            {
                source.append("&");
            }
            source.append(userNameParam);
            source.append("=");

            String userName = (String) request.getAttribute(SSO_REQUEST_ATTRIBUTE_USERNAME);
            if (userName == null)
                userName = "";
            String password = (String) request.getAttribute(SSO_REQUEST_ATTRIBUTE_PASSWORD);
            if (password == null)
                password = "";

            if (type.equals(SSO_TYPE_URL_BASE64))
            {
                Base64 encoder = new Base64();
                userName = new String(encoder.encode(userName.getBytes()));
                password = new String(encoder.encode(password.getBytes()));
            }

            source.append(userName);
            source.append("&");
            source.append(passwordParam);
            source.append("=");
            source.append(password);

            return response.encodeURL(source.toString());
        }
        else if (type.equals(SSO_TYPE_FORM) || type.equals(SSO_TYPE_FORM_GET) || type.equals(SSO_TYPE_FORM_POST))
        {
            PortletSession portletSession = request.getPortletSession(false);
            
            if (portletSession == null || portletSession.getAttribute(SSO_TYPE_FORM_AUTH_FLAG) == null)
            {
                ResourceURL ssoLoginUrl = response.createResourceURL();
                ssoLoginUrl.setResourceID("/WEB-INF/security/sso/sso-iframe-form-login.jsp");
                return ssoLoginUrl.toString();
            }
            
            return baseSource;
        }
        else
        {
            return baseSource;
        }
    }

}
