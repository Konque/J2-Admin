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
package org.apache.jetspeed.portlets.prm;

import java.io.NotSerializableException;
import java.util.Locale;

import org.apache.jetspeed.audit.AuditActivity;
import org.apache.jetspeed.components.portletregistry.PortletRegistry;
import org.apache.jetspeed.om.portlet.PortletApplication;
import org.apache.jetspeed.om.portlet.PortletDefinition;
import org.apache.jetspeed.portlets.AdminPortletWebPage;
import org.apache.jetspeed.portlets.JetspeedServiceLocator;
import org.apache.jetspeed.portlets.prm.ApplicationDataProvider.AppOrderBy;
import org.apache.jetspeed.portlets.prm.PortletDataProvider.PortletOrderBy;
import org.apache.jetspeed.portlets.wicket.AbstractAdminWebApplication;
import org.apache.jetspeed.request.RequestContext;
import org.apache.jetspeed.tools.pamanager.PortletApplicationManagement;
import org.apache.jetspeed.tools.pamanager.servletcontainer.ApplicationServerManager;
import org.apache.jetspeed.tools.pamanager.servletcontainer.ApplicationServerManagerResult;
import org.apache.portals.messaging.PortletMessaging;
import org.apache.wicket.Page;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink.VoidCssProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * View Mode for Portlet Application List widget
 * 
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: ApplicationsListHome.java 1090090 2011-04-08 02:21:14Z woonsan $
 */
public class ApplicationsListHome extends AdminPortletWebPage
{
    private static final String APP_TABLE = "appTable";
    private static final String PORTLET_TABLE = "portletTable";
    
    static final Logger logger = LoggerFactory.getLogger(ApplicationsListHome.class);

    public static final String PORTLET_REGISTRY_MANAGER = "Portlet Registry Manager";
    
    private String clonePopupPagePath = "/system/prm/cloneportlet.psml";
    private String currentModalWindowCloseScript;

    public ApplicationsListHome()
    {
        SearchForm form = new SearchForm("appSearchForm");
        add(form);

        // Construct Data View
        final JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
        final Locale locale = (((AbstractAdminWebApplication) getApplication()).getPortletRequest()).getLocale();
        final ApplicationDataProvider applicationDataProvider = new ApplicationDataProvider(locator);
        final PortletDataProvider portletListDataProvider = new PortletDataProvider("j2-admin", locale, locator);

        final DataView<ApplicationBean> dataView = new DataView<ApplicationBean>(APP_TABLE, applicationDataProvider)
        {
            private static final long serialVersionUID = 1L;

            protected void populateItem(final Item<ApplicationBean> item)
            {
                final ApplicationBean pa = item.getModelObject();
                
                Link<ApplicationBean> nameLink = new Link<ApplicationBean>("nameLink")
                {
                    private static final long serialVersionUID = 1L;

                    public void onClick()
                    {
                        String appName = this.get("nameLabel").getDefaultModelObjectAsString();
                        portletListDataProvider.changeAppName(appName);

                        try
                        {
                            PortletMessaging.publish(
                                                     ((AbstractAdminWebApplication) getApplication()).getPortletRequest(), 
                                                     ApplicationsListApplication.PRM_TOPIC, 
                                                     ApplicationsListApplication.SELECTED_APPLICATION_EVENT, 
                                                     pa
                                                     );
                            DataView portletView = (DataView) getPage().get(PORTLET_TABLE);                    
                            PortletDataProvider pdp = (PortletDataProvider) portletView.getDataProvider();
                            if (pdp.getSearchMode() == true)
                            {
                                pdp.setSearchMode(false);                            
                                pdp.refresh();
                            }
                        }
                        catch (NotSerializableException e)
                        {
                            logger.error("Message to publish is not serializable.", e);
                        }
                    }
                };
                
                nameLink.add(new Label("nameLabel", pa.getApplicationName()));
                item.add(nameLink);
                item.add(new Label("version", pa.getVersion()));
                item.add(new Label("path", pa.getPath()));
                item.add(new RunningPanel("running", item.getModel()));
                item.add(new ActionPanel("actions", item.getModel()));
            }
        };
        
        dataView.setItemsPerPage(((AbstractAdminWebApplication) getApplication()).getPreferenceValueAsInteger("appRows"));

        add(new OrderByLink("appOrderByName", "name", applicationDataProvider, VoidCssProvider.getInstance())
        {
            private static final long serialVersionUID = 1L;

            protected void onSortChanged()
            {
                if (applicationDataProvider.getOrderBy() == AppOrderBy.NAME_ASC)
                {
                    applicationDataProvider.setOrderBy(AppOrderBy.NAME_DESC);
                }
                else
                {
                    applicationDataProvider.setOrderBy(AppOrderBy.NAME_ASC);
                }
                applicationDataProvider.sort();                
                dataView.setCurrentPage(0);
            }
        });

        add(new OrderByLink("appOrderByVersion", "version", applicationDataProvider, VoidCssProvider.getInstance())
        {
            private static final long serialVersionUID = 1L;

            protected void onSortChanged()
            {
                if (applicationDataProvider.getOrderBy() == AppOrderBy.VERSION_ASC)
                {
                    applicationDataProvider.setOrderBy(AppOrderBy.VERSION_DESC);
                }
                else
                {
                    applicationDataProvider.setOrderBy(AppOrderBy.VERSION_ASC);
                }
                applicationDataProvider.sort();
                dataView.setCurrentPage(0);
            }
        });

        add(new OrderByLink("appOrderByPath", "path", applicationDataProvider, VoidCssProvider.getInstance())
        {
            private static final long serialVersionUID = 1L;

            protected void onSortChanged()
            {
                if (applicationDataProvider.getOrderBy() == AppOrderBy.PATH_ASC)
                {
                    applicationDataProvider.setOrderBy(AppOrderBy.PATH_DESC);
                }
                else
                {
                    applicationDataProvider.setOrderBy(AppOrderBy.PATH_ASC);
                }
                applicationDataProvider.sort();
                dataView.setCurrentPage(0);
            }
        });

        add(dataView);
        add(new PagingNavigator("appNavigator", dataView));
        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setEscapeModelStrings(false);
        add(feedback);

        final DataView<PortletDefinitionBean> portletListView = new DataView<PortletDefinitionBean>(PORTLET_TABLE, portletListDataProvider)
        {
            private static final long serialVersionUID = 1L;
            

            protected void populateItem(final Item<PortletDefinitionBean> item)
            {
                final PortletDefinitionBean portletDefinitionBean = item.getModelObject();

                Link<PortletDefinitionBean> link = new Link<PortletDefinitionBean>("nameLink", item.getModel())
                {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick()
                    {
                        try
                        {
                            PortletMessaging.publish(
                                                     ((AbstractAdminWebApplication) getApplication()).getPortletRequest(),
                                                     ApplicationsListApplication.PRM_TOPIC,
                                                     ApplicationsListApplication.SELECTED_PORTLET_EVENT,
                                                     portletDefinitionBean
                                                     );
                            PortletMessaging
                                            .publish(
                                                     ((AbstractAdminWebApplication) getApplication()).getPortletRequest(),
                                                     ApplicationsListApplication.PRM_TOPIC,
                                                     ApplicationsListApplication.SELECTED_APPLICATION_EVENT,
                                                     new ApplicationBean(
                                                                         locator.getPortletRegistry()
                                                                                .getPortletApplication(portletDefinitionBean.getApplicationName()),
                                                                         locator
                                                                                .getPortletFactory()
                                                                                .isPortletApplicationRegistered(
                                                                                                                locator
                                                                                                                       .getPortletRegistry()
                                                                                                                       .getPortletApplication(
                                                                                                                                              portletDefinitionBean
                                                                                                                                                                   .getApplicationName()))));
                        } 
                        catch (NotSerializableException e)
                        {
                            logger.error("Message to publish is not serializable.", e);
                        }
                    }
                };

                link.add(new Label("nameLabel", portletDefinitionBean.getDisplayName()));

                item.add(link);
                item.add(new CloneStatusPanel("status", item.getModel()));
                item.add(new PortletActionPanel("actions", item.getModel()));
            }
        };
        
        portletListView.setItemsPerPage(((AbstractAdminWebApplication) getApplication()).getPreferenceValueAsInteger("portletRows"));
        add(new OrderByBorder("plOrderByDisplayName", "name", portletListDataProvider)
        {
            private static final long serialVersionUID = 1L;

            protected void onSortChanged()
            {
                if (portletListDataProvider.getOrderBy() == PortletOrderBy.DISPLAY_NAME_ASC)
                {
                    portletListDataProvider.setOrderBy(PortletOrderBy.DISPLAY_NAME_DESC);
                }
                else
                {
                    portletListDataProvider.setOrderBy(PortletOrderBy.DISPLAY_NAME_ASC);
                }
                portletListDataProvider.sort();
                dataView.setCurrentPage(0);
            }
        });

        add(portletListView);
        add(new PagingNavigator("plNavigator", portletListView));
        
        CloneModalWindow modalWindow = new CloneModalWindow("modalwindow");
        add(modalWindow);
        
        Label modalWindowCloseScript = new Label("modalWindowCloseScript", new PropertyModel<String>(this, "currentModalWindowCloseScript"));
        modalWindowCloseScript.setOutputMarkupId(true);
        add(modalWindowCloseScript);
    }
    
    public String getCurrentModalWindowCloseScript()
    {
        return currentModalWindowCloseScript;
    }

    public void setCurrentModalWindowCloseScript(String currentModalWindowCloseScript)
    {
        this.currentModalWindowCloseScript = currentModalWindowCloseScript;
    }

    class CloneModalWindow extends ModalWindow
    {
        private static final long serialVersionUID = 1L;
        
        public CloneModalWindow(String id)
        {
            super(id);
        }
        
        public String getCloseJavacript()
        {
            return super.getCloseJavacript();
        }
    }

    class RunningPanel extends Panel
    {
        private static final long serialVersionUID = 1L;

        public RunningPanel(String id, final IModel<ApplicationBean> model)
        {
            super(id, model);
            ApplicationBean pa = (ApplicationBean) model.getObject();
            
            if (pa.isRunning())
            {
                add(new Image("running", new ResourceReference(ApplicationsListHome.class, "running.gif")));
            }
            else
            {
                add(new Image("running", new ResourceReference(ApplicationsListHome.class, "stop.gif")));
            }
        }
    }
    
    class CloneStatusPanel extends Panel
    {
        private static final long serialVersionUID = 1L;

        public CloneStatusPanel(String id, final IModel<PortletDefinitionBean> model)
        {
            super(id, model);
            PortletDefinitionBean pd = (PortletDefinitionBean) model.getObject();
            
            if (pd.isCloned())
            {
                add(new Label("status", new StringResourceModel("pam.details.status.cloned", this, null)));
            }
            else
            {
                add(new Label("status", ""));
            }
        }
    }

    class ActionPanel extends Panel
    {
        private static final long serialVersionUID = 1L;

        @Override
        protected void onBeforeRender()
        {
            ApplicationBean pab = (ApplicationBean) getDefaultModelObject();
            
            final JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
            ApplicationServerManager asm = locator.getApplicationServerManager();
            PortletApplication pa = locator.getPortletRegistry().getPortletApplication(pab.getApplicationName());
            
            boolean isRunning = (pa != null && locator.getPortletFactory().isPortletApplicationRegistered(pa));
            boolean isLocal = (pa != null && pa.getApplicationType() == PortletApplication.LOCAL);
            boolean isAdmin = ((pa != null) && (pa.getContextPath().equals(((AbstractAdminWebApplication) getApplication()).getPortletRequest().getContextPath())));

            get("start").setVisible(
                                    (asm != null && !isRunning) && !(isLocal || isAdmin)
                                    );

            get("stop").setVisible(
                                   (asm != null && isRunning) && !(isLocal || isAdmin)
                                   );

            get("undeploy").setVisible(
                                       (asm != null && !isRunning) && !(isLocal || isAdmin)
                                       );

            get("delete").setVisible(
                                     !(isLocal || isAdmin) && !isRunning
                                     );
            
            super.onBeforeRender();
        }
        
        /**
         * @param id
         *            component id
         * @param model
         *            model for contact
         */
        public ActionPanel(String id, final IModel<ApplicationBean> model)
        {
            super(id, model);
            
            Link<String> start = new Link<String>("start")
            {
                private static final long serialVersionUID = 1L;
                
                public void onClick()
                {
                    JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
                    PortletRegistry registry = locator.getPortletRegistry();
                    ApplicationServerManager asm = locator.getApplicationServerManager();
                    AuditActivity auditActivity = locator.getAuditActivity();

                    ApplicationBean pab = (ApplicationBean) model.getObject();
                    PortletApplication pa = registry.getPortletApplication(pab.getApplicationName());
                    FeedbackPanel feed = (FeedbackPanel) getPage().get("feedback");
                    
                    if (isServerReady(asm, feed, "start", pa, pab))
                    {
                        try
                        {
                            ApplicationServerManagerResult result = asm.start(pa.getContextPath());
                            
                            if (!result.isOk())
                            {
                                throw new Exception(getString("pam.details.action.status.appServerNotConfigured"));
                            }
                            else
                            {
                                StringResourceModel resModel = new StringResourceModel("pam.details.action.status.startOK", this, null, new Object [] { pab.getPath() } );
                                feed.info(resModel.getString());
                                auditActivity.logAdminRegistryActivity(
                                                                       ((AbstractAdminWebApplication) getApplication()).getUserPrincipalName(),
                                                                       ((AbstractAdminWebApplication) getApplication()).getIPAddress(),
                                                                       AuditActivity.REGISTRY_START,
                                                                       PORTLET_REGISTRY_MANAGER
                                                                       );
                            }
                        }
                        catch (Exception e)
                        {
                            StringResourceModel resModel = new StringResourceModel("pam.details.action.status.startFailure", this, null, new Object [] { pab.getPath(), e.getMessage() } );
                            feed.error(resModel.getString());
                        }
                    }
                }
            };

            Link<String> stop = new Link<String>("stop")
            {
                private static final long serialVersionUID = 1L;

                public void onClick()
                {
                    JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
                    PortletRegistry registry = locator.getPortletRegistry();
                    ApplicationServerManager asm = locator.getApplicationServerManager();
                    AuditActivity auditActivity = locator.getAuditActivity();

                    ApplicationBean pab = (ApplicationBean) model.getObject();
                    PortletApplication pa = registry.getPortletApplication(pab.getApplicationName());
                    FeedbackPanel feed = (FeedbackPanel) getPage().get("feedback");
                    
                    if (isServerReady(asm, feed, "stop", pa, pab))
                    {
                        try
                        {
                            ApplicationServerManagerResult result = asm.stop(pa.getContextPath());
                            
                            if (!result.isOk())
                            {
                                throw new Exception(getString("pam.details.action.status.appServerNotConfigured"));
                            }
                            else
                            {
                                StringResourceModel resModel = new StringResourceModel("pam.details.action.status.stopOK", this, null, new Object [] { pab.getPath() } );
                                feed.info(resModel.getString());
                                
                                auditActivity.logAdminRegistryActivity(
                                                                       ((AbstractAdminWebApplication) getApplication()).getUserPrincipalName(),
                                                                       ((AbstractAdminWebApplication) getApplication()).getIPAddress(),
                                                                       AuditActivity.REGISTRY_STOP,
                                                                       PORTLET_REGISTRY_MANAGER
                                                                       );
                            }
                        }
                        catch (Exception e)
                        {
                            StringResourceModel resModel = new StringResourceModel("pam.details.action.status.stopFailure", this, null, new Object [] { pab.getPath(), e.getMessage() } );
                            feed.error(resModel.getString());
                        }
                    }
                }
            };
            
            Link<String> undeploy = new Link<String>("undeploy")
            {
                private static final long serialVersionUID = 1L;

                public void onClick()
                {
                    JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
                    PortletRegistry registry = locator.getPortletRegistry();
                    ApplicationServerManager asm = locator.getApplicationServerManager();
                    AuditActivity auditActivity = locator.getAuditActivity();

                    ApplicationBean pab = (ApplicationBean) model.getObject();
                    PortletApplication pa = registry.getPortletApplication(pab.getApplicationName());
                    FeedbackPanel feed = (FeedbackPanel) getPage().get("feedback");
                    
                    if (isServerReady(asm, feed, "undeploy", pa, pab))
                    {
                        try
                        {
                            ApplicationServerManagerResult result = asm.undeploy(pa.getContextPath());
                            
                            if (!result.isOk())
                            {
                                throw new Exception(getString("pam.details.action.status.appServerNotConfigured"));
                            } 
                            else
                            {
                                StringResourceModel resModel = new StringResourceModel("pam.details.action.status.undeployOK", this, null, new Object [] { pab.getPath() } );
                                feed.info(resModel.getString());
                                auditActivity.logAdminRegistryActivity(
                                                                       ((AbstractAdminWebApplication) getApplication()).getUserPrincipalName(),
                                                                       ((AbstractAdminWebApplication) getApplication()).getIPAddress(),
                                                                       AuditActivity.REGISTRY_UNDEPLOY,
                                                                       PORTLET_REGISTRY_MANAGER
                                                                       );
                            }
                        } 
                        catch (Exception e)
                        {
                            StringResourceModel resModel = new StringResourceModel("pam.details.action.status.undeployFailure", this, null, new Object [] { pab.getPath(), e.getMessage() } );
                            feed.error(resModel.getString());
                        }
                    }
                }
            };

            Link<String> del = new Link<String>("delete")
            {
                private static final long serialVersionUID = 1L;

                public void onClick()
                {
                    JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
                    PortletRegistry registry = locator.getPortletRegistry();
                    ApplicationServerManager asm = locator.getApplicationServerManager();
                    AuditActivity auditActivity = locator.getAuditActivity();
                    PortletApplicationManagement pam = locator.getPortletApplicationManager();

                    ApplicationBean pab = (ApplicationBean) model.getObject();
                    PortletApplication pa = registry.getPortletApplication(pab.getApplicationName());
                    FeedbackPanel feed = (FeedbackPanel) getPage().get("feedback");
                    
                    if (isServerReady(asm, feed, "remove from registry", pa, pab))
                    {
                        try
                        {
                            pam.unregisterPortletApplication(pa.getName());
                            StringResourceModel resModel = new StringResourceModel("pam.details.action.status.deleteOK", this, null, new Object [] { pab.getPath() } );
                            feed.info(resModel.getString());
                            auditActivity.logAdminRegistryActivity(
                                                                   ((AbstractAdminWebApplication) getApplication()).getUserPrincipalName(),
                                                                   ((AbstractAdminWebApplication) getApplication()).getIPAddress(),
                                                                   AuditActivity.REGISTRY_DELETE,
                                                                   PORTLET_REGISTRY_MANAGER
                                                                   );
                        }
                        catch (Exception e)
                        {
                            StringResourceModel resModel = new StringResourceModel("pam.details.action.status.deleteFailure", this, null, new Object [] { pab.getPath(), e.getMessage() } );
                            feed.error(resModel.getString());
                        }
                    }
                }
            };

            add(start);
            add(stop);
            add(undeploy);
            add(del);
        }
    }

    protected boolean isServerReady( ApplicationServerManager asm,
                                     FeedbackPanel feedback, 
                                     String operation, 
                                     PortletApplication pa,
                                     ApplicationBean pab )
    {
        if (pa == null)
        {
            StringResourceModel resModel = new StringResourceModel("pam.details.action.status.serverReadyFailure", this, null, new Object [] { pab.getPath(), operation } );
            feedback.error(resModel.getString());
            return false;
        } 
        else if (asm == null || !asm.isConnected())
        {
            StringResourceModel resModel = new StringResourceModel("pam.details.action.status.serverNotAvailable", this, null, new Object [] { pab.getPath(), operation } );
            feedback.error(resModel.getString());
            return false;
        }
        
        return true;
    }

    class PortletActionPanel extends Panel
    {
        private static final long serialVersionUID = 1L;

        @Override
        protected void onBeforeRender()
        {
            PortletDefinitionBean pdb = (PortletDefinitionBean) getDefaultModelObject();
            
            get("clone").setVisible(true);
            get("delete").setVisible(pdb.isCloned());
            
            super.onBeforeRender();
        }
        
        /**
         * @param id
         *            component id
         * @param model
         *            model for contact
         */
        public PortletActionPanel(String id, final IModel<PortletDefinitionBean> model)
        {
            super(id, model);
            
            AjaxLink<String> clone = new AjaxLink<String>("clone")
            {
                private static final long serialVersionUID = 1L;

                public void onClick(AjaxRequestTarget target)
                {
                    PortletDefinitionBean pdb = (PortletDefinitionBean) model.getObject();
                    FeedbackPanel feed = (FeedbackPanel) getPage().get("feedback");
                    
                    try
                    {
                        JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
                        PortletRegistry registry = locator.getPortletRegistry();
                        PortletDefinition def = registry.getPortletDefinitionByUniqueName(pdb.getUniqueName());

                        CloneModalWindow modalWindow = (CloneModalWindow) getPage().get("modalwindow");
                        RequestContext rc = ((AbstractAdminWebApplication) getApplication()).getPortalRequestContext();
                        final String pagePath = rc.getPortalURL().getBasePath() + clonePopupPagePath + "?portlet=" + def.getUniqueName();
                        modalWindow.setPageCreator(new ModalWindow.PageCreator() 
                        {
                            private static final long serialVersionUID = 1L;

                            public Page createPage()
                            {
                                return new RedirectPage(pagePath);
                            }
                        });
                        ((ApplicationsListHome) getPage()).setCurrentModalWindowCloseScript(modalWindow.getCloseJavacript());
                        target.addComponent(getPage().get("modalWindowCloseScript"));
                        modalWindow.show(target);
                    }
                    catch (Exception e) 
                    {
                        logger.error("Failed to remove a cloned portlet, {}. {}", pdb.getUniqueName(), e);
                        StringResourceModel resModel = new StringResourceModel("pam.details.action.status.clonePortletFailure", this, null, new Object [] { pdb.getUniqueName(), e.getMessage() } );
                        feed.error(resModel.getString());
                    }
                }
            };

            add(clone);
            
            Link<String> del = new Link<String>("delete")
            {
                private static final long serialVersionUID = 1L;

                public void onClick()
                {
                    PortletDefinitionBean pdb = (PortletDefinitionBean) model.getObject();
                    FeedbackPanel feed = (FeedbackPanel) getPage().get("feedback");
                    
                    try
                    {
                        JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
                        PortletRegistry registry = locator.getPortletRegistry();
                        PortletDefinition def = registry.getPortletDefinitionByUniqueName(pdb.getUniqueName());
                        
                        if (def != null && def.isClone()) {
                            registry.removeClone(def);
                            
                            DataView portletView = (DataView) getPage().get(PORTLET_TABLE);
                            PortletDataProvider pdp = (PortletDataProvider) portletView.getDataProvider();
                            
                            if (pdp.getSearchMode())
                            {
                                SearchForm searchForm = (SearchForm) getPage().get("appSearchForm");
                                pdp.searchPortlets(searchForm.getAppSearchField(), searchForm.isFilterPortlet(), searchForm.isFilterClone());
                            }
                            pdp.refresh();
                        }
                    }
                    catch (Exception e) 
                    {
                        logger.error("Failed to remove a cloned portlet, {}. {}", pdb.getUniqueName(), e);
                        StringResourceModel resModel = new StringResourceModel("pam.details.action.status.removeCloneFailure", this, null, new Object [] { pdb.getUniqueName(), e.getMessage() } );
                        feed.error(resModel.getString());
                    }
                }
            };

            add(del);
        }
    }
    
    private class SearchForm extends Form<Void>
    {
        private static final long serialVersionUID = 1L;
        
        private String appSearchField;
        private boolean filterPortlet = true;
        private boolean filterClone = true;

        public SearchForm(String name)
        {
            super(name);

            add(new TextField("appSearchField", new PropertyModel(this, "appSearchField")));
            
            add(new Button("appSearchButton", new ResourceModel("pam.details.action.search"))
            {
                @Override
                public void onSubmit()
                {
                    DataView appView = (DataView) getPage().get(APP_TABLE);
                    DataView portletView = (DataView) getPage().get(PORTLET_TABLE);
                    ApplicationDataProvider adp = (ApplicationDataProvider) appView.getDataProvider();
                    adp.searchApplications(getAppSearchField());
                    PortletDataProvider pdp = (PortletDataProvider) portletView.getDataProvider();
                    pdp.searchPortlets(getAppSearchField(), isFilterPortlet(), isFilterClone());
                }
            });
            
            add(new Link<String>("appDeploy")
            {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick()
                {
                    UploadPortletApp upa = new UploadPortletApp((((AbstractAdminWebApplication) getApplication()).getServiceLocator().getDeploymentManager()));
                    this.setResponsePage(upa);
                }
            });
            
            add(new Link<String>("appHome")
            {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick()
                {
                    DataView appView = (DataView) getPage().get(APP_TABLE);
                    ApplicationDataProvider adp = (ApplicationDataProvider) appView.getDataProvider();
                    adp.setSearchMode(false);
                    adp.refresh();
                    DataView portletView = (DataView) getPage().get(PORTLET_TABLE);                    
                    PortletDataProvider pdp = (PortletDataProvider) portletView.getDataProvider();
                    pdp.setSearchMode(false);
                    pdp.refresh();
                }
            });
            
            add(new CheckBox("filterPortlet", new PropertyModel(this, "filterPortlet")));
            add(new CheckBox("filterClone", new PropertyModel(this, "filterClone")));
        }

        public String getAppSearchField()
        {
            return appSearchField;
        }

        public void setAppSearchField(String appSearchField)
        {
            this.appSearchField = appSearchField;
        }

        public boolean isFilterPortlet()
        {
            return filterPortlet;
        }

        public void setFilterPortlet(boolean filterPortlet)
        {
            this.filterPortlet = filterPortlet;
        }

        public boolean isFilterClone()
        {
            return filterClone;
        }

        public void setFilterClone(boolean filterClone)
        {
            this.filterClone = filterClone;
        }
        
    }
}
