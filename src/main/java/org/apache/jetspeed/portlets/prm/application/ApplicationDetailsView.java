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
import java.util.List;

import org.apache.jetspeed.portlets.AdminPortletWebPage;
import org.apache.jetspeed.portlets.prm.ApplicationBean;
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

public class ApplicationDetailsView extends AdminPortletWebPage
{
    private PortletApplicationNodeBean paNodeBean;
    private boolean dirty = true;
    private String title;

    public ApplicationDetailsView()
    {
        this.title = ((ApplicationDetailsApplication)getApplication()).getPortletName();
    }

    void constructLayout()
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
            
            ITab detailsTab = new AbstractTab(new ResourceModel("pam.details.tabs.pa_details"))
            {

                public Panel getPanel(String panelId)
                {
                    return new DetailsPanel(panelId, paNodeBean);
                }
            };
            tabs.add(detailsTab);

            ITab userAttributesTab = new AbstractTab(new ResourceModel("pam.details.tabs.pa_user_attribtues"))
            {

                public Panel getPanel(String panelId)
                {
                    return new UserAttributePanel(panelId, paNodeBean);
                }
            };
            tabs.add(userAttributesTab);

            ITab metadataTab = new AbstractTab(new ResourceModel("pam.details.tabs.pa_metadata"))
            {

                public Panel getPanel(String panelId)
                {
                    return new MetadataPanel(panelId, paNodeBean);
                }
            };
            tabs.add(metadataTab);

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

    @Override
    protected void onBeforeRender()
    {
        PortletApplicationNodeBean receivedPANodeBean = (ApplicationBean) 
                PortletMessaging.consume(((ApplicationDetailsApplication)getApplication()).getPortletRequest(), 
                ApplicationsListApplication.PRM_TOPIC,
                ApplicationsListApplication.SELECTED_APPLICATION_EVENT);

        if (receivedPANodeBean != null)
        {
            this.paNodeBean = receivedPANodeBean;
            this.title = ((ApplicationDetailsApplication)getApplication()).getPortletName() + " - " + paNodeBean.getApplicationName();
            this.dirty = true;
        }

        if (dirty)
        {
            constructLayout();
            dirty = false;
        }

        ((ApplicationDetailsApplication)getApplication()).setTitle(title);

        super.onBeforeRender();
    }

}
