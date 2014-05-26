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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.JetspeedActions;
import org.apache.jetspeed.PortalReservedParameters;
import org.apache.jetspeed.administration.AdministrationEmailException;
import org.apache.jetspeed.administration.PortalAdministration;
import org.apache.jetspeed.locator.JetspeedTemplateLocator;
import org.apache.jetspeed.locator.LocatorDescriptor;
import org.apache.jetspeed.locator.TemplateDescriptor;
import org.apache.jetspeed.locator.TemplateLocatorException;
import org.apache.jetspeed.request.RequestContext;
import org.apache.jetspeed.security.SecurityException;
import org.apache.jetspeed.security.User;
import org.apache.jetspeed.security.UserManager;
import org.apache.portals.bridges.util.PreferencesHelper;
import org.apache.portals.bridges.velocity.AbstractVelocityMessagingPortlet;
import org.apache.portals.gems.util.ValidationHelper;
import org.apache.velocity.context.Context;

/**
 * This portlet allows a logged on user to change its password.
 * 
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @author <a href="mailto:chris@bluesunrise.com">Chris Schaefer</a>
 * @version $Id: $
 */
public class UserRegistrationPortlet extends AbstractVelocityMessagingPortlet
{

    private PortalAdministration admin;

    private UserManager userManager;

    // commonly USED attributes

    private static final String USER_ATTRIBUTE_EMAIL = "user.business-info.online.email";

    // Messages
    private static final String MSG_MESSAGE = "MSG";

    private static final String MSG_USERINFO = "user";

    private static final String MSG_REGED_USER_MSG = "registeredUserMsg";

    // Init Parameters
    private static final String IP_ROLES = "roles"; // comma separated

    private static final String IP_GROUPS = "groups"; // comma separated

    private static final String IP_TEMPLATE_LOCATION = "emailTemplateLocation";

    private static final String IP_TEMPLATE_NAME = "emailTemplateName";

    private static final String IP_RULES_NAMES = "rulesNames";

    private static final String IP_RULES_VALUES = "rulesValues";

    private static final String IP_REDIRECT_PATH = "redirectPath";

    private static final String IP_RETURN_URL = "returnURL";

    private static final String IP_OPTION_EMAILS_SYSTEM_UNIQUE = "Option_Emails_System_Unique";

    private static final String IP_OPTION_GENERATE_PASSWORDS = "Option_Generate_Passwords";

    private static final String IP_OPTION_USE_EMAIL_AS_USERNAME = "Option_Use_Email_As_Username";

    // Context Variables
    private static final String CTX_RETURN_URL = "returnURL";

    private static final String CTX_MESSAGE = "MSG";

    private static final String CTX_USERINFO = "user";

    private static final String CTX_FIELDS = "fieldsInOrder";

    private static final String CTX_OPTIONALS = "optionalMap";

    private static final String CTX_REGED_USER_MSG = "registeredUserMsg";

    private static final String CTX_OPTION_GENERATE_PASSWORDS = "CTX_Option_Generate_Passwords";

    private static final String CTX_OPTION_USE_EMAIL_AS_USERNAME = "CTX_Option_Use_Email_As_Username";

    // Resource Bundle
    private static final String RB_EMAIL_SUBJECT = "email.subject.registration";
    
    private static final String PATH_SEPARATOR = "/";

    /** email template to use for merging */
    private String templateLocation;

    private String templateName;

    private JetspeedTemplateLocator templateLocator;
    
    /** localized emailSubject */
    private String emailSubject = null;

    /** path where to redirect to after pressing submit on the form */
    private String redirectPath;

    /** servlet path of the return url to be printed and href'd in email template */
    private String returnUrlPath;

    /** roles */
    private List roles;

    /** groups */
    private List groups;

    /** profile rules */
    private Map rules;

    /** will force the passwords to be generated instead of picked by the user */
    private boolean optionForceGeneratedPasswords = false;

    /**
     * will use cause the portlet to use a user request username instead
     * otherwise forces emailaddress
     */
    private boolean optionForceEmailAsUsername = true;

    /** will check to make sure the email address is unique to the system */
    private boolean optionForceEmailsToBeSystemUnique = true;

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

        // roles
        this.roles = getInitParameterList(config, IP_ROLES);

        // groups
        this.groups = getInitParameterList(config, IP_GROUPS);

        // rules (name,value pairs)
        List names = getInitParameterList(config, IP_RULES_NAMES);
        List values = getInitParameterList(config, IP_RULES_VALUES);
        rules = new HashMap();
        for (int ix = 0; ix < ((names.size() < values.size()) ? names.size()
                : values.size()); ix++)
        {
            rules.put(names.get(ix), values.get(ix));
        }

        this.templateLocation = config.getInitParameter(IP_TEMPLATE_LOCATION);
        if (templateLocation == null)
        {
            templateLocation = "/WEB-INF/view/userreg/";
        }
        templateLocation = getPortletContext().getRealPath(templateLocation);
        this.templateName = config.getInitParameter(IP_TEMPLATE_NAME);
        if (templateName == null)
        {
            templateName = "userRegistrationEmail.vm";
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
        
        // user attributes ?

        this.optionForceEmailsToBeSystemUnique = Boolean.valueOf(
                config.getInitParameter(IP_OPTION_EMAILS_SYSTEM_UNIQUE))
                .booleanValue();
        this.optionForceGeneratedPasswords = Boolean.valueOf(
                config.getInitParameter(IP_OPTION_GENERATE_PASSWORDS))
                .booleanValue();
        this.optionForceEmailAsUsername = Boolean.valueOf(
                config.getInitParameter(IP_OPTION_USE_EMAIL_AS_USERNAME))
                .booleanValue();
        if (this.optionForceEmailAsUsername)
        {
            // just to be sure
            this.optionForceEmailsToBeSystemUnique = true;
        }
        this.returnUrlPath = config.getInitParameter(IP_RETURN_URL);
        this.redirectPath = config.getInitParameter(IP_REDIRECT_PATH);
    }

    public void doEdit(RenderRequest request, RenderResponse response)
    throws PortletException, IOException
    {
        response.setContentType("text/html");
        doPreferencesEdit(request, response);
    }
    
    protected void doDispatch(RenderRequest request, RenderResponse response) throws PortletException, IOException
    {
        if ( !request.getWindowState().equals(WindowState.MINIMIZED))
        {
            PortletMode curMode = request.getPortletMode();            
            if (JetspeedActions.EDIT_DEFAULTS_MODE.equals(curMode))
            {
                //request.setAttribute(PARAM_EDIT_PAGE, DEFAULT_EDIT_DEFAULTS_PAGE);
                doEdit(request, response);
            }
            else
            {
                super.doDispatch(request, response);
            }
        }
        
    }
    
    public void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException
    {
        response.setContentType("text/html");
        Context context = getContext(request);

        Object userinfoObject = this
                .receiveRenderMessage(request, MSG_USERINFO);
        context.put(CTX_USERINFO, userinfoObject);
        context.put(CTX_FIELDS, getListOfNonSpecialFormKeys());
        context.put(CTX_OPTIONALS, getOptionalMap());
        context.put(CTX_MESSAGE, consumeRenderMessage(request, MSG_MESSAGE));
        String guid = request.getParameter("newUserGUID");
        if (guid != null)
        {
            // we'll ignore the possibility of an invalid guid for now.

            // NOTE this would be a good place to put the actual registration if
            // that's the process you want to have happen.

            ResourceBundle resource = getPortletConfig().getResourceBundle(
                    request.getLocale());
            context.put(CTX_REGED_USER_MSG, resource
                    .getString("success.login_above"));
        } else
        {
            // not a returning url, but perhaps we just got redirected from the
            // form ?
            // if this is non-null, then we know that we registered
            context.put(CTX_REGED_USER_MSG, consumeRenderMessage(request,
                    MSG_REGED_USER_MSG));
        }
        // next two control the existence of some of the fields in the form
        if (this.optionForceEmailAsUsername)
        {
            context.put(CTX_OPTION_USE_EMAIL_AS_USERNAME, "TRUE");
        }
        if (this.optionForceGeneratedPasswords)
        {
            context.put(CTX_OPTION_GENERATE_PASSWORDS, "TRUE");
        }

        super.doView(request, response);
    }

    private static final Boolean required = new Boolean(true);

    private static final Boolean optional = new Boolean(false);

    private static final Integer IS_STRING = new Integer(1);

    private static final Integer IS_EMAIL = new Integer(2);

    private static final Integer IS_PHONE = new Integer(3);

    private static final Integer IS_URL = new Integer(4);

    private static final Integer IS_BDATE = new Integer(5);

    protected List getListOfNonSpecialFormKeys()
    {
        List l = new ArrayList();
        for (int i = 0; i < formKeys.length; i++)
        {
            String key = (String) formKeys[i][0];
            if (key.equals("user.name"))
            {
                // don't put this in
            } else if (key.equals("user.business-info.online.email"))
            {
                // don't put this in
            } else if (key.equals("password"))
            {
                // don't put this in
            } else if (key.equals("verifyPassword"))
            {
                // don't put this in
            } else
            {
                // but DO add this
                l.add(key);
            }
        }
        return l;
    }

    protected Map getOptionalMap()
    {
        Map m = new HashMap();
        for (int i = 0; i < formKeys.length; i++)
        {
            boolean isRequired = ((Boolean) formKeys[i][1]).booleanValue();
            if (!isRequired)
            {
                m.put(formKeys[i][0], "");
            }
        }
        return m;
    }
    // PLT name, required, max length,  validation type 
    
    protected static Object[][] formKeys =
    { 
        // the next four items are special cases
        
        // this is the offical email used by jetspeed.  You can chnage it, but you have to look around in the code
        {"user.business-info.online.email", required ,      new Integer(80),        IS_EMAIL},
        
        // username is required here 
        // chould be commented out if email is used as username...
        {"user.name",         required ,      new Integer(80),        IS_STRING},
        
        // These last two are special cases you must have them
        // comment them out here if you use the generated password option
        {"password", required, new Integer(80), IS_STRING},
        {"verifyPassword", required, new Integer(80), IS_STRING},
    
        // the following can be placed in any order, and will appear in that order on the page
        
        //      All of the following are optional and are stored as user attributes if collected.
        
        /*
        {"user.bdate",          optional ,      new Integer(25),        IS_BDATE},    // Note: store as a string which is a number, time in milliseconds since 1970... see Portlet Spec. 
        {"user.gender",         optional ,      new Integer(10),        IS_STRING},
        {"user.employer",         optional ,      new Integer(80),        IS_STRING},
        */
        
        {"user.department",         optional ,      new Integer(80),        IS_STRING},
        /*
        {"user.jobtitle",         optional ,      new Integer(80),        IS_STRING},
        {"user.name.prefix",         optional ,      new Integer(10),        IS_STRING},
        */
        {"user.name.given",         optional ,      new Integer(30),        IS_STRING},
        {"user.name.family",         optional ,      new Integer(30),        IS_STRING},
        /*
        {"user.name.middle",         optional ,      new Integer(30),        IS_STRING},
        {"user.name.suffix",         optional ,      new Integer(10),        IS_STRING},
        {"user.name.nickName",         optional ,      new Integer(30),        IS_STRING},
        {"user.home-info.postal.name",         optional ,      new Integer(80),        IS_STRING},
        {"user.home-info.postal.street",         optional ,      new Integer(80),        IS_STRING},
        {"user.home-info.postal.city",         optional ,      new Integer(80),        IS_STRING},
        {"user.home-info.postal.stateprov",         optional ,      new Integer(80),        IS_STRING},
        {"user.home-info.postal.postalcode",         optional ,      new Integer(80),        IS_STRING},
        {"user.home-info.postal.country",         optional ,      new Integer(80),        IS_STRING},
        {"user.home-info.postal.organization",         optional ,      new Integer(80),        IS_STRING},
        {"user.home-info.telecom.telephone.intcode",         optional ,      new Integer(80),        IS_STRING},
        {"user.home-info.telecom.telephone.loccode",         optional ,      new Integer(80),        IS_STRING},
        {"user.home-info.telecom.telephone.number",         optional ,      new Integer(80),        IS_PHONE},
        {"user.home-info.telecom.telephone.ext",         optional ,      new Integer(80),        IS_STRING},
        {"user.home-info.telecom.telephone.comment",         optional ,      new Integer(80),        IS_STRING},
        {"user.home-info.telecom.fax.intcode",         optional ,      new Integer(80),        IS_STRING},
        {"user.home-info.telecom.fax.loccode",         optional ,      new Integer(80),        IS_STRING},
        {"user.home-info.telecom.fax.number",         optional ,      new Integer(80),        IS_STRING},
        {"user.home-info.telecom.fax.ext",         optional ,      new Integer(80),        IS_STRING},
        {"user.home-info.telecom.fax.comment",         optional ,      new Integer(80),        IS_STRING},
        {"user.home-info.telecom.mobile.intcode",         optional ,      new Integer(80),        IS_STRING},
        {"user.home-info.telecom.mobile.loccode",         optional ,      new Integer(80),        IS_STRING},
        {"user.home-info.telecom.mobile.number",optional ,      new Integer(80),        IS_PHONE},
        {"user.home-info.telecom.mobile.ext",         optional ,      new Integer(80),        IS_STRING},
        {"user.home-info.telecom.mobile.comment",         optional ,      new Integer(80),        IS_STRING},
        {"user.home-info.telecom.pager.intcode",         optional ,      new Integer(80),        IS_STRING},
        {"user.home-info.telecom.pager.loccode",         optional ,      new Integer(80),        IS_STRING},
        {"user.home-info.telecom.pager.number",         optional ,      new Integer(80),        IS_PHONE},
        {"user.home-info.telecom.pager.ext",            optional ,      new Integer(80),        IS_STRING},
        {"user.home-info.telecom.pager.comment",         optional ,      new Integer(80),        IS_STRING},
        {"user.home-info.online.email",         optional ,      new Integer(80),        IS_EMAIL},
        {"user.home-info.online.uri",         optional ,      new Integer(80),        IS_URL},
        */
        {"user.business-info.postal.name",         optional ,      new Integer(80),        IS_STRING},
        {"user.business-info.postal.street",         optional ,      new Integer(80),        IS_STRING},
        {"user.business-info.postal.city",         optional ,      new Integer(80),        IS_STRING},
        {"user.business-info.postal.stateprov",         optional ,      new Integer(80),        IS_STRING},
        {"user.business-info.postal.postalcode",         optional ,      new Integer(80),        IS_STRING},
        {"user.business-info.postal.country",         optional ,      new Integer(80),        IS_STRING},
        /*
        {"user.business-info.postal.organization",         optional ,      new Integer(80),        IS_STRING},
        {"user.business-info.telecom.telephone.intcode",         optional ,      new Integer(80),        IS_STRING},
        {"user.business-info.telecom.telephone.loccode",         optional ,      new Integer(80),        IS_STRING},
        {"user.business-info.telecom.telephone.number",         optional ,      new Integer(80),        IS_PHONE},
        {"user.business-info.telecom.telephone.ext",         optional ,      new Integer(80),        IS_STRING},
        {"user.business-info.telecom.telephone.comment",         optional ,      new Integer(80),        IS_STRING},
        {"user.business-info.telecom.fax.intcode",         optional ,      new Integer(80),        IS_STRING},
        {"user.business-info.telecom.fax.loccode",         optional ,      new Integer(80),        IS_STRING},
        {"user.business-info.telecom.fax.number",         optional ,      new Integer(80),        IS_PHONE},
        {"user.business-info.telecom.fax.ext",         optional ,      new Integer(80),        IS_STRING},
        {"user.business-info.telecom.fax.comment",         optional ,      new Integer(80),        IS_STRING},
        {"user.business-info.telecom.mobile.intcode",         optional ,      new Integer(80),        IS_STRING},
        {"user.business-info.telecom.mobile.loccode",         optional ,      new Integer(80),        IS_STRING},
        {"user.business-info.telecom.mobile.number",         optional ,      new Integer(80),        IS_PHONE},
        {"user.business-info.telecom.mobile.ext",         optional ,      new Integer(80),        IS_STRING},
        {"user.business-info.telecom.mobile.comment",         optional ,      new Integer(80),        IS_STRING},
        {"user.business-info.telecom.pager.intcode",         optional ,      new Integer(80),        IS_STRING},
        {"user.business-info.telecom.pager.loccode",         optional ,      new Integer(80),        IS_STRING},
        {"user.business-info.telecom.pager.number",         optional ,      new Integer(80),        IS_PHONE},
        {"user.business-info.telecom.pager.ext",            optional ,      new Integer(80),        IS_STRING},
        {"user.business-info.telecom.pager.comment",         optional ,      new Integer(80),        IS_STRING},
//      --- special case see above  user.business-info.online.email 
        {"user.business-info.online.uri", optional ,      new Integer(80),        IS_URL},
        */
    };

    protected boolean validateFormValue(String value, Integer length,
            Integer validationType)
    {

        if (validationType.equals(IS_STRING))
        {
            if (!ValidationHelper.isAny(value, true, length.intValue())) { return false; }
        } else if (validationType.equals(IS_EMAIL))
        {
            if (!ValidationHelper
                    .isEmailAddress(value, true, length.intValue())) { return false; }
        } else if (validationType.equals(IS_PHONE))
        {
            if (!ValidationHelper.isPhoneNumber(value, true, length.intValue())) { return false; }
        } else if (validationType.equals(IS_URL))
        {
            if (!ValidationHelper.isURL(value, true, length.intValue())) { return false; }
        } else if (validationType.equals(IS_BDATE))
        {
            if (!ValidationHelper.isValidDatetime(value)) { return false; }
        } else
        {
            // unkown type assume string for now
            if (!ValidationHelper.isAny(value, true, length.intValue())) { return false; }
        }
        return true;

    }

    protected String convertIfNeed(String key, String value)
    {
        if ("user.bdate".equals(key))
        {
            // this one needs conversion
            Date d = ValidationHelper.parseDate(value);
            long timeInmS = d.getTime();
            return "" + timeInmS;
        }
        return value;
    }

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException
    {
        List errors = new LinkedList();

        Map userAttributes = new HashMap();

        Map userInfo = new HashMap();
        ResourceBundle resource = getPortletConfig().getResourceBundle(
                actionRequest.getLocale());

        PortletMode curMode = actionRequest.getPortletMode();
        if (curMode == PortletMode.EDIT ||
            curMode.equals(JetspeedActions.EDIT_DEFAULTS_MODE))
        {
            PortletPreferences prefs = actionRequest.getPreferences();
            PreferencesHelper.requestParamsToPreferences(actionRequest);
            prefs.store();
            actionResponse.setPortletMode(PortletMode.VIEW);
            return;
        }
        
        try
        {

            for (int i = 0; i < formKeys.length; i++)
            {
                try
                {
                    String key = (String) formKeys[i][0];
                    Boolean isRequired = (Boolean) formKeys[i][1];
                    String value = actionRequest.getParameter(key);
                    if ((value != null) && (value.length() > 0))
                    {

                        userInfo.put(key, value);

                        // do some validation
                        if (!validateFormValue(value, (Integer) formKeys[i][2],
                                (Integer) formKeys[i][3]))
                        {
                            errors.add(resource
                                    .getString("error.invalid-format." + key));
                        }

                        if (key.startsWith("user."))
                        {
                            value = convertIfNeed(key, value);
                            // we'll assume that these map back to PLT.D values
                            userAttributes.put(key, value);
                        }
                    } else
                    {
                        // don't have that value or it's too short... is it
                        // required ?
                        if (isRequired.booleanValue())
                        {
                            if (this.optionForceEmailAsUsername && key.equals("user.name"))
                            {
                                value = convertIfNeed(key, value);
                                userAttributes.put(key, value);
                                continue;
                            }
                            errors.add(resource.getString("error.lacking."
                                        + key));
                        }
                        // place an empty version in userInfo anyway
                        // so that the template will display the correct fields
                        userInfo.put(key, "");
                    }
                } catch (MissingResourceException mre)
                {
                    errors.add(resource.getString("error.failed_to_add")
                            + mre.toString());
                }

            }
            // publish the whole map so we can reload the form values on error.
            publishRenderMessage(actionRequest, MSG_USERINFO, userInfo);

            // These next checks may duplicate previous checks.
            // however this is a double check given the nature of the values and
            // how they are used.
            if (this.optionForceEmailAsUsername)
            {
                // email is something special
                if (!ValidationHelper.isEmailAddress((String) userInfo
                        .get(USER_ATTRIBUTE_EMAIL), true, 80))
                {
                    errors.add(resource.getString("error.invalid-format."
                            + USER_ATTRIBUTE_EMAIL));
                }
            } else
            {
                if (!ValidationHelper.isAny((String) userInfo.get("user.name"),
                        true, 80))
                {
                    errors.add(resource.getString("error.lacking.user.name"));
                }
            }

            // if we're not generating make sure it's real
            if (!this.optionForceGeneratedPasswords)
            {
                if (!ValidationHelper.isAny((String) userInfo.get("password"),
                        true, 25))
                {
                    errors.add(resource.getString("error.lacking.password"));
                }
            }

            if (optionForceEmailAsUsername)
            {
                // force user.name to be same as email
                userInfo.put("user.name", userInfo.get(USER_ATTRIBUTE_EMAIL));
            }

            boolean userIdExistsFlag = true;
            try
            {
                userManager.getUser((String) userInfo.get("user.name"));
            } catch (SecurityException e)
            {
                userIdExistsFlag = false;
            }
            //
            if (userIdExistsFlag)
            {
                errors.add(resource.getString("error.userid_already_exists"));
                publishRenderMessage(actionRequest, MSG_MESSAGE, errors);
                return;
            }
            if (optionForceEmailsToBeSystemUnique)
            {
                boolean emailExistsFlag = true;
                User user = null;
                try
                {
                    user = admin.lookupUserFromEmail((String) userInfo
                            .get(USER_ATTRIBUTE_EMAIL));
                } catch (AdministrationEmailException e1)
                {
                    emailExistsFlag = false;
                }
                if ((emailExistsFlag) || (user != null))
                {
                    errors
                            .add(resource
                                    .getString("error.email_already_exists"));
                    publishRenderMessage(actionRequest, MSG_MESSAGE, errors);
                    return;
                }

            }

            try
            {
                if (optionForceGeneratedPasswords)
                {
                    String password = admin.generatePassword();
                    userInfo.put("password", password);
                } else
                {
                    if (userInfo.get("password").equals(
                            userInfo.get("verifyPassword")))
                    {

                    } else
                    {
                        errors.add(resource
                                .getString("error.two_passwords_do_not_match"));
                        publishRenderMessage(actionRequest, MSG_MESSAGE, errors);
                        return;
                    }
                }
            } catch (Exception e)
            {
                errors.add(resource.getString("error.failed_to_add")
                        + e.toString());
                publishRenderMessage(actionRequest, MSG_MESSAGE, errors);
            }
            // make sure no errors have occurred
            if (errors.size() > 0)
            {
                publishRenderMessage(actionRequest, MSG_MESSAGE, errors);
                return;
            }

            // Ok, we think we're good to go, let's create the user!
            try
            {
                PortletPreferences prefs = actionRequest.getPreferences();
                String template = prefs.getValue("newUserTemplateDirectory", "");
                if (template.trim().length() == 0)
                    template = null;
                String subsiteRootFolder = prefs.getValue("subsiteRootFolder", "");
                if (subsiteRootFolder.trim().length() == 0)
                    subsiteRootFolder = null;
                List prefRoles = getPreferencesList(prefs, IP_ROLES);
                if (prefRoles.isEmpty())
                    prefRoles = this.roles;
                List prefGroups = getPreferencesList(prefs, IP_GROUPS);
                if (prefGroups.isEmpty())
                    prefGroups = this.groups;
                
                List names = getPreferencesList(prefs, IP_RULES_NAMES);
                List values = getPreferencesList(prefs, IP_RULES_VALUES);
                Map profileRules = new HashMap();
                for (int ix = 0; ix < ((names.size() < values.size()) ? names.size()
                        : values.size()); ix++)
                {
                    profileRules.put(names.get(ix), values.get(ix));
                }
                if (profileRules.isEmpty())
                    profileRules = this.rules;
                
                admin.registerUser((String) userInfo.get("user.name"),
                        (String) userInfo.get("password"), prefRoles,
                        prefGroups, userAttributes, // note use of only
                                                        // PLT.D values here.
                        profileRules, template, subsiteRootFolder,
                        actionRequest.getLocale(), actionRequest.getServerName());
                
                String urlGUID = ForgottenPasswordPortlet.makeGUID(
                        (String) userInfo.get("user.name"), (String) userInfo
                                .get("password"));

                userInfo.put(CTX_RETURN_URL, generateReturnURL(actionRequest,
                        actionResponse, urlGUID));

                String templ = getTemplatePath(actionRequest, actionResponse);

                if (templ == null) { throw new Exception(
                        "email template not available"); }

                boolean sendEmail = prefs.getValue("SendEmail", "true").equals("true");
                if (sendEmail)
                {
                    admin.sendEmail(getPortletConfig(), (String) userInfo
                            .get(USER_ATTRIBUTE_EMAIL),
                            getEmailSubject(actionRequest), templ, userInfo);
                }
                
                if ((this.optionForceEmailAsUsername)
                        || (this.optionForceGeneratedPasswords))
                {
                    publishRenderMessage(actionRequest, MSG_REGED_USER_MSG,
                            resource.getString("success.check_your_email"));
                } else
                {
                    publishRenderMessage(actionRequest, MSG_REGED_USER_MSG,
                            resource.getString("success.login_above"));
                }

                // put an empty map to "erase" all the user info going forward
                publishRenderMessage(actionRequest, MSG_USERINFO, new HashMap());

                actionResponse.sendRedirect(this.generateRedirectURL(
                        actionRequest, actionResponse));

            } catch (Exception e)
            {
                errors.add(resource.getString("error.failed_to_add")
                        + e.toString());
                publishRenderMessage(actionRequest, MSG_MESSAGE, errors);
            }
        } catch (MissingResourceException mre)
        {
            errors.add(resource.getString("error.failed_to_add")
                    + mre.toString());
            publishRenderMessage(actionRequest, MSG_MESSAGE, errors);
        } catch (Exception e)
        {
            errors
                    .add(resource.getString("error.failed_to_add")
                            + e.toString());
            publishRenderMessage(actionRequest, MSG_MESSAGE, errors);
        }

    }

    protected String getEmailSubject(PortletRequest request)
    {
        ResourceBundle resource = getPortletConfig().getResourceBundle(
                request.getLocale());
        try
        {
            this.emailSubject = resource.getString(RB_EMAIL_SUBJECT);
        } catch (java.util.MissingResourceException mre)
        {
            this.emailSubject = null;
        }
        if (this.emailSubject == null)
            this.emailSubject = "Registration Confirmation";
        return this.emailSubject;
    }

    protected List getInitParameterList(PortletConfig config, String ipName)
    {
        String temp = config.getInitParameter(ipName);
        if (temp == null) return new ArrayList();

        String[] temps = temp.split("\\,");
        for (int ix = 0; ix < temps.length; ix++)
            temps[ix] = temps[ix].trim();

        return Arrays.asList(temps);
    }

    protected List getPreferencesList(PortletPreferences prefs, String prefName)
    {
        String temp = prefs.getValue(prefName, "");
        if (temp == null || temp.trim().length() == 0) return new ArrayList();

        String[] temps = temp.split("\\,");
        for (int ix = 0; ix < temps.length; ix++)
            temps[ix] = temps[ix].trim();

        return Arrays.asList(temps);
    }
    
    protected String generateReturnURL(PortletRequest request,
            PortletResponse response, String urlGUID)
    {
        String fullPath = this.returnUrlPath + "?newUserGUID=" + urlGUID;
        // NOTE: getPortalURL will encode the fullPath for us
        String url = admin.getPortalURL(request, response, fullPath);
        return url;
    }

    protected String generateRedirectURL(PortletRequest request,
            PortletResponse response)
    {
        return admin.getPortalURL(request, response, this.redirectPath);
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
