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
package org.apache.jetspeed.portlets.rpad;

import java.io.IOException;
import java.util.List;

import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import org.apache.jetspeed.portlets.AdminPortletWebPage;
import org.apache.jetspeed.portlets.rpad.portlet.deployer.PortletDeployer;
import org.apache.jetspeed.portlets.rpad.portlet.deployer.impl.JetspeedPortletDeployer;
import org.apache.jetspeed.portlets.rpad.simple.SimpleRepository;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:vkumar@apache.org">Vivek Kumar</a>
 * @version $Id$
 */
public class RPADEditor extends AdminPortletWebPage
{
    
    static final Logger logger = LoggerFactory.getLogger(RPADEditor.class);

    private RepositoryManager repositoryManager;

    public RPADEditor()
    {
        final PortletDeployer deployer = new JetspeedPortletDeployer();
        IModel getRepo = new LoadableDetachableModel()
        {

            protected Object load()
            {
                return getRepo();
            }
        };
        final WebMarkupContainer tableGroup = new WebMarkupContainer(
                "tableGroup");
        final ModalWindow metaDataModalWindow = new ModalWindow("modalwindow");
        final PageableListView listview = new PageableListView("repositories",
                getRepo, 10)
        {
            @Override
            protected void populateItem(final ListItem item)
            {
                final Repository repo = (Repository) item.getModelObject();
                item.add(new Label("name", repo.getName()));
                item.add(new Label("url", repo.getConfigPath()));
                item.add(new AjaxLink("edit", item.getModel())
                {

                    @Override
                    public void onClick(AjaxRequestTarget target)
                    {
                        metaDataModalWindow.setContent(new RepositoryPanel(
                                metaDataModalWindow.getContentId(), repo,
                                tableGroup));
                        metaDataModalWindow.show(target);
                    }
                });
                item.add(new AjaxLink("remove", item.getModel()){

                    @Override
                    public void onClick(AjaxRequestTarget target)
                    {
                        try
                        {
                            getPortletRequest().getPreferences().reset(repo.getName());
                            getRepositoryManager().reload(RemotePortletAppDeployer.getReposList(getPortletRequest()));
                            target.addComponent(tableGroup);
                        } catch (ReadOnlyException e)
                        {
                            logger.error("The preference is read-only: {}", repo.getName());
                        }
                    }                    
                });
            }
        };
        listview.setOutputMarkupId(true);

        tableGroup.setOutputMarkupId(true);
        tableGroup.add(new PagingNavigator("navigator", listview));
        tableGroup.add(listview);
        add(metaDataModalWindow);        
        add(tableGroup);
        add(new AjaxLink("newRepo"){

            @Override
            public void onClick(AjaxRequestTarget target)
            {
                metaDataModalWindow.setContent(new RepositoryPanel(
                        metaDataModalWindow.getContentId(), new SimpleRepository("",""),
                        tableGroup));
                metaDataModalWindow.show(target);                
            }            
        });
    }

    public RepositoryManager getRepositoryManager()
    {
        if (repositoryManager == null)
        {
            repositoryManager = RepositoryManager.getInstance(RemotePortletAppDeployer.getReposList(getPortletRequest()));
        }
        return repositoryManager;
    }

    private List<Repository> getRepo()
    {
        return getRepositoryManager().getRepositories();
    }

    private class RepositoryPanel extends Panel
    {

        private String repoName;

        private String repoUrl;

        /**
         * @return the repoName
         */
        public String getRepoName()
        {
            return repoName;
        }

        /**
         * @param repoName
         *            the repoName to set
         */
        public void setRepoName(String repoName)
        {
            this.repoName = repoName;
        }

        /**
         * @return the repoUrl
         */
        public String getRepoUrl()
        {
            return repoUrl;
        }

        /**
         * @param repoUrl
         *            the repoUrl to set
         */
        public void setRepoUrl(String repoUrl)
        {
            this.repoUrl = repoUrl;
        }

        public RepositoryPanel(String id, Repository repo,
                final WebMarkupContainer container)
        {
            super(id);
            this.repoName = repo.getName();
            this.repoUrl = repo.getConfigPath();
            final FeedbackPanel feedback = new FeedbackPanel("feedback");
            feedback.setOutputMarkupId(true);
            add(feedback);
            final Form repoFrom = new Form("repoForm");
            repoFrom.add(new Label("repoName", "Repository Name"));
            repoFrom.add(new Label("repoUrl", "Repository URL"));
            repoFrom.add(new TextField("repoNameText", new PropertyModel(this,
                    "repoName")).setRequired(true));
            repoFrom.add(new TextField("repoUrlText", new PropertyModel(this,
                    "repoUrl")).setRequired(true));
            repoFrom.add(new AjaxButton("save", repoFrom)
            {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form)
                {
                    saveRepo(getRepoName(), getRepoUrl());
                    target.addComponent(container);
                    ((ModalWindow) RepositoryPanel.this.getParent())
                            .close(target);
                }

                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form)
                {
                    target.addComponent(feedback);
                }
            });
            add(repoFrom);

        }
    }

    private void saveRepo(String name, String repoUrl)
    {
        try
        {
            getPortletRequest().getPreferences().setValue(name, repoUrl);
            getPortletRequest().getPreferences().store();
            getRepositoryManager().reload(RemotePortletAppDeployer.getReposList(getPortletRequest()));
        }
        catch (ReadOnlyException e)
        {
            logger.error("The preference is read-only: {}", name);
        }
        catch (ValidatorException e)
        {
            logger.error("The preference value of {} is invalid: {}", name, repoUrl);
        }
        catch (IOException e)
        {
            logger.error("Unexpected IO error.", e);
        } 
    }

}
