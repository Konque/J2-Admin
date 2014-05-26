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

import org.apache.jetspeed.components.portletregistry.PortletRegistry;
import org.apache.jetspeed.components.portletregistry.RegistryException;
import org.apache.jetspeed.om.portlet.Language;
import org.apache.jetspeed.om.portlet.PortletApplication;
import org.apache.jetspeed.om.portlet.PortletDefinition;
import org.apache.jetspeed.portlets.JetspeedServiceLocator;
import org.apache.jetspeed.portlets.prm.LanguageBean;
import org.apache.jetspeed.portlets.prm.PortletApplicationNodeBean;
import org.apache.jetspeed.portlets.prm.model.LanguageBeanModel;
import org.apache.jetspeed.portlets.util.PortletApplicationUtils;
import org.apache.jetspeed.portlets.wicket.AbstractAdminWebApplication;
import org.apache.jetspeed.portlets.wicket.component.editor.EditorTemplate;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LanguagesPanel extends EditorTemplate<LanguageBean>
{
    private static final long serialVersionUID = 1L;
    
    static final Logger logger = LoggerFactory.getLogger(LanguagesPanel.class);

    private PortletApplicationNodeBean paNodeBean;
    private String newTitle, newShortTitle, newKeywords, newLocale;

    public LanguagesPanel(String id, final PortletApplicationNodeBean paNodeBean)
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
                FeedbackPanel feed = (FeedbackPanel) getPage().get("feedback");
                
                try
                {
                    PortletRegistry registry = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletRegistry();
                    PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());
                    PortletDefinition def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());
                    
                    if (newTitle != null && newShortTitle != null && newKeywords != null && newLocale != null)
                    {
                        Locale locale = new Locale(newLocale);
                        Language newLanguage = def.addLanguage(locale);
                        newLanguage.setTitle(newTitle);
                        newLanguage.setShortTitle(newShortTitle);
                        newLanguage.setKeywords(newKeywords);

                        newTitle = null;
                        newShortTitle = null;
                        newKeywords = null;
                        newLocale = null;
                    }

                    registry.savePortletDefinition(def);
                    StringResourceModel resModel = new StringResourceModel("pam.details.action.status.portlet.saveOK", this, null, new Object [] { paNodeBean.getName() } );
                    feed.info(resModel.getString());
                }
                catch (RegistryException e)
                {
                    logger.error("Exception occurred during updating portlet application or saving portlet definition.", e);
                    StringResourceModel resModel = new StringResourceModel("pam.details.action.status.portlet.saveFailure", this, null, new Object [] { paNodeBean.getName(), e.getMessage() } );
                    feed.info(resModel.getString());
                }
            }
        };
    }

    @Override
    public void buildNew(Fragment fragment)
    {
        fragment.add(new TextField<String>("newTitle", new PropertyModel<String>(this, "newTitle")));
        fragment.add(new TextField<String>("newShortTitle", new PropertyModel<String>(this, "newShortTitle")));
        fragment.add(new TextField<String>("newKeywords", new PropertyModel<String>(this, "newKeywords")));
        fragment.add(new TextField<String>("newLocale", new PropertyModel<String>(this, "newLocale")));
    }

    @Override
    public int getColumnCount()
    {
        return 5;
    }

    @Override
    public void buildItems(Fragment fragment, final LanguageBean field)
    {
        JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
        fragment.add(new TextField<String>("title", new PropertyModel(new LanguageBeanModel(locator, paNodeBean, field), "title")));
        fragment.add(new TextField<String>("shortTitle", new PropertyModel(new LanguageBeanModel(locator, paNodeBean, field), "shortTitle")));
        fragment.add(new TextField<String>("keywords", new PropertyModel(new LanguageBeanModel(locator, paNodeBean, field), "keywords")));
        fragment.add(new TextField<String>("locale", new PropertyModel(new LanguageBeanModel(locator, paNodeBean, field), "localeString")));
    }

    @Override
    public void delete(IModel<LanguageBean>[] fieldBeans)
    {
        PortletRegistry registry = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletRegistry();
        PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());
        PortletDefinition def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());
        
        for (Iterator<Language> it = def.getLanguages().iterator(); it.hasNext(); )
        {
            LanguageBean tempBean = new LanguageBean(it.next());
            
            for (IModel<LanguageBean> fieldBean : fieldBeans)
            {
                if (tempBean.getLocale().equals(fieldBean.getObject().getLocale()))
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
            logger.error("Exception occurred during updating portlet application or saving portlet definition.", e);
            StringResourceModel resModel = new StringResourceModel("pam.details.action.status.portlet.saveFailure", this, null, new Object [] { paNodeBean.getName(), e.getMessage() } );
            feed.info(resModel.getString());
        }
    }

    @Override
    public Iterator<IModel<LanguageBean>> getItemModels()
    {
        JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
        PortletApplication app = locator.getPortletRegistry().getPortletApplication(paNodeBean.getApplicationName());
        PortletDefinition def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());
        List<IModel<LanguageBean>> list = new LinkedList<IModel<LanguageBean>>();
        
        for (Language field : def.getLanguages())
        {
            list.add(new LanguageBeanModel(locator, paNodeBean, new LanguageBean(field)));
        }

        return list.iterator();
    }

    @Override
    public IModel<LanguageBean> getNewRowModel(LanguageBean fieldBean)
    {
        JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
        return new LanguageBeanModel(locator, paNodeBean, fieldBean);
    }

}
