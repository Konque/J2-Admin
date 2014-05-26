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
package org.apache.jetspeed.portlets.registration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.PortalReservedParameters;
import org.apache.jetspeed.administration.AdministrationEmailException;
import org.apache.jetspeed.administration.PortalAdministration;
import org.apache.jetspeed.locator.JetspeedTemplateLocator;
import org.apache.jetspeed.locator.LocatorDescriptor;
import org.apache.jetspeed.locator.TemplateDescriptor;
import org.apache.jetspeed.locator.TemplateLocatorException;
import org.apache.jetspeed.request.RequestContext;
import org.apache.jetspeed.security.PasswordCredential;
import org.apache.jetspeed.security.SecurityException;
import org.apache.jetspeed.security.User;
import org.apache.jetspeed.security.UserManager;
import org.apache.portals.bridges.velocity.AbstractVelocityMessagingPortlet;
import org.apache.portals.gems.util.ValidationHelper;
import org.apache.velocity.context.Context;

/**
 * This portlet allows a logged on user to change its password.
 *
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @author <a href="mailto:chris@bluesunrise.com">Chris Schaefer</a>
 * @version $Id$
 */
public class ForgottenPasswordPortlet extends AbstractVelocityMessagingPortlet
{
    private PortalAdministration admin;

    private UserManager userManager;

    // Request Params 
    private static final String RP_EMAIL_ADDRESS = "email";

    // Messages 
    private static final String MSG_MESSAGE = "MSG";
    private static final String MSG_CHANGEDPW_MSG = "CH_PWD_MSD";

    // Context Variables
    private static final String CTX_EMAIL_ADDRESS = "email";

    private static final String CTX_RETURN_URL = "returnURL";

    private static final String CTX_NEW_PASSWORD = "password";

    private static final String CTX_USER_NAME = "username";

    private static final String CTX_MESSAGE = "MSG";

    private static final String CTX_CHANGEDPW_MSG = "updatedPWMsg";

    // Init Parameter Constants
    private static final String IP_REDIRECT_PATH = "redirectPath";

    private static final String IP_RETURN_URL = "returnURL";

    private static final String IP_TEMPLATE_LOCATION = "emailTemplateLocation";

    private static final String IP_TEMPLATE_NAME = "emailTemplateName";

    // Resource Bundle
    private static final String RB_EMAIL_SUBJECT = "email.subject.forgotten.password";

    private static final String PATH_SEPARATOR = "/";

    /** email template to use for merging */
    private String templateLocation;

    private String templateName;

    private JetspeedTemplateLocator templateLocator;

    /** servlet path of the return url to be printed and href'd in email template */
    private String returnUrlPath;

    /** path where to redirect to after pressing submit on the form */
    private String redirectPath;

    /** localized emailSubject */
    private String emailSubject = null;
    
    public void init(PortletConfig config) throws PortletException
    {
        super.init(config);
        admin = (PortalAdministration) getPortletContext().getAttribute(
                CommonPortletServices.CPS_PORTAL_ADMINISTRATION);
        if (null == admin) { throw new PortletException(
                "Failed to find the Portal Administration on portlet initialization"); }
        userManager = (UserManager) getPortletContext().getAttribute(
                CommonPortletServices.CPS_USER_MANAGER_COMPONENT);
        if (null == userManager) { throw new PortletException(
                "Failed to find the User Manager on portlet initialization"); }

        this.returnUrlPath = config.getInitParameter(IP_RETURN_URL);
        this.redirectPath = config.getInitParameter(IP_REDIRECT_PATH);
        this.templateLocation = config.getInitParameter(IP_TEMPLATE_LOCATION);
        if (templateLocation == null)
        {
            templateLocation = "/WEB-INF/view/userreg/";
        }
        templateLocation = getPortletContext().getRealPath(templateLocation);
        this.templateName = config.getInitParameter(IP_TEMPLATE_NAME);
        if (templateName == null)
        {
            templateName = "forgottenPasswdEmail.vm";
        }
        
        ArrayList roots = new ArrayList(1);
        roots.add(templateLocation);

        try
        {
            templateLocator = new JetspeedTemplateLocator(roots, "email", getPortletContext().getRealPath("/"));
            templateLocator.start();
        }
        catch (FileNotFoundException e)
        {
            throw new PortletException("Could not start the template locator.", e);
        }

        
    }

    private boolean isValidGUID(String guid)
    {
        Map map = admin.getNewLoginInfo(guid);
        
        if (map != null) { return true; }
        return false;
    }

    private boolean updatePasswordFromGUID(String guid)
    {
        Map map = admin.getNewLoginInfo(guid);
        
        String userName = (String) map.get("user.name");
        String newPassword = (String) map.get("password");

        // Here's where a break should be.   The following code should be put into the RETURN portlet
        try
        {
            User user = userManager.getUser(userName);
            PasswordCredential pwc = userManager.getPasswordCredential(user);
            pwc.setPassword(null, newPassword);
            pwc.setUpdateRequired(true);
            userManager.storePasswordCredential(pwc);
            
            // if we got here stuff is changed... removed the key from the map
            admin.removeNewLoginInfo(guid);
        } catch (SecurityException e)
        {
            return false;
        }
        return true;
    }

    public void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException
    {
        response.setContentType("text/html");
        Context context = getContext(request);
        String email = request.getParameter(RP_EMAIL_ADDRESS);
        String guid = request.getParameter("guid");

        ResourceBundle resource = getPortletConfig().getResourceBundle(request.getLocale());

        if (guid != null) 
        {
            if(isValidGUID(guid)) 
            {
                try
                {
                    updatePasswordFromGUID(guid);
                    context
                            .put(CTX_CHANGEDPW_MSG, resource.getString("forgotten.successful_pw_update"));
                } catch (Exception e)
                {
                    context.put(CTX_MESSAGE,resource.getString("forgotten.unable_to_update_pw"));
                }
            } else {
                // invalid GUID
                context
                .put(CTX_CHANGEDPW_MSG,resource.getString("forgotten.password_update_link_invalid"));
            }
        } else {
            // might be returning from initial request
            context.put(CTX_CHANGEDPW_MSG,consumeRenderMessage(request, MSG_CHANGEDPW_MSG));
        }
        context.put(CTX_EMAIL_ADDRESS, email);
        context.put(CTX_MESSAGE, consumeRenderMessage(request, MSG_MESSAGE));
        super.doView(request, response);
    }

    public static String makeGUID(String user, String newpw)
    {
        // This is a quicky version
        long num = (long) user.hashCode() + (long) newpw.hashCode(); //  Possible collisions here...
        long d = new Date().getTime();
        long val = num * d;
        String retval = Long.toHexString(val);
        return retval;
    }

    public void processAction(ActionRequest request, ActionResponse response)
            throws PortletException, IOException
    {
        List errors = new LinkedList();

        String email = request.getParameter(RP_EMAIL_ADDRESS);
        Locale locale = request.getLocale();

        ResourceBundle resource = getPortletConfig().getResourceBundle(locale);

        // validation
        if (!ValidationHelper.isEmailAddress(email, true, 80))
        {
            errors.add(resource.getString("forgotten.invalid_email_format_entered"));
        }

        if (errors.size() > 0)
        {
            publishRenderMessage(request, MSG_MESSAGE, errors);
            return;
        }

        User user = null;
        try
        {
            user = admin.lookupUserFromEmail(email);
        } catch (Exception e)
        {
            publishRenderMessage(
                    request,
                    MSG_MESSAGE,
                    makeMessage(resource.getString("forgotten.email_address_not_found")));
            return;
        }

        try
        {
            String userName = user.getName();

            String newPassword = admin.generatePassword();

            String urlGUID = makeGUID(userName, newPassword);

            Map<String, String> userAttributes = new HashMap<String, String>();
            Map<String, String> userInfoMap = user.getInfoMap();
            
            for (Map.Entry<String, String> entry : userInfoMap.entrySet())
            {
                userAttributes.put(entry.getKey(), entry.getValue());
            }

            // special attributes
            userAttributes.put(CTX_RETURN_URL, generateReturnURL(request,
                    response, urlGUID));
            userAttributes.put(CTX_NEW_PASSWORD, newPassword);
            userAttributes.put(CTX_USER_NAME, userName);

            String templ = getTemplatePath(request, response);
            
            if (templ == null) 
            { 
                throw new Exception("email template not available"); 
            }
            admin.sendEmail(this.getPortletConfig(), email,
                    getEmailSubject(request),templ, userAttributes);

            //TODO this is currently hacked with a hashmap... needs to move to either a DB table
            // or to some sort of credential
            Map<String, String> map = new HashMap<String, String>();
            map.put("user.name",userName);
            map.put("password",newPassword);
            admin.putNewLoginInfo(urlGUID, map);

            publishRenderMessage(
                    request,
                    MSG_CHANGEDPW_MSG,
                    makeMessage(resource.getString("an_email_has_been_sent")));
            
            response.sendRedirect(generateRedirectURL(request, response));
        } 
        catch (AdministrationEmailException e)
        {
            publishRenderMessage(request, CTX_MESSAGE, makeMessage(e
                    .getMessage()));
        } 
        catch (Exception e)
        {
            publishRenderMessage(request, CTX_MESSAGE,
                    makeMessage(resource.getString("failed_to_send") + e.toString()));
        }

    }

    protected String getEmailSubject(PortletRequest request)
    {
        ResourceBundle resource = getPortletConfig().getResourceBundle(
                request.getLocale());
        try
        {
            this.emailSubject = resource.getString(RB_EMAIL_SUBJECT);
        } catch (Exception e)
        {
            //TODO  report missing resource somehow
        }
        if (this.emailSubject == null)
                this.emailSubject = "Password Notification";
        return this.emailSubject;
    }

    protected String generateReturnURL(PortletRequest request,
                                       PortletResponse response,
                                       String urlGUID)
    {
        String fullPath = this.returnUrlPath + "?guid=" + urlGUID; 
        // NOTE: getPortalURL will encode the fullPath for us
        String url = admin.getPortalURL(request, response, fullPath);
        return url;
    }

    protected String generateRedirectURL(PortletRequest request,
                                         PortletResponse response)
                                         
    {
        return admin.getPortalURL(request, response, this.redirectPath);
    }
    
    protected List makeMessage(String msg)
    {
        List errors = new LinkedList();
        errors.add(msg);
        return errors;
    }
    
    protected String getTemplatePath(ActionRequest request, ActionResponse response)
    {
        if (templateLocator == null)
        {
            return templateLocation + PATH_SEPARATOR + templateName;
        }

        RequestContext requestContext = (RequestContext) request
                .getAttribute(PortalReservedParameters.REQUEST_CONTEXT_ATTRIBUTE);
        Locale locale = request.getLocale();

        try
        {
            LocatorDescriptor locator = templateLocator.createLocatorDescriptor("email");
            locator.setName(templateName);
            locator.setMediaType(requestContext.getMediaType());
            locator.setLanguage(locale.getLanguage());
            locator.setCountry(locale.getCountry());
            TemplateDescriptor template = templateLocator.locateTemplate(locator);

            return template.getAppRelativePath();
        }
        catch (TemplateLocatorException e)
        {
            return templateLocation + PATH_SEPARATOR + templateName;
        }
    }

}
