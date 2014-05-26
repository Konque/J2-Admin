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
package org.apache.jetspeed.portlets.site;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jetspeed.om.common.SecurityConstraints;
import org.apache.jetspeed.om.folder.Folder;
import org.apache.jetspeed.om.page.Fragment;
import org.apache.jetspeed.om.page.Link;
import org.apache.jetspeed.om.page.Page;
import org.apache.jetspeed.om.portlet.GenericMetadata;
import org.apache.jetspeed.om.portlet.LocalizedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:vkumar@apache.org">Vivek Kumar</a>
 * @version $Id:
 */
public class JetspeedDocument implements Serializable
{
    
    private final Logger logger = LoggerFactory.getLogger(JetspeedDocument.class);
    
    private static final long serialVersionUID = -7429444774638220814L;
    private String name;
    private String title;
    private String shortTitle;
    private String pageDecorator;
    private String portletDecorator;
    private String desktopTheme;
    private boolean hidden;
    private List securityConstraints;
    private String page;
    private String target;
    private String url;
    private String path;
    private List<JetspeedDocumentMetaData> metaData;
    private List<String> documentOrder;
    private String type;

    public JetspeedDocument()
    {
        
    }
    public JetspeedDocument(Folder folder)
    {
        this.type = PortalSiteManager.FOLDER_NODE_TYPE;
        this.path = folder.getPath();
        this.name = folder.getName();
        this.title = folder.getTitle();
        this.shortTitle = folder.getTitle();
        this.hidden = folder.isHidden();
        this.page = folder.getDefaultPage();
        this.pageDecorator = folder.getDefaultDecorator(Fragment.LAYOUT);
        this.portletDecorator = folder.getDefaultDecorator(Fragment.PORTLET); 
        loadMetaData(folder.getMetadata());
        loadSecurityData(folder.getSecurityConstraints());
        loadDocumentOrder(folder.getDocumentOrder());
    }

    public JetspeedDocument(Page page)
    {
        this.type = PortalSiteManager.PAGE_NODE_TYPE;
        this.path = page.getPath();
        this.name = page.getName();
        this.title = page.getTitle();
        this.shortTitle = page.getShortTitle();
        this.hidden = page.isHidden();
        this.pageDecorator = page.getDefaultDecorator(Fragment.LAYOUT);
        this.portletDecorator = page.getDefaultDecorator(Fragment.PORTLET);
        this.desktopTheme = page.getSkin();
        loadMetaData(page.getMetadata());
        loadSecurityData(page.getSecurityConstraints());
    }

    public JetspeedDocument(Link link)
    {
        this.type = PortalSiteManager.LINK_NODE_TYPE;
        this.path = link.getPath();
        this.name = link.getName();
        this.title = link.getTitle();
        this.shortTitle = link.getShortTitle();
        this.hidden = link.isHidden();
        this.target = link.getTarget();
        this.url = link.getUrl();
        loadMetaData(link.getMetadata());
        loadSecurityData(link.getSecurityConstraints());
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
        this.name = getEscapedName(name);
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @return the shortTitle
     */
    public String getShortTitle()
    {
        return shortTitle;
    }

    /**
     * @param shortTitle
     *            the shortTitle to set
     */
    public void setShortTitle(String shortTitle)
    {
        this.shortTitle = shortTitle;
    }

    /**
     * @return the pageDecorator
     */
    public String getPageDecorator()
    {
        return pageDecorator;
    }

    /**
     * @param pageDecorator
     *            the pageDecorator to set
     */
    public void setPageDecorator(String pageDecorator)
    {
        this.pageDecorator = pageDecorator;
    }

    /**
     * @return the portletDecorator
     */
    public String getPortletDecorator()
    {
        return portletDecorator;
    }

    /**
     * @param portletDecorator
     *            the portletDecorator to set
     */
    public void setPortletDecorator(String portletDecorator)
    {
        this.portletDecorator = portletDecorator;
    }

    /**
     * @return the desktopTheme
     */
    public String getDesktopTheme()
    {
        return desktopTheme;
    }

    /**
     * @param desktopTheme
     *            the desktopTheme to set
     */
    public void setDesktopTheme(String desktopTheme)
    {
        this.desktopTheme = desktopTheme;
    }

    /**
     * @return the hidden
     */
    public boolean isHidden()
    {
        return hidden;
    }

    /**
     * @param hidden
     *            the hidden to set
     */
    public void setHidden(boolean hidden)
    {
        this.hidden = hidden;
    }

    /**
     * @return the securityConstraints
     */
    public List getSecurityConstraints()
    {
        return securityConstraints;
    }

    /**
     * @param securityConstraints
     *            the securityConstraints to set
     */
    public void setSecurityConstraints(List securityConstraints)
    {
        this.securityConstraints = securityConstraints;
    }

    /**
     * @return the metaData
     */
    public List<JetspeedDocumentMetaData> getMetaData()
    {
        return metaData;
    }

    /**
     * @param metaData
     *            the metaData to set
     */
    public void setMetaData(List<JetspeedDocumentMetaData> metaData)
    {
        this.metaData = metaData;
    }

    
    
    /**
     * @return the page
     */
    public String getPage()
    {
        return page;
    }

    /**
     * @param page the page to set
     */
    public void setPage(String page)
    {
        this.page = page;
    }

    /**
     * @return the target
     */
    public String getTarget()
    {
        return target;
    }

    /**
     * @param target the target to set
     */
    public void setTarget(String target)
    {
        this.target = target;
    }

    /**
     * @return the url
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url)
    {
        this.url = url;
    }
        
    /**
     * @return the documentOrder
     */
    public List<String> getDocumentOrder()
    {
        return documentOrder;
    }
    /**
     * @param documentOrder the documentOrder to set
     */
    public void setDocumentOrder(List<String> documentOrder)
    {
        this.documentOrder = documentOrder;
    }
    
    /**
     * @return the path
     */
    public String getPath()
    {
        return path;
    }
            
    /**
     * @return the type
     */
    public String getType()
    {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }
    private void loadSecurityData(SecurityConstraints constraints)
    {
        this.securityConstraints = new ArrayList();
        if (constraints != null)
        {
            securityConstraints.addAll(constraints.getSecurityConstraintsRefs());
        }
    }

    private void loadMetaData(GenericMetadata objectMetaData)
    {
        this.metaData = new ArrayList<JetspeedDocumentMetaData>();
        if (objectMetaData.getFields() != null)
        {
            Iterator metaDataIterator = objectMetaData.getFields().iterator();
            LocalizedField field;
            while (metaDataIterator.hasNext())
            {
                field = (LocalizedField) metaDataIterator.next();
                this.metaData.add(new JetspeedDocumentMetaData(field.getName(), field.getLocale().toString(), field.getValue()));
            }
        }
    }
    
    private void loadDocumentOrder(List documentOrder)
    {
        this.documentOrder = new ArrayList<String>();
        for(int index=0;index<documentOrder.size();index++)
        {
            this.documentOrder.add(documentOrder.get(index).toString());
        }
    }
    
    protected String getEscapedName(String pageName)
    {
        if(pageName== null)
        {
            return pageName;
        }
        try
        {           
            return URLEncoder.encode(pageName, "UTF-8").replace('%', '_');
        }
        catch (UnsupportedEncodingException e)
        {
            logger.error("Unsupported encoding: UTF-8");
            return pageName;
        }
    }
}
