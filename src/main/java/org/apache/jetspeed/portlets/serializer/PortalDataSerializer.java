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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.jetspeed.portlets.AdminPortletWebPage;
import org.apache.jetspeed.portlets.wicket.component.DynamicResourceLink;
import org.apache.jetspeed.portlets.wicket.component.ResourceProvider;
import org.apache.jetspeed.serializer.JetspeedSerializer;
import org.apache.jetspeed.serializer.SerializerException;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wicket Serializer WebPage for import and export of portal data.
 * 
 * @author
 * @version $Id: PortalDataSerializer.java 788789 2009-06-26 17:48:48Z rwatler $
 */
public class PortalDataSerializer extends AdminPortletWebPage
{
    
    static final Logger logger = LoggerFactory.getLogger(PortalDataSerializer.class);
    
    private static final String USERS_GROUPS_ROLES = "usersGroupsRoles";

    private static final String PERMISSIONS = "permissions";

    private static final String PROFILING = "profiling";

    private static final String CAPABILITIES = "capabilities";

    private static final String SSO = "sso";

    private static final String USER_PREFS = "userPrefs";

    public PortalDataSerializer()
    {
        super();

        // a first form to export data
        add(new ExportForm("exportForm"));

        // a second form to import data
        add(new ImportForm("importForm"));
    }

    private class ExportForm extends Form<Object>
    {
        private static final long serialVersionUID = 0L;

        private final ExportResourceProvider exportResourceProvider;

        private boolean doUserGroupsRoles = true;

        private boolean doPermissions = true;

        private boolean doProfiling = true;

        private boolean doCapabilities = true;

        private boolean doSSO = true;

        private boolean doUserPrefs = true;

        private boolean doEntities = true;

        /* Constructor */
        public ExportForm(final String id)
        {
            super(id);

            add(new Label("exportHeader", new ResourceModel("export.header")));

            add(new ExportCheckBox(USERS_GROUPS_ROLES, new PropertyModel<Boolean>(this, "doUserGroupsRoles")));
            add(new Label("usersGroupsRolesLabel", new ResourceModel("export.users_groups_roles")));

            add(new ExportCheckBox(PERMISSIONS, new PropertyModel<Boolean>(this, "doPermissions")));
            add(new Label("permissionsLabel", new ResourceModel("export.permissions")));

            add(new ExportCheckBox(PROFILING, new PropertyModel<Boolean>(this, "doProfiling")));
            add(new Label("profilingLabel", new ResourceModel("export.profiling_rules")));

            add(new ExportCheckBox(CAPABILITIES, new PropertyModel<Boolean>(this, "doCapabilities")));
            add(new Label("capabilitiesLabel", new ResourceModel("export.capabilities")));

            add(new ExportCheckBox(SSO, new PropertyModel<Boolean>(this, "doSSO")));
            add(new Label("ssoLabel", new ResourceModel("export.sso")));

            add(new ExportCheckBox(USER_PREFS, new PropertyModel<Boolean>(this, "doUserPrefs")));
            add(new Label("userPrefsLabel", new ResourceModel("export.user_preferences")));

            this.exportResourceProvider = new ExportResourceProvider(getPortletRequest().getUserPrincipal().getName());

            DynamicResourceLink downloadLink = new DynamicResourceLink("exportLink", new PropertyModel<ExportResourceProvider>(this,
                    "exportResourceProvider"))
            {

                private static final long serialVersionUID = 0L;

                @Override
                public void onClick()
                {
                    Map<String, Object> settings = new HashMap<String, Object>();
                    settings.put(JetspeedSerializer.KEY_PROCESS_USERS, new Boolean(doUserGroupsRoles));
                    settings.put(JetspeedSerializer.KEY_PROCESS_PERMISSIONS, new Boolean(doPermissions));
                    settings.put(JetspeedSerializer.KEY_PROCESS_PROFILER, new Boolean(doProfiling));
                    settings.put(JetspeedSerializer.KEY_PROCESS_CAPABILITIES, new Boolean(doCapabilities));
                    settings.put(JetspeedSerializer.KEY_PROCESS_SSO, new Boolean(doSSO));
                    settings.put(JetspeedSerializer.KEY_PROCESS_USER_PREFERENCES, new Boolean(doUserPrefs));
                    settings.put(JetspeedSerializer.KEY_EXPORT_INDENTATION, "\t");
                    settings.put(JetspeedSerializer.KEY_OVERWRITE_EXISTING, Boolean.TRUE);
                    settings.put(JetspeedSerializer.KEY_BACKUP_BEFORE_PROCESS, Boolean.FALSE);

                    exportResourceProvider.setSettings(settings);
                }
            };
            downloadLink.add(new Label("exportAction", new ResourceModel("export.action")));
            add(downloadLink);
        };

        private class ExportCheckBox extends AjaxCheckBox
        {
            private static final long serialVersionUID = 1L;

            private ExportCheckBox(final String id, final IModel model)
            {
                super(id, model);
            }

            @Override
            protected void onUpdate(AjaxRequestTarget target)
            {
                // nothing
            }
        }

        private class ExportResourceProvider implements ResourceProvider, Serializable
        {
            private static final long serialVersionUID = 1L;

            private final String userName;

            private Map<String, Object> settings = new HashMap<String, Object>();

            private File exportFile = null;

            private String onOpenError = null;

            public ExportResourceProvider(final String userName)
            {
                super();

                this.userName = userName;
            }

            public void close()
            {
                try
                {
                    if (this.exportFile != null)
                    {
                        this.exportFile.delete();
                        this.exportFile = null;
                    }
                }
                catch (Throwable t)
                {
                    logger.error("Failed to delete export file {}: {}", this.exportFile, t.getMessage());
                }
            }

            public String getContentType()
            {

                // make browser open/save dialog appear
                return "application/octet-stream";
            }

            public long getLastModified()
            {
                return (exportFile != null) ? exportFile.lastModified() : 0;
            }

            public long getLength()
            {
                return (exportFile != null) ? exportFile.length() : 0;
            }

            public String getName()
            {
                return (exportFile != null) ? exportFile.getName() : "";
            }

            public InputStream getResource()
            {

                if (this.onOpenError != null) { return new ByteArrayInputStream(this.onOpenError.getBytes()); }

                try
                {
                    return new FileInputStream(this.exportFile);
                }
                catch (Throwable t)
                {
                    // FileNotFoundException by FileInputStream or any other
                    // unexpected condition
                    return new ByteArrayInputStream(createMessage("export.message.exception", new Object[]
                    { t.getClass().getName(), t.getMessage()}).getBytes());
                }
            }

            public void open()
            {

                try
                {
                    final File tempDir = new File(System.getProperty("java.io.tmpdir"));
                    final File userTempDir = new File(tempDir, userName);
                    if (!userTempDir.exists())
                    {
                        userTempDir.mkdirs();
                    }

                    exportFile = new File(userTempDir, "export.xml");

                    getJetspeedSerializer().exportData("jetspeedadmin_export", exportFile.getCanonicalPath(), this.settings);
                }
                catch (Throwable t)
                {
                    // SerializerException and IOException by exportData()
                    // but also SecurityException by mkdir() or any other
                    // unexpected condition
                    this.onOpenError = createMessage("export.message.exception", new Object[]
                    { t.getClass().getName(), t.getMessage()});
                }
            }

            public void setSettings(Map<String, Object> settings)
            {
                this.settings = settings;
            }
        }
    }

    private class ImportForm extends Form<Object>
    {
        private static final long serialVersionUID = 1L;

        private final Map<String, Boolean> settings = new HashMap<String, Boolean>();
        {
            settings.put(JetspeedSerializer.KEY_PROCESS_USERS, Boolean.TRUE);
            settings.put(JetspeedSerializer.KEY_PROCESS_CAPABILITIES, Boolean.TRUE);
            settings.put(JetspeedSerializer.KEY_PROCESS_PROFILER, Boolean.TRUE);
            settings.put(JetspeedSerializer.KEY_PROCESS_USER_PREFERENCES, Boolean.TRUE);
            settings.put(JetspeedSerializer.KEY_PROCESS_PORTAL_PREFERENCES, Boolean.TRUE);
            settings.put(JetspeedSerializer.KEY_OVERWRITE_EXISTING, Boolean.TRUE);
            settings.put(JetspeedSerializer.KEY_BACKUP_BEFORE_PROCESS, Boolean.FALSE);
        }

        private String errorMessage;

        private String infoMessage;

        private String fileName;

        /* Constructor */
        private ImportForm(final String id)
        {

            super(id);

            setMultiPart(true);

            add(new Label("importHeader", new ResourceModel("import.header")));

            add(new FileUploadField("importFile", new PropertyModel(this, "fileName")));
            add(new Label("importFileLabel", new ResourceModel("import.choose.file")));

            add(new Label("importText", new ResourceModel("import.text")));
            add(new Button("importAction", new ResourceModel("import.action")));

            add((new Label("importErrorMessage", new PropertyModel(this, "errorMessage"))
            {

                @Override
                public boolean isVisible()
                {
                    return (errorMessage != null);
                }
            }).setVisibilityAllowed(true));

            add((new Label("importInfoMessage", new PropertyModel(this, "infoMessage"))
            {

                @Override
                public boolean isVisible()
                {
                    return (infoMessage != null);
                }
            }).setVisibilityAllowed(true));
        }

        @Override
        public void onSubmit()
        {
            this.clearMessages();

            File tempDir = new File(System.getProperty("java.io.tmpdir"));
            File userTempDir = new File(tempDir, getPortletRequest().getUserPrincipal().getName());

            final FileUpload upload = ((FileUploadField) this.get("importFile")).getFileUpload();

            if (upload == null)
            {
                this.errorMessage = createMessage("import.message.nofile", null);
                return;
            }

            synchronized (this)
            {
                try
                {
                    if (!userTempDir.isDirectory())
                    {
                        userTempDir.mkdirs();
                    }

                    final File importFile = new File(userTempDir, upload.getClientFileName());
                    this.fileName = importFile.getCanonicalPath();

                    if (importFile.exists())
                    {
                        importFile.delete();
                    }

                    upload.writeTo(importFile);
                }
                catch (IOException ioe)
                {
                    this.errorMessage = createMessage("import.message.exception", new Object[]
                    { upload.getClientFileName(), ioe.getMessage()});
                }

                try
                {
                    getJetspeedSerializer().importData(this.fileName, this.settings);

                    this.infoMessage = createMessage("import.message.success", new Object[]
                    { this.fileName});
                }
                catch (SerializerException se)
                {
                    this.errorMessage = createMessage("import.message.exception", new Object[]
                    { upload.getClientFileName(), se.getMessage()});
                }
            }
        }

        private void clearMessages()
        {
            this.errorMessage = null;
            this.infoMessage = null;
        }
    }

    private String createMessage(String resourceKey, Object[] args)
    {
        String message = getLocalizer().getString(resourceKey, (Component) null);

        if (args != null)
        {
            // apply the arguments
            final MessageFormat format = new MessageFormat(message, getLocale());
            message = format.format(args);
        }

        return message;
    }

    private JetspeedSerializer getJetspeedSerializer()
    {
        return getServiceLocator().getJetspeedSerializer();
    }
}
