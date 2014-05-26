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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jetspeed.components.portletregistry.PortletRegistry;
import org.apache.jetspeed.components.portletregistry.RegistryException;
import org.apache.jetspeed.om.portlet.GenericMetadata;
import org.apache.jetspeed.om.portlet.LocalizedField;
import org.apache.jetspeed.om.portlet.PortletApplication;
import org.apache.jetspeed.om.portlet.PortletDefinition;
import org.apache.jetspeed.portlets.JetspeedServiceLocator;
import org.apache.jetspeed.portlets.prm.model.LocalizedFieldBeanModel;
import org.apache.jetspeed.portlets.util.PortletApplicationUtils;
import org.apache.jetspeed.portlets.wicket.AbstractAdminWebApplication;
import org.apache.jetspeed.portlets.wicket.component.editor.EditorTemplate;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataPanel extends EditorTemplate<LocalizedFieldBean>
{
    private static final long serialVersionUID = 1L;
    
    static final Logger logger = LoggerFactory.getLogger(MetadataPanel.class);

    private PortletApplicationNodeBean paNodeBean;
    private String newName, newValue, newLocale;

    public MetadataPanel(String id, final PortletApplicationNodeBean paNodeBean)
    {
        super(id);
        this.paNodeBean = paNodeBean;
        initLayout();
    }

    @Override
    protected Button saveButton(String componentId)
    {
        return new Button(componentId)
        {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit()
            {
                try
                {
                    PortletRegistry registry = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletRegistry();
                    PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());
                    PortletDefinition def = null;
                    
                    if (paNodeBean.getName() != null)
                    {
                        def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());
                    }
                    
                    if (newName != null && newValue != null && newLocale != null)
                    {
                        GenericMetadata metadata = (def == null ? app.getMetadata() : def.getMetadata());
                        LocalizedFieldBean fieldBean = new LocalizedFieldBean(metadata.createLocalizedField());
                        fieldBean.setName(newName);
                        fieldBean.setLocaleString(newLocale);
                        fieldBean.setValue(newValue);
                        metadata.addField(fieldBean.getLocalizedField());

                        newName = null;
                        newValue = null;
                        newLocale = null;
                    }

                    if (def == null)
                    {
                        registry.updatePortletApplication(app);
                    }
                    else
                    {
                        registry.savePortletDefinition(def);
                    }
                }
                catch (RegistryException e)
                {
                    logger.error("Exception occurred during updating portlet application or saving portlet definition.", e);
                }
            }
        };
    }

    @Override
    public void buildNew(Fragment fragment)
    {
        fragment.add(new TextField<String>("newName", new PropertyModel<String>(this, "newName")));
        fragment.add(new TextField<String>("newValue", new PropertyModel<String>(this, "newValue")));
        fragment.add(new TextField<String>("newLocale", new PropertyModel<String>(this, "newLocale")));
    }

    @Override
    public int getColumnCount()
    {
        return 4;
    }

    @Override
    public void buildItems(Fragment fragment, final LocalizedFieldBean field)
    {
        JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
        fragment.add(new TextField<String>("name", new PropertyModel(new LocalizedFieldBeanModel(locator, paNodeBean, field), "name")));
        fragment.add(new TextField<String>("locale", new PropertyModel(new LocalizedFieldBeanModel(locator, paNodeBean, field), "localeString")));
        fragment.add(new TextField<String>("value", new PropertyModel(new LocalizedFieldBeanModel(locator, paNodeBean, field), "value")));
    }

    @Override
    public void delete(IModel<LocalizedFieldBean>[] fieldBeans)
    {
        PortletRegistry registry = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletRegistry();
        PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());
        PortletDefinition def = null;
        
        if (paNodeBean.getName() != null)
        {
            def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());
        }
        
        GenericMetadata metadata = (def == null ? app.getMetadata() : def.getMetadata());

        for (Iterator<LocalizedField> it = metadata.getFields().iterator(); it.hasNext(); )
        {
            LocalizedFieldBean tempBean = new LocalizedFieldBean(it.next());
            
            for (IModel<LocalizedFieldBean> fieldBean : fieldBeans)
            {
                if (tempBean.equals(fieldBean.getObject()))
                {
                    it.remove();
                    break;
                }
            }
        }

        try
        {
            if (def == null)
            {
                registry.updatePortletApplication(app);
            }
            else
            {
                registry.savePortletDefinition(def);
            }
        }
        catch (RegistryException e)
        {
            logger.error("Exception occurred during updating portlet application or saving portlet definition.", e);
        }
    }

    @Override
    public Iterator<IModel<LocalizedFieldBean>> getItemModels()
    {
        JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
        PortletApplication app = locator.getPortletRegistry().getPortletApplication(paNodeBean.getApplicationName());
        PortletDefinition def = null;
        
        if (paNodeBean.getName() != null)
        {
            def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());
        }
        
        List<IModel<LocalizedFieldBean>> list = new ArrayList<IModel<LocalizedFieldBean>>();
        GenericMetadata metadata = (def == null ? app.getMetadata() : def.getMetadata());

        for (LocalizedField field : metadata.getFields())
        {
            list.add(new LocalizedFieldBeanModel(locator, paNodeBean, new LocalizedFieldBean(field)));
        }

        return list.iterator();
    }

    @Override
    public IModel<LocalizedFieldBean> getNewRowModel(LocalizedFieldBean fieldBean)
    {
        JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
        return new LocalizedFieldBeanModel(locator, paNodeBean, fieldBean);
    }

}
