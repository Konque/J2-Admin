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
package org.apache.jetspeed.portlets.wicket.component.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jetspeed.portlets.JetspeedServiceLocator;
import org.apache.jetspeed.portlets.prm.DescriptionBean;
import org.apache.jetspeed.portlets.prm.PortletApplicationNodeBean;
import org.apache.jetspeed.portlets.prm.model.DescriptionBeanModel;
import org.apache.jetspeed.portlets.wicket.AbstractAdminWebApplication;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

/**
 * 
 * @author Ruben Alexander de Gooijer
 */
public abstract class LocalizedDescriptionEditor extends EditorTemplate<DescriptionBean>
{
    private static final long serialVersionUID = 1L;
    
    protected JetspeedServiceLocator locator = null;
    protected PortletApplicationNodeBean paNodeBean = null;
    protected Class type;
    protected String name = null;

    protected String newLocale, newDescription;
    
    public LocalizedDescriptionEditor(String id, JetspeedServiceLocator locator, PortletApplicationNodeBean paNodeBean, Class type, String name)
    {
        super(id);
        this.locator = locator;
        this.paNodeBean = paNodeBean;
        this.type = type;
        this.name = name;
        initLayout();
    }

    @Override
    public int getColumnCount()
    {
        return 2;
    }

    @Override
    public void buildItems(Fragment fragment, final DescriptionBean descriptionBean)
    {
        JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
        fragment.add(new TextField<String>("locale", new PropertyModel(new DescriptionBeanModel(locator, paNodeBean, type, name, descriptionBean), "localeString"))); 
        fragment.add(new TextField<String>("description", new PropertyModel(new DescriptionBeanModel(locator, paNodeBean, type, name, descriptionBean), "description")));
    }
    
    @Override
    public void buildNew(Fragment fragment)
    {
        fragment.add(new TextField<String>("newLocale", new PropertyModel<String>(this, "newLocale")));
        fragment.add(new TextField<String>("newDescription", new PropertyModel<String>(this, "newDescription")));
    }
    
    @Override
    public Iterator<IModel<DescriptionBean>> getItemModels()
    {
        IDataProvider<DescriptionBean> dataProvider = getDataProvider();
        List<IModel<DescriptionBean>> list = new ArrayList<IModel<DescriptionBean>>();
        JetspeedServiceLocator locator = null;
        PortletApplicationNodeBean paNodeBean = null;
        String name = null;
        
        Iterator<? extends DescriptionBean> it = dataProvider.iterator(0, dataProvider.size());
        
        while(it.hasNext()) 
        {
            DescriptionBean descriptionBean = it.next();
            list.add(new DescriptionBeanModel(locator, paNodeBean, type, name, descriptionBean));
        }
        
        return list.iterator();
    }
    
    @Override
    public IModel<DescriptionBean> getNewRowModel(DescriptionBean descriptionBean)
    {
        return new DescriptionBeanModel(locator, paNodeBean, type, name, descriptionBean);
    }
    
    public abstract IDataProvider<DescriptionBean> getDataProvider();
}
