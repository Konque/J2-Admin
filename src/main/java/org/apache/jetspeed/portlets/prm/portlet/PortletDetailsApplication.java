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

import org.apache.jetspeed.portlets.wicket.AbstractAdminWebApplication;
import org.apache.wicket.resource.loader.BundleStringResourceLoader;

public class PortletDetailsApplication extends AbstractAdminWebApplication
{
    public static final String RESOURCE_BUNDLE_NAME = "org.apache.jetspeed.portlets.prm.resources.PRMResources";
    public static final String SELECTED_PORTLET_EVENT = "org.apache.jetspeed.portlets.prm.portlets.details.SelectedPortletEvent";

    @Override
    public Class<PortletDetailsView> getHomePage()
    {
        return PortletDetailsView.class;
    }

    protected void init()
    {
        // mountBookmarkablePage("/edit", ApplicationsListEdit.class);
        
        getResourceSettings().addStringResourceLoader(new BundleStringResourceLoader(RESOURCE_BUNDLE_NAME));
    }

}
