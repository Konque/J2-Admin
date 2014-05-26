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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.portals.applications.webcontent.proxy.SSOSiteCredentials;
import org.apache.portals.applications.webcontent.proxy.impl.DefaultSSOSiteCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

public class TestSSOSiteCredentialsProvider extends TestCase
{
    
    private static Logger log = LoggerFactory.getLogger(TestSSOSiteCredentialsProvider.class);
    
    private MockHttpSession session;
    private List<SSOSiteCredentials> ssoSiteCredsOfSubject;
    
    @Override
    public void setUp()
    {
        session = new MockHttpSession();
        
        ssoSiteCredsOfSubject = new ArrayList<SSOSiteCredentials>();
        
        DefaultSSOSiteCredentials siteCreds = new DefaultSSOSiteCredentials("http://www.localhost.com", "www.localhost.com");
        siteCreds.setUsername("admin");
        siteCreds.setPassword("admin");
        ssoSiteCredsOfSubject.add(siteCreds);
        
        siteCreds = new DefaultSSOSiteCredentials("http://www.localhost.com/basicauth", "www.localhost.com");
        siteCreds.setUsername("basic");
        siteCreds.setPassword("basic");
        ssoSiteCredsOfSubject.add(siteCreds);

        siteCreds = new DefaultSSOSiteCredentials("http://www.localhost.com/formauth", "www.localhost.com");
        siteCreds.setFormAuthentication(true);
        siteCreds.setFormUserField("user");
        siteCreds.setFormPwdField("pass");
        siteCreds.setUsername("form");
        siteCreds.setPassword("form");
        ssoSiteCredsOfSubject.add(siteCreds);
        
        session.setAttribute(SSOReverseProxyIFramePortlet.SUBJECT_SSO_SITE_CREDS, ssoSiteCredsOfSubject);
    }
    
    public void testBestURLMatches() throws Exception
    {
        DefaultSSOSiteCredentialsProviderImpl provider = new DefaultSSOSiteCredentialsProviderImpl();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);
        
        List<SSOSiteCredentials> siteCreds = provider.getSSOCredentials(request, "http://www.localhost.com");
        log.info("siteCreds: " + siteCreds);
        assertNotNull(siteCreds);
        assertTrue(siteCreds.size() == 1);
        assertEquals("admin", siteCreds.get(0).getUsername());
        
        siteCreds = provider.getSSOCredentials(request, "http://www.localhost.com/index.html");
        log.info("siteCreds: " + siteCreds);
        assertNotNull(siteCreds);
        assertTrue(siteCreds.size() == 1);
        assertEquals("admin", siteCreds.get(0).getUsername());
        
        siteCreds = provider.getSSOCredentials(request, "http://www.localhost.com/basicauth");
        log.info("siteCreds: " + siteCreds);
        assertNotNull(siteCreds);
        assertTrue(siteCreds.size() == 2);
        assertEquals("basic", siteCreds.get(0).getUsername());
        assertEquals("admin", siteCreds.get(1).getUsername());
        
        siteCreds = provider.getSSOCredentials(request, "http://www.localhost.com/basicauth/index.html");
        log.info("siteCreds: " + siteCreds);
        assertNotNull(siteCreds);
        assertTrue(siteCreds.size() == 2);
        assertEquals("basic", siteCreds.get(0).getUsername());
        assertEquals("admin", siteCreds.get(1).getUsername());
        
        siteCreds = provider.getSSOCredentials(request, "http://www.localhost.com/formauth");
        log.info("siteCreds: " + siteCreds);
        assertNotNull(siteCreds);
        assertTrue(siteCreds.size() == 2);
        assertEquals("form", siteCreds.get(0).getUsername());
        assertEquals("admin", siteCreds.get(1).getUsername());
        
        siteCreds = provider.getSSOCredentials(request, "http://www.localhost.com/formauth/index.html");
        log.info("siteCreds: " + siteCreds);
        assertNotNull(siteCreds);
        assertTrue(siteCreds.size() == 2);
        assertEquals("form", siteCreds.get(0).getUsername());
        assertEquals("admin", siteCreds.get(1).getUsername());
    }
}
