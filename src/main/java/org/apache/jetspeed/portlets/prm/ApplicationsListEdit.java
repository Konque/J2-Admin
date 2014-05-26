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

import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;

import org.apache.jetspeed.portlets.AdminPortletWebPage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.validator.RangeValidator;

/**
 * Edit Mode for Portlet Application List widget
 * 
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: ApplicationsListEdit.java 772093 2009-05-06 08:06:42Z taylor $
 */
public class ApplicationsListEdit extends AdminPortletWebPage
{
    public ApplicationsListEdit()
    {
        WebMarkupContainer formGroup = new WebMarkupContainer("formGroup");
        add(formGroup);
        int appRows = ((ApplicationsListApplication)this.getApplication()).getPreferenceValueAsInteger("appRows");
        int portletRows = ((ApplicationsListApplication)this.getApplication()).getPreferenceValueAsInteger("portletRows");
        EditModeForm form = new EditModeForm("editModeForm", appRows, portletRows);        
        formGroup.add(form);
    }
    
    @SuppressWarnings("serial")
    private class EditModeForm extends Form<Void>
    {
        private int appRows = 8;
        private int portletRows = 8;
        
        @SuppressWarnings("unchecked")
        public EditModeForm(String name, final int appRows, final int portletRows)
        {
            super(name);
            if (appRows > 0)
                this.appRows = appRows;
            if (portletRows > 0)
                this.portletRows = portletRows;
            add(new TextField("appRows", new PropertyModel(this, "appRows"), Integer.class).setRequired(true).add(new RangeValidator(1,1000)));
            add(new TextField("portletRows", new PropertyModel(this, "portletRows"), Integer.class).setRequired(true).add(new RangeValidator(1,1000)));
            add(new Button("editModeSaveButton", new ResourceModel("pam.details.action.save"))
            {
                @Override
                public void onSubmit()
                {
                    PortletPreferences prefs = ((ApplicationsListApplication)this.getApplication()).getPortletRequest().getPreferences();
                    FeedbackPanel feedback = (FeedbackPanel)this.getParent().get("feedback");
                    try
                    {
                        prefs.setValue("appRows", Integer.toString(EditModeForm.this.getAppRows()));
                        prefs.setValue("portletRows", Integer.toString(EditModeForm.this.getPortletRows()));
                        prefs.store();
                        ActionResponse ar = (ActionResponse)((ApplicationsListApplication)this.getApplication()).getPortletResponse();
                        ar.setPortletMode(PortletMode.VIEW);                        
                    }
                    catch (ReadOnlyException e)
                    {
                        feedback.error(getString("pam.details.message.errorReadonly"));
                    }
                    catch (Exception oe)
                    {
                        feedback.error(getString("pam.details.message.errorPrefs") + oe.getMessage()); 
                    }                    
                }
            });        
            FeedbackPanel feedback = new FeedbackPanel("feedback");
            add(feedback);
        }
                
        public int getAppRows()
        {
            return appRows;
        }
        
        public void setAppRows(int appRows)
        {
            this.appRows = appRows;
        }

        public int getPortletRows()
        {
            return portletRows;
        }
        
        public void setPortletRows(int portletRows)
        {
            this.portletRows = portletRows;
        }
        
    }     
    
    
}
