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
package org.apache.jetspeed.security.mfa.portlets;

import java.io.IOException;
import java.io.NotSerializableException;
import java.util.Map;
import java.util.Random;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.audit.AuditActivity;
import org.apache.jetspeed.login.LoginConstants;
import org.apache.jetspeed.request.RequestContext;
import org.apache.jetspeed.security.AuthenticatedUser;
import org.apache.jetspeed.security.AuthenticationProvider;
import org.apache.jetspeed.security.PasswordCredential;
import org.apache.jetspeed.security.SecurityAttribute;
import org.apache.jetspeed.security.SecurityException;
import org.apache.jetspeed.security.User;
import org.apache.jetspeed.security.UserCredential;
import org.apache.jetspeed.security.UserManager;
import org.apache.jetspeed.security.mfa.impl.CaptchaImageResource;
import org.apache.jetspeed.security.mfa.util.QuestionFactory;
import org.apache.jetspeed.security.mfa.util.SecurityHelper;
import org.apache.portals.bridges.common.GenericServletPortlet;
import org.apache.portals.messaging.PortletMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: $
 */
public class MFALogin extends GenericServletPortlet
{
    
    static final Logger logger = LoggerFactory.getLogger(MFALogin.class);
    
    private UserManager userManager;
    private AuditActivity audit;
    private AuthenticationProvider authorization;
    private Random rand = new Random();
	
	private int cookieLifetime = 172800;	// 48 hours
    private int maxNumberOfAuthenticationFailures = 3;
    
	private QuestionFactory questionFactory;

    public static final String RETRYCOUNT = "mfaRetryCount";
    public static final String ERRORCODE = "mfaErrorCode";
    public static final String QUESTION_FACTORY = "mfaQuestionFactory";
    public static final String LOGIN_ENROLL_ACTIVITY = "login-enroll";
    
    public void init(PortletConfig config)
    throws PortletException 
    {
        super.init(config);
        userManager = (UserManager) getPortletContext().getAttribute(CommonPortletServices.CPS_USER_MANAGER_COMPONENT);
        if (null == userManager)
        {
            throw new PortletException("Failed to find the User Manager on portlet initialization");
        }
        audit = (AuditActivity)getPortletContext().getAttribute(CommonPortletServices.CPS_AUDIT_ACTIVITY);
        if (null == audit)
        {
            throw new PortletException("Failed to find the Audit Activity on portlet initialization");            
        }
        authorization = (AuthenticationProvider)getPortletContext().getAttribute(CommonPortletServices.CPS_AUTHENTICATION_PROVIDER);
        if (null == authorization)
        {
            throw new PortletException("Failed to find the Authorization Provider on portlet initialization");            
        }
        
        // Read maximum lifetime for authentication cookies.
        String cookie = getInitParameter("cookieLifetime");
        String max = getInitParameter("maxNumberOfAuthenticationFailures");
        if ( cookie != null )
        {
        	try
        	{
        		cookieLifetime = Integer.parseInt(cookie);
                this.maxNumberOfAuthenticationFailures = Integer.parseInt(max);
        		//System.out.println("Config file specified cookie lifetime of " + cookieLifetime + " seconds.");
        	}
        	catch (NumberFormatException e)
        	{
        		//System.out.println("Warning: cookie lifetime " + cookie + " is not a valid integer.");
        	}
        }
        else
        {
        	//System.out.println("Warning: cookie lifetime not specified; defaulting to 48 hours");
        }
        
        // Read random questions.
        questionFactory = new QuestionFactory( getInitParameter("randomQuestions") );
        
        // Read whether or not to refuse login to misconfigured users.
        // (i.e. users who have not set up challenge questions.)
        //String failOnMisconfig = getInitParameter("failOnMisconfiguredUser");
        //failOnMisconfiguredUser = (!failOnMisconfig.equals("false"));
        //stealthOnMisconfiguredUser = (failOnMisconfig.equals("strict"));
    }
    
    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException
    {
        response.setContentType("text/html");
        String  view = (String)PortletMessaging.receive(request, VIEW);
        if (request.getUserPrincipal() != null)
        {
            // user is logged on, force them to logged on view
            view = this.setView(request, "loggedon", SUCCESS1);
            request.setAttribute(PARAM_VIEW_PAGE, view);            
        }
        else
        {
            if (view != null)
            {
                RequestContext rc = SecurityHelper.getRequestContext(request);                
                Integer ecode = (Integer)rc.getSessionAttribute(LoginConstants.ERRORCODE);
                if (ecode != null && (ecode.equals(LoginConstants.ERROR_USER_DISABLED) || ecode.equals(LoginConstants.ERROR_CREDENTIAL_DISABLED)))
                {
                    view = this.setView(request, "three", FAILURE2);
                }
                request.setAttribute(PARAM_VIEW_PAGE, view);
            }
            else
            {            
                request.setAttribute(PARAM_VIEW_PAGE, this.getDefaultViewPage());            
            }
        }
        StatusMessage message = (StatusMessage) PortletMessaging.consume(request, STATUS_MESSAGE);
        if (message != null)
        {
            request.setAttribute(STATUS_MESSAGE, message);
        }
        if (view == null || view.equals("one"))
        {
            clearLoginMessages(request);
        }
        request.setAttribute(QUESTION_FACTORY, this.questionFactory);
        super.doView(request, response);
    }
    
    protected String[][] SUCCESS1_MAP =
    {
            { "one", "/WEB-INF/view/mfa/login3.jsp" },  // success and a valid cookie, move on to password
            { "two", "/WEB-INF/view/mfa/login3.jsp" },  // success answer to personal question
            { "three", "/WEB-INF/view/mfa/login3.jsp" }, // stay on the same page in case of errors
            { "enroll", "/WEB-INF/view/mfa/enroll-login.jsp" },
            { "enroll-login", "/WEB-INF/view/mfa/enroll.jsp"},
            { "loggedon", "/WEB-INF/view/mfa/loggedon.jsp" }, 
            { "restart", "/WEB-INF/view/mfa/login1.jsp" }
    };
    protected String[][] SUCCESS2_MAP =
    {
            { "one", "/WEB-INF/view/mfa/login2.jsp" },  // success but no valid cookie, move on to asking question
    };
    protected String[][] SUCCESS3_MAP =
    {
            { "one", "/WEB-INF/view/mfa/enroll-login.jsp" },  // success but no preferences configured yet, move on to enrollment mode
    };        
    protected String[][] FAILURE1_MAP =
    {
            { "one", "/WEB-INF/view/mfa/login1.jsp" },  // return back, display message
            { "two", "/WEB-INF/view/mfa/login1.jsp" },   // stay on same page
            { "three", "/WEB-INF/view/mfa/login3.jsp" },
            { "enroll", "/WEB-INF/view/mfa/enroll.jsp" },  // validation error
            { "enroll-login", "/WEB-INF/view/mfa/enroll-login.jsp"},            
    };
    protected String[][] FAILURE2_MAP =
    {
            { "one", "/WEB-INF/view/mfa/login2.jsp" }, // send them on like they succeeded, but its a trap
            { "two", "/WEB-INF/view/mfa/login1.jsp" }, // user disabled, reset
            { "enroll", "/WEB-INF/view/mfa/login1.jsp" },  // reset
            { "enroll-login", "/WEB-INF/view/mfa/login4.jsp"},    
            { "three", "/WEB-INF/view/mfa/login4.jsp" }
    };

    protected String[][][] TRANSITIONS =
    {
            SUCCESS1_MAP,
            SUCCESS2_MAP,
            SUCCESS3_MAP,
            FAILURE1_MAP,
            FAILURE2_MAP
    };
    
    public static final String VIEW = "mfa.view";
    public static final String USERBEAN = "userBean";
    public static final String STATUS_MESSAGE = "statusMsg";
    protected static final int SUCCESS1 = 0;
    protected static final int SUCCESS2 = 1;
    protected static final int SUCCESS3 = 2;    
    protected static final int FAILURE1 = 3;
    protected static final int FAILURE2 = 4;
   
    
    protected String setView(PortletRequest request, String phase, int state)
    throws PortletException
    {
        String[][] views = TRANSITIONS[state];
        String result = "/WEB-INF/view/mfa/login1.jsp";
        for (int ix = 0; ix < views.length; ix++)
        {
            if (views[ix][0].equals(phase))
            {
                try
                {
                    PortletMessaging.publish(request, VIEW, views[ix][1]);
                    result = views[ix][1];
                }
                catch (Exception e)
                {
                    throw new PortletException(e);
                }
                break;
            }
        }
        return result;
    }
    
    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException,
    IOException
    {
        String phase = actionRequest.getParameter("phase");
        UserBean userBean = (UserBean)actionRequest.getPortletSession().getAttribute(USERBEAN, PortletSession.APPLICATION_SCOPE);            
        if (userBean != null && phase != null)
        {
            if (phase.equals("one"))
            {
                // process captcha validation, and verify username
                String captcha = actionRequest.getParameter("captcha");
                String username = actionRequest.getParameter("username");
                
                if (SecurityHelper.isEmpty(captcha) || !userBean.getCaptcha().equals(captcha))
                {
                    StatusMessage msg = new StatusMessage("The text entered does not match the displayed text.", StatusMessage.ERROR);
                    PortletMessaging.publish(actionRequest, STATUS_MESSAGE, msg);
                    setView(actionRequest, phase, FAILURE1);                                                
                    return;
                }                
                
                if (userManager.userExists(username))
                {                    
                    User user = null;
                    try
                    {
                        user = userManager.getUser(username);
                    }
                    catch (Exception e)
                    {
                        StatusMessage msg = new StatusMessage("User not accessible.", StatusMessage.ERROR);
                        PortletMessaging.publish(actionRequest, STATUS_MESSAGE, msg);
                        userBean.setInvalidUser(true);
                        userBean.setQuestion( questionFactory.getRandomQuestion() );                        
                        setView(actionRequest, phase, SUCCESS2); // act like nothing happening
                        return;
                    }
                    userBean.setUsername(username);
                    userBean.setUser(user);                    
                    UserCredential credential = null;
                    try
                    {
                        credential = SecurityHelper.getCredential(userManager, user);
                    }
                    catch (SecurityException e)
                    {}
                    if (credential != null)
                    {
                        if (credential.isEnabled() == false)
                        {
                            userBean.setInvalidUser(true);
                            setView(actionRequest, phase, SUCCESS2);
                            userBean.setQuestion( questionFactory.getRandomQuestion() );                            
                            StatusMessage msg = new StatusMessage("The account has been disabled.", StatusMessage.ERROR);
                            PortletMessaging.publish(actionRequest, STATUS_MESSAGE, msg);                            
                            return;
                        }
                    }
                    userBean.setUser(user);
                    userBean.setUsername(username);
                    // see if we have a valid MFA Cookie
                    Cookie mfaCookie = SecurityHelper.getMFACookie(actionRequest, username);
                    if (mfaCookie == null)
                    {
                        if (generateQuestionAndAnswer(userBean))
                        {
                            setView(actionRequest, phase, SUCCESS2);
                        }
                        else
                        {
                            // go into enrollment mode
                            setView(actionRequest, phase, SUCCESS3);                                
                        }                            
                    }
                    else
                    {
                    	Map<String, SecurityAttribute> userAttributes = userBean.getUser().getSecurityAttributes().getAttributeMap();
                    	String cookie = getUserAttribute(userAttributes, "user.cookie", username);                        	
                        if (mfaCookie.getValue().equals(cookie))
                        {                        
                            userBean.setHasCookie(true);
                        	userBean.setPassPhrase(getUserAttribute(userAttributes, "user.passphrase", ""));
                            setView(actionRequest, phase, SUCCESS1);
                        }
                        else
                        {
                            userBean.setHasCookie(false);
                            if (generateQuestionAndAnswer(userBean))
                            {
                                setView(actionRequest, phase, SUCCESS2);
                            }
                            else
                            {
                                // go into enrollment mode
                                setView(actionRequest, phase, SUCCESS3);
                            }                                                                
                        }
                    }
                }
                else
                {
                    // Proceed on but mark the User Bean as invalid user to prevent user harvesting
                	// Also need to supply a random challenge question.
                    userBean.setInvalidUser(true);
                	userBean.setQuestion( questionFactory.getRandomQuestion() );
                    StatusMessage msg = new StatusMessage("The text entered does not match the displayed text.", StatusMessage.ERROR);
                    PortletMessaging.publish(actionRequest, STATUS_MESSAGE, msg);                    
                    setView(actionRequest, phase, SUCCESS2);
                }                
            }            
            else if (phase.equals("two"))
            {
                if (userBean.isInvalidUser())
                {
                    // prevent harvesting
                    StatusMessage msg = new StatusMessage("Invalid User.", StatusMessage.ERROR);
                    PortletMessaging.publish(actionRequest, STATUS_MESSAGE, msg);                    
                    setView(actionRequest, phase, FAILURE1);
                }
                else
                {
                    if (userBean.getUser() == null)
                    {
                        StatusMessage msg = new StatusMessage("User not accessible.", StatusMessage.ERROR);
                        PortletMessaging.publish(actionRequest, STATUS_MESSAGE, msg);
                        setView(actionRequest, phase, FAILURE1);                                                                                                            
                        return;
                    }
                    String typedAnswer = actionRequest.getParameter("answer");
                    String publicTerminal = actionRequest.getParameter("publicTerminal");
                    userBean.setPublicTerminal(publicTerminal != null);
                    Map<String, SecurityAttribute> userAttributes = userBean.getUser().getSecurityAttributes().getAttributeMap();
                    int failures = Integer.parseInt(getUserAttribute(userAttributes, "user.question.failures", "0"));                    
                    if (SecurityHelper.isEmpty(typedAnswer) || !typedAnswer.equalsIgnoreCase(userBean.getAnswer()))
                    {
                        int count = failures + 1;
                        if (count >= this.maxNumberOfAuthenticationFailures)
                        {
                            try
                            {
                                RequestContext rc = SecurityHelper.getRequestContext(actionRequest);
                                User user = userManager.getUser(userBean.getUsername());                                                        
                                PasswordCredential pwc = userManager.getPasswordCredential(user);
                                pwc.setEnabled(false);
                                userManager.storePasswordCredential(pwc);
                                SecurityHelper.updateCredentialInSession(rc, pwc);
                                userBean.setUser(user);
                                userAttributes = userBean.getUser().getSecurityAttributes().getAttributeMap();                                
                                user.getSecurityAttributes().getAttribute("user.question.failures", true).setStringValue("0");                                
                                userManager.updateUser(user);                                
                                audit.logUserActivity(userBean.getUsername(), 
                                        rc.getRequest().getRemoteAddr(), 
                                        AuditActivity.USER_DISABLE, "Failed question and answer limit reached");                                
                            }
                            catch (Exception e)
                            {
                            }
                            StatusMessage msg = new StatusMessage("Disabling user after too many failed questions.", StatusMessage.ERROR);
                            PortletMessaging.publish(actionRequest, STATUS_MESSAGE, msg); 
                            setView(actionRequest, phase, FAILURE2);                            
                        }
                        else
                        {                            
                            try
                            {
                                userBean.getUser().getSecurityAttributes().getAttribute("user.question.failures", true).setStringValue(Integer.toString(count));                                
                                userManager.updateUser(userBean.getUser());
                            }
                            catch (SecurityException e)
                            {}
                            StatusMessage msg = new StatusMessage("Invalid answer to question.", StatusMessage.ERROR);
                            PortletMessaging.publish(actionRequest, STATUS_MESSAGE, msg); 
                            setView(actionRequest, phase, FAILURE1);
                        }
                    }
                    else
                    {
                        try
                        {
                        	userBean.setPassPhrase(getUserAttribute(userAttributes, "user.passphrase", ""));
                            userBean.getUser().getSecurityAttributes().getAttribute("user.question.failures", true).setStringValue("0");                                
                            userManager.updateUser(userBean.getUser());
                        }
                        catch (Exception e)
                        {}
                        setView(actionRequest, phase, SUCCESS1);                                
                    }
                }
            }
            else if (phase.equals("enroll"))
            {
                boolean success = false;
                String password = userBean.getPassword();
                User user = userBean.getUser();
                if (user != null && password != null)
                {
                    AuthenticatedUser authUser = null;
                    try
                    {
                        authUser = authorization.authenticate(userBean.getUsername(), password);
                    }
                    catch (SecurityException e) 
                    {
                        RequestContext rc = SecurityHelper.getRequestContext(actionRequest);
                        audit.logUserActivity(userBean.getUsername(), rc.getRequest().getRemoteAddr(), 
                                AuditActivity.AUTHENTICATION_FAILURE, "PortalFilter");                    
                    }
                    if (authUser != null)
                    {
                        // validate request parameers, if valid update user preferences
                        String question1 = actionRequest.getParameter("question1");
                        String question2 = actionRequest.getParameter("question2");
                        String question3 = actionRequest.getParameter("question3");
                        String answer1 = actionRequest.getParameter("answer1");
                        String answer2 = actionRequest.getParameter("answer2");
                        String answer3 = actionRequest.getParameter("answer3");
                        String passPhrase = actionRequest.getParameter("passphrase");
                        
                        // validation (SecurityHelper.isEmpty, unique questions)
                        if (SecurityHelper.isEmpty(answer1) || SecurityHelper.isEmpty(answer2) || SecurityHelper.isEmpty(answer3))
                        {
                            StatusMessage msg = new StatusMessage("Please enter a valid answer for all 3 questions.", StatusMessage.ERROR);
                            PortletMessaging.publish(actionRequest, STATUS_MESSAGE, msg);
                            setView(actionRequest, phase, FAILURE1);                                                                    
                            return;
                        }
                        if (SecurityHelper.isEmpty(passPhrase))
                        {
                            StatusMessage msg = new StatusMessage("Please enter a valid pass phrase.", StatusMessage.ERROR);
                            PortletMessaging.publish(actionRequest, STATUS_MESSAGE, msg);
                            setView(actionRequest, phase, FAILURE1);                                                                    
                            return;
                        }
                        if (question1.equals(question2) || question1.equals(question3) || question2.equals(question3))
                        {
                            StatusMessage msg = new StatusMessage("Please select a unique question in all cases.", StatusMessage.ERROR);
                            PortletMessaging.publish(actionRequest, STATUS_MESSAGE, msg);
                            setView(actionRequest, phase, FAILURE1);                                                                    
                            return;                    
                        }
        
                        // update the prefs db (we are not logged in yet
                        user = userBean.getUser();
                        try
                        {
                            user.getSecurityAttributes().getAttribute("user.question.1", true).setStringValue(question1);
                            user.getSecurityAttributes().getAttribute("user.question.2", true).setStringValue(question2);
                            user.getSecurityAttributes().getAttribute("user.question.3", true).setStringValue(question3);
                            user.getSecurityAttributes().getAttribute("user.answer.1", true).setStringValue(answer1);
                            user.getSecurityAttributes().getAttribute("user.answer.2", true).setStringValue(answer2);
                            user.getSecurityAttributes().getAttribute("user.answer.3", true).setStringValue(answer3);
                            user.getSecurityAttributes().getAttribute("user.passphrase", true).setStringValue(passPhrase);
                            user.getSecurityAttributes().getAttribute("user.cookie", true).setStringValue(CaptchaImageResource.randomString(8, 16));
                            userManager.updateUser(user);
                            success = true;
                        }
                        catch (SecurityException e)
                        {
                            success = false;
                            RequestContext rc = SecurityHelper.getRequestContext(actionRequest);
                            audit.logUserActivity(userBean.getUsername(), rc.getRequest().getRemoteAddr(), AuditActivity.AUTHENTICATION_FAILURE, "Exception updating attributes" );
                            setView(actionRequest, phase, FAILURE2);                                                
                        }
                        if (success)
                        {
                            String username = userBean.getUsername();
                            String redirect = actionRequest.getParameter("redirect");
                            RequestContext rc = SecurityHelper.getRequestContext(actionRequest);
                            audit.logUserActivity(username, rc.getRequest().getRemoteAddr(), 
                                    LOGIN_ENROLL_ACTIVITY, "enrolling user with questions and passphrase");
                            redirect(actionRequest, actionResponse, redirect, username, password);
                        }
                    }
                }
                if (success == false)
                {
                    RequestContext rc = SecurityHelper.getRequestContext(actionRequest);
                    audit.logUserActivity(userBean.getUsername(), rc.getRequest().getRemoteAddr(), AuditActivity.AUTHENTICATION_FAILURE, "Unauthorized Attribute Modification Attempt.");
                    setView(actionRequest, phase, FAILURE2);                    
                }
            }
            else if (phase.equals("enroll-login"))
            {                
                String username = userBean.getUsername();
                String password = actionRequest.getParameter(LoginConstants.PASSWORD);
                if (SecurityHelper.isEmpty(password))
                {
                    RequestContext rc = SecurityHelper.getRequestContext(actionRequest);
                    rc.setSessionAttribute(MFALogin.ERRORCODE, LoginConstants.ERROR_INVALID_PASSWORD);                    
                    setView(actionRequest, phase, FAILURE1);
                    return;
                }                
                // are we in the enrollment phase?
                if (SecurityHelper.isEmpty(userBean.getPassPhrase()))
                {
                    AuthenticatedUser authUser = null;
                    boolean authenticated = false;
                    try
                    {
                        authUser = authorization.authenticate(username, password);
                        authenticated = true;
                    }
                    catch (SecurityException e) 
                    {}
                    if (authenticated)
                    {
                        userBean.setPassword(password);
                        setView(actionRequest, phase, SUCCESS1);
                        clearLoginMessages(actionRequest);
                    }
                    else
                    {
                        failedLoginProcessing(actionRequest, phase, username, userBean);                        
                    }                    
                }
            }
            else if (phase.equals("three"))
            {
                String redirect = actionRequest.getParameter("redirect");

                String username = userBean.getUsername();
                String password = actionRequest.getParameter(LoginConstants.PASSWORD);
                if (SecurityHelper.isEmpty(password) || SecurityHelper.isEmpty(redirect))
                {
                    RequestContext rc = SecurityHelper.getRequestContext(actionRequest);                    
                    rc.setSessionAttribute(MFALogin.ERRORCODE, LoginConstants.ERROR_INVALID_PASSWORD);                                        
                    setView(actionRequest, phase, FAILURE1);
                    return;
                }                
                // process authentication
                AuthenticatedUser authUser = null;
                boolean authenticated = false;
                try
                {
                    authUser = authorization.authenticate(username, password);
                    authenticated = true;
                }
                catch (SecurityException e) 
                {}                
                if (authenticated)
                {
                    userBean.setPassword(password);
                    setView(actionRequest, phase, SUCCESS1);
                    clearLoginMessages(actionRequest);
                    if (!userBean.isHasCookie() && !userBean.isPublicTerminal())
                    {
                        Map<String, SecurityAttribute> userAttributes = userBean.getUser().getSecurityAttributes().getAttributeMap();                        
                        String cookie = getUserAttribute(userAttributes, "user.cookie", username);                        
                        SecurityHelper.addMFACookie(actionRequest, username, cookie, cookieLifetime);
                        userBean.setHasCookie(true);
                    }                               
                    // set cookie
                    setView(actionRequest, phase, SUCCESS1);
                    redirect(actionRequest, actionResponse, redirect, username, password);                    
                }
                else
                {
                    failedLoginProcessing(actionRequest, phase, username, userBean);                        
                }                    
            }
            else if (phase.equals("restart"))
            {
                clearLoginMessages(actionRequest);                
                setView(actionRequest, phase,  SUCCESS1);                                                                    
            }                
        }
    }

    private void failedLoginProcessing(ActionRequest actionRequest, String phase, String username, UserBean userBean) throws NotSerializableException, PortletException
    {
        int nextView = FAILURE1;
        User user = null;
        try
        {
            user = userManager.getUser(username);
        }
        catch (Exception e)
        {
            logger.error("Failed to retrieve user, {}: {}", username, e.getMessage());
            return;
        }
        UserCredential pwdCredential = null;
        try
        {
            pwdCredential = SecurityHelper.getCredential(userManager, user);
        }
        catch (SecurityException e)
        {}
        RequestContext rc = SecurityHelper.getRequestContext(actionRequest);
        if (pwdCredential != null)
        {
            userBean.setUser(user);
            // Failed login processing
            HttpSession session = rc.getRequest().getSession(true);
            Integer retryCount = (Integer) session.getAttribute(MFALogin.RETRYCOUNT);
            if (retryCount == null)
                retryCount = new Integer(1);
            else
                retryCount = new Integer(retryCount.intValue() + 1);
            session.setAttribute(MFALogin.RETRYCOUNT, retryCount);
            if ( pwdCredential == null || !pwdCredential.isEnabled() )
            {
                rc.setSessionAttribute(MFALogin.ERRORCODE, LoginConstants.ERROR_CREDENTIAL_DISABLED);
                nextView = FAILURE2;
            }
            else if ( pwdCredential.isExpired() )
            {
                rc.setSessionAttribute(MFALogin.ERRORCODE, LoginConstants.ERROR_CREDENTIAL_EXPIRED);
            }
            else if ( maxNumberOfAuthenticationFailures > 1 && pwdCredential.getAuthenticationFailures() == maxNumberOfAuthenticationFailures -1  )
            {
                rc.setSessionAttribute(MFALogin.ERRORCODE, LoginConstants.ERROR_FINAL_LOGIN_ATTEMPT);
            }
            else
            {
                rc.setSessionAttribute(MFALogin.ERRORCODE, LoginConstants.ERROR_INVALID_PASSWORD);
            }
        }
        audit.logUserActivity(username, rc.getRequest().getRemoteAddr(), AuditActivity.AUTHENTICATION_FAILURE, "MFA");
        //StatusMessage msg = new StatusMessage("invalid password.", StatusMessage.ERROR);
        //PortletMessaging.publish(actionRequest, STATUS_MESSAGE, msg);
        setView(actionRequest, phase, nextView);
    }

    private void clearLoginMessages(PortletRequest actionRequest)
    {
        RequestContext rc = SecurityHelper.getRequestContext(actionRequest);
        HttpSession session = rc.getRequest().getSession(true);
        session.removeAttribute(MFALogin.RETRYCOUNT);
        session.removeAttribute(MFALogin.ERRORCODE);
        session.removeAttribute(LoginConstants.RETRYCOUNT);
        session.removeAttribute(LoginConstants.ERRORCODE);                
    }
    
    private void redirect(ActionRequest actionRequest, ActionResponse actionResponse, String redirect, String username, String password) throws IOException
    {
        StringBuffer s = new StringBuffer();
        s.append(redirect);
        if (!redirect.endsWith("/"))
            s.append("/");
        s.append("login/proxy");
        /*
        s.append("?");
        s.append(LoginConstants.USERNAME);
        s.append("=");
        s.append(username);
        s.append("&");
        s.append(LoginConstants.PASSWORD);
        s.append("=");
        s.append(password);
        */
        //System.out.println("Redirect: " + s.toString());
        RequestContext rc = SecurityHelper.getRequestContext(actionRequest);
        HttpServletRequest request = rc.getRequest();
        HttpSession session = request.getSession(true);
        session.setAttribute(LoginConstants.USERNAME, username);
        session.setAttribute(LoginConstants.PASSWORD, password);
        actionResponse.sendRedirect(s.toString());
    }
            
    public boolean generateQuestionAndAnswer(UserBean userBean)
    {
        User user = userBean.getUser();
        if (user == null)
        {
            if (userBean.getUsername() == null)
            {
                // hard out of luck
                return false;
            }
            else
            {
                try
                {
                    user = userManager.getUser(userBean.getUsername());
                }
                catch (Exception e)
                {
                   	// not a valid user; present a random question.
                	userBean.setQuestion( questionFactory.getRandomQuestion() );
                    return false;
                }
            }
        }
        Map<String, SecurityAttribute> userAttributes = userBean.getUser().getSecurityAttributes().getAttributeMap();
        String[] questions = new String[3];
        String[] answers = new String[3];
        int max = 3;
        
        questions[0] = getUserAttribute(userAttributes, "user.question.1", null);
        answers[0] = getUserAttribute(userAttributes, "user.answer.1", null);
        if (SecurityHelper.isEmpty(questions[0]) || SecurityHelper.isEmpty(answers[0]))
        {
            return false;
        }
        questions[1] = getUserAttribute(userAttributes,"user.question.2", null);
        answers[1] = getUserAttribute(userAttributes,"user.answer.2", null);
        if (SecurityHelper.isEmpty(questions[1]) || SecurityHelper.isEmpty(answers[1]))
        {
            // work with what we got
            userBean.setQuestion(questions[0]);
            userBean.setAnswer(answers[0]);
            return true;
        }
        questions[2] = getUserAttribute(userAttributes,"user.question.3", null);
        answers[2] = getUserAttribute(userAttributes,"user.answer.3", null);
        if (SecurityHelper.isEmpty(questions[2]) || SecurityHelper.isEmpty(answers[2]))
        {
            // work with what we got
            max = 2;
        }
        
        int index = rand.nextInt(max);
        userBean.setQuestion(questions[index]);
        userBean.setAnswer(answers[index]);
        return true;        
    }

    private String getUserAttribute(Map<String, SecurityAttribute> map, String name, String defaultValue)
    {
        String value = defaultValue;
        SecurityAttribute sa = map.get(name);
        if (sa != null)
        {
            value = sa.getStringValue();
            if (value == null)
                value = defaultValue;
        }
        return value;
    }
    
}
