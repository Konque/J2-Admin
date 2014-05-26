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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.jetspeed.components.portletregistry.PortletRegistry;
import org.apache.jetspeed.components.portletregistry.RegistryException;
import org.apache.jetspeed.om.portlet.Description;
import org.apache.jetspeed.om.portlet.PortletApplication;
import org.apache.jetspeed.om.portlet.UserAttribute;
import org.apache.jetspeed.portlets.JetspeedServiceLocator;
import org.apache.jetspeed.portlets.prm.DescriptionBean;
import org.apache.jetspeed.portlets.prm.PortletApplicationNodeBean;
import org.apache.jetspeed.portlets.prm.model.UserAttributeModel;
import org.apache.jetspeed.portlets.wicket.AbstractAdminWebApplication;
import org.apache.jetspeed.portlets.wicket.component.editor.EditorTemplate;
import org.apache.jetspeed.portlets.wicket.component.editor.LocalizedDescriptionEditor;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserAttributePanel extends EditorTemplate<UserAttribute>
{
    private static final long serialVersionUID = 1L;
    
    static final Logger logger = LoggerFactory.getLogger(UserAttributePanel.class);
    
    private PortletApplicationNodeBean paNodeBean;
    private String newName, newLocale, newDescription;

    public UserAttributePanel(String id, PortletApplicationNodeBean paNodeBean)
    {
        super(id);
        this.paNodeBean = paNodeBean;
        initLayout();
    }

    @Override
    public void buildNew(Fragment fragment)
    {
        fragment.add(new TextField<String>("newName", new PropertyModel<String>(this, "newName")));
        fragment.add(new TextField<String>("newLocale", new PropertyModel<String>(this, "newLocale")));
        fragment.add(new TextField<String>("newDescription", new PropertyModel<String>(this, "newDescription")));
    }

    @Override
    public int getColumnCount()
    {
        return 3;
    }

    @Override
    public void buildItems(Fragment fragment, final UserAttribute userAttr)
    {
        JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
        
        fragment.add(new TextField<String>("name", new PropertyModel<String>(new UserAttributeModel(locator, paNodeBean, userAttr), "name")));
        
        fragment.add(new LocalizedDescriptionEditor("localizationEditor", locator, paNodeBean, UserAttribute.class, userAttr.getName())
        {

            @Override
            public IDataProvider<DescriptionBean> getDataProvider()
            {
                JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
                PortletApplication app = locator.getPortletRegistry().getPortletApplication(paNodeBean.getApplicationName());
                final UserAttribute attribute = app.getUserAttribute(userAttr.getName());

                List<DescriptionBean> list = new ArrayList<DescriptionBean>();

                for (Description description : attribute.getDescriptions())
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
                        FeedbackPanel feed = (FeedbackPanel) getPage().get("feedback");
                        
                        try
                        {
                            PortletRegistry registry = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletRegistry();
                            PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());
                            
                            if (newLocale != null && newDescription != null)
                            {
                                UserAttribute attr = app.getUserAttribute(userAttr.getName());
                                Locale locale = new Locale(newLocale);
                                Description targetDescription = null;
                                
                                for (Description description : attr.getDescriptions())
                                {
                                    if (description.getLocale().equals(locale))
                                    {
                                        targetDescription = description;
                                        break;
                                    }
                                }

                                if (targetDescription == null)
                                {
                                    targetDescription = attr.addDescription(newLocale);
                                }
                                
                                targetDescription.setDescription(newDescription);
                                
                                newLocale = null;
                                newDescription = null;
                            }

                            registry.updatePortletApplication(app);
                            StringResourceModel resModel = new StringResourceModel("pam.details.action.status.application.saveOK", this, null, new Object [] { paNodeBean.getApplicationName() } );
                            feed.info(resModel.getString());
                        }
                        catch (RegistryException e)
                        {
                            logger.error("Failed to update portlet application.", e);
                            StringResourceModel resModel = new StringResourceModel("pam.details.action.status.application.saveFailure", this, null, new Object [] { paNodeBean.getApplicationName(), e.getMessage() } );
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
                    UserAttribute attr = app.getUserAttribute(name);
                    
                    if (attr != null)
                    {
                        for (Iterator<Description> it = attr.getDescriptions().iterator(); it.hasNext(); )
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
                        
                        registry.updatePortletApplication(app);
                        StringResourceModel resModel = new StringResourceModel("pam.details.action.status.application.saveOK", this, null, new Object [] { paNodeBean.getApplicationName() } );
                        feed.info(resModel.getString());
                    }
                }
                catch (RegistryException e)
                {
                    logger.error("Failed to update portlet application.", e);
                    StringResourceModel resModel = new StringResourceModel("pam.details.action.status.application.saveFailure", this, null, new Object [] { paNodeBean.getApplicationName(), e.getMessage() } );
                    feed.info(resModel.getString());
                }
            }
        });

    }

    @Override
    public void delete(IModel<UserAttribute>[] fields)
    {
        PortletRegistry registry = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletRegistry();
        PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());

        for (Iterator<UserAttribute> it = app.getUserAttributes().iterator(); it.hasNext(); )
        {
            String id = it.next().getName();
            
            for (IModel<UserAttribute> field : fields)
            {
                if (field.getObject().getName().equals(id))
                {
                    it.remove();
                    break;
                }
            }
        }

        FeedbackPanel feed = (FeedbackPanel) getPage().get("feedback");
        
        try
        {
            registry.updatePortletApplication(app);
            StringResourceModel resModel = new StringResourceModel("pam.details.action.status.application.saveOK", this, null, new Object [] { paNodeBean.getApplicationName() } );
            feed.info(resModel.getString());
        }
        catch (RegistryException e)
        {
            logger.error("Failed to update portlet application.", e);
            StringResourceModel resModel = new StringResourceModel("pam.details.action.status.application.saveFailure", this, null, new Object [] { paNodeBean.getApplicationName(), e.getMessage() } );
            feed.info(resModel.getString());
        }
    }

    @Override
    public Iterator<IModel<UserAttribute>> getItemModels()
    {
        JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
        PortletApplication app = locator.getPortletRegistry().getPortletApplication(paNodeBean.getApplicationName());
        List<IModel<UserAttribute>> userAttributeBeans = new LinkedList<IModel<UserAttribute>>();
        
        for (UserAttribute userAttribute : app.getUserAttributes())
        {
            userAttributeBeans.add(new UserAttributeModel(locator, paNodeBean, userAttribute));
        }
        
        return userAttributeBeans.iterator();
    }

    @Override
    public IModel<UserAttribute> getNewRowModel(UserAttribute userAttribute)
    {
        JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
        return new UserAttributeModel(locator, paNodeBean, userAttribute);
    }

    @Override
    protected Button saveButton(String componentId)
    {
        return new Button(componentId)
        {
            @Override
            public void onSubmit()
            {
                FeedbackPanel feed = (FeedbackPanel) getPage().get("feedback");
                
                try
                {
                    JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
                    PortletApplication app = locator.getPortletRegistry().getPortletApplication(paNodeBean.getApplicationName());

                    if (newName != null && newDescription != null && newLocale != null)
                    {
                        UserAttribute attribute = app.addUserAttribute(newName);
                        attribute.addDescription(newLocale).setDescription(newDescription);
                        
                        newName = null;
                        newDescription = null;
                        newLocale = null;
                    }
                    
                    locator.getPortletRegistry().updatePortletApplication(app);
                    StringResourceModel resModel = new StringResourceModel("pam.details.action.status.application.saveOK", this, null, new Object [] { paNodeBean.getApplicationName() } );
                    feed.info(resModel.getString());
                }
                catch (RegistryException e)
                {
                    logger.error("Failed to update portlet application", e);
                    StringResourceModel resModel = new StringResourceModel("pam.details.action.status.application.saveFailure", this, null, new Object [] { paNodeBean.getApplicationName(), e.getMessage() } );
                    feed.info(resModel.getString());
                }
            }
        };
    }

}
