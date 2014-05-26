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
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.security.auth.Subject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.auth.AuthState;
import org.apache.commons.httpclient.auth.BasicScheme;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.security.JSSubject;
import org.apache.jetspeed.security.JetspeedPrincipal;
import org.apache.jetspeed.security.PasswordCredential;
import org.apache.jetspeed.security.SecurityException;
import org.apache.jetspeed.security.UserManager;
import org.apache.jetspeed.security.mfa.util.SecurityHelper;
import org.apache.jetspeed.sso.SSOException;
import org.apache.jetspeed.sso.SSOManager;
import org.apache.jetspeed.sso.SSOSite;
import org.apache.portals.applications.gems.browser.StatusMessage;
import org.apache.portals.applications.webcontent.portlet.WebContentPortlet;
import org.apache.portals.applications.webcontent.rewriter.WebContentRewriter;
import org.apache.portals.messaging.PortletMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * SSOWebContentPortlet
 * 
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: SSOWebContentPortlet.java 929639 2010-03-31 17:51:48Z woonsan $
 */
public class SSOWebContentPortlet extends WebContentPortlet
{
    // sso.type
    public static final String SSO_TYPE = "sso.type";
    
    public static final String SSO_TYPE_HTTP = "http";                          // BOZO - depricate in favor of 'basic'
    public static final String SSO_TYPE_BASIC = "basic";          
    public static final String SSO_TYPE_BASIC_PREEMPTIVE = "basic.preemptive";
    
    public static final String SSO_TYPE_FORM = "form";
    public static final String SSO_TYPE_FORM_GET = "form.get";
    public static final String SSO_TYPE_FORM_POST = "form.post";
    
    public static final String SSO_TYPE_URL = "url";
    public static final String SSO_TYPE_URL_BASE64 = "url.base64";
    
    public static final String SSO_TYPE_CERTIFICATE = "certificate";
    
    public static final String SSO_TYPE_DEFAULT = SSO_TYPE_BASIC;  // handled well even if nothing but credentials are set (see: doRequestedAuthentication)
    
    public static final String NO_CREDENTIALS = "<p>No credentials configured for current user.</p>";
    		
    public static final String[] SSO_TYPES = 
    {
        SSO_TYPE_BASIC,
        SSO_TYPE_BASIC_PREEMPTIVE,
        SSO_TYPE_FORM,
        SSO_TYPE_FORM_GET,
        SSO_TYPE_FORM_POST,
        SSO_TYPE_URL,
        SSO_TYPE_URL_BASE64,
        SSO_TYPE_CERTIFICATE
    };
    
    // ...standardized auth types
    
    public static final String BASIC_AUTH_SCHEME_NAME = (new BasicScheme()).getSchemeName();

    // supporting parameters - for various sso types
    
    // ...names of query args for sso.type=url|url.base64
    
    public static final String SSO_TYPE_URL_USERNAME_PARAM = "sso.url.Principal";
    public static final String SSO_TYPE_URL_PASSWORD_PARAM = "sso.url.Credential";
    
    // ...names of fields for sso.type=form|form.get|form.post
    
    public static final String SSO_TYPE_FORM_ACTION_URL = "sso.form.Action";
    public static final String SSO_TYPE_FORM_ACTION_ARGS = "sso.form.Args";
    public static final String SSO_TYPE_FORM_USERNAME_FIELD = "sso.form.Principal";
    public static final String SSO_TYPE_FORM_PASSWORD_FIELD = "sso.form.Credential";
    
    // ...tags for passing creditials along on the current request object
    
    public static final String SSO_REQUEST_ATTRIBUTE_USERNAME = "sso.ra.username";
    public static final String SSO_REQUEST_ATTRIBUTE_PASSWORD = "sso.ra.password";
    
    // ...field names for EDIT mode
    
    public static final String SSO_EDIT_FIELD_PRINCIPAL = "ssoPrincipal";
    public static final String SSO_EDIT_FIELD_CREDENTIAL = "ssoCredential";
    
    // SSOWebContent session variables 

    public static final String FORM_AUTH_STATE = "ssowebcontent.form.authstate" ;
    
    
    // Class Data
    
    protected final static Logger log = LoggerFactory.getLogger(SSOWebContentPortlet.class);
    
    
    // Data Members
    
    protected PortletContext context;
    protected SSOManager sso;
    protected UserManager userManager;
    protected List<String> ssoTypesList;
    
    // Methods

    public void init(PortletConfig config) throws PortletException
    {
        super.init(config);
        context = getPortletContext();
        sso = (SSOManager)context.getAttribute("cps:SSO");
        if (null == sso)
        {
           throw new PortletException("Failed to find SSO Manager on portlet initialization");
        }        
        userManager = (UserManager) context.getAttribute(CommonPortletServices.CPS_USER_MANAGER_COMPONENT);
        if (null == userManager)
        {
            throw new PortletException("Failed to find the User Manager on portlet initialization");
        }
        ssoTypesList = new LinkedList<String>();
        for (String s : SSO_TYPES)
            ssoTypesList.add(s);
    }
    
    protected JetspeedPrincipal getLocalPrincipal(String localUserName){
        JetspeedPrincipal localPrincipal = null;
        
        try{
            localPrincipal = userManager.getUser(localUserName);
        } catch (SecurityException secex){
            
        }
        
        return localPrincipal;
    }
    
    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse)
    throws PortletException, IOException
    {
        // grab parameters - they will be cleared in processing of edit response
        String webContentParameter = actionRequest.getParameter(WebContentRewriter.ACTION_PARAMETER_URL);
        String ssoPrincipalName = actionRequest.getParameter(SSO_EDIT_FIELD_PRINCIPAL);
        String ssoPrincipalPassword = actionRequest.getParameter(SSO_EDIT_FIELD_CREDENTIAL);        

        // save the prefs
        super.processAction(actionRequest, actionResponse);
  
        // process credentials
        if (webContentParameter == null || actionRequest.getPortletMode() == PortletMode.EDIT)            
        {
            // processPreferencesAction(request, actionResponse);
            // get the POST params -- requires HTML post params named above 
            String siteUrl = actionRequest.getPreferences().getValue("SRC", "");
            String localUser = actionRequest.getUserPrincipal().getName();
            SSOSite site = sso.getSiteByUrl(siteUrl);
            try
            {        
                if (!SecurityHelper.isEmpty(siteUrl) && !SecurityHelper.isEmpty(ssoPrincipalName) && !SecurityHelper.isEmpty(ssoPrincipalPassword))
                {
                    if (site == null)
                    {
                        site = sso.newSite(siteUrl, siteUrl);
                        sso.addSite(site);
                        SSOPortletUtil.updateUser(sso, actionRequest, site, ssoPrincipalName, ssoPrincipalPassword);                                                
                    }
                    else
                    {
                        SSOPortletUtil.updateUser(sso, actionRequest, site, ssoPrincipalName, ssoPrincipalPassword);                                
                    }
                }
            }
            catch (SSOException e)
            {
                String errorMessage = "Failed to add remote user for the portal principal, " + actionRequest.getUserPrincipal().getName() + ".";
                if (e.getCause() != null)
                {
                    errorMessage += " (" + e.getCause() + ")";
                }
                StatusMessage statusMessage = new StatusMessage(errorMessage, StatusMessage.ERROR);
                PortletMessaging.publish(actionRequest, "SSOWebContent", "status", statusMessage);            
                actionResponse.setPortletMode(PortletMode.EDIT); // stay on edit                
            }                
        }
    }
        
    public void doView(RenderRequest request, RenderResponse response)
    throws PortletException, IOException
    {
        String siteName = request.getPreferences().getValue("SRC", null);
        SSOSite site = null;
        if (siteName != null)
        {
            site = sso.getSiteByUrl(siteName);
        }
        if (site == null)
        {
            response.getWriter().print(NO_CREDENTIALS);
            return;
        }
        else 
        {
        	PasswordCredential pwc = SSOPortletUtil.getCredentialsForSite(sso,siteName,request);
        	if (pwc != null)
        	{
                request.setAttribute(SSO_REQUEST_ATTRIBUTE_USERNAME, pwc.getUserName());
                request.setAttribute(SSO_REQUEST_ATTRIBUTE_PASSWORD, pwc.getPassword());
        	} else 
        	{
                response.getWriter().print(NO_CREDENTIALS);
                return;
        	}
        }
        StatusMessage msg = (StatusMessage)PortletMessaging.consume(request, "SSOWebContent", "status");
        if (msg != null)
        {
            this.getContext(request).put("statusMsg", msg);            
        }                          
        super.doView(request, response);
    }
    

    public void doEdit(RenderRequest request, RenderResponse response)
    throws PortletException, IOException
    {
        String site = request.getPreferences().getValue("SRC", "");
    	PasswordCredential pwc = SSOPortletUtil.getCredentialsForSite(sso,site,request);
    	if (pwc != null)
    	{
            getContext(request).put(SSO_EDIT_FIELD_PRINCIPAL, pwc.getUserName());
            getContext(request).put(SSO_EDIT_FIELD_CREDENTIAL, pwc.getPassword());
        } 
    	else 
    	{
            // no credentials configured in SSO store
            // switch to SSO Configure View
            getContext(request).put(SSO_EDIT_FIELD_PRINCIPAL, "");
            getContext(request).put(SSO_EDIT_FIELD_CREDENTIAL, "");
        }
        StatusMessage msg = (StatusMessage)PortletMessaging.consume(request, "SSOWebContent", "status");
        if (msg != null)
        {
            this.getContext(request).put("statusMsg", msg);            
        }     
        this.getContext(request).put("ssoTypes", SSO_TYPES);
        this.getContext(request).put("ssoTypeSelected", request.getPreferences().getValue("sso.type", SSO_TYPE_BASIC));
        super.doEdit(request, response);
    }

    private Subject getSubject()
    {
        AccessControlContext context = AccessController.getContext();
        return JSSubject.getSubject(context);         
    }
    
    protected byte[] doPreemptiveAuthentication(HttpClient client,HttpMethod method, RenderRequest request, RenderResponse response)
    {
    	byte[] result = super.doPreemptiveAuthentication(client, method, request, response);
        if ( result != null)
        {
            // already handled
            return result ;
        }
        
        // System.out.println("SSOWebContentPortlet.doPreemptiveAuthentication...");
        
        PortletPreferences prefs = request.getPreferences();
        String type = getSingleSignOnAuthType(prefs);

        if (type.equalsIgnoreCase(SSO_TYPE_BASIC_PREEMPTIVE))
        {
            // Preemptive, basic authentication
            String userName = (String)request.getAttribute(SSO_REQUEST_ATTRIBUTE_USERNAME);
            if (userName == null) userName = "";
            String password = (String)request.getAttribute(SSO_REQUEST_ATTRIBUTE_PASSWORD);
            if (password == null) password = "";
            
            // System.out.println("...performing preemptive basic authentication with userName: "+userName+", and password: "+password);
            method.setDoAuthentication(true);
            method.getHostAuthState().setPreemptive();
            client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, password));
            
            // handled!
            return result ;
            
        }
        else if (type.startsWith(SSO_TYPE_FORM))
        {
            try
            {
                Boolean formAuth = (Boolean)PortletMessaging.receive(request, FORM_AUTH_STATE);
                if (formAuth != null)
                {
                    // already been here, done that
                    return (formAuth.booleanValue() ? result : null);
                }
                else
                {
                    // stop recursion, but assume failure, ...for now
                    PortletMessaging.publish(request, FORM_AUTH_STATE, Boolean.FALSE);
                }

                String formAction = prefs.getValue(SSO_TYPE_FORM_ACTION_URL, "");
                if (formAction == null || formAction.length() == 0)
                {
                    log.warn("sso.type specified as 'form', but no: "+SSO_TYPE_FORM_ACTION_URL+", action was specified - unable to preemptively authenticate by form.");
                    return null ;
                }
                String userNameField = prefs.getValue(SSO_TYPE_FORM_USERNAME_FIELD, "");
                if (userNameField == null || userNameField.length() == 0)
                {
                    log.warn("sso.type specified as 'form', but no: "+SSO_TYPE_FORM_USERNAME_FIELD+", username field was specified - unable to preemptively authenticate by form.");
                    return null ;
                }
                String passwordField = prefs.getValue(SSO_TYPE_FORM_PASSWORD_FIELD, "password");
                if (passwordField == null || passwordField.length() == 0)
                {
                    log.warn("sso.type specified as 'form', but no: "+SSO_TYPE_FORM_PASSWORD_FIELD+", password field was specified - unable to preemptively authenticate by form.");
                    return null ;
                }
                
                String userName = (String)request.getAttribute(SSO_REQUEST_ATTRIBUTE_USERNAME);
                if (userName == null) userName = "";
                String password = (String)request.getAttribute(SSO_REQUEST_ATTRIBUTE_PASSWORD);
                if (password == null) password = "";

                // get submit method
                int i = type.indexOf('.');
                boolean isPost = i > 0 ? type.substring(i+1).equalsIgnoreCase("post") : true ;    // default to post, since it is a form 
            
                // get parameter map
                HashMap formParams = new HashMap();
                formParams.put(userNameField,new String[]{ userName });
                formParams.put(passwordField,new String[]{ password });
                String formArgs = prefs.getValue(SSO_TYPE_FORM_ACTION_ARGS, "");
                if (formArgs != null && formArgs.length() > 0)
                {
                    StringTokenizer iter = new StringTokenizer(formArgs, ";");
                    while (iter.hasMoreTokens())
                    {
                        String pair = iter.nextToken();
                        i = pair.indexOf('=') ;
                        if (i > 0)
                            formParams.put(pair.substring(0,i), new String[]{pair.substring(i+1)});
                    }
                }

                // resuse client - in case new cookies get set - but create a new method (for the formAction)
                String formMethod = (isPost) ? FORM_POST_METHOD : FORM_GET_METHOD;                
                method = getHttpMethod(client, getURLSource(formAction, formParams, request, response), formParams, formMethod, request);
                // System.out.println("...posting credentials");
                result = doHttpWebContent(client, method, 0, request, response) ;
                // System.out.println("Result of attempted authorization: "+success);
                PortletMessaging.publish(request, FORM_AUTH_STATE, Boolean.valueOf(result != null));
                return result ;
            }
            catch (Exception ex)
            {
                // bad
                log.error("Form-based authentication failed", ex);
            }
        }
        else if (type.equalsIgnoreCase(SSO_TYPE_URL) || type.equalsIgnoreCase(SSO_TYPE_URL_BASE64))
        {
            // set user name and password parameters in the HttpMethod
            String userNameParam = prefs.getValue(SSO_TYPE_URL_USERNAME_PARAM, "");
            if (userNameParam == null || userNameParam.length() == 0)
            {
                log.warn("sso.type specified as 'url', but no: "+SSO_TYPE_URL_USERNAME_PARAM+", username parameter was specified - unable to preemptively authenticate by URL.");
                return null ;
            }
            String passwordParam = prefs.getValue(SSO_TYPE_URL_PASSWORD_PARAM, "");
            if (passwordParam == null || passwordParam.length() == 0)
            {
                log.warn("sso.type specified as 'url', but no: "+SSO_TYPE_URL_PASSWORD_PARAM+", password parameter was specified - unable to preemptively authenticate by URL.");
                return null ;
            }
            String userName = (String)request.getAttribute(SSO_REQUEST_ATTRIBUTE_USERNAME);
            if (userName == null) userName = "";
            String password = (String)request.getAttribute(SSO_REQUEST_ATTRIBUTE_PASSWORD);
            if (password == null) password = "";
            if (type.equalsIgnoreCase(SSO_TYPE_URL_BASE64))
            {
                Base64 encoder = new Base64() ;
                userName = new String(encoder.encode(userName.getBytes()));
                password = new String(encoder.encode(password.getBytes()));
            }
            
            // GET and POST accept args differently
            if ( method instanceof PostMethod )
            {
                // add POST data
                PostMethod postMethod = (PostMethod)method ;
                postMethod.addParameter(userNameParam, userName);
                postMethod.addParameter(passwordParam, password);
            }
            else
            {
                // augment GET query string
                NameValuePair[] authPairs = new NameValuePair[]{ new NameValuePair(userNameParam, userName), new NameValuePair(passwordParam, password) } ; 
                String existingQuery = method.getQueryString() ;
                method.setQueryString(authPairs);
                if (existingQuery != null && existingQuery.length() > 0)
                {
                    // augment existing query with new auth query
                    existingQuery = existingQuery + '&' + method.getQueryString();
                    method.setQueryString(existingQuery);
                }
            }
            
            return result ;
        }
        // else System.out.println("...sso.type: "+type+", no pre-emptive authentication");
        
        // not handled
        return null ;
    }

    protected boolean doRequestedAuthentication(HttpClient client,HttpMethod method, RenderRequest request, RenderResponse response)
    {
        if ( super.doRequestedAuthentication(client, method, request, response))
        {
            // already handled
            return true ;
        }
        
        // System.out.println("SSOWebContentPortlet.doRequestedAuthentication...");
        
        if (method.getHostAuthState().getAuthScheme().getSchemeName().equals(BASIC_AUTH_SCHEME_NAME))
        {
            // Basic authentication being requested
            String userName = (String)request.getAttribute(SSO_REQUEST_ATTRIBUTE_USERNAME);
            if (userName == null) userName = "";
            String password = (String)request.getAttribute(SSO_REQUEST_ATTRIBUTE_PASSWORD);
            if (password == null) password = "";
            
            // System.out.println("...providing basic authentication with userName: "+userName+", and password: "+password);
            method.setDoAuthentication(true);
            AuthState state = method.getHostAuthState();
            AuthScope scope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, state.getRealm(), state.getAuthScheme().getSchemeName()) ;
            client.getState().setCredentials(scope, new UsernamePasswordCredentials(userName, password));
            
            // handled!
            return true ;
        }
        else
        {
            log.warn("SSOWebContentPortlent.doAuthenticate() - unexpected authentication scheme: "+method.getHostAuthState().getAuthScheme().getSchemeName());
        }

        // only know how to handle Basic authentication, in this context
        return false;
    }
    
    protected String getSingleSignOnAuthType(PortletPreferences prefs)
    {
        String type = prefs.getValue(SSO_TYPE,SSO_TYPE_DEFAULT);
        
        if (type != null && type.equalsIgnoreCase(SSO_TYPE_HTTP))
        {
            log.warn("sso.type: "+SSO_TYPE_HTTP+", has been deprecated - use: "+SSO_TYPE_BASIC+", or: "+SSO_TYPE_BASIC_PREEMPTIVE);
            type = SSO_TYPE_BASIC ;
        }
        
        return type ;
    }
}
