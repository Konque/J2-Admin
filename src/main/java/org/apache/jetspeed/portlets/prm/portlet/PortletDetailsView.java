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
import java.util.List;

import org.apache.jetspeed.portlets.AdminPortletWebPage;
import org.apache.jetspeed.portlets.prm.ApplicationsListApplication;
import org.apache.jetspeed.portlets.prm.MetadataPanel;
import org.apache.jetspeed.portlets.prm.PortletApplicationNodeBean;
import org.apache.portals.messaging.PortletMessaging;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;

public class PortletDetailsView extends AdminPortletWebPage
{
    private String title;
    private boolean dirty = true;
    private PortletApplicationNodeBean paNodeBean;

    public PortletDetailsView()
    {
        this.title = getPortletName();
    }

    @Override
    protected void onBeforeRender()
    {
        PortletApplicationNodeBean receivedPANodeBean = (PortletApplicationNodeBean) PortletMessaging.consume(getPortletRequest(),
                ApplicationsListApplication.PRM_TOPIC, ApplicationsListApplication.SELECTED_PORTLET_EVENT);

        if (receivedPANodeBean != null)
        {
            paNodeBean = receivedPANodeBean;
            this.title = getPortletName() + " - " + paNodeBean.getName();
            this.dirty = true;
        }

        if (dirty)
        {
            constructLayout();
            dirty = false;
        }

        setTitle(title);

        super.onBeforeRender();
    }

    private void constructLayout()
    {
        if (paNodeBean == null)
        {
            removeAll();
            add(new Label("status", "No application selected"));
            add(new Label("tabs").setEnabled(false).setVisible(false));
        }
        else
        {
            addOrReplace(new Label("status", ""));

            List<ITab> tabs = new ArrayList<ITab>();
            
            ITab detailsTab = new AbstractTab(new ResourceModel("pam.details.tabs.pd_details"))
            {

                public Panel getPanel(String panelId)
                {
                    return new DetailsPanel(panelId, paNodeBean);
                }
            };
            tabs.add(detailsTab);

            ITab metadataTab = new AbstractTab(new ResourceModel("pam.details.tabs.pd_metadata"))
            {

                public Panel getPanel(String panelId)
                {
                    return new MetadataPanel(panelId, paNodeBean);
                }
            };
            tabs.add(metadataTab);

            ITab preferencesTab = new AbstractTab(new ResourceModel("pam.details.tabs.pd_preferences"))
            {

                public Panel getPanel(String panelId)
                {
                    return new PreferencePanel(panelId, paNodeBean);
                }
            };
            tabs.add(preferencesTab);
            
            ITab languagesTab = new AbstractTab(new ResourceModel("pam.details.tabs.pd_languages"))
            {
                public Panel getPanel(String panelId)
                {
                    return new LanguagesPanel(panelId, paNodeBean);
                }
            };
            tabs.add(languagesTab);

            ITab parametersTab = new AbstractTab(new ResourceModel("pam.details.tabs.pd_parameters"))
            {

                public Panel getPanel(String panelId)
                {
                    return new ParameterPanel(panelId, paNodeBean);
                }
            };
            tabs.add(parametersTab);

            ITab securityTab = new AbstractTab(new ResourceModel("pam.details.tabs.pd_security"))
            {
                public Panel getPanel(String panelId)
                {
                    return new SecurityPanel(panelId, paNodeBean);
                }
            };
            tabs.add(securityTab);
            
            ITab supportsTab = new AbstractTab(new ResourceModel("pam.details.tabs.pd_content_type"))
            {
                public Panel getPanel(String panelId)
                {
                    return new SupportsPanel(panelId, paNodeBean);
                }
            };
            tabs.add(supportsTab);
            
            TabbedPanel tabbedPanel = new TabbedPanel("tabs", tabs);
            if (get("tabs") == null)
            {
                tabbedPanel.setSelectedTab(0);
            }
            else if (get("tabs") instanceof TabbedPanel)
            {
                TabbedPanel current = (TabbedPanel) get("tabs");
                remove(current);

                if (current.getTabs() == tabbedPanel.getTabs())
                {
                    tabbedPanel.setSelectedTab(current.getSelectedTab());
                }
            }

            addOrReplace(tabbedPanel);
        }
        
        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setEscapeModelStrings(false);
        addOrReplace(feedback);

    }
}
