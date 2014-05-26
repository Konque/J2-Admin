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

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.portals.applications.webcontent.proxy.SSOSiteCredentials;
import org.apache.portals.applications.webcontent.proxy.SSOSiteCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultSSOSiteCredentialsProviderImpl implements SSOSiteCredentialsProvider, Serializable
{
    private static final long serialVersionUID = 1L;
    
    private static Logger log = LoggerFactory.getLogger(DefaultSSOSiteCredentialsProviderImpl.class);

    public List<SSOSiteCredentials> getSSOCredentials(HttpServletRequest request, String siteURL)
    {
        List<SSOSiteCredentials> ssoSiteCreds = new ArrayList<SSOSiteCredentials>();
        HttpSession session = request.getSession(false);
        
        if (session == null)
        {
            return ssoSiteCreds;
        }
        
        List<SSOSiteCredentials> ssoSiteCredsOfSubject = (List<SSOSiteCredentials>) session.getAttribute(SSOReverseProxyIFramePortlet.SUBJECT_SSO_SITE_CREDS);
        
        if (ssoSiteCredsOfSubject != null)
        {
            URI siteURI = URI.create(siteURL);
            
            for (SSOSiteCredentials ssoCreds : ssoSiteCredsOfSubject)
            {
                try
                {
                    String siteBaseURL = ssoCreds.getBaseURL();
                    
                    if (StringUtils.startsWith(siteURL, siteBaseURL) && ssoCreds.getHost().equals(siteURI.getHost()) && ssoCreds.getPort() == siteURI.getPort())
                    {
                        ssoSiteCreds.add(ssoCreds);
                    }
                }
                catch (Exception e)
                {
                    if (log.isWarnEnabled())
                    {
                        log.warn("Failed to match site uri. {}", e.toString());
                    }
                }
            }
        }
        
        if (!ssoSiteCreds.isEmpty())
        {
            Collections.sort(ssoSiteCreds, new URLMatchDifferenceBasedComparator<SSOSiteCredentials>(siteURL));
        }
        
        return ssoSiteCreds;
    }
    
    private class URLMatchDifferenceBasedComparator<SSOCredentials> implements Comparator<SSOSiteCredentials>
    {
        private String siteURL;
        
        private URLMatchDifferenceBasedComparator(String siteURL)
        {
            this.siteURL = siteURL;
        }
        
        public int compare(SSOSiteCredentials creds1, SSOSiteCredentials creds2)
        {
            int diff1 = StringUtils.indexOfDifference(siteURL, creds1.getBaseURL());
            int diff2 = StringUtils.indexOfDifference(siteURL, creds2.getBaseURL());
            
            if (diff1 == -1)
            {
                return -1;
            }
            else if (diff2 == -1)
            {
                return 1;
            }
            else if (diff1 > diff2)
            {
                return -1;
            }
            else if (diff1 < diff2)
            {
                return 1;
            }
            
            return 0;
        }
    }
}
