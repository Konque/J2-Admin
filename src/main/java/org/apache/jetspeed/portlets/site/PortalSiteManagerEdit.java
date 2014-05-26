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
package org.apache.jetspeed.portlets.site;

import java.io.IOException;

import javax.portlet.PortletRequest;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import org.apache.jetspeed.portlets.AdminPortletWebPage;
import org.apache.jetspeed.portlets.wicket.AbstractAdminWebApplication;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:vkumar@apache.org">Vivek Kumar</a>
 * @version $Id$
 */
public class PortalSiteManagerEdit extends AdminPortletWebPage
{
    private final Logger logger = LoggerFactory.getLogger(PortalSiteManagerEdit.class);
    private String treeRoot;

    public PortalSiteManagerEdit()
    {
        super();
        PortletRequest request = ((AbstractAdminWebApplication) getApplication()).getPortletRequest();
        treeRoot = request.getPreferences().getValue(PortalSiteManager.TREE_ROOT, "/");
        add(new FeedbackPanel("feedback"));
        Form userPrefernces = new Form("userPrefernces");
        userPrefernces.add(new Label("treerootLabel", new ResourceModel("treeroot")));
        userPrefernces.add(new TextField("treeroot", new PropertyModel(this, "treeRoot")).setRequired(true));
        userPrefernces.add(new Button("addUserPrefernces", new ResourceModel("common.save"))
        {
            @Override
            public void onSubmit()
            {
                PortletRequest request = ((AbstractAdminWebApplication) getApplication()).getPortletRequest();
                try
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Setting tree root " + getTreeRoot());
                    }
                    request.getPreferences().setValue(PortalSiteManager.TREE_ROOT, getTreeRoot());
                    request.getPreferences().store();
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Default settiing for portlet saved . ");
                    }
                }
                catch (ValidatorException e)
                {
                    if (logger.isErrorEnabled())
                    {
                        logger.error(e.getMessage());
                    }
                    error(e.getMessage());
                }
                catch (IOException e)
                {
                    if (logger.isErrorEnabled())
                    {
                        logger.error(e.getMessage());
                    }
                    error(e.getMessage());
                }
                catch (ReadOnlyException e)
                {
                    if (logger.isErrorEnabled())
                    {
                        logger.error(e.getMessage());
                    }
                    error(e.getMessage());
                }
            }
        });
        add(userPrefernces);
    }
    /**
     * @return the treeRoot
     */
    public String getTreeRoot()
    {
        return treeRoot;
    }

    /**
     * @param treeRoot
     *            the treeRoot to set
     */
    public void setTreeRoot(String treeRoot)
    {
        this.treeRoot = treeRoot;
    }
}
