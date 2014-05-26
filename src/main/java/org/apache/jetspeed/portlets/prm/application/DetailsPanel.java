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
package org.apache.jetspeed.portlets.prm.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.components.portletregistry.PortletRegistry;
import org.apache.jetspeed.components.portletregistry.RegistryException;
import org.apache.jetspeed.factory.PortletFactory;
import org.apache.jetspeed.om.page.SecurityConstraintsDef;
import org.apache.jetspeed.om.portlet.Description;
import org.apache.jetspeed.om.portlet.JetspeedServiceReference;
import org.apache.jetspeed.om.portlet.PortletApplication;
import org.apache.jetspeed.om.portlet.PortletDefinition;
import org.apache.jetspeed.page.PageManager;
import org.apache.jetspeed.portlets.JetspeedServiceLocator;
import org.apache.jetspeed.portlets.prm.KeyVal;
import org.apache.jetspeed.portlets.prm.PortletApplicationNodeBean;
import org.apache.jetspeed.portlets.wicket.AbstractAdminWebApplication;
import org.apache.jetspeed.portlets.wicket.component.JavascriptEventConfirmation;
import org.apache.jetspeed.search.SearchEngine;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DetailsPanel extends Panel
{
    private static final long serialVersionUID = 1L;
    
    static final Logger logger = LoggerFactory.getLogger(DetailsPanel.class);

    private transient List<KeyVal> detailList;
    private String jetspeedSecurityConstraint;
    private List<String> jetspeedSecurityContraintNames;

    public DetailsPanel(String id, final PortletApplicationNodeBean paNodeBean)
    {
        super(id);

        detailList = new ArrayList<KeyVal>();
        JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
        PortletApplication app = locator.getPortletRegistry().getPortletApplication(paNodeBean.getApplicationName());
        
        detailList.add(new KeyVal("Name", app.getName()));
        detailList.add(new KeyVal("Version", app.getVersion()));
        detailList.add(new KeyVal("Description", getDescription(app)));
        detailList.add(new KeyVal("Type", getType(app)));
        detailList.add(new KeyVal("Services", new Object()
        {

            String toString(Iterator<JetspeedServiceReference> it)
            {
                StringBuffer buffer = new StringBuffer();
                while (it.hasNext())
                {
                    buffer.append(it.next().getName() + "<br/>");
                }
                return buffer.toString();
            }
        }.toString(app.getJetspeedServices().iterator())));

        final RefreshingView<KeyVal> dataRepeater = new RefreshingView<KeyVal>("data", new PropertyModel<List<KeyVal>>(this, "detailList"))
        {

            @Override
            protected void populateItem(Item<KeyVal> item)
            {
                final KeyVal field = item.getModelObject();
                item.add(new Label("name", field.getKey()));
                item.add(new Label("value", field.getValue()).setEscapeModelStrings(false));
            }

            @Override
            protected Iterator<IModel<KeyVal>> getItemModels()
            {
                List<KeyVal> list = (List<KeyVal>) getDefaultModelObject();
                return new ModelIteratorAdapter<KeyVal>(list.iterator())
                {

                    @Override
                    protected IModel<KeyVal> model(KeyVal object)
                    {
                        return new Model<KeyVal>(object);
                    }
                };
            }
        };

        jetspeedSecurityContraintNames = new ArrayList<String>(Arrays.asList(""));
        PageManager pageManager = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPageManager();
        try
        {
            for (Object secConstDefObj : pageManager.getPageSecurity().getSecurityConstraintsDefs())
            {
                SecurityConstraintsDef secConstDef = (SecurityConstraintsDef) secConstDefObj;
                jetspeedSecurityContraintNames.add(secConstDef.getName());
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to retrieve jetspeed security constraint defs from page manager.", e);
        }
        
        jetspeedSecurityConstraint = app.getJetspeedSecurityConstraint();

        Form form = new Form("form");
        form.add(dataRepeater);
        form.add(new DropDownChoice<String>("jetspeedConstraint", new PropertyModel<String>(this, "jetspeedSecurityConstraint"), jetspeedSecurityContraintNames));
        form.add(new Button("jsecSave", new ResourceModel("pam.details.action.save"))
        {
            @Override
            public void onSubmit()
            {
                FeedbackPanel feed = (FeedbackPanel) getPage().get("feedback");
                
                try
                {
                    PortletRegistry registry = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletRegistry();
                    PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());
                    app.setJetspeedSecurityConstraint("".equals(jetspeedSecurityConstraint) ? null : jetspeedSecurityConstraint);
                    registry.updatePortletApplication(app);
                    StringResourceModel resModel = new StringResourceModel("pam.details.action.status.application.saveOK", this, null, new Object [] { paNodeBean.getApplicationName() } );
                    feed.info(resModel.getString());
                }
                catch (RegistryException e)
                {
                    logger.error("Failed to update jetspeed security constraint defs of application.", e);
                    StringResourceModel resModel = new StringResourceModel("pam.details.action.status.application.saveFailure", this, null, new Object [] { paNodeBean.getApplicationName(), e.getMessage() } );
                    feed.info(resModel.getString());
                }
            }
        });
        
        form.add(new Label("resourceBundle", app.getResourceBundle()));
        
        Button reloadResourceBundlesButton = new Button("reloadResourceBundle", new ResourceModel("pam.details.resource.bundle.reload"))
        {
            @Override
            public void onSubmit()
            {
                FeedbackPanel feed = (FeedbackPanel) getPage().get("feedback");
                
                try
                {
                    PortletRegistry registry = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletRegistry();
                    PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());
                    PortletFactory factory = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletFactory();
                    factory.reloadResourceBundles(app);
                }
                catch (Exception e)
                {
                    logger.error("Failed to reload the resource bundle of application.", e);
                    StringResourceModel resModel = new StringResourceModel("pam.details.action.status.resource.bundle.reloadFailure", this, null, new Object [] { paNodeBean.getApplicationName(), e.getMessage() } );
                    feed.info(resModel.getString());
                }
            }
        };
        
        reloadResourceBundlesButton.add(new JavascriptEventConfirmation("onclick", new ResourceModel("pam.details.resource.bundle.reload.confirm")));
        
        reloadResourceBundlesButton.setEnabled(!StringUtils.isBlank(app.getResourceBundle()));
        
        form.add(reloadResourceBundlesButton);

        Button refreshSearchIndexButton = new Button("refreshSearchIndex", new ResourceModel("pam.details.search.index.refresh"))
        {
            @Override
            public void onSubmit()
            {
                FeedbackPanel feed = (FeedbackPanel) getPage().get("feedback");
                
                try
                {
                    SearchEngine searchEngine = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getSearchEngine();
                    PortletRegistry registry = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletRegistry();
                    
                    PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());
                    List<PortletDefinition> portletDefList = app.getPortlets();
                    
                    List<Object> list = new ArrayList<Object>();
                    list.addAll(portletDefList);
                    list.add(app);
                    
                    searchEngine.remove(list);
                    
                    list.clear();
                    list.add(app);
                    list.addAll(portletDefList);
                    
                    searchEngine.add(list);
                }
                catch (Exception e)
                {
                    logger.error("Failed to refresh the search index of application.", e);
                    StringResourceModel resModel = new StringResourceModel("pam.details.action.status.search.index.refreshFailure", this, null, new Object [] { paNodeBean.getApplicationName(), e.getMessage() } );
                    feed.info(resModel.getString());
                }
            }
        };
        
        refreshSearchIndexButton.add(new JavascriptEventConfirmation("onclick", new ResourceModel("pam.details.search.index.refresh.confirm")));
        
        refreshSearchIndexButton.setEnabled(true);
        
        form.add(refreshSearchIndexButton);

        add(form);
    }

    private String getDescription(PortletApplication app)
    {
        Description desc = app.getDescription(new Locale("en"));
        if (desc == null) { return null; }
        return desc.getDescription();
    }

    private String getType(PortletApplication pa)
    {
        int type = pa.getApplicationType();
        if (type == PortletApplication.LOCAL)
        {
            return "local";
        }
        else if (type == PortletApplication.WEBAPP) { return "webapp"; }
        return "unknown";
    }
    
}
