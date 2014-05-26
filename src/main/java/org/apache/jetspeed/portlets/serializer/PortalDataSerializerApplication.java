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
package org.apache.jetspeed.portlets.serializer;

import org.apache.jetspeed.portlets.wicket.AbstractAdminWebApplication;
import org.apache.wicket.resource.loader.BundleStringResourceLoader;

/**
 * Data Serializer Wicket Application
 * 
 * @author 
 * @version $Id: PortalDataSerializerApplication.java 764098 2009-04-10 23:20:57Z taylor $
 */
public class PortalDataSerializerApplication extends AbstractAdminWebApplication
{

	protected void init()
	{
        super.init();
        
        getResourceSettings().addStringResourceLoader(
        		new BundleStringResourceLoader("org.apache.jetspeed.portlets.serializer.resources.PortalDataSerializer"));
	}
	
	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	public Class getHomePage()
	{
		return PortalDataSerializer.class;
	}
}