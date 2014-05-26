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
import java.util.LinkedList;
import java.util.List;

import javax.portlet.PortletMode;

import org.apache.jetspeed.components.portletregistry.PortletRegistry;
import org.apache.jetspeed.components.portletregistry.RegistryException;
import org.apache.jetspeed.om.portlet.PortletApplication;
import org.apache.jetspeed.om.portlet.PortletDefinition;
import org.apache.jetspeed.om.portlet.Supports;
import org.apache.jetspeed.portlets.JetspeedServiceLocator;
import org.apache.jetspeed.portlets.prm.PortletApplicationNodeBean;
import org.apache.jetspeed.portlets.prm.model.SupportsModel;
import org.apache.jetspeed.portlets.util.PortletApplicationUtils;
import org.apache.jetspeed.portlets.wicket.AbstractAdminWebApplication;
import org.apache.jetspeed.portlets.wicket.component.editor.EditorTemplate;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SupportsPanel extends EditorTemplate<Supports>
{
    private static final long serialVersionUID = 1L;
    
    static final Logger logger = LoggerFactory.getLogger(SupportsPanel.class);

    private PortletApplicationNodeBean paNodeBean;
    private String newMimeType;
    private List<String> newPortletModes;
    private List<String> availablePortletModes;

    public SupportsPanel(String id, final PortletApplicationNodeBean paNodeBean)
    {
        super(id);
        
        this.paNodeBean = paNodeBean;
        newPortletModes = new ArrayList<String>();
        availablePortletModes = new ArrayList<String>();
        
        PortletRegistry registry = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletRegistry();
        PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());
        
        for (PortletMode mode : app.getSupportedPortletModes())
        {
            availablePortletModes.add(mode.toString());
        }
        
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
                    
                    if (newMimeType != null && !newPortletModes.isEmpty())
                    {
                        Supports supports = def.addSupports(newMimeType);
                        
                        for (String portletMode : newPortletModes)
                        {
                            supports.addPortletMode(portletMode);
                        }
                            
                        newMimeType = null;
                        newPortletModes.clear();
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
        fragment.add(new TextField<String>("newMimeType", new PropertyModel<String>(this, "newMimeType")));
        fragment.add(new CheckBoxMultipleChoice("newPortletModes", new PropertyModel(this, "newPortletModes"), availablePortletModes));
    }

    @Override
    public int getColumnCount()
    {
        return 4;
    }

    @Override
    public void buildItems(Fragment fragment, final Supports field)
    {
        JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
        fragment.add(new TextField<String>("mimeType", new PropertyModel(new SupportsModel(locator, paNodeBean, field), "mimeType")));
        fragment.add(new CheckBoxMultipleChoice("portletModes", new PropertyModel(new SupportsModel(locator, paNodeBean, field), "portletModes"), availablePortletModes));
    }

    @Override
    public void delete(IModel<Supports>[] fieldBeans)
    {
        PortletRegistry registry = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPortletRegistry();
        PortletApplication app = registry.getPortletApplication(paNodeBean.getApplicationName());
        PortletDefinition def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());
        
        for (Iterator<Supports> it = def.getSupports().iterator(); it.hasNext(); )
        {
            Supports tempSupports = it.next();
            
            for (IModel<Supports> fieldBean : fieldBeans)
            {
                if (tempSupports.getMimeType().equals(fieldBean.getObject().getMimeType()))
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
    public Iterator<IModel<Supports>> getItemModels()
    {
        JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
        PortletApplication app = locator.getPortletRegistry().getPortletApplication(paNodeBean.getApplicationName());
        PortletDefinition def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());
        List<IModel<Supports>> list = new LinkedList<IModel<Supports>>();
        
        for (Supports field : def.getSupports())
        {
            list.add(new SupportsModel(locator, paNodeBean, field));
        }

        return list.iterator();
    }

    @Override
    public IModel<Supports> getNewRowModel(Supports fieldBean)
    {
        JetspeedServiceLocator locator = ((AbstractAdminWebApplication) getApplication()).getServiceLocator();
        return new SupportsModel(locator, paNodeBean, fieldBean);
    }

}
