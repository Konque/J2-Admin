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

import java.util.Locale;

import org.apache.jetspeed.om.portlet.Description;
import org.apache.jetspeed.om.portlet.InitParam;
import org.apache.jetspeed.om.portlet.PortletApplication;
import org.apache.jetspeed.om.portlet.PortletDefinition;
import org.apache.jetspeed.om.portlet.SecurityRoleRef;
import org.apache.jetspeed.om.portlet.UserAttribute;
import org.apache.jetspeed.portlets.JetspeedServiceLocator;
import org.apache.jetspeed.portlets.prm.DescriptionBean;
import org.apache.jetspeed.portlets.prm.PortletApplicationNodeBean;
import org.apache.jetspeed.portlets.util.PortletApplicationUtils;
import org.apache.wicket.model.LoadableDetachableModel;

public class DescriptionBeanModel extends LoadableDetachableModel<DescriptionBean>
{
    private static final long serialVersionUID = 1L;
    
    private JetspeedServiceLocator locator;
    private PortletApplicationNodeBean paNodeBean;
    private Class type;
    private String name;
    private Locale locale;

    public DescriptionBeanModel(JetspeedServiceLocator locator, PortletApplicationNodeBean paNodeBean, Class type, String name, DescriptionBean descriptionBean)
    {
        super(descriptionBean);
        this.locator = locator;
        this.paNodeBean = paNodeBean;
        this.type = type;
        this.name = name;
        this.locale = descriptionBean.getLocale();
    }
    
    @Override
    protected DescriptionBean load()
    {
        Description description = null;
        PortletApplication app = locator.getPortletRegistry().getPortletApplication(paNodeBean.getApplicationName());
        PortletDefinition def = null;
        
        if (paNodeBean.getName() != null)
        {
            def = PortletApplicationUtils.getPortletOrClone(app, paNodeBean.getName());
        }
        
        if (def != null)
        {
            if (type == InitParam.class)
            {
                InitParam param = def.getInitParam(name);
                description = param.getDescription(locale);
            }
            else if (type == SecurityRoleRef.class)
            {
                SecurityRoleRef securityRoleRef = def.getSecurityRoleRef(name);
                description = securityRoleRef.getDescription(locale);
            }
        }
        else
        {
            UserAttribute attr = app.getUserAttribute(name);
            description = attr.getDescription(locale);
        }
        
        return new DescriptionBean(description);
    }
    
}