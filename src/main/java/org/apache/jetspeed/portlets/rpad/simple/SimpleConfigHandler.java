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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.jetspeed.portlets.rpad.PortletApplication;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class SimpleConfigHandler extends DefaultHandler
{
    private List<PortletApplication> portletApplications;
    private PortletApplication portletApplication;
    private List qNameList;

    public SimpleConfigHandler()
    {
        portletApplications = new ArrayList<PortletApplication>();
        qNameList = new ArrayList();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes)
    {
        if ("repository".equals(qName))
        {
            // TODO version
            // TODO id
        }
        else if ("portlet".equals(qName))
        {
            portletApplication = new PortletApplication();
            String artifactId = attributes.getValue("id");
            if (artifactId != null)
            {
                portletApplication.setArtifactId(artifactId);
            }
            String groupId = attributes.getValue("group");
            if (groupId != null)
            {
                portletApplication.setGroupId(groupId);
            }
            String version = attributes.getValue("version");
            if (version != null)
            {
                portletApplication.setVersion(version);
            }
        }
        synchronized (qNameList)
        {
            qNameList.add(qName);
        }
    }

    public void characters(char[] ch, int start, int length)
    {
        if (qNameList.size() < 1)
        {
            return;
        }
        String value = new String(ch, start, length);
        String qName = (String) qNameList.get(qNameList.size() - 1);
        String parentQName;
        if (qNameList.size() - 2 >= 0)
        {
            parentQName = (String) qNameList.get(qNameList.size() - 2);
        }
        else
        {
            parentQName = "";
        }
        if ("portletSpecVersion".equals(qName))
        {
            portletApplication.setPortletSpecVersion(value);
        }
        else if ("packaging".equals(qName))
        {
            portletApplication.setPackaging(value);
        }
        else if ("name".equals(qName))
        {
            if ("publisher".equals(parentQName))
            {
                portletApplication.setPublisherName(value);
            }
            else if ("license".equals(parentQName))
            {
                portletApplication.setLicenseName(value);
            }
            else
            {
                portletApplication.setName(value);
            }
        }
        else if ("description".equals(qName))
        {
            portletApplication.setDescription(value);
        }
        else if ("tag".equals(qName))
        {
            portletApplication.addTag(value);
        }
        else if ("url".equals(qName))
        {
            if ("publisher".equals(parentQName))
            {
                portletApplication.setPublisherUrl(value);
            }
            else if ("license".equals(parentQName))
            {
                portletApplication.setLicenseUrl(value);
            }
        }
        else if ("binaryURL".equals(qName))
        {
            portletApplication.setBinaryUrl(value);
        }
        else if ("sourceURL".equals(qName))
        {
            portletApplication.setSourceUrl(value);
        }
        else if ("imageURL".equals(qName))
        {
            portletApplication.setImageUrl(value);
        }
        // TODO dependencies
        // TODO license
        else if ("compiledJDKVersion".equals(qName))
        {
            portletApplication.setCompiledJDKVersion(value);
        }
        else if ("locale".equals(qName))
        {
            Locale l = getLocaleFromString(value);
            if (l != null)
            {
                portletApplication.addSupportedLocale(l);
            }
        }
    }

    private Locale getLocaleFromString(String localeString)
    {
        StringTokenizer st = new StringTokenizer(localeString, "_-");
        String[] buf = new String[3];
        int count = 0;
        while (st.hasMoreTokens())
        {
            buf[count] = st.nextToken();
            count++;
        }
        if (count > 2)
        {
            return new Locale(buf[0], buf[1], buf[2]);
        }
        else if (count > 1)
        {
            return new Locale(buf[0], buf[1]);
        }
        else if (count > 0)
        {
            return new Locale(buf[0]);
        }
        return null;
    }

    public void endElement(String uri, String localName, String qName)
    {
        if ("portlet".equals(qName))
        {
            portletApplications.add(portletApplication);
            portletApplication = null;
        }
        synchronized (qNameList)
        {
            if (qNameList.size() < 1)
            {
                throw new IllegalStateException("The stacked QName is 0.");
            }
            String stackedQName = (String) qNameList.remove(qNameList.size() - 1);
            if (!qName.equals(stackedQName))
            {
                throw new IllegalStateException("The expected QName is " + stackedQName + ". But the current value is " + qName);
            }
        }
    }

    /**
     * @return the portletApplications
     */
    public List<PortletApplication> getPortletApplications()
    {
        return portletApplications;
    }

    /**
     * @param portletApplications
     *            the portletApplications to set
     */
    public void setPortletApplications(List<PortletApplication> portletApplications)
    {
        this.portletApplications = portletApplications;
    }
}
