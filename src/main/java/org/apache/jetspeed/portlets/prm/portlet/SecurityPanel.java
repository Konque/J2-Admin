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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.jetspeed.components.portletregistry.FailedToStorePortletDefinitionException;
import org.apache.jetspeed.components.portletregistry.PortletRegistry;
import org.apache.jetspeed.components.portletregistry.RegistryException;
import org.apache.jetspeed.om.page.SecurityConstraintsDef;
import org.apache.jetspeed.om.portlet.Description;
import org.apache.jetspeed.om.portlet.PortletApplication;
import org.apache.jetspeed.om.portlet.PortletDefinition;
import org.apache.jetspeed.om.portlet.SecurityRoleRef;
import org.apache.jetspeed.page.PageManager;
import org.apache.jetspeed.portlets.JetspeedServiceLocator;
import org.apache.jetspeed.portlets.prm.DescriptionBean;
import org.apache.jetspeed.portlets.prm.PortletApplicationNodeBean;
import org.apache.jetspeed.portlets.prm.model.SecurityRoleRefModel;
import org.apache.jetspeed.portlets.util.PortletApplicationUtils;
import org.apache.jetspeed.portlets.wicket.AbstractAdminWebApplication;
import org.apache.jetspeed.portlets.wicket.component.editor.EditorTemplate;
import org.apache.jetspeed.portlets.wicket.component.editor.LocalizedDescriptionEditor;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityPanel extends EditorTemplate<SecurityRoleRef>
{
    private static final long serialVersionUID = 1L;
    
    static final Logger logger = LoggerFactory.getLogger(SecurityPanel.class);
    
    private PortletApplicationNodeBean paNodeBean;
    private String newRoleName;
    private String newRoleLink;
    private String newLocale;
    private String newDescription;
    private String jetspeedSecurityConstraint;
    private List<String> jetspeedSecurityContraintNames;

    public SecurityPanel(String id, final PortletApplicationNodeBean paNodeBean)
    {
        super(id);
        this.paNodeBean = paNodeBean;
        Panel panel = initLayout();
        
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
        
        PortletRegistry registry = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletRegistry();
        PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());
        PortletDefinition def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());
        jetspeedSecurityConstraint = def.getJetspeedSecurityConstraint();
        
        Form form = (Form) panel.get("form");
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
                    PortletDefinition def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());
                    def.setJetspeedSecurityConstraint("".equals(jetspeedSecurityConstraint) ? null : jetspeedSecurityConstraint);
                    registry.savePortletDefinition(def);
                    StringResourceModel resModel = new StringResourceModel("pam.details.action.status.portlet.saveOK", this, null, new Object [] { paNodeBean.getName() } );
                    feed.info(resModel.getString());
                }
                catch (FailedToStorePortletDefinitionException e)
                {
                    logger.error("Failed to retrieve jetspeed security constraint defs of portlet definition.", e);
                    StringResourceModel resModel = new StringResourceModel("pam.details.action.status.portlet.saveFailure", this, null, new Object [] { paNodeBean.getName(), e.getMessage() } );
                    feed.info(resModel.getString());
                }
            }
        });
    }
    
    public String getJetspeedSecurityConstraint()
    {
        return jetspeedSecurityConstraint;
    }
    
    public void setJetspeedSecurityConstraint(String jetspeedSecurityConstraint)
    {
        this.jetspeedSecurityConstraint = jetspeedSecurityConstraint;
    }

    @Override
    public void buildNew(Fragment fragment)
    {
        fragment.add(new TextField<String>("newRoleName", new PropertyModel<String>(this, "newRoleName")));
        fragment.add(new TextField<String>("newRoleLink", new PropertyModel<String>(this, "newRoleLink")));
        fragment.add(new TextField<String>("newLocale", new PropertyModel<String>(this, "newLocale")));
        fragment.add(new TextField<String>("newDescription", new PropertyModel<String>(this, "newDescription")));
    }

    @Override
    public int getColumnCount()
    {
        return 3;
    }

    @Override
    public void buildItems(Fragment fragment, final SecurityRoleRef securityRoleRef)
    {
        JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();

        fragment.add(new TextField<String>("roleName", new PropertyModel<String>(new SecurityRoleRefModel(locator, paNodeBean, securityRoleRef), "roleName")));
        fragment.add(new TextField<String>("roleLink", new PropertyModel<String>(new SecurityRoleRefModel(locator, paNodeBean, securityRoleRef), "roleLink")));

        fragment.add(new LocalizedDescriptionEditor("localizationEditor", locator, paNodeBean, SecurityRoleRef.class, securityRoleRef.getRoleName())
        {

            @Override
            public IDataProvider<DescriptionBean> getDataProvider()
            {
                PortletRegistry registry = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletRegistry();
                PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());
                PortletDefinition def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());

                final SecurityRoleRef secRoleRef = def.getSecurityRoleRef(securityRoleRef.getRoleName());

                List<DescriptionBean> list = new ArrayList<DescriptionBean>();

                for (Description description : secRoleRef.getDescriptions())
                {
                    list.add(new DescriptionBean(description));
                }

                return new ListDataProvider<DescriptionBean>(list);
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
                        
                        if (newLocale != null && newDescription != null)
                        {
                            SecurityRoleRef secRoleRef = def.getSecurityRoleRef(securityRoleRef.getRoleName());
                            Locale locale = new Locale(newLocale);
                            Description targetDescription = null;
                            
                            for (Description description : secRoleRef.getDescriptions())
                            {
                                if (description.getLocale().equals(locale))
                                {
                                    targetDescription = description;
                                    break;
                                }
                            }
                            
                            if (targetDescription == null)
                            {
                                targetDescription = securityRoleRef.addDescription(newLocale);
                            }
                            
                            targetDescription.setDescription(newDescription);
                            
                            newLocale = null;
                            newDescription = null;
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

            @Override
            public void delete(IModel<DescriptionBean>[] fields)
            {
                FeedbackPanel feed = (FeedbackPanel) getPage().get("feedback");
                
                try
                {
                    PortletRegistry registry = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletRegistry();
                    PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());
                    PortletDefinition def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());
                    SecurityRoleRef secRoleRef = def.getSecurityRoleRef(securityRoleRef.getRoleName());
                    
                    if (secRoleRef != null)
                    {
                        for (Iterator<Description> it = secRoleRef.getDescriptions().iterator(); it.hasNext(); )
                        {
                            Description description = it.next();
                            
                            for (IModel<DescriptionBean> descriptionBeanModel : fields)
                            {
                                if (descriptionBeanModel.getObject().getLocale().equals(description.getLocale()))
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
                }
                catch (FailedToStorePortletDefinitionException e)
                {
                    logger.error("Failed to save portlet definition.", e);
                    StringResourceModel resModel = new StringResourceModel("pam.details.action.status.portlet.saveFailure", this, null, new Object [] { paNodeBean.getName(), e.getMessage() } );
                    feed.info(resModel.getString());
                }
            }
        });

    }

    @Override
    public void delete(IModel<SecurityRoleRef>[] fields)
    {
        PortletRegistry registry = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletRegistry();
        PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());
        PortletDefinition def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());

        Iterator<SecurityRoleRef> it = def.getSecurityRoleRefs().iterator();
        while (it.hasNext())
        {
            SecurityRoleRef securityRoleRef = it.next();
            String roleName = securityRoleRef.getRoleName();
            
            for (IModel<SecurityRoleRef> field : fields)
            {
                if (field.getObject().getRoleName().equals(roleName))
                {
                    it.remove();
                    break;
                }
            }
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

    @Override
    public Iterator<IModel<SecurityRoleRef>> getItemModels()
    {
        JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
        PortletRegistry registry = locator.getPortletRegistry();
        PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());
        PortletDefinition def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());
        
        List<IModel<SecurityRoleRef>> securityRoleRefModels = new ArrayList<IModel<SecurityRoleRef>>();
        
        for (SecurityRoleRef securityRoleRef : def.getSecurityRoleRefs())
        {
            securityRoleRefModels.add(new SecurityRoleRefModel(locator, paNodeBean, securityRoleRef));
        }
        
        return securityRoleRefModels.iterator();
    }

    @Override
    public IModel<SecurityRoleRef> getNewRowModel(SecurityRoleRef securityRoleRef)
    {
        JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
        return new SecurityRoleRefModel(locator, paNodeBean, securityRoleRef);
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

                if (newRoleName != null && newRoleLink != null)
                {
                    SecurityRoleRef securityRoleRef = def.addSecurityRoleRef(newRoleName);
                    securityRoleRef.setRoleLink(newRoleLink);

                    if (newLocale != null && newDescription != null)
                    {
                        Description desc = securityRoleRef.addDescription(newLocale);
                        desc.setDescription(newDescription);
                    }
                    
                    newRoleName = null;
                    newRoleLink = null;
                    newLocale = null;
                    newDescription = null;
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

}
