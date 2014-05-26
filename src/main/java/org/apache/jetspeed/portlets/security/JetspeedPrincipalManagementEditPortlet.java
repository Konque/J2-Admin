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
package org.apache.jetspeed.portlets.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.portlet.PortletRequest;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.om.folder.Folder;
import org.apache.jetspeed.om.folder.FolderNotFoundException;
import org.apache.jetspeed.page.PageManager;
import org.apache.jetspeed.page.document.NodeSet;
import org.apache.jetspeed.portlets.AdminPortletWebPage;
import org.apache.jetspeed.portlets.wicket.AbstractAdminWebApplication;
import org.apache.jetspeed.profiler.rules.ProfilingRule;
import org.apache.jetspeed.security.SecurityException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vkumar <a href="vkumar@apache.org">Vivek Kumar</a>
 */
public class JetspeedPrincipalManagementEditPortlet extends AdminPortletWebPage
{
    private String subite;
    private String profile;
    private String role;
    private String templateDir;
    private String subSiteRoot;
    private Logger log = LoggerFactory.getLogger(JetspeedPrincipalManagementEditPortlet.class);
    IModel getProfileList = new LoadableDetachableModel()
    {
        protected Object load()
        {
            return getProfiles();
        }
    };
    IModel getRoleNames = new LoadableDetachableModel()
    {
        protected Object load()
        {
            return getRoles();
        }
    };
    IModel getSubSites = new LoadableDetachableModel()
    {
        protected Object load()
        {
            return getSubSites();
        }
    };

    public JetspeedPrincipalManagementEditPortlet()
    {
        PortletRequest request = ((AbstractAdminWebApplication) getApplication()).getPortletRequest();
        subite = request.getPreferences().getValue(JetspeedPrincipalManagementPortlet.DEFAULT_SUBSITE, "");
        profile = request.getPreferences().getValue(JetspeedPrincipalManagementPortlet.DEFAULT_PROFILE, "");
        role = request.getPreferences().getValue(JetspeedPrincipalManagementPortlet.DEFAULT_ROLE, "");
        templateDir = request.getPreferences().getValue(JetspeedPrincipalManagementPortlet.NEW_USER_TEMPLATE_DIR, "");
        subSiteRoot = request.getPreferences().getValue(JetspeedPrincipalManagementPortlet.SUB_SITE_ROOT, "");
        add(new FeedbackPanel("feedback"));
        Form userPrefernces = new Form("userPrefernces");
        userPrefernces.add(new Label("subsiterootLabel", new ResourceModel("subsiteroot")));
        userPrefernces.add(new TextField("subsiteroot", new PropertyModel(this, "subSiteRoot")));        
        userPrefernces.add(new Label("subsiteLabel", new ResourceModel("default.subsite")));
        userPrefernces.add(new DropDownChoice("defaultSubSite", new PropertyModel(this, "subite"),getSubSites));
        userPrefernces.add(new Label("templateDirLabel", new ResourceModel("templatedir")));
        userPrefernces.add(new RequiredTextField("templateDir", new PropertyModel(this, "templateDir")));
        userPrefernces.add(new Label("profileLabel", new ResourceModel("default.profile")));
        userPrefernces.add(new DropDownChoice("profile", new PropertyModel(this, "profile"), getProfileList));
        userPrefernces.add(new Label("roleLabel", new ResourceModel("default.role")));
        userPrefernces.add(new DropDownChoice("role", new PropertyModel(this, "role"), getRoleNames));
        userPrefernces.add(new Button("addUserPrefernces", new ResourceModel("common.save"))
        {
            @Override
            public void onSubmit()
            {
                PortletRequest request = ((AbstractAdminWebApplication) getApplication()).getPortletRequest();
                try
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Setting default role as " + getRole());
                        log.debug("Setting default subsite as " + getSubite());
                        log.debug("Setting default profile as " + getSubite());
                    }
                    request.getPreferences().setValue(JetspeedPrincipalManagementPortlet.DEFAULT_ROLE, getRole());
                    request.getPreferences().setValue(JetspeedPrincipalManagementPortlet.DEFAULT_SUBSITE, getSubite());
                    request.getPreferences().setValue(JetspeedPrincipalManagementPortlet.DEFAULT_PROFILE, getProfile());
                    request.getPreferences().setValue(JetspeedPrincipalManagementPortlet.NEW_USER_TEMPLATE_DIR, getTemplateDir());
                    request.getPreferences().setValue(JetspeedPrincipalManagementPortlet.SUB_SITE_ROOT, getSubSiteRoot());
                    request.getPreferences().store();
                    if (log.isDebugEnabled())
                    {
                        log.debug("Default settiing for portlet saved . ");
                    }
                }
                catch (ValidatorException e)
                {
                    if (log.isErrorEnabled())
                    {
                        log.error(e.getMessage());
                    }
                    error(e.getMessage());
                }
                catch (IOException e)
                {
                    if (log.isErrorEnabled())
                    {
                        log.error(e.getMessage());
                    }
                    error(e.getMessage());
                }
                catch (ReadOnlyException e)
                {
                    if (log.isErrorEnabled())
                    {
                        log.error(e.getMessage());
                    }
                    error(e.getMessage());
                }
            }
        });
        add(userPrefernces);
    }

    /**
     * @return the subite
     */
    public String getSubite()
    {
        return subite;
    }

    /**
     * @param subite
     *            the subite to set
     */
    public void setSubite(String subite)
    {
        this.subite = subite;
    }

    /**
     * @return the profile
     */
    public String getProfile()
    {
        return profile;
    }

    /**
     * @param profile
     *            the profile to set
     */
    public void setProfile(String profile)
    {
        this.profile = profile;
    }

    /**
     * @return the role
     */
    public String getRole()
    {
        return role;
    }

    /**
     * @param role
     *            the role to set
     */
    public void setRole(String role)
    {
        this.role = role;
    }

    /**
     * @return the templateDir
     */
    public String getTemplateDir()
    {
        return templateDir;
    }

    /**
     * @param templateDir
     *            the templateDir to set
     */
    public void setTemplateDir(String templateDir)
    {
        this.templateDir = templateDir;
    }

    /**
     * @return the subSiteRootr
     */
    public String getSubSiteRoot()
    {
        return subSiteRoot;
    }

    /**
     * @param subSiteRootr
     *            the subSiteRootr to set
     */
    public void setSubSiteRoot(String subSiteRoot)
    {
        this.subSiteRoot = subSiteRoot;
    }

    private List<String> getProfiles()
    {
        ArrayList<String> profileList = new ArrayList<String>();
        List profileRules = (List) ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getProfiler().getRules();
        for (int counter = 0; counter < profileRules.size(); counter++)
        {
            profileList.add(((ProfilingRule) profileRules.get(counter)).getId());
        }
        return profileList;
    }

    private List<String> getRoles()
    {
        List<String> roleNames = Collections.EMPTY_LIST;
        try
        {
            roleNames = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getRoleManager().getRoleNames("");
        }
        catch (SecurityException e)
        {
            error(e.getMessage());
        }
        return roleNames;
    }

    private List<String> getSubSites()
    {
        List<String> subsites = new ArrayList<String>();
        subsites.add("");
        PageManager pageManager = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPageManager();
        if (!StringUtils.isEmpty(getSubSiteRoot()))
        {
            try
            {
                Folder subsiteFolder = pageManager.getFolder(getSubSiteRoot());
                NodeSet set = pageManager.getFolders(subsiteFolder);
                if (set != null && !set.isEmpty())
                {
                    Iterator setIterator = set.iterator();
                    while (setIterator.hasNext())
                    {
                        Folder f = (Folder) setIterator.next();
                        subsites.add(f.getTitle());
                    }
                }
            }
            catch (FolderNotFoundException fnfe)
            {
                // subsites not used, ignore
            }
            catch (Exception e)
            {
            }
        }
        return subsites;
    }
}
