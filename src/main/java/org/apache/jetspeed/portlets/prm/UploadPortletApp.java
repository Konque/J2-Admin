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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.jetspeed.audit.AuditActivity;
import org.apache.jetspeed.deployment.DeploymentManager;
import org.apache.jetspeed.portlets.AdminPortletWebPage;
import org.apache.jetspeed.portlets.wicket.AbstractAdminWebApplication;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * View Mode, Upload Page for Portlet Application List widget: uploads deploys portlet application
 * 
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: UploadPortletApp.java 772093 2009-05-06 08:06:42Z taylor $
 */
public class UploadPortletApp extends AdminPortletWebPage
{
    private transient DeploymentManager dm;
    private transient FileUpload fileUpload;
    private final static Logger log = LoggerFactory.getLogger(UploadPortletApp.class);

    @SuppressWarnings("serial")
    public UploadPortletApp(DeploymentManager dm)
    {
        this.dm = dm;
        FileUploadForm form = new FileUploadForm("uploadForm");
        form.add(new Button("uploadFile", new ResourceModel("pam.details.action.uploadFile"))
        {
            /*
             * (non-Javadoc)
             * @see org.apache.wicket.markup.html.form.Button#onSubmit()
             */
            @Override
            public void onSubmit()
            {
                AbstractAdminWebApplication app = ((AbstractAdminWebApplication)getApplication());
                FeedbackPanel feedback = (FeedbackPanel) UploadPortletApp.this.get("feedback");
                final FileUpload upload = fileUpload;
                if (upload != null)
                {
                    InputStream warStream = null;
                    File tempFile = null;
                    try
                    {
                        warStream = upload.getInputStream();            
                        tempFile = File.createTempFile(upload.getClientFileName(), "");                        
                        String tmpDir = System.getProperty("java.io.tmpdir");
                        tempFile = new File(tmpDir, upload.getClientFileName());
                        if (tempFile.exists())
                            tempFile.delete();
                        FileOutputStream fos = new FileOutputStream(tempFile);
                        drain(warStream, fos);
                        fos.close();
                        
                        UploadPortletApp.this.dm.deploy(tempFile);
                        feedback.info("Deployed 1 portlet application to server: " + upload.getClientFileName());
                        app.getServiceLocator().getAuditActivity().logAdminRegistryActivity(
                                app.getUserPrincipalName(), app.getIPAddress(), AuditActivity.REGISTRY_DEPLOY, ApplicationsListHome.PORTLET_REGISTRY_MANAGER);                                                    
                    }
                    catch (Exception e)
                    {
                        String msg = "Failed to upload document: " + upload.getClientFileName();
                        log.error(msg, e);
                        feedback.error(msg);
                    }
                    finally
                    {
                        if (tempFile != null)
                        {
                            tempFile.delete();
                        }
                        if (warStream != null)
                        {
                            try
                            {
                                warStream.close();
                            }
                            catch (IOException e)
                            {
                            }
                        }
                    }
                }
            }
        });
        form.add(new Button("cancelPage", new ResourceModel("pam.details.action.cancel"))
        {
            /*
             * (non-Javadoc)
             * @see org.apache.wicket.markup.html.form.Button#onSubmit()
             */
            @Override
            public void onSubmit()
            {
                setResponsePage(ApplicationsListHome.class);
            }
        });
        add(form);        
        final FeedbackPanel feedback = new FeedbackPanel("feedback");
        add(feedback);
    }

    /**
     * @return the fileUploadField
     */
    public FileUpload getFileUploadField()
    {
        return fileUpload;
    }

    /**
     * @param fileUploadField
     *            the fileUploadField to set
     */
    public void setFileUploadField(FileUpload fileUploadField)
    {
        this.fileUpload = fileUploadField;
    }

    private class FileUploadForm extends Form<Void>
    {
        private static final long serialVersionUID = 1L;

        public FileUploadForm(String name)
        {
            super(name);
            // set this form to multipart mode (allways needed for uploads!)
            setMultiPart(true);
            // Add one file input field
            add(new FileUploadField("fileInput", new PropertyModel(UploadPortletApp.this, "fileUploadField")));
        }
    }

    static final int BLOCK_SIZE=4096;
    
    protected void drain(InputStream r, OutputStream w) throws IOException
    {
        byte[] bytes = new byte[BLOCK_SIZE];
        try
        {
            int length = r.read(bytes);
            while (length != -1)
            {
                if (length != 0)
                {
                    w.write(bytes, 0, length);
                }
                length = r.read(bytes);
            }
        }
        finally
        {
            bytes = null;
        }

    }
    
  
}
