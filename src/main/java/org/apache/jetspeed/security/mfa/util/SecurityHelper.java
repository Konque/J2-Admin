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
package org.apache.jetspeed.security.mfa.util;

import java.util.Iterator;
import java.util.Set;

import javax.portlet.PortletRequest;
import javax.security.auth.Subject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jetspeed.PortalReservedParameters;
import org.apache.jetspeed.request.RequestContext;
import org.apache.jetspeed.security.PasswordCredential;
import org.apache.jetspeed.security.SecurityException;
import org.apache.jetspeed.security.User;
import org.apache.jetspeed.security.UserCredential;
import org.apache.jetspeed.security.UserManager;

/**
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: $
 */
public final class SecurityHelper
{
    public static UserCredential getCredential(UserManager um, User user)
    throws SecurityException
    {
        UserCredential credential = null;
        
        Subject subject = um.getSubject(user);
        Set credentials = subject.getPrivateCredentials();
        Iterator iter = credentials.iterator();
        while (iter.hasNext())
        {
            Object o = iter.next();
            if (o instanceof UserCredential)
            {
                credential = (UserCredential)o;
                break;
            }
        }
        return credential;
    }
    
    public static final String MFA_COOKIE = "jetspeed_mfa";
    public static final String MFA_VALID_COOKIE = "validated";
    
    public static Cookie getMFACookie(PortletRequest pRequest, String username)
    {
    	String MFAUserCookie = MFA_COOKIE + "_" + username;
        HttpServletRequest request = SecurityHelper.getHttpServletRequest(pRequest);
        Cookie auth = null;
        Cookie[] cookies = request.getCookies();
        for (int ix = 0; ix < cookies.length; ix++ ) 
        {
            if (cookies[ix].getName().equals(MFAUserCookie)) 
            {
                auth = cookies[ix];
                break;
            }
        }
        return auth;        
    }
    
    public static void addMFACookie(PortletRequest pRequest, String username, String value)
    {
    	final int FORTY_EIGHT_HOURS = 172800;
    	addMFACookie(pRequest, username, value, FORTY_EIGHT_HOURS);
    }

    public static void addMFACookie(PortletRequest pRequest, String username, String value, int lifetime)
    {
    	String MFAUserCookie = MFA_COOKIE + "_" + username;
        HttpServletResponse response = SecurityHelper.getHttpServletResponse(pRequest);
        String path = SecurityHelper.getHttpServletRequest(pRequest).getContextPath();        
        Cookie auth = new Cookie(MFAUserCookie, value);        
        auth.setPath(path);
        auth.setMaxAge(lifetime);
        response.addCookie(auth);
    }

    public static RequestContext getRequestContext(PortletRequest request)
    {
        return (RequestContext) request.getAttribute(PortalReservedParameters.REQUEST_CONTEXT_ATTRIBUTE);
    }

    public static HttpServletRequest getHttpServletRequest(PortletRequest pRequest)
    {
        return getRequestContext(pRequest).getRequest();
    }    

    public static HttpServletResponse getHttpServletResponse(PortletRequest pRequest)
    {
        return getRequestContext(pRequest).getResponse();
    }    
    
    public static boolean isEmpty(String s)
    {
        if (s == null)
            return true;
        if (s.trim().length() == 0)
            return true;
        return false;
    }
    
    public static void updateCredentialInSession(RequestContext requestContext, PasswordCredential credential)
    {
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
}