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
package org.apache.jetspeed.security.mfa.impl;

import java.io.InputStream;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.jetspeed.security.mfa.MFA;
import org.apache.jetspeed.security.mfa.MultiFacetedAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: $
 */
public class MFAServletListener implements ServletContextListener
{
    
    static final Logger logger = LoggerFactory.getLogger(MFAServletListener.class);
    
	// TODO: Re-read the properties files as few times as possible.

    public void contextDestroyed(ServletContextEvent arg0)
    {
        MultiFacetedAuthentication mfa = MFA.getInstance();
        if (mfa != null)
            mfa.destroy();
    }

    public void contextInitialized(ServletContextEvent event)
    {
        String configLocation = "/WEB-INF/mfa.properties";
        String ttsConfigLocation = "/WEB-INF/tts.properties";
        
        try
        {
            InputStream is = event.getServletContext().getResourceAsStream(configLocation);
            PropertiesConfiguration config = new PropertiesConfiguration();
            config.load(is);
            is.close();

            InputStream tis = event.getServletContext().getResourceAsStream(ttsConfigLocation);            
            PropertiesConfiguration ttsConfig = new PropertiesConfiguration();
            ttsConfig.load(tis);
            tis.close();
            
            String rootPath = event.getServletContext().getRealPath("/");
            MultiFacetedAuthentication mfa = new MultiFacetedAuthenticationImpl(config, ttsConfig, rootPath);
            MFA.setInstance(mfa);
        }
        catch (Throwable e)
        {
            logger.error("Unexpected error during loading configuration.", e);
            PropertiesConfiguration config = new PropertiesConfiguration();
            PropertiesConfiguration ttsConfig = new PropertiesConfiguration();
            MultiFacetedAuthentication mfa = new MultiFacetedAuthenticationImpl(config, ttsConfig);
            MFA.setInstance(mfa);            
        }
    }
    
}