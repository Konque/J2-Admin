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
package org.apache.jetspeed.portlets.security;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.security.auth.Subject;

import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.PortalReservedParameters;
import org.apache.jetspeed.audit.AuditActivity;
import org.apache.jetspeed.request.RequestContext;
import org.apache.jetspeed.security.InvalidNewPasswordException;
import org.apache.jetspeed.security.InvalidPasswordException;
import org.apache.jetspeed.security.PasswordAlreadyUsedException;
import org.apache.jetspeed.security.PasswordCredential;
import org.apache.jetspeed.security.SecurityException;
import org.apache.jetspeed.security.User;
import org.apache.jetspeed.security.UserCredential;
import org.apache.jetspeed.security.UserManager;
import org.apache.portals.bridges.common.GenericServletPortlet;

/**
 * This portlet allows a logged on user to change its password.
 *
 * @author <a href="mailto:ate@apache.org">Ate Douma</a>
 * @version $Id: ChangePasswordPortlet.java 348264 2005-11-22 22:06:45Z taylor $
 */
public class ChangePasswordPortlet extends GenericServletPortlet
{
    private UserManager manager;
    private AuditActivity audit;
    
    public static final String USER_ADMINISTRATION = "J2 User Administration";
    
    public static final String CURRENT_PASSWORD = "currentPassword";
    public static final String NEW_PASSWORD = "newPassword";
    public static final String NEW_PASSWORD_AGAIN = "newPasswordAgain";
    public static final String ERROR_MESSAGES = "errorMessages";
    public static final String PASSWORD_CHANGED = "passwordChanged";
    public static final String WHY = "why";
    public static final String REQUIRED = "required";
    public static final String CANCELLED = "cancelled";
    
    public void init(PortletConfig config)
    throws PortletException 
    {
        super.init(config);
        manager = (UserManager) getPortletContext().getAttribute(CommonPortletServices.CPS_USER_MANAGER_COMPONENT);
        if (null == manager)
        {
            throw new PortletException("Failed to find the User Manager on portlet initialization");
        }
        audit = (AuditActivity)getPortletContext().getAttribute(CommonPortletServices.CPS_AUDIT_ACTIVITY);
        if (null == audit)
        {
            throw new PortletException("Failed to find the Audit Activity on portlet initialization");            
        }        
    }
    
    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException
    {
        response.setContentType("text/html");

        if ( request.getUserPrincipal() != null )
        {
            RequestContext requestContext = (RequestContext)request.getAttribute(PortalReservedParameters.REQUEST_CONTEXT_ATTRIBUTE);
            Integer passwordDaysValid = (Integer)requestContext.getAttribute(PasswordCredential.PASSWORD_CREDENTIAL_DAYS_VALID_REQUEST_ATTR_KEY);
            
            if ( passwordDaysValid != null )
            {
                ResourceBundle bundle = ResourceBundle.getBundle("org.apache.jetspeed.portlets.security.resources.ChgPwdResources",request.getLocale());
                if ( passwordDaysValid.intValue() < 1 )
                {
                    request.setAttribute(WHY,bundle.getString("chgpwd.message.change.required"));
                    request.setAttribute(REQUIRED,Boolean.TRUE);
                }
                else if ( passwordDaysValid.intValue() == 1 )
                {
                    request.setAttribute(WHY,bundle.getString("chgpwd.message.expires.today"));
                    request.setAttribute(REQUIRED,Boolean.TRUE);
                }
                else
                {
                    MessageFormat mf = new MessageFormat(bundle.getString("chgpwd.message.expires.in.days"));
                    request.setAttribute(WHY,mf.format(new Integer[]{passwordDaysValid}));
                }
            }
            
            ArrayList errorMessages = (ArrayList)request.getPortletSession().getAttribute(ERROR_MESSAGES);
            if (errorMessages != null )
            {
                request.getPortletSession().removeAttribute(ERROR_MESSAGES);
                request.setAttribute(ERROR_MESSAGES,errorMessages);
            }
            else
            {
                Boolean password_changed = (Boolean)request.getPortletSession().getAttribute(PASSWORD_CHANGED);
                if ( password_changed != null )
                {
                    request.getPortletSession().removeAttribute(PASSWORD_CHANGED);
                    request.setAttribute(PASSWORD_CHANGED,password_changed);
                }
            }        
        }
        super.doView(request, response);
    }
    
    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException,
    IOException
    {
        if ( actionRequest.getUserPrincipal() != null )
        {
            ResourceBundle bundle = ResourceBundle.getBundle("org.apache.jetspeed.portlets.security.resources.ChgPwdResources",actionRequest.getLocale());

            ArrayList errorMessages = new ArrayList();
            
            String cancelled = actionRequest.getParameter(CANCELLED);
            if ( cancelled == null )
            {
                String currPassword = actionRequest.getParameter(CURRENT_PASSWORD);
                String newPassword = actionRequest.getParameter(NEW_PASSWORD);
                String newPasswordAgain = actionRequest.getParameter(NEW_PASSWORD_AGAIN);
                String userName = actionRequest.getUserPrincipal().getName();
            
                if (currPassword == null || currPassword.length() == 0)
                {
                    errorMessages.add(bundle.getString("chgpwd.error.currentPasswordNull"));
                    currPassword = null;
                }
                if (newPassword == null || newPassword.length() == 0)
                {
                    errorMessages.add(bundle.getString("chgpwd.error.newPasswordNull"));
                    newPassword = null;
                }
                if (newPassword != null && newPassword.length() == 0)
                {
                    newPassword = null;
                }
                
                if (newPassword != null && 
                        (newPasswordAgain == null || (newPasswordAgain != null && !newPassword.equals(newPasswordAgain))))
                {
                    errorMessages.add(bundle.getString("chgpwd.error.newPasswordsDoNotMatch"));
                }
                if ( errorMessages.size() == 0 )
                {
                    try
                    {
                        //manager.setPassword(userName, currPassword, newPassword);
                    	// Changes for new Security API's
                    	User user = manager.getUser(userName);
                    	PasswordCredential credential = manager.getPasswordCredential(user);
                    	credential.setPassword(currPassword, newPassword);
                    	manager.storePasswordCredential(credential);
                        audit.logUserActivity(userName, getIPAddress(actionRequest), AuditActivity.PASSWORD_CHANGE_SUCCESS, USER_ADMINISTRATION);

                        // update Subject in session to reflect the changed PasswordCredential
                        RequestContext requestContext = (RequestContext)actionRequest.getAttribute(PortalReservedParameters.REQUEST_CONTEXT_ATTRIBUTE);                  
                        Subject subject = (Subject)requestContext.getSessionAttribute(PortalReservedParameters.SESSION_KEY_SUBJECT);
                        Iterator<Object> iter = subject.getPrivateCredentials().iterator();
                        while (iter.hasNext())
                        {
                            Object o = iter.next();
                            if (o instanceof UserCredential)
                            {
                                ((UserCredential)o).synchronize(credential);
                                break;
                            }
                        }
                    }
                    catch ( InvalidPasswordException ipe )
                    {
                        errorMessages.add(bundle.getString("chgpwd.error.invalidPassword"));
                    }
                    catch ( InvalidNewPasswordException inpe )
                    {
                        errorMessages.add(bundle.getString("chgpwd.error.invalidNewPassword"));
                    }
                    catch ( PasswordAlreadyUsedException paue )
                    {
                        errorMessages.add(bundle.getString("chgpwd.error.passwordAlreadyUsed"));
                    }
                    catch ( SecurityException e)
                    {
                        // todo: localization of all exception messages
                        errorMessages.add(e.getMessage());
                    }
                }
                if ( errorMessages.size() > 0 )
                {
                    actionRequest.getPortletSession().setAttribute(ERROR_MESSAGES,errorMessages);
                    audit.logUserActivity(userName, getIPAddress(actionRequest), AuditActivity.PASSWORD_CHANGE_FAILURE, errorMessages.toString());                    
                }
                else
                {
                    actionRequest.getPortletSession().setAttribute(PASSWORD_CHANGED,Boolean.TRUE);
                }
            }
        }
    }

    protected String getIPAddress(PortletRequest request)
    {
        RequestContext context = (RequestContext)request.getAttribute(PortalReservedParameters.REQUEST_CONTEXT_ATTRIBUTE);
        if (context == null)
            return "";
        return context.getRequest().getRemoteAddr();
    }
    
}
