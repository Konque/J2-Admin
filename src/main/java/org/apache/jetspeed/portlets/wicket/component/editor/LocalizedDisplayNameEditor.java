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
import org.apache.jetspeed.portlets.prm.DisplayNameBean;
import org.apache.jetspeed.portlets.prm.PortletApplicationNodeBean;
import org.apache.jetspeed.portlets.prm.model.DisplayNameBeanModel;
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
public abstract class LocalizedDisplayNameEditor extends EditorTemplate<DisplayNameBean>
{
    private static final long serialVersionUID = 1L;
    
    protected JetspeedServiceLocator locator = null;
    protected PortletApplicationNodeBean paNodeBean = null;

    protected String newLocale, newDisplayName;
    
    public LocalizedDisplayNameEditor(String id, JetspeedServiceLocator locator, PortletApplicationNodeBean paNodeBean)
    {
        super(id);
        this.locator = locator;
        this.paNodeBean = paNodeBean;
        initLayout();
    }

    @Override
    public int getColumnCount()
    {
        return 2;
    }

    @Override
    public void buildItems(Fragment fragment, final DisplayNameBean displayNameBean)
    {
        JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
        fragment.add(new TextField<String>("locale", new PropertyModel(new DisplayNameBeanModel(locator, paNodeBean, displayNameBean), "localeString"))); 
        fragment.add(new TextField<String>("displayName", new PropertyModel(new DisplayNameBeanModel(locator, paNodeBean, displayNameBean), "displayName")));
    }
    
    @Override
    public void buildNew(Fragment fragment)
    {
        fragment.add(new TextField<String>("newLocale", new PropertyModel<String>(this, "newLocale")));
        fragment.add(new TextField<String>("newDisplayName", new PropertyModel<String>(this, "newDisplayName")));
    }
    
    @Override
    public Iterator<IModel<DisplayNameBean>> getItemModels()
    {
        IDataProvider<DisplayNameBean> dataProvider = getDataProvider();
        List<IModel<DisplayNameBean>> list = new ArrayList<IModel<DisplayNameBean>>();
        JetspeedServiceLocator locator = null;
        PortletApplicationNodeBean paNodeBean = null;
        String name = null;
        
        Iterator<? extends DisplayNameBean> it = dataProvider.iterator(0, dataProvider.size());
        
        while(it.hasNext()) 
        {
            DisplayNameBean displayNameBean = it.next();
            list.add(new DisplayNameBeanModel(locator, paNodeBean, displayNameBean));
        }
        
        return list.iterator();
    }
    
    @Override
    public IModel<DisplayNameBean> getNewRowModel(DisplayNameBean displayNameBean)
    {
        return new DisplayNameBeanModel(locator, paNodeBean, displayNameBean);
    }
    
    public abstract IDataProvider<DisplayNameBean> getDataProvider();
    
}
