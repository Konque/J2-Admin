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

import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.components.portletpreferences.PortletPreferencesProvider;
import org.apache.jetspeed.components.portletregistry.PortletRegistry;
import org.apache.jetspeed.om.portlet.PortletApplication;
import org.apache.jetspeed.om.portlet.PortletDefinition;
import org.apache.jetspeed.om.portlet.Preference;
import org.apache.jetspeed.portlets.JetspeedServiceLocator;
import org.apache.jetspeed.portlets.prm.PortletApplicationNodeBean;
import org.apache.jetspeed.portlets.prm.model.PreferenceModel;
import org.apache.jetspeed.portlets.util.PortletApplicationUtils;
import org.apache.jetspeed.portlets.wicket.AbstractAdminWebApplication;
import org.apache.jetspeed.portlets.wicket.component.editor.EditorTemplate;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreferencePanel extends EditorTemplate<Preference>
{
    private static final long serialVersionUID = 1L;
    
    static final Logger logger = LoggerFactory.getLogger(PreferencePanel.class);
    
    private PortletApplicationNodeBean paNodeBean;
    private String newName, newValue;
    private transient List<Preference> curPrefs;

    public PreferencePanel(String id, PortletApplicationNodeBean paNodeBean)
    {
        super(id);
        this.paNodeBean = paNodeBean;
        initLayout();
    }

    /**
     * TODO: first fix the duplicated name fields
     */
    @Override
    protected Button saveButton(String componentId)
    {
        return new Button(componentId)
        {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit()
            {
                FeedbackPanel feed = (FeedbackPanel) getPage().get("feedback");
                
                try
                {
                    PortletRegistry registry = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletRegistry();
                    PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());
                    PortletDefinition def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());
                    PortletPreferencesProvider prefProvider = (PortletPreferencesProvider) 
                        ((AbstractAdminWebApplication) getApplication()).getServiceLocator()
                        .getService(CommonPortletServices.CPS_PORTLET_PREFERENCES_PROVIDER);
                    
                    if (newName != null && newValue != null)
                    {
                        Preference pref = def.getPortletPreferences().addPreference(newName);
                        pref.addValue(newValue);
                        
                        prefProvider.storeDefaults(def, pref);
                        
                        newName = null;
                        newValue = null;
                    }

                    for (Preference pref : curPrefs)
                    {
                        prefProvider.storeDefaults(def, pref);
                    }
                    
                    StringResourceModel resModel = new StringResourceModel("pam.details.action.status.portlet.saveOK", this, null, new Object [] { paNodeBean.getName() } );
                    feed.info(resModel.getString());
                }
                catch (Exception e)
                {
                    logger.error("Failed to store portlet default preferences.", e);
                    StringResourceModel resModel = new StringResourceModel("pam.details.action.status.portlet.saveFailure", this, null, new Object [] { paNodeBean.getName(), e.getMessage() } );
                    feed.info(resModel.getString());
                }
            }
        };
    }

    @Override
    public void buildNew(Fragment fragment)
    {
        fragment.add(new TextField<String>("newName", new PropertyModel<String>(this, "newName")));
        fragment.add(new TextField<String>("newValue", new PropertyModel<String>(this, "newValue")));
    }

    @Override
    public int getColumnCount()
    {
        return 3;
    }

    @Override
    public void buildItems(Fragment fragment, final Preference preference)
    {
        fragment.add(new TextField<String>("name", new PropertyModel<String>(preference, "name")));
        
        RefreshingView<String> view = new RefreshingView<String>("values")
        {
            private static final long serialVersionUID = 1L;

            @Override
            protected Iterator<IModel<String>> getItemModels()
            {
                List<IModel<String>> preferenceValueModels = new LinkedList<IModel<String>>();
                int preferenceCount = preference.getValues().size();
                
                for (int i = 0; i < preferenceCount; i++)
                {
                    preferenceValueModels.add(new PropertyModel<String>(preference, "values." + i));
                }

                return preferenceValueModels.iterator();
            }

            @Override
            protected void populateItem(Item<String> item)
            {
                item.add(new TextField<String>("value", item.getModel()));
            }
        };

        fragment.add(view);
    }

    @Override
    public void delete(IModel<Preference>[] fields)
    {
        FeedbackPanel feed = (FeedbackPanel) getPage().get("feedback");
        
        try
        {
            PortletRegistry registry = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletRegistry();
            PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());
            PortletDefinition def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());
            PortletPreferencesProvider prefProvider = (PortletPreferencesProvider) 
                ((AbstractAdminWebApplication) getApplication()).getServiceLocator()
                .getService(CommonPortletServices.CPS_PORTLET_PREFERENCES_PROVIDER);
            
            for (IModel<Preference> field : fields)
            {
                prefProvider.removeDefaults(def, field.getObject().getName());
            }
            
            StringResourceModel resModel = new StringResourceModel("pam.details.action.status.portlet.saveOK", this, null, new Object [] { paNodeBean.getName() } );
            feed.info(resModel.getString());
        }
        catch (Exception e)
        {
            logger.error("Failed to remove portlet default preference.", e);
            StringResourceModel resModel = new StringResourceModel("pam.details.action.status.portlet.saveFailure", this, null, new Object [] { paNodeBean.getName(), e.getMessage() } );
            feed.info(resModel.getString());
        }
    }

    @Override
    public Iterator<IModel<Preference>> getItemModels()
    {
        final JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
        PortletRegistry registry = locator.getPortletRegistry();
        PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());
        PortletDefinition def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());

        curPrefs = def.getPortletPreferences().getPortletPreferences();
        
        return new ModelIteratorAdapter<Preference>(curPrefs.iterator())
        {
            @Override
            protected IModel<Preference> model(Preference preference)
            {
                return new PreferenceModel(locator, paNodeBean, preference);
            }
        };
    }

    @Override
    public IModel<Preference> getNewRowModel(Preference preference)
    {
        final JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
        return new PreferenceModel(locator, paNodeBean, preference);
    }
    
}
