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
package org.apache.jetspeed.portlets.rpad.simple;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.jetspeed.portlets.rpad.PortletApplication;
import org.apache.jetspeed.portlets.rpad.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

public class SimpleRepository implements Repository
{
    /**
     * Logger for this class
     */
    private static final Logger log = LoggerFactory.getLogger(SimpleRepository.class);
    private String name;
    private String configPath;
    private List<PortletApplication> portletApplications;
    private boolean available;

    public SimpleRepository(String name, String configPath)
    {
        this.name = name;
        this.configPath = configPath;
        available = false;
    }

    public void init()
    {
        if (log.isDebugEnabled())
        {
            log.debug("Loading a repository: " + configPath);
        }
        if (configPath == null)
        {
            throw new IllegalStateException("The configuration path is null.");
        }
        // load config
        try
        {
            InputSource inputSource = getInputSource(configPath);
            if (inputSource == null)
            {
                log.error("Failed to load: " + configPath);
                return;
            }
            SAXParserFactory spfactory = SAXParserFactory.newInstance();
            SAXParser parser = spfactory.newSAXParser();
            SimpleConfigHandler repoConfigHandler = new SimpleConfigHandler();
            parser.parse(inputSource, repoConfigHandler);
            portletApplications = repoConfigHandler.getPortletApplications();
            if (portletApplications == null)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("No portlet application.");
                }
                portletApplications = new ArrayList<PortletApplication>();
            }
            available = true;
        }
        catch (Exception e)
        {
            log.error("Could not load a repository.", e);
            portletApplications = new ArrayList<PortletApplication>();
            available = false;
        }
    }

    protected InputSource getInputSource(String path)
    {
        if (path.startsWith("http:") || path.startsWith("https:"))
        {
            try
            {
                URL url = new URL(path);
                return new InputSource(url.openStream());
            }
            catch (MalformedURLException e)
            {
                log.error("Wrong url: " + path, e);
            }
            catch (IOException e)
            {
                log.error("Could not load " + path, e);
            }
        }
        else if (path.startsWith("file:"))
        {
            try
            {
                return new InputSource(new FileInputStream(new File(path.substring(5))));
            }
            catch (FileNotFoundException e)
            {
                log.error("Could not load " + path, e);
            }
        }
        return null;
    }

    public PortletApplication getPortletApplication(String groupId, String artifactId, String version, String packaging)
    {
        if (groupId != null && artifactId != null && version != null && packaging != null)
        {
            for (PortletApplication pApps : portletApplications)
                if (groupId.equals(pApps.getGroupId()) && artifactId.equals(pApps.getArtifactId()) && version.equals(pApps.getVersion()) &&
                    packaging.equals(pApps.getPackaging()))
                {
                    return pApps;
                }
        }
        return null;
    }

    /**
     * @return the configPath
     */
    public String getConfigPath()
    {
        return configPath;
    }

    /**
     * @param configPath
     *            the configPath to set
     */
    public void setConfigPath(String configPath)
    {
        this.configPath = configPath;
    }

    /**
     * @return the portletApplications
     */
    public List<PortletApplication> getPortletApplications()
    {
        return portletApplications;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    public String toXMLString()
    {
        return "  <repository>\n" //
               +
               "    <name>" + getName() + "</name>\n" //
               + "    <class name=\"" + "org.apache.jetspeed.portlets.rpad.simple.SimpleRepository" + "\"/>\n" //
               + "    <property name=\"configPath\">" + getConfigPath() + "</property>\n" //
               + "  </repository>\n";
    }

    /**
     * @return the available
     */
    public boolean isAvailable()
    {
        return available;
    }

    /**
     * @param available
     *            the available to set
     */
    public void setAvailable(boolean available)
    {
        this.available = available;
    }
}
