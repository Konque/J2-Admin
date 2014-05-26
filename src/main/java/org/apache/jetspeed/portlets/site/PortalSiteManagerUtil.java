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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.exception.JetspeedException;
import org.apache.jetspeed.om.folder.Folder;
import org.apache.jetspeed.om.page.DynamicPage;
import org.apache.jetspeed.om.page.FragmentDefinition;
import org.apache.jetspeed.om.page.Link;
import org.apache.jetspeed.om.page.Page;
import org.apache.jetspeed.om.page.PageTemplate;
import org.apache.jetspeed.page.PageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:vkumar@apache.org">Vivek Kumar</a>
 * @version $Id$
 */
public class PortalSiteManagerUtil
{

    private static final Logger logger = LoggerFactory.getLogger(PortalSiteManagerUtil.class);
    private static String pathSeprator = System.getProperty("file.separator");
    private static String pageRoot = System.getProperty("java.io.tmpdir");

    private PortalSiteManagerUtil()
    {

    }

    public static String getDownloadLink(String objectName, String userName, String objectType) throws Exception
    {
        if ("/".equals(objectName))
        {
            objectName = "";
        }
        
        String link = "";
        
        String basePath = System.getProperty("java.io.tmpdir");
        
        if (objectType.equalsIgnoreCase("folder"))
        {
            String sourcePath = null;
            
            if (!StringUtils.isEmpty(objectName))
            {
                sourcePath = getUserFolder(userName, false) + pathSeprator + objectName;
            }
            else
            {
                sourcePath = getUserFolder(userName, false);
            }
            
            String target = sourcePath + ".zip";
            boolean success = zipObject(sourcePath, target);
            if (!success)
                throw new Exception("Error Occurered in zipping the file");
            link = target;
        }
        else
        {
            link = basePath + "/" + userName + "/" + objectName;
        }
        return link;
    }
    
    public static Folder importFolder(PageManager castorPageManager, Folder srcFolder, String userName, String destination) throws JetspeedException
    {
        return importFolder(castorPageManager, srcFolder, userName, destination, true);
    }
    
    public static Folder importFolder(PageManager castorPageManager, Folder srcFolder, String userName, String destination, boolean copyIds) throws JetspeedException
    {
        String newPath = "";
        Folder dstFolder = lookupFolder(castorPageManager,srcFolder.getPath());
        dstFolder = castorPageManager.copyFolder(srcFolder, getUserFolder(userName, true) + destination);
        castorPageManager.updateFolder(dstFolder);
        Iterator pages = srcFolder.getPages().iterator();
        while (pages.hasNext())
        {
            Page srcPage = (Page) pages.next();
            Page dstPage = lookupPage(castorPageManager,srcPage.getPath());
            newPath = getUserFolder(userName, true) + destination + getRealPath(srcPage.getPath());
            dstPage = castorPageManager.copyPage(srcPage, newPath, copyIds);
            castorPageManager.updatePage(dstPage);
        }
        Iterator pageTemplates = srcFolder.getPageTemplates().iterator();
        while (pageTemplates.hasNext())
        {
            PageTemplate srcPageTemplate = (PageTemplate) pageTemplates.next();
            PageTemplate dstPageTemplate = lookupPageTemplate(castorPageManager,srcPageTemplate.getPath());
            newPath = getUserFolder(userName, true) + destination + getRealPath(srcPageTemplate.getPath());
            dstPageTemplate = castorPageManager.copyPageTemplate(srcPageTemplate, newPath, copyIds);
            castorPageManager.updatePageTemplate(dstPageTemplate);
        }        
        Iterator dynamicPages = srcFolder.getDynamicPages().iterator();
        while (dynamicPages.hasNext())
        {
            DynamicPage srcDynamicPage = (DynamicPage) dynamicPages.next();
            DynamicPage dstDynamicPage = lookupDynamicPage(castorPageManager,srcDynamicPage.getPath());
            newPath = getUserFolder(userName, true) + destination + getRealPath(srcDynamicPage.getPath());
            dstDynamicPage = castorPageManager.copyDynamicPage(srcDynamicPage, newPath, copyIds);
            castorPageManager.updateDynamicPage(dstDynamicPage);
        }
        Iterator fragmentDefinitions = srcFolder.getFragmentDefinitions().iterator();
        while (fragmentDefinitions.hasNext())
        {
            FragmentDefinition srcFragmentDefinition = (FragmentDefinition) fragmentDefinitions.next();
            FragmentDefinition dstFragmentDefinition = lookupFragmentDefinition(castorPageManager,srcFragmentDefinition.getPath());
            newPath = getUserFolder(userName, true) + destination + getRealPath(srcFragmentDefinition.getPath());
            dstFragmentDefinition = castorPageManager.copyFragmentDefinition(srcFragmentDefinition, newPath, copyIds);
            castorPageManager.updateFragmentDefinition(dstFragmentDefinition);
        }
        Iterator links = srcFolder.getLinks().iterator();
        while (links.hasNext())
        {
            Link srcLink = (Link) links.next();
            Link dstLink = lookupLink(castorPageManager,srcLink.getPath());
            newPath = getUserFolder(userName, true) + destination + getRealPath(srcLink.getPath());
            dstLink = castorPageManager.copyLink(srcLink, newPath);
            castorPageManager.updateLink(dstLink);
        }
        Iterator folders = srcFolder.getFolders().iterator();
        while (folders.hasNext())
        {
            Folder folder = (Folder) folders.next();
            newPath = destination + getRealPath(folder.getPath());
            importFolder(castorPageManager,folder, userName, newPath, copyIds);
        }
        return dstFolder;
    }

    public static void zipFiles(File cpFile, String sourcePath, ZipOutputStream cpZipOutputStream)
    {
        if (cpFile.isDirectory())
        {
            File[] fList = cpFile.listFiles();
            for (int i = 0; i < fList.length; i++)
            {
                zipFiles(fList[i], sourcePath, cpZipOutputStream);
            }
        }
        else
        {
            FileInputStream cpFileInputStream = null;
            
            try
            {
                String strAbsPath = cpFile.getAbsolutePath();
                String strZipEntryName = strAbsPath.substring(sourcePath.length() + 1, strAbsPath.length());
                cpFileInputStream = new FileInputStream(cpFile);
                ZipEntry cpZipEntry = new ZipEntry(strZipEntryName);
                cpZipOutputStream.putNextEntry(cpZipEntry);
                IOUtils.copy(cpFileInputStream, cpZipOutputStream);
                cpZipOutputStream.closeEntry();
            }
            catch (Exception e)
            {
                logger.error("Unexpected exception during zipping files.", e);
            }
            finally
            {
                if (cpFileInputStream != null)
                {
                    try 
                    {
                        cpFileInputStream.close();
                    }
                    catch (Exception ce)
                    {
                    }
                }
            }
        }
    }

    public static String getUserFolder(String userName, boolean fullPath)
    {
        if (pathSeprator == null || pathSeprator.equals(""))
            pathSeprator = "/";
        if (fullPath)
        {
            return userName + pathSeprator;
        }
        else
        {
            return pageRoot + pathSeprator + userName;
        }
    }

    private static boolean zipObject(String sourcePath, String target)
    {
        FileOutputStream fos = null;
        ZipOutputStream cpZipOutputStream = null;
        
        try
        {
            File cpFile = new File(sourcePath);
            if (!cpFile.isDirectory())
            {
                return false;
            }
            fos = new FileOutputStream(target);
            cpZipOutputStream = new ZipOutputStream(fos);
            cpZipOutputStream.setLevel(9);
            zipFiles(cpFile, sourcePath, cpZipOutputStream);
            cpZipOutputStream.finish();
        }
        catch (Exception e)
        {
            logger.error("Unexpected exception during writing to zip output stream.", e);
            return false;
        }
        finally
        {
            if (cpZipOutputStream != null)
            {
                try
                {
                    cpZipOutputStream.close();
                }
                catch (Exception ce)
                {
                }
            }
            
            if (fos != null)
            {
                try
                {
                    fos.close();
                }
                catch (Exception ce)
                {
                }
            }
        }
        return true;
    }

    private static  Page lookupPage(PageManager castorPageManager, String path)
    {
        try
        {
            return castorPageManager.getPage(path);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static PageTemplate lookupPageTemplate(PageManager castorPageManager, String path)
    {
        try
        {
            return castorPageManager.getPageTemplate(path);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static DynamicPage lookupDynamicPage(PageManager castorPageManager, String path)
    {
        try
        {
            return castorPageManager.getDynamicPage(path);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static FragmentDefinition lookupFragmentDefinition(PageManager castorPageManager, String path)
    {
        try
        {
            return castorPageManager.getFragmentDefinition(path);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static  Link lookupLink(PageManager castorPageManager, String path)
    {
        try
        {
            return castorPageManager.getLink(path);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static Folder lookupFolder(PageManager castorPageManager, String path)
    {
        try
        {
            return castorPageManager.getFolder(path);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static String getRealPath(String path)
    {
        int index = path.lastIndexOf("/");
        if (index > 0)
        {
            return path.substring(index);
        }
        return path;
    }

    public static String getParentPath(String path)
    {
        int index = path.lastIndexOf("/");
        if (index == 0)
        {
            return "/";
        }
        else
        {
            return path.substring(0, index);
        }
    }
}
