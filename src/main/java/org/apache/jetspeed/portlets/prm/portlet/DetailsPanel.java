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
package org.apache.jetspeed.portlets.prm.portlet;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.components.portletregistry.FailedToStorePortletDefinitionException;
import org.apache.jetspeed.components.portletregistry.PortletRegistry;
import org.apache.jetspeed.components.portletregistry.RegistryException;
import org.apache.jetspeed.factory.PortletFactory;
import org.apache.jetspeed.om.portlet.DisplayName;
import org.apache.jetspeed.om.portlet.PortletApplication;
import org.apache.jetspeed.om.portlet.PortletDefinition;
import org.apache.jetspeed.portlets.JetspeedServiceLocator;
import org.apache.jetspeed.portlets.prm.DisplayNameBean;
import org.apache.jetspeed.portlets.prm.PortletApplicationNodeBean;
import org.apache.jetspeed.portlets.util.PortletApplicationUtils;
import org.apache.jetspeed.portlets.wicket.AbstractAdminWebApplication;
import org.apache.jetspeed.portlets.wicket.component.JavascriptEventConfirmation;
import org.apache.jetspeed.portlets.wicket.component.editor.LocalizedDisplayNameEditor;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DetailsPanel extends Panel
{
    private static final long serialVersionUID = 1L;
    
    static final Logger logger = LoggerFactory.getLogger(DetailsPanel.class);
    
    private PortletApplicationNodeBean paNodeBean;
    private Integer expirationCache;
    private String newLocale, newDisplayName;
    
    public DetailsPanel(String id, PortletApplicationNodeBean paNodeBeanParam)
    {
        super(id);
        this.paNodeBean = paNodeBeanParam;
        
        PortletApplication app = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletRegistry().getPortletApplication(this.paNodeBean.getApplicationName());
        PortletDefinition definition = PortletApplicationUtils.getPortletOrClone(app, this.paNodeBean.getName());
        setExpirationCache(new Integer(definition.getExpirationCache()));

        Form form = new Form("form");
        form.add(new TextField<Integer>("expirationCache", new PropertyModel<Integer>(this, "expirationCache")));
        form.add(new Label("uniqueName", definition.getUniqueName()));
        form.add(new Label("preferenceValidator", definition.getPreferenceValidatorClassname()));
        form.add(new Label("className", definition.getPortletClass()));
        
        JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
        
        form.add(new LocalizedDisplayNameEditor("localizationEditor", locator, paNodeBean)
        {

            @Override
            public IDataProvider<DisplayNameBean> getDataProvider()
            {
                PortletRegistry registry = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletRegistry();
                PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());
                PortletDefinition def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());
                List<DisplayNameBean> list = new LinkedList<DisplayNameBean>();

                for (DisplayName displayName : def.getDisplayNames())
                {
                    list.add(new DisplayNameBean(displayName));
                }

                return new ListDataProvider<DisplayNameBean>(list);
            }

            @Override
            public void delete(IModel<DisplayNameBean>[] fields)
            {
                FeedbackPanel feed = (FeedbackPanel) getPage().get("feedback");
                
                try
                {
                    PortletRegistry registry = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletRegistry();
                    PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());
                    PortletDefinition def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());
                    
                    for (Iterator<DisplayName> it = def.getDisplayNames().iterator(); it.hasNext(); )
                    {
                        DisplayName displayName = it.next();
                        
                        for (IModel<DisplayNameBean> displayNameBeanModel : fields)
                        {
                            if (displayNameBeanModel.getObject().getLocale().equals(displayName.getLocale()))
                            {
                                it.remove();
                                break;
                            }
                        }
                    }
                    
                    registry.savePortletDefinition(def);
                    StringResourceModel resModel = new StringResourceModel("pam.details.action.status.portlet.saveOK", this, null, new Object [] { paNodeBean.getName() } );
                    feed.info(resModel.getString());
                }
                catch (FailedToStorePortletDefinitionException e)
                {
                    logger.error("Failed to save portlet definition.", e);
                    StringResourceModel resModel = new StringResourceModel("pam.details.action.status.portlet.saveFailure", this, null, new Object [] { paNodeBean.getName(), e.getMessage() } );
                    feed.info(resModel.getString());
                }
            }

            @Override
            protected Button saveButton(String componentId)
            {
                return new Button(componentId)
                {

                    @Override
                    public void onSubmit()
                    {
                        PortletRegistry registry = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletRegistry();
                        PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());
                        PortletDefinition def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());
                        
                        if (newLocale != null && newDisplayName != null)
                        {
                            Locale locale = new Locale(newLocale);
                            DisplayName targetDisplayName = null;
                            
                            for (DisplayName displayName : def.getDisplayNames())
                            {
                                if (displayName.getLocale().equals(locale))
                                {
                                    targetDisplayName = displayName;
                                    break;
                                }
                            }
                            
                            if (targetDisplayName == null)
                            {
                                targetDisplayName = def.addDisplayName(newLocale);
                            }
                            
                            targetDisplayName.setDisplayName(newDisplayName);
                            
                            newLocale = null;
                            newDisplayName = null;
                        }

                        FeedbackPanel feed = (FeedbackPanel) getPage().get("feedback");

                        try
                        {
                            registry.savePortletDefinition(def);
                            StringResourceModel resModel = new StringResourceModel("pam.details.action.status.portlet.saveOK", this, null, new Object [] { paNodeBean.getName() } );
                            feed.info(resModel.getString());
                        }
                        catch (RegistryException e)
                        {
                            logger.error("Failed to save portlet definition.", e);
                            StringResourceModel resModel = new StringResourceModel("pam.details.action.status.portlet.saveFailure", this, null, new Object [] { paNodeBean.getName(), e.getMessage() } );
                            feed.info(resModel.getString());
                        }
                    }
                };
            }
            
        });
        
        form.add(new Button("save", new ResourceModel("pam.details.action.save"))
        {
            @Override
            public void onSubmit()
            {
                FeedbackPanel feed = (FeedbackPanel) getPage().get("feedback");
                
                try
                {
                    PortletRegistry registry = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletRegistry();
                    PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());
                    PortletDefinition def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());
                    def.setExpirationCache(getExpirationCache().intValue());
                    registry.savePortletDefinition(def);
                    StringResourceModel resModel = new StringResourceModel("pam.details.action.status.portlet.saveOK", this, null, new Object [] { paNodeBean.getName() } );
                    feed.info(resModel.getString());
                }
                catch (FailedToStorePortletDefinitionException e)
                {
                    logger.error("Failed to store portlet definition.", e);
                    StringResourceModel resModel = new StringResourceModel("pam.details.action.status.portlet.saveFailure", this, null, new Object [] { paNodeBean.getName(), e.getMessage() } );
                    feed.info(resModel.getString());
                }
            }
        });
        
        form.add(new Label("resourceBundle", definition.getResourceBundle()));
        
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
                    PortletDefinition def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());
                    PortletFactory factory = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletFactory();
                    factory.reloadResourceBundles(def);
                }
                catch (Exception e)
                {
                    logger.error("Failed to reload the resource bundle of application.", e);
                    StringResourceModel resModel = new StringResourceModel("pam.details.action.status.resource.bundle.reloadFailure", this, null, new Object [] { paNodeBean.getApplicationName() + "::" + paNodeBean.getName(), e.getMessage() } );
                    feed.info(resModel.getString());
                }
            }
        };
        
        reloadResourceBundlesButton.add(new JavascriptEventConfirmation("onclick", new ResourceModel("pam.details.resource.bundle.reload.confirm")));
        
        reloadResourceBundlesButton.setEnabled(!StringUtils.isBlank(definition.getResourceBundle()));
        
        form.add(reloadResourceBundlesButton);

        CloneForm cloneForm = new CloneForm("cloneForm");
        add(cloneForm);                
        
        add(form);
    }

    public void setExpirationCache(Integer expirationCache)
    {
        this.expirationCache = expirationCache;
    }
    
    public Integer getExpirationCache()
    {
        return expirationCache;
    }

    private class CloneForm extends Form<Void>
    {
        private static final long serialVersionUID = 1L;
        
        private String cloneName;

        public CloneForm(String name)
        {
            super(name);
            add(new TextField("cloneName", new PropertyModel(this, "cloneName")));        
            add(new Button("cloneButton", new ResourceModel("pam.details.action.clone"))
            {
                @Override
                public void onSubmit()
                {
                    FeedbackPanel feed = (FeedbackPanel) getPage().get("feedback");
                    
                    try
                    {
                        String cloneName = getCloneName();
                        
                        if (!StringUtils.isBlank(cloneName))
                        {
                            PortletRegistry registry = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletRegistry();
                            PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());
                            PortletDefinition def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());
                            PortletDefinition clone = registry.clonePortletDefinition(def, cloneName);
                            StringResourceModel resModel = new StringResourceModel("pam.details.action.status.portlet.cloneOK", this, null, new Object [] { paNodeBean.getName() } );
                            feed.info(resModel.getString());
                        }
                        else
                        {
                            StringResourceModel resModel = new StringResourceModel("pam.details.action.clone.emptyPortletName", this, null, (Object []) null );
                            feed.info(resModel.getString());
                        }
                    }
                    catch (RegistryException e)
                    {
                        logger.error("Failed to clone portlet definition.", e);
                        StringResourceModel resModel = new StringResourceModel("pam.details.action.status.portlet.cloneFailure", this, null, new Object [] { paNodeBean.getName(), e.getMessage() } );
                        feed.info(resModel.getString());
                    }
                }
            });
        }

        public String getCloneName()
        {
            return cloneName;
        }
        
        public void setCloneName(String name)
        {
            this.cloneName = name;
        }        
    }
        
    
}
