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

import org.apache.jetspeed.portlets.wicket.AbstractAdminWebApplication;
import org.apache.wicket.resource.loader.BundleStringResourceLoader;

/**
 * Portlet Registry Manager
 * 
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: ApplicationsListApplication.java 771734 2009-05-05 14:14:06Z woonsan $
 */
public class ApplicationsListApplication extends AbstractAdminWebApplication
{
    public static final String RESOURCE_BUNDLE_NAME = "org.apache.jetspeed.portlets.prm.resources.PRMResources";
    public static final String PRM_TOPIC = "org.apache.jetspeed.portlets.prm.Topic";
    public static final String SELECTED_APPLICATION_EVENT = "org.apache.jetspeed.portlets.prm.SelectedAppEvent";
    public static final String SELECTED_PORTLET_EVENT = "org.apache.jetspeed.portlets.prm.SelectedPortletEvent";
    
    @Override
    public Class<ApplicationsListHome> getHomePage()
    {
        return ApplicationsListHome.class;
    }

    protected void init()
    {
        mountBookmarkablePage("/edit", ApplicationsListEdit.class);
        
        getResourceSettings().addStringResourceLoader(new BundleStringResourceLoader(RESOURCE_BUNDLE_NAME));
    }
}