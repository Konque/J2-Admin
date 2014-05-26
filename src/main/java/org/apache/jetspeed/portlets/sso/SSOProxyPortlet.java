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

import java.io.IOException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.util.Collection;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.security.auth.Subject;

import org.apache.jetspeed.security.JSSubject;
import org.apache.jetspeed.security.JetspeedPrincipal;
import org.apache.jetspeed.sso.SSOClient;
import org.apache.jetspeed.sso.SSOException;
import org.apache.jetspeed.sso.SSOManager;
import org.apache.jetspeed.sso.SSOSite;
import org.apache.jetspeed.sso.SSOUser;
import org.apache.portals.bridges.common.ScriptPostProcess;
import org.apache.portals.bridges.velocity.GenericVelocityPortlet;

/**
 * SSOProxyPortlet
 * This portlet can be used as a bridge to any URL.
 * It acts as a http client and therefore it can store
 * cookies.
 * The main purpose is that the SSOProxy portlet authenticates
 * any SSO credential for the principal user not knowing in advance
 * what URL the user might select. No login prompt will appear for any url
 * in the portlet for that an SSO entry exists and the principal user has permissions.
 * 
 * @author Roger Ruttimann <rogerrut@apache.org>
 *
 */
public class SSOProxyPortlet extends GenericVelocityPortlet {
    private PortletContext context;
    private SSOManager sso;
    
    /* Re-use Proxy client inside the SSO Component */
    private boolean isAuthenticated = false;
    
    /** Default encoding UTF-8*/
    public String defaultEncoding = "UTF-8";
    
    /** Block Size */
    static final int BLOCK_SIZE = 4096;
    
    /** ACTION_PARAMETER_SSOPROXY*/
    static final String ACTION_PARAMETER_SSOPROXY = "SSOProxy";
    
    /** Preference values */
    /** DestinationURL */
    static final String DESTINATION_URL = "DestinationURL";
    
    /** SSOSite */
    static final String SSO_SITE = "SSOSite";
    
    /** ForceSSORefresh*/
    static final String FORCE_SSO_REFRESH = "ForceSSORefresh";

    /** Encoding*/
    static final String ENCODING = "Encoding";
    
    private String encoding;

    public void init(PortletConfig config) throws PortletException
    {
        super.init(config);
        context = getPortletContext();
        sso = (SSOManager) context.getAttribute("cps:SSO");
        if (null == sso)
        {
           throw new PortletException("Failed to find SSO Provider on portlet initialization");
        }
        
    }
    
    public void processAction(ActionRequest request, ActionResponse actionResponse)
    throws PortletException, IOException
    {
       	String ssoProxyAction = request.getParameter(ACTION_PARAMETER_SSOPROXY); 
       	
       	
//     	if ( ssoProxyAction != null && ssoProxyAction.length() > 0)
//     		this.destinationURL = ssoProxyAction;
//     	else
//     		this.destinationURL = request.getParameter(DESTINATION_URL);
     	
     	
        // this.ssoSiteName = request.getParameter(SSO_SITE);
        this.encoding = request.getParameter(ENCODING);
        if (this.encoding == null)
        	this.encoding =  this.defaultEncoding;

        // save the prefs
        super.processAction(request, actionResponse);
    }
    
    public void doView(RenderRequest request, RenderResponse response)
    throws PortletException, IOException
    {
        boolean forceRefresh = Boolean.parseBoolean(request.getPreferences().getValue(FORCE_SSO_REFRESH, "false"));
        String destinationURL = request.getPreferences().getValue(DESTINATION_URL,null);
        String ssoSite = request.getPreferences().getValue(SSO_SITE,null);
        
        if (ssoSite == null)
        {
            response.getWriter().print(SSOWebContentPortlet.NO_CREDENTIALS);
            return;
        }       
        // Set the content type
        response.setContentType("text/html");        
        try
        {
        	StringBuffer page= new StringBuffer();
        	SSOSite site = sso.getSiteByUrl(ssoSite);
        	if (site == null)
        	{
        		response.getWriter().println("<P>Could not find site with name "+ssoSite+"</P>");
        		return;
        	}
        	if (destinationURL == null)
        	{
        		destinationURL = site.getURL();
        	}        	
        	Principal p = request.getUserPrincipal();
        	if (p instanceof JetspeedPrincipal)
        	{
                Collection<SSOUser> remoteUsers = sso.getRemoteUsers(site,getSubject());
                if (remoteUsers.size() > 0)
                {
                    // TODO: in case of multiple users, invent a way to choose one of them
                    //   right now, simply the first SSO user is selected
                    SSOUser remoteUser = remoteUsers.iterator().next();
                    SSOClient client = sso.getClient(site, remoteUser);
                    if (client == null)
                    {
                        response.getWriter().println("<P>Could not create client for site with name " + ssoSite + " and user "+request.getUserPrincipal().getName()+"</P>");
                        return;
                    }
                    
                    client.write(destinationURL, forceRefresh, response.getWriter());

                    PortletURL actionURL = response.createActionURL();
                    ScriptPostProcess processor = new ScriptPostProcess();
                    processor.setInitalPage(page);
                    processor.postProcessPage(actionURL, ACTION_PARAMETER_SSOPROXY);
                    String finalPage = processor.getFinalizedPage();
                    
                    // Write the page
                    response.getWriter().println(finalPage);
                }
        	}
        
        }
        catch (SSOException e)
        {
        	response.getWriter().println("<P>Error rendering page. Error message<BR>" + e.getMessage() + "</P>");        	
        }          
    }
    

    public void doEdit(RenderRequest request, RenderResponse response)
    throws PortletException, IOException
    {
         super.doEdit(request, response);
    }

    /*
     * Helper methods
     */
    private Subject getSubject()
    {
        AccessControlContext context = AccessController.getContext();
        return JSSubject.getSubject(context);         
    }
    
    /*
    private String getContentCharSet(InputStream is) throws IOException
    {
        if (!is.markSupported())
        {
            return null;
        }

        byte[] buf = new byte[BLOCK_SIZE];
        try
        {
            is.read(buf, 0, BLOCK_SIZE);
            String content = new String(buf, "ISO-8859-1");
            String lowerCaseContent = content.toLowerCase();
            int startIndex = lowerCaseContent.indexOf("<head");
            if (startIndex == -1)
            {
                startIndex = 0;
            }
            int endIndex = lowerCaseContent.indexOf("</head");
            if (endIndex == -1)
            {
                endIndex = content.length();
            }
            content = content.substring(startIndex, endIndex);

            StringTokenizer st = new StringTokenizer(content, "<>");
            while (st.hasMoreTokens())
            {
                String element = st.nextToken();
                String lowerCaseElement = element.toLowerCase();
                if (lowerCaseElement.startsWith("meta") && lowerCaseElement.indexOf("content-type") > 0)
                {
                    StringTokenizer est = new StringTokenizer(element, " =\"\';");
                    while (est.hasMoreTokens())
                    {
                        if (est.nextToken().equalsIgnoreCase("charset"))
                        {
                            if (est.hasMoreTokens())
                            {
                                is.reset();
                                return est.nextToken();
                            }
                        }
                    }
                }
            }
        }
        catch (IOException e)
        {
        }

        is.reset();

        return null;
    }
    */
   
}
