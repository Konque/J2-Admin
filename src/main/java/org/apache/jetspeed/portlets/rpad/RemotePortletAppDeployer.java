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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.portlets.AdminPortletWebPage;
import org.apache.jetspeed.portlets.rpad.portlet.deployer.PortletDeployer;
import org.apache.jetspeed.portlets.rpad.portlet.deployer.impl.JetspeedPortletDeployer;
import org.apache.jetspeed.portlets.rpad.simple.SimpleRepository;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.time.Duration;

public class RemotePortletAppDeployer extends AdminPortletWebPage
{

    /** available sites for selection. */
    @SuppressWarnings("unchecked")
    private List repositories;
    private String message;
    private String ticker;

    private List<PortletApplication> portletApplications;

    private Repository selectedRepository;
    final PortletDeployer deployer;

    private RepositoryManager repositoryManager;

    @SuppressWarnings("unchecked")
    public RemotePortletAppDeployer()
    {
        deployer = new JetspeedPortletDeployer();
        Form form = new Form("form");
        repositories = getRepositoryManager().getRepositories();
        form.add(new DropDownChoice("repositorySelection", new PropertyModel(
                this, "selectedRepository"), repositories, new ChoiceRenderer(
                "name", "name")));

        Button selectButton = new Button("selectButton")
        {

            private static final long serialVersionUID = 1L;

            public void onSubmit()
            {

            }
        };

        form.add(selectButton);
        add(form);

        IModel getRepo = new LoadableDetachableModel()
        {

            protected Object load()
            {
                return getRepo();
            }
        };

        final PageableListView listview = new PageableListView("repositories",
                getRepo, 10)
        {

            private static final long serialVersionUID = 1L;

            // This method is called for each 'entry' in the list.
            @Override
            protected void populateItem(final ListItem item)
            {
                final PortletApplication application = (PortletApplication) item
                        .getModelObject();
                item.add(new Label("groupId", application.getGroupId()));
                item.add(new Label("artifactId", application.getArtifactId()));
                item.add(new Label("name", application.getName()));
                item.add(new Label("version", application.getVersion()));
                item.add(new Label("type", application.getPackaging()));
                Link actionLink = new Link("action", item.getModel())
                {

                    public void onClick()
                    {
                        PortletApplication portletApplication = (PortletApplication) getModelObject();
                        deployer.deploy(portletApplication, getServiceLocator()
                                .getDeploymentManager());
                    }
                };
                actionLink.setVisibilityAllowed(true);
                actionLink.setOutputMarkupId(true);
                if(deployer.getStatus() == PortletDeployer.DEPLOYING)
                {
                    actionLink.setVisible(false);
                }                
                item.add(actionLink);
            }
        };
        listview.setOutputMarkupId(true);
        final WebMarkupContainer tableGroup = new WebMarkupContainer("tableGroup");
        tableGroup.setOutputMarkupId(true);
        //tableGroup.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(5)));
        tableGroup.add(new PagingNavigator("navigator", listview));
        tableGroup.add(listview);
        final MultiLineLabel statusLabel = new MultiLineLabel("status",
                new PropertyModel(this, "message"));
        statusLabel.setOutputMarkupId(true);
        final Label tickerLabel =new Label("ticker",new PropertyModel(this,"ticker")); 
        tickerLabel.setOutputMarkupId(true);
        add(new AbstractAjaxTimerBehavior(Duration.seconds(3))
        {
            protected void onTimer(AjaxRequestTarget target)
            {
                setMessage(deployer.getMessage());
                target.addComponent(tickerLabel);
                target.addComponent(statusLabel);
                target.addComponent(tableGroup);
            }
        });
        add(statusLabel);
        add(tickerLabel);
        add(tableGroup);

    }

    // mock data for testing listview
    private List<PortletApplication> getRepo()
    {
        if (getSelectedRepository() != null
                && !StringUtils.isBlank(getSelectedRepository().getName()))
        {
            portletApplications = getRepositoryManager()
                    .getPortletApplications(getSelectedRepository().getName());
        } else
        {
            portletApplications = getRepositoryManager()
                    .getPortletApplications();

        }
        return portletApplications;
    }

    /**
     * @return the selectedRepository
     */
    public Repository getSelectedRepository()
    {
        return selectedRepository;
    }

    /**
     * @param selectedRepository
     *            the selectedRepository to set
     */
    public void setSelectedRepository(Repository selectedRepository)
    {
        this.selectedRepository = selectedRepository;
    }

    public RepositoryManager getRepositoryManager()
    {
        if (repositoryManager == null)
        {            
            repositoryManager = RepositoryManager.getInstance(getReposList(getPortletRequest()));
        }
        return repositoryManager;
    }
    
    /**
     * @return the message
     */
    public String getMessage()
    {
        return message;
    }

    
    /**
     * @param message the message to set
     */
    public void setMessage(String message)
    {
        this.message = message;
    }
    
    /**
     * @return the ticker
     */
    public String getTicker()
    {
        if(deployer.getStatus()!=deployer.READY)
        {
            if(ticker.length()<5)
            {
                ticker+=".";
            }else{
                ticker= ".";
            }    
        }else{
            ticker="";
        }        
        return ticker;
    }
    public static final Map<String,Repository> getReposList(PortletRequest request)
    {
        Map<String,Repository> portletRepositry = new HashMap<String, Repository>();
        String repoName;       
        Enumeration<String> repoNames = request.getPreferences().getNames();
        while(repoNames.hasMoreElements())
        {
            repoName = (String)repoNames.nextElement();
            portletRepositry.put(repoName,new SimpleRepository(repoName,request.getPreferences().getValue(repoName,"")));               
        }
        return portletRepositry;
    }
}
