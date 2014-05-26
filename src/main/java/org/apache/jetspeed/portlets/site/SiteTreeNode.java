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

import org.apache.jetspeed.om.folder.Folder;
import org.apache.jetspeed.om.page.Link;
import org.apache.jetspeed.om.page.Page;

/**
 * @author <a href="mailto:vkumar@apache.org">Vivek Kumar</a>
 * @version $Id:
 */
public class SiteTreeNode implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 2349948500132928089L;
    private String nodeName;
    private String nodePath;
    private boolean hasChildern;
    private boolean loaded;
    public enum FileType {Folder,Page,Link};
    private FileType docType;

    public SiteTreeNode(Folder folder,boolean loaded)
    {
        this.nodeName = folder.getName();
        this.nodePath = folder.getPath();
        docType = FileType.Folder;
        this.loaded = loaded;
    }

    public SiteTreeNode(Folder folder)
    {
        this.nodeName = folder.getName();
        this.nodePath = folder.getPath();
        docType = FileType.Folder;
    }


    public SiteTreeNode(Page page)
    {
        this.nodeName = page.getName();
        this.nodePath = page.getPath();
        docType = FileType.Page;
    }
  
    public SiteTreeNode(Link link)
    {
        this.nodeName = link.getName();
        this.nodePath = link.getPath();
        docType = FileType.Link;
    }
    
    
    public SiteTreeNode(String name, String path,FileType type)
    {
        this.nodeName = name;
        this.nodePath = path;
        this.docType = type;
    }
    public SiteTreeNode(String name, String path,FileType type,boolean loaded)
    {
        this.nodeName = name;
        this.nodePath = path;
        this.docType = type;
        this.loaded = loaded;
    }
    /**
     * @return the nodeName
     */
    public String getNodeName()
    {
        return nodeName;
    }

    /**
     * @param nodeName
     *            the nodeName to set
     */
    public void setNodeName(String nodeName)
    {
        this.nodeName = nodeName;
    }

    /**
     * @return the nodePath
     */
    public String getNodePath()
    {
        return nodePath;
    }

    /**
     * @param nodePath
     *            the nodePath to set
     */
    public void setNodePath(String nodePath)
    {
        this.nodePath = nodePath;
    }

    /**
     * @return the hasChildern
     */
    public boolean isHasChildern()
    {
        return hasChildern;
    }

    /**
     * @param hasChildern
     *            the hasChildern to set
     */
    public void setHasChildern(boolean hasChildern)
    {
        this.hasChildern = hasChildern;
    }

    /**
     * @return the loaded
     */
    public boolean isLoaded()
    {
        return loaded;
    }

    /**
     * @param loaded the loaded to set
     */
    public void setLoaded(boolean loaded)
    {
        this.loaded = loaded;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return nodeName!=null?nodeName:super.toString();
    }

    /**
     * @return the docType
     */
    public FileType getDocType()
    {
        return docType;
    }

    /**
     * @param docType the docType to set
     */
    public void setDocType(FileType docType)
    {
        this.docType = docType;
    }
    
    
}
