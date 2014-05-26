/*
 * contributor license agreements.  See the NOTICE file distributed with
 * Licensed to the Apache Software Foundation (ASF) under one or more
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
import java.util.List;

import org.apache.jetspeed.om.folder.FolderNotFoundException;
import org.apache.jetspeed.om.folder.InvalidFolderException;
import org.apache.jetspeed.om.folder.MenuDefinition;
import org.apache.jetspeed.page.PageNotFoundException;
import org.apache.jetspeed.page.document.NodeException;
import org.apache.jetspeed.portlets.JetspeedServiceLocator;

/**
 * @author <a href="mailto:vkumar@apache.org">Vivek Kumar</a>
 * @version $Id:
 */
public class MenuTreeNode implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String path;
    private String name;
    private String type;
    private JetspeedServiceLocator locator;

    public MenuTreeNode(String name, JetspeedServiceLocator locator)
    {
        this.name = name;
        this.locator = locator;
    }

    public MenuTreeNode(String name, String path, String type, JetspeedServiceLocator locator)
    {
        this.name = name;
        this.locator = locator;
        this.type = type;
        this.path = path;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return name != null ? name : super.toString();
    }

    public MenuDefinition getDefinition()
    {
      if (type.equals(PortalSiteManager.FOLDER_NODE_TYPE))
      {
          getFolderMenuDefinition();
      }
      else if (type.equals(PortalSiteManager.PAGE_NODE_TYPE))
      {
          getPageMenuDefinition();
      }
      return null;
    }

    private MenuDefinition getPageMenuDefinition()
    {
        MenuDefinition definition = null;
        boolean definitionFound;
        try
        {
            return getMenu(locator.getPageManager().getPage(path).getMenuDefinitions());
        }
        catch (PageNotFoundException e)
        {
            definitionFound = false;
        }
        catch (InvalidFolderException e)
        {
            definitionFound = false;
        }
        catch (NodeException e)
        {
            definitionFound = false;
        }
        if (!definitionFound)
        {
            definition = locator.getPageManager().newPageMenuDefinition();
        }
        return definition;
    }

    private MenuDefinition getFolderMenuDefinition()
    {
        MenuDefinition definition = null;
        boolean definitionFound;
        try
        {
            return getMenu(locator.getPageManager().getFolder(path).getMenuDefinitions());
        }
        catch (FolderNotFoundException e)
        {
            definitionFound = false;
        }
        catch (InvalidFolderException e)
        {
            definitionFound = false;
        }
        catch (NodeException e)
        {
            definitionFound = false;
        }
        if (!definitionFound)
        {
            definition = locator.getPageManager().newFolderMenuDefinition();
        }
        return definition;
    }


    private MenuDefinition getMenu(List menuDefinitions)
    {
        for (Object menuDefinition : menuDefinitions) {
            MenuDefinition definition = (MenuDefinition) menuDefinition;
            if (definition.getName().equals(name)) {
                return definition;
            }
        }
        if (type.equals(PortalSiteManager.PAGE_NODE_TYPE))
        {
            return locator.getPageManager().newPageMenuDefinition();
        }
        return locator.getPageManager().newFolderMenuDefinition();
    }
}
