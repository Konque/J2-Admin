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
package org.apache.jetspeed.portlets.tracking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletRequest;

import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.aggregator.PortletTrackingManager;
import org.apache.jetspeed.container.PortletWindow;
import org.apache.jetspeed.om.portlet.PortletDefinition;
import org.apache.wicket.RequestContext;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.CheckGroupSelector;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.protocol.http.portlet.PortletRequestContext;

public class WicketPortletTrackingPortlet extends WebPage {

    private transient PortletTrackingManager trackingManager;
    private transient List<PortletDefinition> outOfService;

    public WicketPortletTrackingPortlet() {
        super();

        trackingManager = getPortletTrackingManager();
        outOfService = createList();
     
        add(new Label("header", new ResourceModel("tracker.header")));
        
        Form trackingForm = new Form("trackingForm");
        add(trackingForm);

        final CheckGroup<List> checkGroup = new CheckGroup<List>("checkGroup", new ArrayList());
        trackingForm.add(checkGroup);
        
        Button refresh = new Button("refresh", new ResourceModel("tracker.refresh")) {

            @Override
            public void onSubmit() {
                outOfService = createList();
                checkGroup.getModelObject().clear();
            }
            
        };
        trackingForm.add(refresh);
        
        Button putInService = new Button("putInService", new ResourceModel("tracker.putInService")) {

            @Override
            public void onSubmit() {
                List<String> portletNames = new ArrayList<String>();
                List selection = (List) checkGroup.getModelObject();
                for (Object object : selection) {
                    PortletDefinition pd = (PortletDefinition) object;
                    portletNames.add(pd.getUniqueName());
                }
                if (portletNames.size() > 0) {
                    trackingManager.putIntoService(portletNames);
                    outOfService = createList();
                    checkGroup.getModelObject().clear();
                }
            }
            
        };
        trackingForm.add(putInService);
        
        trackingForm.add(new Label("countLabel", new ResourceModel("tracker.count")));
        Label countValue = new Label("count", new Model(outOfService.size()));
        trackingForm.add(countValue);
        
        checkGroup.add(new Label("portletsLabel", new ResourceModel("tracker.portlets")));
        
        PageableListView portletsList = new PageableListView("portlets", new PropertyModel(this, "outOfService"), 10)
        {
            protected void populateItem(ListItem item)
            {
                final PortletDefinition portletDefinition = (PortletDefinition) item.getModelObject();
                item.add(new Check("checkbox", item.getModel()));
                item.add(new Label("name", new PropertyModel(portletDefinition, "uniqueName")));
            }
        };
        
        checkGroup.add(new CheckGroupSelector("groupSelector"));
        checkGroup.add(portletsList);
        
    }

    protected List<PortletDefinition> createList()
    {
        List<PortletDefinition> result = new ArrayList<PortletDefinition>();
        Iterator<PortletWindow> outOfService = trackingManager.getOutOfServiceList().iterator();
        Map<String,String> portlets = new HashMap<String,String>();
        while (outOfService.hasNext())
        {
            PortletWindow window = outOfService.next();
            String id = window.getId().toString();
            PortletDefinition pd = window.getPortletDefinition();
            String uniqueName = pd.getUniqueName();
            if (!portlets.containsKey(uniqueName))
            {
                portlets.put(uniqueName, id);
                result.add(pd);              
            }
        }
        return result;
    }
    
    protected PortletRequest getPortletRequest() {
        return ((PortletRequestContext) RequestContext.get()).getPortletRequest();
    }

    protected PortletTrackingManager getPortletTrackingManager() {
        return (PortletTrackingManager) getPortletRequest().getAttribute(CommonPortletServices.CPS_PORTLET_TRACKING_MANAGER);
    }

}
