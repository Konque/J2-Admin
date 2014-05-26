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
package org.apache.jetspeed.portlets.prm.model;

import org.apache.jetspeed.om.portlet.PortletApplication;
import org.apache.jetspeed.om.portlet.PortletDefinition;
import org.apache.jetspeed.om.portlet.Supports;
import org.apache.jetspeed.portlets.JetspeedServiceLocator;
import org.apache.jetspeed.portlets.prm.PortletApplicationNodeBean;
import org.apache.jetspeed.portlets.util.PortletApplicationUtils;
import org.apache.wicket.model.LoadableDetachableModel;

public class SupportsModel extends LoadableDetachableModel<Supports>
{
    
    private static final long serialVersionUID = 1L;
    
    private JetspeedServiceLocator locator;
    private PortletApplicationNodeBean paNodeBean;
    private String mimeType;

    public SupportsModel(JetspeedServiceLocator locator, PortletApplicationNodeBean paNodeBean, Supports supports)
    {
        super(supports);
        this.locator = locator;
        this.paNodeBean = paNodeBean;
        this.mimeType = supports.getMimeType();
    }
    
    @Override
    protected Supports load()
    {
        PortletApplication app = locator.getPortletRegistry().getPortletApplication(paNodeBean.getApplicationName());
        PortletDefinition def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());
        Supports supports = def.getSupports(mimeType);
        return supports;
    }
    
}