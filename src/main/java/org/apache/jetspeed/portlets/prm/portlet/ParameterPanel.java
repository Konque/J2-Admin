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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.jetspeed.components.portletregistry.FailedToStorePortletDefinitionException;
import org.apache.jetspeed.components.portletregistry.PortletRegistry;
import org.apache.jetspeed.components.portletregistry.RegistryException;
import org.apache.jetspeed.om.portlet.Description;
import org.apache.jetspeed.om.portlet.InitParam;
import org.apache.jetspeed.om.portlet.PortletApplication;
import org.apache.jetspeed.om.portlet.PortletDefinition;
import org.apache.jetspeed.portlets.JetspeedServiceLocator;
import org.apache.jetspeed.portlets.prm.DescriptionBean;
import org.apache.jetspeed.portlets.prm.PortletApplicationNodeBean;
import org.apache.jetspeed.portlets.prm.model.InitParamModel;
import org.apache.jetspeed.portlets.util.PortletApplicationUtils;
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

public class ParameterPanel extends EditorTemplate<InitParam>
{
    private static final long serialVersionUID = 1L;
    
    static final Logger logger = LoggerFactory.getLogger(ParameterPanel.class);
    
    private PortletApplicationNodeBean paNodeBean;
    private String newName;
    private String newLocale;
    private String newDescription;
    private String newValue;

    public ParameterPanel(String id, PortletApplicationNodeBean paNodeBean)
    {
        super(id);
        this.paNodeBean = paNodeBean;
        initLayout();
    }

    @Override
    public void buildNew(Fragment fragment)
    {
        fragment.add(new TextField<String>("newName", new PropertyModel<String>(this, "newName")));
        fragment.add(new TextField<String>("newValue", new PropertyModel<String>(this, "newValue")));
        fragment.add(new TextField<String>("newLocale", new PropertyModel<String>(this, "newLocale")));
        fragment.add(new TextField<String>("newDescription", new PropertyModel<String>(this, "newDescription")));
    }

    @Override
    public int getColumnCount()
    {
        return 3;
    }

    @Override
    public void buildItems(Fragment fragment, final InitParam initParam)
    {
        JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();

        fragment.add(new TextField<String>("name", new PropertyModel<String>(new InitParamModel(locator, paNodeBean, initParam), "paramName")));
        fragment.add(new TextField<String>("value", new PropertyModel<String>(new InitParamModel(locator, paNodeBean, initParam), "paramValue")));

        fragment.add(new LocalizedDescriptionEditor("localizationEditor", locator, paNodeBean, InitParam.class, initParam.getParamName())
        {

            @Override
            public IDataProvider<DescriptionBean> getDataProvider()
            {
                PortletRegistry registry = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletRegistry();
                PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());
                PortletDefinition def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());

                final InitParam param = def.getInitParam(initParam.getParamName());

                List<DescriptionBean> list = new ArrayList<DescriptionBean>();

                for (Description description : param.getDescriptions())
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
                            InitParam param = def.getInitParam(initParam.getParamName());
                            Locale locale = new Locale(newLocale);
                            Description targetDescription = null;
                            
                            for (Description description : param.getDescriptions())
                            {
                                if (description.getLocale().equals(locale))
                                {
                                    targetDescription = description;
                                    break;
                                }
                            }
                            
                            if (targetDescription == null)
                            {
                                targetDescription = param.addDescription(newLocale);
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
                    InitParam initParam = def.getInitParam(name);
                    
                    if (initParam != null)
                    {
                        for (Iterator<Description> it = initParam.getDescriptions().iterator(); it.hasNext(); )
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
    public void delete(IModel<InitParam>[] fields)
    {
        PortletRegistry registry = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletRegistry();
        PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());
        PortletDefinition def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());

        Iterator<InitParam> it = def.getInitParams().iterator();
        while (it.hasNext())
        {
            InitParam initParam = it.next();
            String paramName = initParam.getParamName();
            
            for (IModel<InitParam> field : fields)
            {
                if (field.getObject().getParamName().equals(paramName))
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
    public Iterator<IModel<InitParam>> getItemModels()
    {
        JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
        PortletRegistry registry = locator.getPortletRegistry();
        PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());
        PortletDefinition def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());
        if (def == null)
            def = app.getClone(paNodeBean.getName()); // TODO: support clones here?
        
        List<IModel<InitParam>> initParamBeanModels = new ArrayList<IModel<InitParam>>();
        
        for (InitParam initParam : def.getInitParams())
        {
            initParamBeanModels.add(new InitParamModel(locator, paNodeBean, initParam));
        }
        
        return initParamBeanModels.iterator();
    }

    @Override
    public IModel<InitParam> getNewRowModel(InitParam initParam)
    {
        JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
        return new InitParamModel(locator, paNodeBean, initParam);
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

                if (newName != null && newValue != null)
                {
                    InitParam param = def.addInitParam(newName);
                    param.setParamValue(newValue);

                    if (newLocale != null && newDescription != null)
                    {
                        Description desc = param.addDescription(newLocale);
                        desc.setDescription(newDescription);
                    }
                    
                    newName = null;
                    newValue = null;
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
