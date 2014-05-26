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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.portlet.PortletRequest;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.PortalReservedParameters;
import org.apache.jetspeed.components.portletregistry.PortletRegistry;
import org.apache.jetspeed.decoration.DecorationFactory;
import org.apache.jetspeed.exception.JetspeedException;
import org.apache.jetspeed.om.common.SecurityConstraints;
import org.apache.jetspeed.om.folder.Folder;
import org.apache.jetspeed.om.folder.FolderNotFoundException;
import org.apache.jetspeed.om.folder.InvalidFolderException;
import org.apache.jetspeed.om.folder.MenuDefinition;
import org.apache.jetspeed.om.folder.MenuExcludeDefinition;
import org.apache.jetspeed.om.folder.MenuIncludeDefinition;
import org.apache.jetspeed.om.folder.MenuOptionsDefinition;
import org.apache.jetspeed.om.folder.MenuSeparatorDefinition;
import org.apache.jetspeed.om.page.DynamicPage;
import org.apache.jetspeed.om.page.Fragment;
import org.apache.jetspeed.om.page.FragmentDefinition;
import org.apache.jetspeed.om.page.Link;
import org.apache.jetspeed.om.page.Page;
import org.apache.jetspeed.om.page.PageTemplate;
import org.apache.jetspeed.om.page.SecurityConstraintsDef;
import org.apache.jetspeed.om.portlet.LocalizedField;
import org.apache.jetspeed.page.FolderNotRemovedException;
import org.apache.jetspeed.page.FolderNotUpdatedException;
import org.apache.jetspeed.page.PageManager;
import org.apache.jetspeed.page.PageNotFoundException;
import org.apache.jetspeed.page.document.DocumentNotFoundException;
import org.apache.jetspeed.page.document.Node;
import org.apache.jetspeed.page.document.NodeException;
import org.apache.jetspeed.page.document.UnsupportedDocumentTypeException;
import org.apache.jetspeed.portalsite.MenuElement;
import org.apache.jetspeed.portlets.AdminPortletWebPage;
import org.apache.jetspeed.portlets.site.SiteTreeNode.FileType;
import org.apache.jetspeed.portlets.site.model.ExcludesDefinitionBean;
import org.apache.jetspeed.portlets.site.model.IncludesDefinitionBean;
import org.apache.jetspeed.portlets.site.model.JetspeedMenuDefinition;
import org.apache.jetspeed.portlets.site.model.OptionsDefinitionBean;
import org.apache.jetspeed.portlets.site.model.SeparatorDefinitionBean;
import org.apache.jetspeed.portlets.wicket.AbstractAdminWebApplication;
import org.apache.jetspeed.portlets.wicket.component.DynamicResourceLink;
import org.apache.jetspeed.portlets.wicket.component.JavascriptEventConfirmation;
import org.apache.jetspeed.portlets.wicket.component.ResourceProvider;
import org.apache.jetspeed.request.RequestContext;
import org.apache.jetspeed.security.SecurityException;
import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxPreprocessingCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.markup.html.tree.LinkIconPanel;
import org.apache.wicket.markup.html.tree.LinkTree;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.validation.validator.StringValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the Portal Site
 * 
 * @author <a href="mailto:vkumar@apache.org">Vivek Kumar</a>
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: PortalSiteManager.java 1178649 2011-10-04 00:40:14Z ate $
 */
public class PortalSiteManager extends AdminPortletWebPage
{
    
    public static final String PORTAL_SITE_MANAGER_INIT_NODE_TYPE_PARAM = "_jssmnodetype";
    
    public static final String PORTAL_SITE_MANAGER_INIT_NODE_PATH_PARAM = "_jssmnodepath";

    protected static final Logger log = LoggerFactory.getLogger(PortalSiteManager.class);
    // components
    protected PortletRegistry registry;

    protected DecorationFactory decorationFactory;

    public final static String TREE_ROOT = "jsRoot";

    private final static String JETSPEED_PATH_SEPRATOR = "/";

    private DefaultTreeModel treeModel;

    private AjaxTabbedPanel tabPanel;

    private DefaultTreeModel treeRoot;

    private JetspeedDocument document;

    private MenuDefinition menuDefinition;

    private List<String> pageDecorators;

    private List<String> portletDecorators;

    private List<String> pageThemes;

    private List<String> constraintsDefs;

    private List<String> targetList;

    private final static String ADD_ACTION = "add";

    private final static String REMOVE_ACTION = "remove";

    private final static String SAVE_ACTION = "save";

    private final static String INCLUDES = "includes";

    private final static String EXCLUDES = "excludes";

    public final static String FOLDER_NODE_TYPE = "folder";

    public final static String PAGE_NODE_TYPE = "page";
    
    public final static String LINK_NODE_TYPE = "link";
    List<ITab> menuTabs = new ArrayList<ITab>();

    private List menuOptions = new ArrayList();

    String userFolder;

    private DefaultTreeModel menuTreeRoot;

    private LinkTree menuTree;

    public PortalSiteManager()
    {
        super();
        List<ITab> tabList = new ArrayList<ITab>();
        DefaultMutableTreeNode rootNode = populateTree();
        populateDocument(getInitSiteTreeNode());
        PortalTree siteTree = new PortalTree("siteTree", new PropertyModel(
                this, "treeRoot"));
        siteTree.getTreeState().expandNode(rootNode);
        tabPanel = new AjaxTabbedPanel("tabs", tabList);
        tabPanel.setOutputMarkupId(true);
        add(siteTree);
        Form treeForm = new Form("treeForm");
        treeForm.add(new AutoCompleteTextField<String>("userFolder",
                new PropertyModel(this, "userFolder"))
        {

            @Override
            protected Iterator<String> getChoices(String input)
            {
                if (Strings.isEmpty(input) || input.length() < 1)
                {
                    List<String> emptyList = Collections.emptyList();
                    return emptyList.iterator();
                }
                List<String> choices = new ArrayList<String>(10);
                List<String> userNames = null;
                try
                {
                    userNames = getServiceLocator().getUserManager()
                            .getUserNames(input);
                }
                catch (SecurityException e)
                {
                    log.error("Failed to retrieve user names.", e);
                }
                if (userNames == null)
                {
                    List<String> emptyList = Collections.emptyList();
                    return emptyList.iterator();
                }
                if (userNames.size() > 10)
                {
                    return userNames.subList(0, 10).iterator();
                } else
                {
                    return userNames.iterator();
                }
            }
        }.setRequired(true));
        treeForm.add(new Button("userFolderButton")
        {

            @Override
            public void onSubmit()
            {
                ((LinkTree) getPage().get("siteTree")).getTreeState()
                        .expandNode(populateUserTree(userFolder));
            }
        });
        treeForm.add(new Button("portalFolderButton")
        {

            @Override
            public void onSubmit()
            {
                setUserFolder("");
                ((LinkTree) getPage().get("siteTree")).getTreeState()
                        .expandNode(populateTree());
            }
        });
        add(new FeedbackPanel("feedback"));
        add(treeForm);
        add(tabPanel);
        controlTabs();
        
        setVisibilitiesOfChildComponentsByPreferences("component.visibility.", true);
    }

    private class PortalTree extends LinkTree
    {

        private static final long serialVersionUID = 3315834315652490831L;

        public PortalTree(String id, IModel model)
        {
            super(id, model);
        }

        @Override
        protected Component newNodeComponent(String id, IModel model)
        {
            final IModel nodeModel = model;
            return new LinkIconPanel(id, model, PortalTree.this)
            {

                private static final long serialVersionUID = 1L;

                String path = "images";

                @Override
                protected Component newContentComponent(String componentId,
                        BaseTree tree, IModel model)
                {
                    return new Label(componentId, getNodeTextModel(model));
                }

                @Override
                protected void onNodeLinkClicked(Object node, BaseTree tree,
                        AjaxRequestTarget target)
                {
                    DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
                    SiteTreeNode siteNode = (SiteTreeNode) treeNode.getUserObject();
                    
                    populateDocument(siteNode);
                    
                    if (siteNode.getDocType() == SiteTreeNode.FileType.Folder && !siteNode.isLoaded())
                    {
                        retrieveFolders(siteNode.getNodePath(), treeNode);
                        siteNode.setLoaded(true);
                    }
                    
                    if (!tree.getTreeState().isNodeExpanded(treeNode))
                    {
                        tree.getTreeState().expandNode(treeNode);
                    }
                    else
                    {
                        tree.getTreeState().collapseNode(treeNode);
                    }
                    
                    controlTabs(siteNode);
                    
                    target.addComponent(tabPanel);
                    
                    tree.getTreeState().selectNode(node, true);
                    tree.updateTree(target);
                }

                @Override
                protected Component newImageComponent(String componentId,
                        BaseTree tree, IModel<Object> model)
                {
                    return new Image(componentId)
                    {

                        private static final long serialVersionUID = 1L;

                        @Override
                        protected ResourceReference getImageResourceReference()
                        {
                            SiteTreeNode siteModel = (SiteTreeNode) ((DefaultMutableTreeNode) nodeModel
                                    .getObject()).getUserObject();
                            if (siteModel.getDocType() == SiteTreeNode.FileType.Folder)
                            {
                                return new ResourceReference(PortalTree.class,
                                        path + "/folder.gif");
                            } else if (siteModel.getDocType() == SiteTreeNode.FileType.Page)
                            {
                                return new ResourceReference(PortalTree.class,
                                        path + "/page.gif");
                            } else if (siteModel.getDocType() == SiteTreeNode.FileType.Link) { return new ResourceReference(
                                    PortalTree.class, path + "/link.gif"); }
                            return new ResourceReference(PortalTree.class, path
                                    + "/folder_closed.gif");
                        }
                    };
                }
            };
        }
    }

    protected class InformationTab extends BasePanel
    {

        private static final long serialVersionUID = 3039163446419682350L;

        public InformationTab(String id, JetspeedDocument document)
        {
            super(id, document);
            Form infoForm = new Form("infoForm");
            Label nameLabel = new Label("name",
                    new ResourceModel("common.name"));
            TextField nameField = new TextField("nameField", new PropertyModel(
                    this, "document.name"));
            infoForm.add(nameLabel);
            infoForm.add(nameField);
            Label titleLabel = new Label("title", new ResourceModel("Title"));
            TextField titleField = new TextField("titleField",
                    new PropertyModel(this, "document.title"));
            infoForm.add(titleLabel);
            infoForm.add(titleField);
            Label shortTitleLabel = new Label("shortTitle", new ResourceModel(
                    "ShortTitle"));
            TextField shortTitleField = new TextField("shortTitleField",
                    new PropertyModel(this, "document.shortTitle"));
            infoForm.add(shortTitleField);
            infoForm.add(shortTitleLabel);
            Label pageDecoratorLabel = new Label("pageDecorator",
                    "Page Decorator");
            DropDownChoice decoratorsList = new DropDownChoice(
                    "decoratorsList", new PropertyModel(this,
                            "document.pageDecorator"), getPageDecorators());
            infoForm.add(decoratorsList);
            infoForm.add(pageDecoratorLabel);
            Label portletDecoratorLabel = new Label("portletDecorator",
                    "Portlet Decorator");
            DropDownChoice portletDecoratorsList = new DropDownChoice(
                    "portletDecoratorsList", new PropertyModel(this,
                            "document.portletDecorator"),
                    getPortletDecorators());
            infoForm.add(portletDecoratorLabel);
            infoForm.add(portletDecoratorsList);
            Label themeLabel = new Label("theme", "Theme");
            DropDownChoice themeField = new DropDownChoice("themeList",
                    new PropertyModel(this, "document.desktopTheme"),
                    getThemes());
            themeField.setVisibilityAllowed(true);
            themeLabel.setVisibilityAllowed(true);
            infoForm.add(themeField);
            infoForm.add(themeLabel);
            Label visibleLabel = new Label("visible", new ResourceModel(
                    "Hidden"));
            CheckBox visibleCheckbox = new CheckBox("visibleCheck",
                    new PropertyModel(this, "document.hidden"));
            infoForm.add(visibleLabel);
            infoForm.add(visibleCheckbox);
            Label urlLabel = new Label("urlLabel", new ResourceModel("Url"));
            TextField urlField = new TextField("urlField", new PropertyModel(
                    this, "document.url"));
            infoForm.add(urlLabel);
            infoForm.add(urlField);
            urlLabel.setVisibilityAllowed(true);
            urlField.setVisibilityAllowed(true);
            Label targetLabel = new Label("targetLabel", "Target Window");
            DropDownChoice targetField = new DropDownChoice("target",
                    new PropertyModel(this, "document.target"), getTargetList());
            infoForm.add(targetLabel);
            infoForm.add(targetField);
            targetLabel.setVisibilityAllowed(true);
            targetField.setVisibilityAllowed(true);
            Label defaultPageLabel = new Label("defaultPage",
                    new ResourceModel("DefaultPage"));
            TextField defaultPageField = new TextField("defaultPageField",
                    new PropertyModel(this, "document.page"));
            infoForm.add(defaultPageLabel);
            infoForm.add(defaultPageField);
            defaultPageLabel.setVisibilityAllowed(true);
            defaultPageField.setVisibilityAllowed(true);
            final ModalWindow metaDataModalWindow;
            add(metaDataModalWindow = new ModalWindow("modalwindow"));
            
            AjaxButton addFolder = new AjaxButton("addFolder",new ResourceModel("add.subfolder"),infoForm)
            {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form)
                {
                    metaDataModalWindow.setContent(new AddFolderPanel(metaDataModalWindow.getContentId()));
                    metaDataModalWindow.show(target);
                }
            };
            infoForm.add(addFolder);
            
            AjaxButton addPage = new AjaxButton("addPage",new ResourceModel("add.page"),infoForm)
            {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form)
                {
                    metaDataModalWindow.setContent(new AddPagePanel(
                            metaDataModalWindow.getContentId()));
                    metaDataModalWindow.show(target);
                }
            };
            infoForm.add(addPage);
            
            AjaxButton addLink = new AjaxButton("addLink",new ResourceModel("add.link"),infoForm)
            {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form)
                {
                    metaDataModalWindow.setContent(new AddLinkPanel(metaDataModalWindow.getContentId()));
                    metaDataModalWindow.show(target);
                }
            };
            infoForm.add(addLink);
            
            addFolder.setVisibilityAllowed(true);
            addPage.setVisibilityAllowed(true);
            addLink.setVisibilityAllowed(true);
            SiteTreeNode node = getUserSelectedNode();
            
            if (node.getDocType() == SiteTreeNode.FileType.Folder)
            {
                urlLabel.setVisible(false);
                urlField.setVisible(false);
                targetLabel.setVisible(false);
                targetField.setVisible(false);
                addFolder.setVisible(true);
                addPage.setVisible(true);
                addLink.setVisible(true);
            }
            else if (node.getDocType() == SiteTreeNode.FileType.Page)
            {
                defaultPageLabel.setVisible(false);
                defaultPageField.setVisible(false);
                urlLabel.setVisible(false);
                urlField.setVisible(false);
                targetLabel.setVisible(false);
                targetField.setVisible(false);
                addFolder.setVisible(false);
                addPage.setVisible(false);
                addLink.setVisible(false);
            }
            else if (node.getDocType() == SiteTreeNode.FileType.Link)
            {
                defaultPageLabel.setVisible(false);
                defaultPageField.setVisible(false);
                targetLabel.setVisible(true);
                defaultPageField.setVisible(false);
                pageDecoratorLabel.setVisible(false);
                decoratorsList.setVisible(false);
                portletDecoratorLabel.setVisible(false);
                portletDecoratorsList.setVisible(false);
                themeField.setVisible(false);
                themeLabel.setVisible(false);
                addFolder.setVisible(false);
                addPage.setVisible(false);
                addLink.setVisible(false);
            }
            
            if (node.getDocType() == SiteTreeNode.FileType.Link)
            {
                ExternalLink viewLink = new ExternalLink("view", new Model(getDocument().getUrl()), new ResourceModel("common.view"))
                {
                    @Override
                    protected void onComponentTag(ComponentTag tag)
                    {
                        super.onComponentTag(tag);
                        
                        if (tag.getName().equalsIgnoreCase("input") && ((getLabel() != null) && (getLabel().getObject() != null)))
                        {
                            tag.put("value", getDefaultModelObjectAsString(getLabel().getObject()));
                        }
                    }
                };
                viewLink.setPopupSettings(new PopupSettings());
                infoForm.add(viewLink);
            }
            else
            {
                RequestContext context = (RequestContext) getPortletRequest().getAttribute(PortalReservedParameters.REQUEST_CONTEXT_ATTRIBUTE);
                String basePath = context.getPortalURL().getBasePath().replace("portal", "configure");
                ExternalLink viewLink = new ExternalLink("view", new Model(basePath + getDocument().getPath()), new ResourceModel("common.view"))
                {
                    @Override
                    protected void onComponentTag(ComponentTag tag)
                    {
                        super.onComponentTag(tag);
                        
                        if (tag.getName().equalsIgnoreCase("input") && ((getLabel() != null) && (getLabel().getObject() != null)))
                        {
                            tag.put("value", getDefaultModelObjectAsString(getLabel().getObject()));
                        }
                    }
                };
                viewLink.setPopupSettings(new PopupSettings());
                infoForm.add(viewLink);
            }

            infoForm.add(new AjaxButton("remove",new ResourceModel("common.remove"),infoForm)
            {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form form)
                {
                    excuteAction(getDocument(), REMOVE_ACTION);
                    DefaultMutableTreeNode node = getSelectedNode();
                    if (node != null && node.getParent() != null)
                    {
                        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
                        parentNode.remove(node);
                        LinkTree tree = (PortalTree) getPage().get("siteTree");
                        tree.updateTree(target);
                    }
                }
            }.add(new JavascriptEventConfirmation("onclick", new ResourceModel("action.delete.confirm"))));
            infoForm.add(new AjaxButton("save",new ResourceModel("common.save"), infoForm)
            {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form form)
                {
                    excuteAction(getDocument(), SAVE_ACTION);
                }
            });
            infoForm.add(new AjaxButton("copy",new ResourceModel("common.copy"),infoForm)
            {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form)
                {
                    SiteTreeNode selectedNode = getUserSelectedNode();
                    metaDataModalWindow.setContent(new CopyMoveWindow(
                            metaDataModalWindow.getContentId(), selectedNode
                                    .getDocType().name(), selectedNode
                                    .getNodeName(), getUserSelectedNode()
                                    .getNodePath(), true));
                    metaDataModalWindow.show(target);
                }
            });
            infoForm.add(new AjaxButton("move",new ResourceModel("common.move"),infoForm)
            {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form)
                {
                    SiteTreeNode selectedNode = getUserSelectedNode();
                    metaDataModalWindow.setContent(new CopyMoveWindow(
                            metaDataModalWindow.getContentId(), selectedNode
                                    .getDocType().name(), selectedNode
                                    .getNodeName(), getUserSelectedNode()
                                    .getNodePath(), false));
                    metaDataModalWindow.show(target);
                }
            });
            add(infoForm);
        }
    }

    protected class SecurityTab extends BasePanel
    {

        private static final long serialVersionUID = 7948533482848224251L;

        public SecurityTab(String id, JetspeedDocument document)
        {
            super(id, document);
            final ModalWindow metaDataModalWindow;
            add(metaDataModalWindow = new ModalWindow("modalwindow"));
            final WebMarkupContainer ajaxPanel = new WebMarkupContainer(
                    "basePanel");
            ajaxPanel.setOutputMarkupId(true);
            ajaxPanel.add(new Label("constraintLabel",new ResourceModel("security.constraint.title")));
            ajaxPanel.add(new Label("constraintTypeLabel",new ResourceModel("security.type.title")));
            
            ajaxPanel.add(new ListView("metaData", new PropertyModel(this,
                    "document.securityConstraints"))
            {

                public void populateItem(final ListItem listItem)
                {
                    final String constraints = (String) listItem
                            .getModelObject();
                    listItem.add(new Label("name", constraints));
                    listItem.add(new Label("type", new ResourceModel("security.ref")));
                    AjaxLink editLink = new AjaxLink("edit")
                    {

                        @Override
                        public void onClick(AjaxRequestTarget target)
                        {
                            metaDataModalWindow
                                    .setContent(new SecurityTabWindowPanel(
                                            metaDataModalWindow.getContentId(),
                                            constraints, ajaxPanel));
                            metaDataModalWindow.show(target);
                            target.addComponent(ajaxPanel);
                        }
                    };
                    editLink.add(new Label("editLabel",new ResourceModel("common.edit")));
                    listItem.add(editLink);
                    
                    AjaxLink deleteLink = new AjaxLink("delete")
                    {

                        @Override
                        public void onClick(AjaxRequestTarget target)
                        {
                            securityConstraintAction(REMOVE_ACTION,
                                    constraints, "");
                            target.addComponent(ajaxPanel);
                        }
                    };
                    deleteLink.add(new JavascriptEventConfirmation("onclick",new ResourceModel("action.delete.confirm")));                    
                    deleteLink.add(new Label("deleteLabel",new ResourceModel("common.delete")));
                    listItem.add(deleteLink);
                }
            });
            Form securityForm = new Form("securityFrom");
            add(new AjaxButton("new",new ResourceModel("common.new"),securityForm)
            {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form)
                {
                    metaDataModalWindow.setContent(new SecurityTabWindowPanel(
                            metaDataModalWindow.getContentId(), "", ajaxPanel));
                    metaDataModalWindow.show(target);
                }
            });
            add(ajaxPanel);
            add(securityForm);
        }
    }

    protected class MetaDataTab extends BasePanel
    {

        private static final long serialVersionUID = 6429774536790672910L;

        public MetaDataTab(String id, JetspeedDocument doc)
        {
            super(id, doc);
            final ModalWindow metaDataModalWindow;
            final WebMarkupContainer ajaxPanel = new WebMarkupContainer(
                    "basePanel");
            add(metaDataModalWindow = new ModalWindow("modalwindow"));

            ajaxPanel.add(new Label("nameLabel", new ResourceModel(
                    "metedataTab.name")));
            ajaxPanel.add(new Label("languageLabel", new ResourceModel(
                    "metedataTab.language")));

            final ListView metaDataListView = new ListView("metaData",
                    new PropertyModel(this, "document.metaData"))
            {

                private static final long serialVersionUID = 1L;

                public void populateItem(final ListItem listItem)
                {
                    final JetspeedDocumentMetaData metaData = (JetspeedDocumentMetaData) listItem
                            .getModelObject();
                    listItem.add(new Label("name", metaData.getName()));
                    listItem.add(new Label("language", metaData.getLanguage()));
                    listItem.add(new Label("value", metaData.getValue()));
                    AjaxLink editLink = new AjaxLink("edit", new Model("edit"))
                    {

                        @Override
                        public void onClick(AjaxRequestTarget target)
                        {
                            metaDataModalWindow.setContent(new MetaDataPanel(
                                    metaDataModalWindow.getContentId(),
                                    metaData, ajaxPanel));
                            metaDataModalWindow.show(target);
                        }
                    };
                    editLink.add(new Label("editLabel", new ResourceModel(
                            "common.edit")));
                    listItem.add(editLink);

                    AjaxLink deleteLink = new AjaxLink("delete", new Model(
                            "delete"))
                    {

                        private static final long serialVersionUID = 1L;

                        @Override
                        public void onClick(AjaxRequestTarget target)
                        {
                            metaDataAction(REMOVE_ACTION, metaData, null);
                            target.addComponent(ajaxPanel);
                        }
                    };

                    deleteLink.add(new Label("deleteLabel", new ResourceModel(
                            "common.delete")));
                    deleteLink.add(new JavascriptEventConfirmation("onclick",
                            new ResourceModel("action.delete.confirm")));
                    listItem.add(deleteLink);
                }
            };
            metaDataListView.setOutputMarkupId(true);
            ajaxPanel.setOutputMarkupId(true);
            ajaxPanel.add(metaDataListView);
            Form metaForm = new Form("metaForm");
            add(metaForm);
            add(new AjaxButton("new",new ResourceModel("common.new"),metaForm)
            {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form)
                {
                    metaDataModalWindow
                    .setContent(new MetaDataPanel(metaDataModalWindow
                            .getContentId(),
                            new JetspeedDocumentMetaData("", "", ""),
                            ajaxPanel));
                      metaDataModalWindow.show(target);
                }
            });
            add(ajaxPanel);
        }
    }

    protected class SecurityTabWindowPanel extends Panel
    {

        private static final long serialVersionUID = -3223669376958653554L;

        private String constraintName;

        /**
         * @return the constraintName
         */
        public String getConstraintName()
        {
            return constraintName;
        }

        /**
         * @param constraintName
         *            the constraintName to set
         */
        public void setConstraintName(String constraintName)
        {
            this.constraintName = constraintName;
        }

        public SecurityTabWindowPanel(String id, final String constrainName,
                final WebMarkupContainer markUp)
        {
            super(id);
            this.constraintName = constrainName;
            final FeedbackPanel feedback = new FeedbackPanel("feedback");
            feedback.setOutputMarkupId(true);
            add(feedback);
            Form securityTabForm = new Form("securityDataForm");
            securityTabForm.add(new Label("securityConstraintLabel",
                    "Security Constraint"));
            DropDownChoice constraintsChoices = new DropDownChoice(
                    "constraints", new PropertyModel(this, "constraintName"),
                    getConstraintsDef());
            constraintsChoices.setRequired(true);
            securityTabForm.add(constraintsChoices);
            securityTabForm.add(new AjaxButton("save",new ResourceModel("common.save"), securityTabForm)
            {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form form)
                {
                    securityConstraintAction(SAVE_ACTION, getConstraintName(),
                            constrainName);
                    ((ModalWindow) SecurityTabWindowPanel.this.getParent())
                            .close(target);
                    target.addComponent(markUp);
                }

                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form)
                {
                    target.addComponent(feedback);
                }
            });
            add(securityTabForm);
        }
    }

    protected class OptionsWindow extends WindowPanel
    {

        private static final long serialVersionUID = -3223669376958653554L;

        private OptionsDefinitionBean  optionsDefinition;

        /**
         * @return the optionsDefinition
         */
        public OptionsDefinitionBean getOptionsDefinition()
        {
            return optionsDefinition;
        }

        /**
         * @param optionsDefinition
         *            the optionsDefinition to set
         */
        public void setOptionsDefinition(OptionsDefinitionBean optionsDefinition)
        {
            this.optionsDefinition = optionsDefinition;
        }

        public OptionsWindow(String id, MenuOptionsDefinition options, final WebMarkupContainer markUp)
        {
            super(id);
            final String name = options.getOptions();
            this.optionsDefinition = new OptionsDefinitionBean(options);
            final Form menuform = new Form("menuForm");
            menuform.add(new TextField("optionsField", new PropertyModel(this,
                    "optionsDefinition.options")));
            menuform.add(new Label("optionLabel",new ResourceModel("menu.option")));
            menuform.add(new TextField("depthField", new PropertyModel(this,
                    "optionsDefinition.depth")));
            menuform.add(new Label("depthLabel",new ResourceModel("menu.depth")));
            menuform.add(new CheckBox("pathField", new PropertyModel(this,
                    "optionsDefinition.paths")));
            menuform.add(new Label("pathLabel",new ResourceModel("menu.path")));
            menuform.add(new CheckBox("regExpField", new PropertyModel(this,
                    "optionsDefinition.regexp")));
            menuform.add(new Label("regExpLabel",new ResourceModel("menu.regExp")));
            menuform.add(new TextField("profileField", new PropertyModel(this,
                    "optionsDefinition.profile")));
            menuform.add(new Label("profileLabel",new ResourceModel("menu.profile")));
            menuform.add(new TextField("skinField", new PropertyModel(this,
                    "optionsDefinition.skin")));
            menuform.add(new Label("skinLabel",new ResourceModel("menu.skin")));
            menuform.add(new TextField("orderField", new PropertyModel(this,
                    "optionsDefinition.order")));
            menuform.add(new Label("orderLabel",new ResourceModel("menu.order")));
            menuform.add(new AjaxButton("save",new ResourceModel("common.save"))
            {

                protected void onSubmit(AjaxRequestTarget target, Form menuform)
                {
                    menuActions(SAVE_ACTION,  name,
                            getOptionsDefinition());
                    ((ModalWindow) OptionsWindow.this.getParent())
                            .close(target);
                    target.addComponent(markUp);
                }
            });
            add(menuform);
        }
    }

    protected class MenuWindow extends WindowPanel
    {

        private JetspeedMenuDefinition menuDef;

        public JetspeedMenuDefinition getMenuDef()
        {
            return menuDef;
        }

        public void setMenuDef(JetspeedMenuDefinition menuDef)
        {
            this.menuDef = menuDef;
        }

        public MenuWindow(String id, final MenuDefinition definition,
                final AjaxTabbedPanel tabPanel)
        {
            super(id);
            this.menuDef = new JetspeedMenuDefinition(definition);
            final FeedbackPanel feedback = new FeedbackPanel("feedback");
            feedback.setOutputMarkupId(true);
            add(feedback);
            Form menuform = new Form("menuForm");
            TextField nameField = new RequiredTextField("nameField",
                    new PropertyModel(this, "menuDef.name"));
            menuform.add(new Label("nameLabel",new ResourceModel("common.name")));
            menuform.add(nameField);
            TextField optionsField = new TextField("optionsField",
                    new PropertyModel(this, "menuDef.options"));
            menuform.add(new Label("optionLabel",new ResourceModel("menu.option")));
            menuform.add(optionsField);
            TextField depthField = new TextField("depthField",
                    new PropertyModel(this, "menuDef.depth"));
            menuform.add(new Label("depthLabel",new ResourceModel("menu.depth")));
            menuform.add(depthField);
            CheckBox pathField = new CheckBox("pathField", new PropertyModel(
                    this, "menuDef.paths"));
            menuform.add(new Label("pathLabel",new ResourceModel("menu.path")));
            menuform.add(pathField);
            CheckBox regExpField = new CheckBox("regExpField",
                    new PropertyModel(this, "menuDef.regexp"));
            menuform.add(new Label("regExpLabel",new ResourceModel("menu.regExp")));
            menuform.add(regExpField);
            TextField profileField = new TextField("profileField",
                    new PropertyModel(this, "menuDef.profile"));
            menuform.add(new Label("profileLabel",new ResourceModel("menu.profile")));
            menuform.add(profileField);
            TextField titleField = new TextField("titleField",
                    new PropertyModel(this, "menuDef.title"));
            menuform.add(new Label("titleLabel",new ResourceModel("menu.title")));
            menuform.add(titleField);
            TextField skinField = new TextField("skinField", new PropertyModel(
                    this, "menuDef.skin"));
            menuform.add(new Label("skinLabel",new ResourceModel("menu.skin")));
            menuform.add(skinField);
            TextField orderField = new TextField("orderField",
                    new PropertyModel(this, "menuDef.order"));
            menuform.add(new Label("orderLabel",new ResourceModel("menu.order")));
            menuform.add(orderField);
            menuform.add(new AjaxButton("save",new ResourceModel("common.save"), menuform)
            {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form)
                {
                    menuActions(SAVE_ACTION, definition.getName(), getMenuDef());
                    DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) menuTreeRoot
                            .getRoot();
                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(
                            new MenuTreeNode(getMenuDef().getName(),document.getPath(),document.getType(),getServiceLocator()));
                    rootNode.insert(childNode, rootNode.getChildCount());
                    menuTree.getTreeState().expandNode(rootNode);
                    menuTree.getTreeState().selectNode(childNode, true);
                    menuTree.updateTree(target);
                    controlMenuTabs(true);
                    tabPanel.setSelectedTab(0);
                    target.addComponent(menuTree);
                    ((ModalWindow) MenuWindow.this.getParent()).close(target);
                }

                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form)
                {
                    target.addComponent(feedback);
                }
            });

            add(menuform);
        }
    }

    protected class IncludesWindow extends WindowPanel
    {

        private static final long serialVersionUID = -3223669376958653554L;

        private IncludesDefinitionBean includes;

        /**
         * @return the includes
         */
        public IncludesDefinitionBean getIncludes()
        {
            return includes;
        }

        /**
         * @param includes
         *            the includes to set
         */
        public void setIncludes(IncludesDefinitionBean includes)
        {
            this.includes = includes;
        }

        public IncludesWindow(String id,MenuIncludeDefinition includes, final WebMarkupContainer markUp)
        {
            super(id);
            final String includesName = includes.getName(); 
            this.includes = new IncludesDefinitionBean(includes);
            final MenuIncludeDefinition oldDef = includes;
            final Form menuform = new Form("menuForm");
            menuform.add(new TextField("nameField", new PropertyModel(this,
                    "includes.name")));
            menuform.add(new CheckBox("nestField", new PropertyModel(this,
                    "includes.nest")));
            menuform.add(new Label("namelabel",new ResourceModel("common.name")));
            menuform.add(new Label("nestlabel",new ResourceModel("menu.nest.label")));
            menuform.add(new AjaxButton("save",new ResourceModel("common.save"),menuform)
            {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form)
                {
                    menuActions(SAVE_ACTION, includesName, getIncludes());
                    ((ModalWindow) IncludesWindow.this.getParent())
                            .close(target);
                    target.addComponent(markUp);
                }
            });
            add(menuform);
        }
    }

    protected class ExcludesWindow extends WindowPanel
    {

        private static final long serialVersionUID = -3223669376958653554L;

        private ExcludesDefinitionBean excludes;

        /**
         * @return the excludes
         */
        public ExcludesDefinitionBean getExcludes()
        {
            return excludes;
        }

        /**
         * @param excludes
         *            the excludes to set
         */
        public void setExcludes(ExcludesDefinitionBean excludes)
        {
            this.excludes = excludes;
        }
        
        public ExcludesWindow(String id,MenuExcludeDefinition excludes, final WebMarkupContainer markUp)
        {
            super(id);
            final String excludeName = excludes.getName();
            this.excludes = new ExcludesDefinitionBean(excludes);
            final Form menuform = new Form("menuForm");
            menuform.add(new Label("namelabel",new ResourceModel("common.name")));
            menuform.add(new TextField("nameField", new PropertyModel(this,
                    "excludes.name")));
            menuform.add(new AjaxButton("save",new ResourceModel("common.save"),menuform)
            {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form)
                {
                    menuActions(SAVE_ACTION,excludeName,getExcludes());
                    ((ModalWindow) ExcludesWindow.this.getParent())
                            .close(target);
                    target.addComponent(markUp);
                }
            });
            add(menuform);
        }
    }

    protected class SeparatorWindow extends WindowPanel
    {

        private static final long serialVersionUID = -3223669376958653554L;

        private  SeparatorDefinitionBean separator;

        /**
         * @return the constraintName
         */
        public SeparatorDefinitionBean getSeparator()
        {
            return separator;
        }

        /**
         * @param constraintName
         *            the constraintName to set
         */
        public void setSeparator(SeparatorDefinitionBean constraintName)
        {
            this.separator = constraintName;
        }

        public SeparatorWindow(String id, MenuSeparatorDefinition separator,
                final WebMarkupContainer markUp)
        {
            super(id);
            this.separator = new SeparatorDefinitionBean(separator);
            final String textName = separator.getText();
            Form separatorForm = new Form("separatorDataForm");
            separatorForm.add(new TextField("separatorText", new PropertyModel(
                    this, "separator.text")));
            separatorForm.add(new TextField("separatorTitle",
                    new PropertyModel(this, "separator.title")));
            separatorForm.add(new Label("nameLabel",new ResourceModel("common.name")));
            separatorForm.add(new Label("titleLabel",new ResourceModel("common.title")));
            separatorForm.add(new AjaxButton("save",new ResourceModel("common.save"), separatorForm)
            {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form form)
                {
                    menuActions(SAVE_ACTION,textName, getSeparator());
                    ((ModalWindow) SeparatorWindow.this.getParent())
                            .close(target);
                    target.addComponent(markUp);
                }
            });
            add(separatorForm);
        }
    }

    protected class ImportDataTab extends BasePanel
    {

        private static final long serialVersionUID = 3676403374892366510L;

        private boolean recursively = false;

        private FileUpload fileUpload;

        private ExportJetspeedObject exportObject;
        
        private boolean copyIdsOnImport = true;

        /**
         * @return the recursively
         */
        public boolean isRecursively()
        {
            return recursively;
        }

        /**
         * @param recursively
         *            the recursively to set
         */
        public void setRecursively(boolean recursively)
        {
            this.recursively = recursively;
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
        
        public boolean getCopyIdsOnImport()
        {
            return copyIdsOnImport;
        }
        
        public void setCopyIdsOnImport(boolean copyIdsOnImport)
        {
            this.copyIdsOnImport = copyIdsOnImport;
        }
        
        public ImportDataTab(String id, JetspeedDocument jetspeeeDoc)
        {
            super(id, jetspeeeDoc);
            final SiteTreeNode node = getUserSelectedNode();
            Form exportForm = new Form("exportForm");
            Label exportLabel = new Label("exportName", "Export recursively");
            AjaxCheckBox exportChkBox = new AjaxCheckBox("recursively",
                    new PropertyModel(this, "recursively"))
            {

                @Override
                protected void onUpdate(AjaxRequestTarget target)
                {
                }
            };
            Label exporNameLabel = new Label("expLabel", "Export Object");
            TextField exportName = new TextField("expName", new PropertyModel(
                    this, "document.name"));
            exportLabel.setVisibilityAllowed(true);
            exportChkBox.setVisibilityAllowed(true);
            if (node.getDocType() != SiteTreeNode.FileType.Folder)
            {
                exportLabel.setVisible(false);
                exportChkBox.setVisible(false);
            }
            exportForm.add(exporNameLabel);
            exportForm.add(exportName);
            exportForm.add(exportLabel);
            exportForm.add(exportChkBox);
            exportObject = new ExportJetspeedObject();
            DynamicResourceLink exportButton = new DynamicResourceLink(
                    "exprtButton", new PropertyModel(this, "exportObject"))
            {

                @Override
                public void onClick()
                {
                    if (node.getDocType().equals(SiteTreeNode.FileType.Folder))
                    {
                        exportObject.setFileName(getDocument().getName()
                                + ".zip");
                    } else
                    {
                        exportObject.setFileName(getDocument().getName());
                    }
                    exportObject.setNode(getUserSelectedNode());
                    exportObject.setUserName(getPortletRequest()
                            .getUserPrincipal().getName());
                    exportObject.setRecursively(isRecursively());
                }
            };
            exportForm.add(exportButton);
            add(exportForm);
            // Adding Upload form Folder
            Form uploadForm = new Form("uploadForm");
            add(uploadForm);
            uploadForm.setVisibilityAllowed(true);
            uploadForm.setVisible(false);
            if (node.getDocType().equals(FileType.Folder))
            {
                uploadForm.setMultiPart(true);
                // Add one file input field
                uploadForm.add(new FileUploadField("fileInput",
                        new PropertyModel(this, "fileUploadField")));
                uploadForm.add(new CheckBox("copyIdsOnImport", new PropertyModel(this, "copyIdsOnImport")));
                uploadForm.add(new Button("uploadFile")
                {

                    /*
                     * (non-Javadoc)
                     * 
                     * @see org.apache.wicket.markup.html.form.Button#onSubmit()
                     */
                    /**
                     * 
                     */
                    @Override
                    public void onSubmit()
                    {
                        final FileUpload upload = fileUpload;
                        final String userName = getPortletRequest()
                                .getUserPrincipal().getName();
                        String pathSeparator = System
                                .getProperty("file.separator");
                        boolean copyIds = getCopyIdsOnImport();
                        boolean success = false;
                        if (upload != null)
                        {
                            try
                            {
                                PageManager pageManager = getServiceLocator()
                                        .getPageManager();
                                String fileName = upload.getClientFileName();
                                String fileType = fileExt(upload.getClientFileName());
                                cleanUserFolder(userName);
                                String usrFolder = getTempFolder(userName);
                                String destPath = node.getNodePath();
                                upload.writeTo(new File(usrFolder
                                        + pathSeparator
                                        + upload.getClientFileName()));
                                // File writed in temp folder
                                if (fileType != null && !fileType.equals("")
                                        && fileName != null
                                        && !fileName.equals("")
                                        && destPath != null
                                        && !destPath.equals(""))
                                {
                                    // if "/" is path, then file separator will
                                    // work, as root.
                                    if (destPath.equals("/")) destPath = "";
                                    Folder folder = getServiceLocator()
                                            .getCastorPageManager().getFolder(
                                                    userName);
                                    if (fileType.equalsIgnoreCase("psml"))
                                    {
                                        Page source = folder.getPage(fileName);
                                        Page page = null;
                                        if (pageManager.pageExists(destPath
                                                + pathSeparator + fileName))
                                        {
                                            pageManager.removePage(pageManager
                                                    .getPage(destPath
                                                            + pathSeparator
                                                            + fileName));
                                        }
                                        page = pageManager.copyPage(source,
                                                destPath + pathSeparator
                                                        + fileName,
                                                        copyIds);
                                        pageManager.updatePage(page);
                                        success = true;
                                    } else if (fileType.equalsIgnoreCase("tpsml"))
                                    {
                                        PageTemplate source = folder.getPageTemplate(fileName);
                                        PageTemplate pageTemplate = null;
                                        if (pageManager.pageTemplateExists(destPath
                                                + pathSeparator + fileName))
                                        {
                                            pageManager.removePageTemplate(pageManager
                                                    .getPageTemplate(destPath
                                                            + pathSeparator
                                                            + fileName));
                                        }
                                        pageTemplate = pageManager.copyPageTemplate(source,
                                                destPath + pathSeparator
                                                + fileName,
                                                copyIds);
                                        pageManager.updatePageTemplate(pageTemplate);
                                        success = true;
                                    } else if (fileType.equalsIgnoreCase("dpsml"))
                                    {
                                        DynamicPage source = folder.getDynamicPage(fileName);
                                        DynamicPage dynamicPage = null;
                                        if (pageManager.dynamicPageExists(destPath
                                                + pathSeparator + fileName))
                                        {
                                            pageManager.removeDynamicPage(pageManager
                                                    .getDynamicPage(destPath
                                                            + pathSeparator
                                                            + fileName));
                                        }
                                        dynamicPage = pageManager.copyDynamicPage(source,
                                                destPath + pathSeparator
                                                + fileName,
                                                copyIds);
                                        pageManager.updateDynamicPage(dynamicPage);
                                        success = true;
                                    } else if (fileType.equalsIgnoreCase("fpsml"))
                                    {
                                        FragmentDefinition source = folder.getFragmentDefinition(fileName);
                                        FragmentDefinition fragmentDefinition = null;
                                        if (pageManager.fragmentDefinitionExists(destPath
                                                + pathSeparator + fileName))
                                        {
                                            pageManager.removeFragmentDefinition(pageManager
                                                    .getFragmentDefinition(destPath
                                                            + pathSeparator
                                                            + fileName));
                                        }
                                        fragmentDefinition = pageManager.copyFragmentDefinition(source,
                                                destPath + pathSeparator
                                                + fileName,
                                                copyIds);
                                        pageManager.updateFragmentDefinition(fragmentDefinition);
                                        success = true;
                                    } else if (fileType
                                            .equalsIgnoreCase("link"))
                                    {
                                        Link source = folder.getLink(fileName);
                                        Link link;
                                        if (pageManager.linkExists(destPath
                                                + pathSeparator + fileName))
                                        {
                                            pageManager.removeLink(pageManager
                                                    .getLink(destPath
                                                            + pathSeparator
                                                            + fileName));
                                            link = pageManager.copyLink(source,
                                                    destPath + pathSeparator
                                                            + fileName);
                                        }
                                        link = pageManager.copyLink(source,
                                                destPath + pathSeparator
                                                        + fileName);
                                        pageManager.updateLink(link);
                                        success = true;
                                    } else if (fileType.equalsIgnoreCase("zip"))
                                    {
                                        unzipfile(fileName, StringUtils.removeEnd(usrFolder, pathSeparator), pathSeparator);
                                        folder = getServiceLocator()
                                                .getCastorPageManager()
                                                .getFolder(userName);
                                        importFolders(
                                                pageManager,
                                                getServiceLocator()
                                                        .getCastorPageManager(),
                                                folder, userName, destPath, copyIds);
                                        success = true;
                                    }
                                }
                            } catch (Exception e)
                            {
                                log.error("Unexpected error during uploading page(s) or folder(s).", e);
                            }
                            getUserSelectedNode().setLoaded(false);
                        }
                    }
                });
                uploadForm.setVisible(true);
            }
        }
    }

    protected class MetaDataPanel extends Panel
    {

        private JetspeedDocumentMetaData metaData;

        private JetspeedDocumentMetaData oldMetaData;

        public MetaDataPanel(String id, JetspeedDocumentMetaData jmetaData,
                final WebMarkupContainer markup)
        {
            super(id);
            final FeedbackPanel feedback = new FeedbackPanel("feedback");
            feedback.setOutputMarkupId(true);
            add(feedback);
            this.metaData = jmetaData;
            this.oldMetaData = jmetaData;
            Form metaDataForm = new Form("metaDataForm");
            FormComponent fc;
            fc = new RequiredTextField("name", new PropertyModel(this,
                    "metaData.name"));
            fc.add(StringValidator.minimumLength(2));
            metaDataForm.add(fc);
            metaDataForm.add(new Label("name-label", new ResourceModel(
                    "metedataTab.name")));
            fc = new RequiredTextField("language", new PropertyModel(this,
                    "metaData.language"));
            fc.add(StringValidator.minimumLength(2));
            metaDataForm.add(fc);
            metaDataForm.add(new Label("language-label", new ResourceModel(
                    "metedataTab.language")));
            fc = new RequiredTextField("metaValue", new PropertyModel(this,
                    "metaData.value"));
            fc.add(StringValidator.minimumLength(2));
            metaDataForm.add(fc);
            metaDataForm.add(new Label("value-label", new ResourceModel(
                    "metedataTab.value")));
            metaDataForm.add(new AjaxButton("save", new ResourceModel(
                    "common.save"), metaDataForm)
            {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form form)
                {
                    metaDataAction(SAVE_ACTION, metaData, oldMetaData);
                    ((ModalWindow) MetaDataPanel.this.getParent())
                            .close(target);
                    target.addComponent(markup);
                }

                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form)
                {
                    target.addComponent(feedback);
                }
            });
            add(metaDataForm);
        }
    }

    protected class DocumentOrderingTabPanel extends BasePanel
    {

        List<String> listChoice = new ArrayList<String>();

        String orderedList = "";

        public DocumentOrderingTabPanel(String id, JetspeedDocument document)
        {
            super(id, document);
            getSubFoldersPage(getDocument());
            Form documentOrdering = new Form("docuOrderingForm");
            Label doucmentOrderLabel = new Label("doucmentOrderLabel",
                    "Document ordering");
            documentOrdering.add(doucmentOrderLabel);
            final ListMultipleChoice documnetOrder = new ListMultipleChoice(
                    "docOrders", new PropertyModel(this, "listChoice"),
                    getDocument().getDocumentOrder());
            final HiddenField pageOrder = new HiddenField("pageOrder",
                    new PropertyModel(this, "orderedList"));
            documentOrdering.add(documnetOrder);
            documentOrdering.add(pageOrder);
            documentOrdering.add(new AjaxButton("save",new ResourceModel("common.save"), documentOrdering)
            {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form form)
                {
                    metaOrderAction(getDocument(), getOrderedList());
                }

                @Override
                protected IAjaxCallDecorator getAjaxCallDecorator()
                {
                    return new AjaxPreprocessingCallDecorator(super
                            .getAjaxCallDecorator())
                    {

                        private static final long serialVersionUID = 7495281332320552876L;

                        @Override
                        public CharSequence preDecorateScript(
                                CharSequence script)
                        {
                            return "convertToString();" + script;
                        }
                    };
                }
            });
            add(documentOrdering);
        }

        /**
         * @return the orderedList
         */
        public String getOrderedList()
        {
            return orderedList;
        }

        /**
         * @param orderedList
         *            the orderedList to set
         */
        public void setOrderedList(String orderedList)
        {
            this.orderedList = orderedList;
        }

        /**
         * @return the listChoice
         */
        public List<String> getListChoice()
        {
            return listChoice;
        }

        /**
         * @param listChoice
         *            the listChoice to set
         */
        public void setListChoice(List<String> listChoice)
        {
            this.listChoice = listChoice;
        }
    }

    protected class MenuTabPanel extends BasePanel
    {

        public DefaultTreeModel getMenuTreeRoot()
        {
            return menuTreeRoot;
        }

        public MenuTabPanel(String id, JetspeedDocument document)
        {
            super(id, document);
            ITab tab = null;
            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("_MenusRootNode_");
            final ModalWindow metaDataModalWindow;
            add(metaDataModalWindow = new ModalWindow("modalwindow"));
            // Adding menu tree node
            menuTreeRoot = new DefaultTreeModel(rootNode);
            controlMenuTabs(false);
            final AjaxTabbedPanel menusTab = new AjaxTabbedPanel("menuTabs", menuTabs);
            menuTree = new LinkTree("menuTree", new PropertyModel(this, "menuTreeRoot"))
            {
                @Override
                protected void onNodeLinkClicked(Object node, BaseTree tree,
                        AjaxRequestTarget target)
                {
                    Object userObject = ((DefaultMutableTreeNode) node).getUserObject();
                    
                    if (!(userObject instanceof MenuTreeNode))
                    {
                        return;
                    }
                    
                    MenuTreeNode menuNode = (MenuTreeNode) userObject;
                
                    if (menuNode.getName() != null && !menuNode.getName().equalsIgnoreCase("_MenusRootNode_"))
                    {
                        setMenuDefinition(menuNode.getDefinition());
                        controlMenuTabs(true);
                        menusTab.setSelectedTab(0);
                        target.addComponent(menusTab);
                    }
                }
            };
            
            if (getNodeType().equals(FOLDER_NODE_TYPE))
            {
                setMenuDefinition(getServiceLocator().getPageManager().newFolderMenuDefinition());
            } 
            else
            {
                setMenuDefinition(getServiceLocator().getPageManager().newPageMenuDefinition());
            }
            
            getServiceLocator().getPageManager().newFolderMenuDefinition();
            // menusTab.setSelectedTab(0);
            getMenus(getDocument(), rootNode);
            menuTree.getTreeState().expandNode(rootNode);
            menuTree.setRootLess(true);
            add(new ContextImage("menusRootIcon", "images/tree/folder.gif"));
            add(menuTree);
            Form menuForm = new Form("menuForm");
            add(menuForm);
            add(new AjaxButton("newsave",new ResourceModel("add.menu"),menuForm)
            {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form)
                {
                    MenuDefinition def = null;
                    if (getNodeType().equals(FOLDER_NODE_TYPE))
                    {
                        def = getServiceLocator().getPageManager().newFolderMenuDefinition();
                    }
                    else
                    {
                        def = getServiceLocator().getPageManager().newPageMenuDefinition();
                    }
                    metaDataModalWindow.setContent(new MenuWindow(metaDataModalWindow.getContentId(), def, menusTab));
                    metaDataModalWindow.show(target);
                }
            });
            add(new AjaxButton("remove",new ResourceModel("common.remove"),menuForm)
            {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form)
                {
                    menuActions(REMOVE_ACTION, getMenuDefinition().getName(), new JetspeedMenuDefinition());
                    DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) menuTreeRoot
                            .getRoot();
                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(
                            new MenuTreeNode(getMenuDefinition().getName(),getServiceLocator()));
                    rootNode.remove(childNode);
                    menuTree.getTreeState().expandNode(rootNode);
                    menuTree.updateTree(target);
                    target.addComponent(menuTree);
                    target.addComponent(tabPanel);
                }
            }.add(new JavascriptEventConfirmation("onclick",new ResourceModel("action.delete.confirm"))));
            add(menusTab);
        }

        /**
         * @return the menuTabs
         */
        public List<ITab> getMenuTabs()
        {
            return getMenuTab();
        }
    }

    protected class MenuInfoPanel extends MenuBasePanel
    {
        private JetspeedMenuDefinition menuDef; 
        public JetspeedMenuDefinition getMenuDef()
        {
            return menuDef;
        }

        public void setMenuDef(JetspeedMenuDefinition menuDef)
        {
            this.menuDef = menuDef;
        }

        public MenuInfoPanel(String id, JetspeedDocument document,
                MenuDefinition definition)
        {
            super(id, document, definition);
            final String menuDefinitionName = definition.getName();
            setMenuDef(new JetspeedMenuDefinition((MenuDefinition) copyMenuElement(getNodeType(),
                    definition)));
            final WebMarkupContainer ajaxPanel = new WebMarkupContainer(
                    "ajaxPanel");
            final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
            ajaxPanel.setOutputMarkupId(true);
            Form menuform = new Form("menuForm");
            TextField nameField = new RequiredTextField("nameField",
                    new PropertyModel(this, "menuDef.name"));
            menuform.add(nameField);
            TextField optionsField = new TextField("optionsField",
                    new PropertyModel(this, "menuDef.options"));
            menuform.add(optionsField);
            TextField depthField = new TextField("depthField",
                    new PropertyModel(this, "menuDef.depth"));
            menuform.add(depthField);
            CheckBox pathField = new CheckBox("pathField", new PropertyModel(
                    this, "menuDef.paths"));
            menuform.add(pathField);
            CheckBox regExpField = new CheckBox("regExpField",
                    new PropertyModel(this, "menuDef.regexp"));
            menuform.add(regExpField);
            TextField profileField = new TextField("profileField",
                    new PropertyModel(this, "menuDef.profile"));
            menuform.add(profileField);
            TextField titleField = new TextField("titleField",
                    new PropertyModel(this, "menuDef.title"));
            menuform.add(titleField);
            TextField skinField = new TextField("skinField", new PropertyModel(
                    this, "menuDef.skin"));
            menuform.add(skinField);
            TextField orderField = new TextField("orderField",
                    new PropertyModel(this, "menuDef.order"));
            menuform.add(orderField);
            AjaxButton saveButton = new AjaxButton("save", menuform)
            {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form)
                {
                    menuActions(SAVE_ACTION, menuDefinitionName,getMenuDef());
                    target.addComponent(ajaxPanel);
                }

                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form)
                {
                    target.addComponent(feedbackPanel);
                }

            };
            menuform.add(saveButton);
            ajaxPanel.add(menuform);
            add(ajaxPanel);
            add(feedbackPanel);
        }
    }

    protected class MenuSeparatorPanel extends MenuBasePanel
    {

        public MenuSeparatorPanel(String id, JetspeedDocument document,
                MenuDefinition definition)
        {
            super(id, document, definition);
            final ModalWindow metaDataModalWindow;
            getMenuElements(definition, MenuElement.SEPARATOR_ELEMENT_TYPE);
            add(metaDataModalWindow = new ModalWindow("modalwindow"));
            final WebMarkupContainer ajaxPanel = new WebMarkupContainer(
                    "basePanel");
            ajaxPanel.setOutputMarkupId(true);
            ajaxPanel.add(new Label("textLabel",new ResourceModel("menu.text")));
            ajaxPanel.add(new Label("titleLabel",new ResourceModel("menu.title")));
            ajaxPanel.add(new ListView("separator", new PropertyModel(this,
                    "menuOptions"))
            {

                public void populateItem(final ListItem listItem)
                {
                    final MenuSeparatorDefinition separator = (MenuSeparatorDefinition) listItem
                            .getModelObject();
                    listItem.add(new Label("text", separator.getText()));
                    listItem.add(new Label("title", separator.getTitle()));
                    AjaxLink editLink = new AjaxLink("edit")
                    {

                        @Override
                        public void onClick(AjaxRequestTarget target)
                        {
                            metaDataModalWindow.setContent(new SeparatorWindow(
                                    metaDataModalWindow.getContentId(),separator, ajaxPanel));
                            metaDataModalWindow.show(target);
                        }
                    };
                    editLink.add(new Label("editLabel",new ResourceModel("common.edit")));
                    listItem.add(editLink);
                    AjaxLink deleteLink = new AjaxLink("delete")
                    {

                        @Override
                        public void onClick(AjaxRequestTarget target)
                        {
                            menuActions(REMOVE_ACTION, separator.getText() ,new SeparatorDefinitionBean());
                            target.addComponent(ajaxPanel);
                        }                        
                    };
                    deleteLink.add(new Label("deleteLabel",new ResourceModel("common.delete")));
                    deleteLink.add(new JavascriptEventConfirmation("onclick",new ResourceModel("action.delete.confirm")));  
                    listItem.add(deleteLink);
                }
            });
            Form separtorForm = new Form("sepForm");
            add(separtorForm);
            add(new AjaxButton("new",new ResourceModel("common.new"),separtorForm)
            {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form)
                {
                    MenuSeparatorDefinition separatorDefinition = null;
                    if (getUserSelectedNode().getDocType() == FileType.Folder)
                    {
                        separatorDefinition = getServiceLocator()
                                .getPageManager()
                                .newFolderMenuSeparatorDefinition();
                    } else
                    {
                        separatorDefinition = getServiceLocator()
                                .getPageManager()
                                .newPageMenuSeparatorDefinition();
                    }
                    metaDataModalWindow.setContent(new SeparatorWindow(
                            metaDataModalWindow.getContentId(), separatorDefinition, ajaxPanel));
                    metaDataModalWindow.show(target);
                }
            });
            add(ajaxPanel);
        }
    }

    protected class MenuExlcudesPanel extends MenuBasePanel
    {

        public MenuExlcudesPanel(String id, JetspeedDocument document,
                MenuDefinition definition)
        {
            super(id, document, definition);
            getMenuElements(definition, EXCLUDES);
            final ModalWindow metaDataModalWindow;
            add(metaDataModalWindow = new ModalWindow("modalwindow"));
            final WebMarkupContainer ajaxPanel = new WebMarkupContainer(
                    "basePanel");
            ajaxPanel.setOutputMarkupId(true);
            ajaxPanel.add(new Label("nameLabel",new ResourceModel("common.name")));
            ajaxPanel.add(new ListView("menuData", new PropertyModel(this,
                    "menuOptions"))
            {

                public void populateItem(final ListItem listItem)
                {
                    final MenuExcludeDefinition option = (MenuExcludeDefinition) listItem
                            .getModelObject();
                    listItem.add(new Label("name", option.getName()));
                    AjaxLink editLink = new AjaxLink("edit")
                    {

                        @Override
                        public void onClick(AjaxRequestTarget target)
                        {
                            metaDataModalWindow.setContent(new ExcludesWindow(
                                    metaDataModalWindow.getContentId(),option, ajaxPanel));
                            metaDataModalWindow.show(target);
                        }
                    };
                    editLink.add(new Label("editLabel",new ResourceModel("common.edit")));
                    listItem.add(editLink);
                    AjaxLink deleteLink = new AjaxLink("delete")
                    {
                        @Override
                        public void onClick(AjaxRequestTarget target)
                        {
                            menuActions(REMOVE_ACTION,option.getName(), new ExcludesDefinitionBean());
                            target.addComponent(ajaxPanel);
                        }
                    };
                    deleteLink.add(new JavascriptEventConfirmation("onclick",
                            new ResourceModel("action.delete.confirm")));
                    deleteLink.add(new Label("deleteLabel",new ResourceModel("common.delete")));
                    listItem.add(deleteLink);
                }
            });
            Form excludeForm = new Form("excludeForm");
            add(excludeForm);
            add(new AjaxButton("new",new ResourceModel("common.new"),excludeForm)
            {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form)
                {
                    MenuExcludeDefinition excludeDefinition = null;
                    if (getUserSelectedNode().getDocType() == FileType.Folder)
                    {
                        excludeDefinition = getServiceLocator()
                                .getPageManager()
                                .newFolderMenuExcludeDefinition();
                    } else
                    {
                        excludeDefinition = getServiceLocator()
                                .getPageManager()
                                .newPageMenuExcludeDefinition();
                    }
                    metaDataModalWindow.setContent(new ExcludesWindow(
                            metaDataModalWindow.getContentId(),excludeDefinition, ajaxPanel));
                    metaDataModalWindow.show(target);
                }
            });
            add(ajaxPanel);
        }
    }

    protected class MenuIncludesPanel extends MenuBasePanel
    {

        public MenuIncludesPanel(String id, JetspeedDocument document,
                MenuDefinition definition)
        {
            super(id, document, definition);
            getMenuElements(definition, INCLUDES);
            final ModalWindow metaDataModalWindow;
            add(metaDataModalWindow = new ModalWindow("modalwindow"));
            final WebMarkupContainer ajaxPanel = new WebMarkupContainer(
                    "basePanel");
            ajaxPanel.setOutputMarkupId(true);
            ajaxPanel.add(new Label("nameLabel",new ResourceModel("common.name")));
            ajaxPanel.add(new Label("nestedLabel",new ResourceModel("menu.nest.label")));
            ajaxPanel.add(new ListView("menuData", new PropertyModel(this,
                    "menuOptions"))
            {

                public void populateItem(final ListItem listItem)
                {
                    final MenuIncludeDefinition option = (MenuIncludeDefinition) listItem
                            .getModelObject();
                    listItem.add(new Label("name", option.getName()));
                    listItem.add(new Label("nest", Boolean.toString(option
                            .isNest())));
                    AjaxLink editLink =new AjaxLink("edit")
                    {

                        @Override
                        public void onClick(AjaxRequestTarget target)
                        {
                            metaDataModalWindow.setContent(new IncludesWindow(
                                    metaDataModalWindow.getContentId(),option, ajaxPanel));
                            metaDataModalWindow.show(target);
                        }
                    };
                    editLink.add(new Label("editLabel",new ResourceModel("common.edit")));
                    listItem.add(editLink);
                    AjaxLink deleteLink = new AjaxLink("delete")
                    {

                        @Override
                        public void onClick(AjaxRequestTarget target)
                        {
                            menuActions(REMOVE_ACTION,option.getName(),new IncludesDefinitionBean());
                            target.addComponent(ajaxPanel);
                        }
                    };
                    deleteLink.add(new JavascriptEventConfirmation("onclick",new ResourceModel("action.delete.confirm")));
                    deleteLink.add(new Label("deleteLabel",new ResourceModel("common.delete")));
                    listItem.add(deleteLink);                    
                }
            });
            add(new AjaxLink("new")
            {

                @Override
                public void onClick(AjaxRequestTarget target)
                {
                    MenuIncludeDefinition includeDefinition = null;
                    if (getUserSelectedNode().getDocType() == FileType.Folder)
                    {
                        includeDefinition = getServiceLocator()
                                .getPageManager()
                                .newFolderMenuIncludeDefinition();
                    } else
                    {
                        includeDefinition = getServiceLocator()
                                .getPageManager()
                                .newPageMenuIncludeDefinition();
                    }
                    metaDataModalWindow.setContent(new IncludesWindow(
                            metaDataModalWindow.getContentId(),
                            includeDefinition, ajaxPanel));
                    metaDataModalWindow.show(target);
                }
            });
            add(ajaxPanel);
        }
    }

    protected class MenuOptionsPanel extends MenuBasePanel
    {

        public MenuOptionsPanel(String id, JetspeedDocument document,
                MenuDefinition definition)
        {
            super(id, document, definition);
            getMenuElements(getMenuDefinition(), MenuElement.OPTION_ELEMENT_TYPE);
            final ModalWindow metaDataModalWindow;
            add(metaDataModalWindow = new ModalWindow("modalwindow"));
            final WebMarkupContainer ajaxPanel = new WebMarkupContainer(
                    "basePanel");
            ajaxPanel.setOutputMarkupId(true);
            ajaxPanel.add(new ListView("menuData", new PropertyModel(this,
                    "menuOptions"))
            {

                public void populateItem(final ListItem listItem)
                {
                    final MenuOptionsDefinition option = (MenuOptionsDefinition) listItem
                            .getModelObject();
                    listItem.add(new Label("option", option.getOptions()));
                    listItem.add(new Label("order", option.getOrder()));
                    listItem.add(new Label("path", Boolean.toString(option
                            .isPaths())));
                    listItem.add(new Label("regexp", Boolean.toString(option
                            .isRegexp())));
                    listItem.add(new AjaxLink("edit")
                    {

                        @Override
                        public void onClick(AjaxRequestTarget target)
                        {
                            metaDataModalWindow.setContent(new OptionsWindow(
                                    metaDataModalWindow.getContentId(),option, ajaxPanel));
                            metaDataModalWindow.show(target);
                            target.addComponent(ajaxPanel);
                        }
                    });
                    listItem.add(new AjaxLink("delete")
                    {

                        @Override
                        public void onClick(AjaxRequestTarget target)
                        {
                            menuActions(REMOVE_ACTION,option.getOptions(), new OptionsDefinitionBean());
                            target.addComponent(ajaxPanel);
                        }
                    }.add(new JavascriptEventConfirmation("onclick",
                            new ResourceModel("action.delete.confirm"))));
                }
            });
            add(new AjaxLink("new")
            {

                @Override
                public void onClick(AjaxRequestTarget target)
                {
                    MenuOptionsDefinition optionDefinition = null;
                    if (getUserSelectedNode().getDocType() == FileType.Folder)
                    {
                        optionDefinition = getServiceLocator().getPageManager()
                                .newFolderMenuOptionsDefinition();
                    } else
                    {
                        optionDefinition = getServiceLocator().getPageManager()
                                .newPageMenuOptionsDefinition();
                    }
                    metaDataModalWindow.setContent(new OptionsWindow(
                            metaDataModalWindow.getContentId(),
                            optionDefinition, ajaxPanel));
                    metaDataModalWindow.show(target);
                }
            });
            add(ajaxPanel);
        }
    }

    protected class BasePanel extends Panel
    {

        private static final long serialVersionUID = -6442196391739061842L;

        private JetspeedDocument document;

        public BasePanel(String id, JetspeedDocument jDoc)
        {
            super(id);
            this.document = jDoc;
            final FeedbackPanel feedback = new FeedbackPanel("feedback");
            feedback.setOutputMarkupId(true);
            add(feedback);
        }

        public BasePanel(String id)
        {
            super(id);
            final FeedbackPanel feedback = new FeedbackPanel("feedback");
            feedback.setOutputMarkupId(true);
            add(feedback);
        }

        /**
         * @return the document
         */
        public JetspeedDocument getDocument()
        {
            if (PortalSiteManager.this.document != document)
            {
                document = PortalSiteManager.this.document;
            }
            return document;
        }
    }

    protected class ExportJetspeedObject implements ResourceProvider,
            Serializable
    {

        private SiteTreeNode node;

        private boolean recursively;

        private String userName;

        private ExportObject exportObject;

        private String fileName;

        private String filePath;
        
        private long length = -1; // default or fallback to ensure stream writing works anyways
        
        private long lastModified;

        /**
         * @param fileName
         *            the fileName to set
         */
        public void setFileName(String fileName)
        {
            this.fileName = fileName;
        }

        /**
         * @param recursively
         *            the recursively to set
         */
        public void setRecursively(boolean recursively)
        {
            this.recursively = recursively;
        }

        /**
         * @return the recursively
         */
        public boolean isRecursively()
        {
            return recursively;
        }

        /**
         * @param node
         *            the node to set
         */
        public void setNode(SiteTreeNode node)
        {
            this.node = node;
        }

        /**
         * @param userName
         *            the userName to set
         */
        public void setUserName(String userName)
        {
            this.userName = userName;
        }

        public void close()
        {
            //   
        }

        public String getContentType()
        {
            return exportObject.getContentType();
        }

        public long getLastModified()
        {
            return lastModified;
        }

        public long getLength()
        {
            return length;
        }

        public String getName()
        {
            return this.fileName;
        }

        public InputStream getResource()
        {
            InputStream fileStream = null;
            try
            {
                fileStream = new FileInputStream(exportObject.getFilePath());
            } catch (Exception e)
            {
                // TODO: handle exception
            }
            return fileStream;
        }

        public void open()
        {
            exportObject = exportJetspeedtObject(node, recursively, userName);
            filePath = exportObject.getFilePath();
            File f = new File(filePath);
            if (f.exists())
            {
                length = f.length();
                lastModified = f.lastModified();
            }
        }
    }

    protected class MenuBasePanel extends Panel
    {

        private JetspeedDocument document;

        public MenuBasePanel(String id, JetspeedDocument jDoc,
                MenuDefinition definition)
        {
            super(id);
            this.document = jDoc;
            setMenuDefinition(definition);
        }

        public MenuBasePanel(String id)
        {
            super(id);
        }

        /**
         * @return the document
         */
        public JetspeedDocument getDocument()
        {
            if (PortalSiteManager.this.document != document)
            {
                document = PortalSiteManager.this.document;
            }
            return document;
        }

        /**
         * @return the menuOptions
         */
        public List getMenuOptions()
        {
            return getMenuOption();
        }

        /**
         * @param menuOptions
         *            the menuOptions to set
         */
        public void setMenuOptions(List menuOptions)
        {
            setMenuOption(menuOptions);
        }
    }

    protected class WindowPanel extends Panel
    {

        public WindowPanel(String id)
        {
            super(id);
        }
    }

    private class AddFolderPanel extends Panel
    {

        private JetspeedDocument document;

        public AddFolderPanel(String id)
        {
            super(id);
            final FeedbackPanel feedback = new FeedbackPanel("feedback");
            feedback.setOutputMarkupId(true);
            add(feedback);
            document = new JetspeedDocument();
            Form infoForm = new Form("newForm");
            Label nameLabel = new Label("name", "Name");
            TextField nameField = new TextField("nameField", new PropertyModel(
                    this, "document.name"));
            nameField.setRequired(true);
            infoForm.add(nameLabel);
            infoForm.add(nameField);
            Label titleLabel = new Label("title", "Title");
            TextField titleField = new TextField("titleField",
                    new PropertyModel(this, "document.title"));
            titleField.setRequired(true);
            infoForm.add(titleLabel);
            infoForm.add(titleField);
            Label shortTitleLabel = new Label("shortTitle", "ShortTitle");
            TextField shortTitleField = new TextField("shortTitleField",
                    new PropertyModel(this, "document.shortTitle"));
            infoForm.add(shortTitleField);
            infoForm.add(shortTitleLabel);
            infoForm.add(new AjaxButton("new",new ResourceModel("add.subfolder"),infoForm)
            {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form)
                {
                    SiteTreeNode treeNode = getUserSelectedNode();
                    DefaultMutableTreeNode node = getSelectedNode();
                    String documentPath = "";
                    if (treeNode.getNodePath().equals(JETSPEED_PATH_SEPRATOR))
                    {
                        documentPath = JETSPEED_PATH_SEPRATOR;
                    } else
                    {
                        documentPath = treeNode.getNodePath()
                                + JETSPEED_PATH_SEPRATOR;
                    }
                    Folder folder = getServiceLocator().getPageManager()
                            .newFolder(documentPath + document.getName());
                    folder.setTitle(document.getTitle());
                    folder.setShortTitle(document.getShortTitle());
                    folderAction(folder, SAVE_ACTION);
                    node.insert(new DefaultMutableTreeNode(new SiteTreeNode(
                            folder)), 0);
                    LinkTree tree = (PortalTree) getPage().get("siteTree");
                    tree.getTreeState().expandNode(node);
                    tree.updateTree(target);
                    ((ModalWindow) AddFolderPanel.this.getParent())
                            .close(target);
                }

                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form)
                {
                    target.addComponent(feedback);
                }
            });
            add(infoForm);
        }
    }

    private class AddPagePanel extends Panel
    {

        private JetspeedDocument document;

        public AddPagePanel(String id)
        {
            super(id);
            final FeedbackPanel feedback = new FeedbackPanel("feedback");
            feedback.setOutputMarkupId(true);
            add(feedback);
            document = new JetspeedDocument();
            Form infoForm = new Form("newForm");
            Label nameLabel = new Label("name", "Name");
            TextField nameField = new TextField("nameField", new PropertyModel(
                    this, "document.name"));
            nameField.setRequired(true);
            infoForm.add(nameLabel);
            infoForm.add(nameField);
            Label titleLabel = new Label("title", "Title");
            TextField titleField = new TextField("titleField",
                    new PropertyModel(this, "document.title"));
            titleField.setRedirect(true);
            infoForm.add(titleLabel);
            infoForm.add(titleField);
            Label shortTitleLabel = new Label("shortTitle", "ShortTitle");
            TextField shortTitleField = new TextField("shortTitleField",
                    new PropertyModel(this, "document.shortTitle"));
            infoForm.add(shortTitleField);
            infoForm.add(shortTitleLabel);
            Label pageDecoratorLabel = new Label("pageDecorator",
                    "Page Decorator");
            DropDownChoice decoratorsList = new DropDownChoice(
                    "decoratorsList", new PropertyModel(this,
                            "document.pageDecorator"), getPageDecorators());
            infoForm.add(decoratorsList);
            infoForm.add(pageDecoratorLabel);
            Label portletDecoratorLabel = new Label("portletDecorator",
                    "Portlet Decorator");
            DropDownChoice portletDecoratorsList = new DropDownChoice(
                    "portletDecoratorsList", new PropertyModel(this,
                            "document.portletDecorator"),
                    getPortletDecorators());
            infoForm.add(portletDecoratorLabel);
            infoForm.add(portletDecoratorsList);
            Label themeLabel = new Label("theme", "Theme");
            DropDownChoice themeField = new DropDownChoice("themeList",
                    new PropertyModel(this, "document.desktopTheme"),
                    getThemes());
            infoForm.add(themeField);
            infoForm.add(themeLabel);
            Label visibleLabel = new Label("visible", "Hidden");
            CheckBox visibleCheckbox = new CheckBox("visibleCheck",
                    new PropertyModel(this, "document.hidden"));
            infoForm.add(visibleLabel);
            infoForm.add(visibleCheckbox);
            infoForm.add(new AjaxButton("new",new ResourceModel("add.page"),infoForm)
            {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form)
                {
                    SiteTreeNode treeNode = getUserSelectedNode();
                    DefaultMutableTreeNode node = getSelectedNode();
                    String documentPath = "";
                    if (treeNode.getNodePath().equals(JETSPEED_PATH_SEPRATOR))
                    {
                        documentPath = JETSPEED_PATH_SEPRATOR;
                    } else
                    {
                        documentPath = treeNode.getNodePath()
                                + JETSPEED_PATH_SEPRATOR;
                    }
                    Page page = getServiceLocator().getPageManager().newPage(
                            documentPath + document.getName() + ".psml");
                    page.setTitle(document.getTitle());
                    page.setShortTitle(document.getShortTitle());
                    page.setDefaultDecorator(document.getPageDecorator(),
                            Fragment.LAYOUT);
                    page.setDefaultDecorator(document.getPortletDecorator(),
                            Fragment.PORTLET);
                    page.setSkin(document.getDesktopTheme());
                    page.setHidden(document.isHidden());
                    if (page.getRootFragment() instanceof Fragment)
                    {
                        String layoutName = getServiceLocator().getPortalConfiguration().getString("layout.page.default", "jetspeed-layouts::VelocityOneColumn");
                        ((Fragment)page.getRootFragment()).setName(layoutName);
                    }
                    PageAction(page, SAVE_ACTION);
                    node.insert(new DefaultMutableTreeNode(new SiteTreeNode(
                            page)), 0);
                    LinkTree tree = (PortalTree) getPage().get("siteTree");
                    tree.getTreeState().expandNode(node);
                    tree.updateTree(target);
                    ((ModalWindow) AddPagePanel.this.getParent()).close(target);
                }

                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form)
                {
                    target.addComponent(feedback);
                }
            });
            add(infoForm);
        }
    }

    private class AddLinkPanel extends Panel
    {

        private JetspeedDocument document;

        public AddLinkPanel(String id)
        {
            super(id);
            final FeedbackPanel feedback = new FeedbackPanel("feedback");
            feedback.setOutputMarkupId(true);
            add(feedback);
            document = new JetspeedDocument();
            Form infoForm = new Form("newForm");
            Label nameLabel = new Label("name", "Name");
            TextField nameField = new TextField("nameField", new PropertyModel(
                    this, "document.name"));
            nameField.setRequired(true);
            infoForm.add(nameLabel);
            infoForm.add(nameField);
            Label titleLabel = new Label("title", "Title");
            TextField titleField = new TextField("titleField",
                    new PropertyModel(this, "document.title"));
            infoForm.add(titleLabel);
            infoForm.add(titleField);
            Label shortTitleLabel = new Label("shortTitle", "ShortTitle");
            TextField shortTitleField = new TextField("shortTitleField",
                    new PropertyModel(this, "document.shortTitle"));
            infoForm.add(shortTitleField);
            infoForm.add(shortTitleLabel);
            Label urlLabel = new Label("urlLabel", "URL");
            TextField urlField = new TextField("urlField", new PropertyModel(
                    this, "document.url"));
            urlField.setRequired(true);
            infoForm.add(urlLabel);
            infoForm.add(urlField);
            Label targetLabel = new Label("targetLabel", "Target Window");
            DropDownChoice targetField = new DropDownChoice("target",
                    new PropertyModel(this, "document.target"), getTargetList());
            infoForm.add(targetLabel);
            infoForm.add(targetField);
            Label visibleLabel = new Label("visible", "Hidden");
            CheckBox visibleCheckbox = new CheckBox("visibleCheck",
                    new PropertyModel(this, "document.hidden"));
            infoForm.add(visibleLabel);
            infoForm.add(visibleCheckbox);
            infoForm.add(new AjaxButton("new",new ResourceModel("add.link"),infoForm)
            {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form)
                {
                    SiteTreeNode treeNode = getUserSelectedNode();
                    DefaultMutableTreeNode node = getSelectedNode();
                    String documentPath = "";
                    if (treeNode.getNodePath().equals(JETSPEED_PATH_SEPRATOR))
                    {
                        documentPath = JETSPEED_PATH_SEPRATOR;
                    } else
                    {
                        documentPath = treeNode.getNodePath()
                                + JETSPEED_PATH_SEPRATOR;
                    }
                    Link link = getServiceLocator().getPageManager().newLink(
                            documentPath + document.getName() + ".link");
                    link.setTitle(document.getTitle());
                    link.setShortTitle(document.getShortTitle());
                    link.setUrl(document.getUrl());
                    link.setTarget(document.getTarget());
                    link.setHidden(document.isHidden());
                    linkAction(link, SAVE_ACTION);
                    node.insert(new DefaultMutableTreeNode(new SiteTreeNode(
                            link)), 0);
                    LinkTree tree = (PortalTree) getPage().get("siteTree");
                    tree.getTreeState().expandNode(node);
                    tree.updateTree(target);
                    ((ModalWindow) AddLinkPanel.this.getParent()).close(target);
                }

                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form)
                {
                    target.addComponent(feedback);
                }
            });
            add(infoForm);
        }
    }

    private class CopyMoveWindow extends Panel
    {

        TreeModel copyTreeNode;

        String docType;

        String docName;

        String docPath;
        
        boolean copyIds;
        
        /**
         * @return the docPath
         */
        public String getDocPath()
        {
            return docPath;
        }

        /**
         * @param docPath
         *            the docPath to set
         */
        public void setDocPath(String docPath)
        {
            this.docPath = docPath;
        }

        /**
         * @return the docName
         */
        public String getDocName()
        {
            return docName;
        }

        /**
         * @param docName
         *            the docName to set
         */
        public void setDocName(String docName)
        {
            this.docName = docName;
        }

        /**
         * @return the docType
         */
        public String getDocType()
        {
            return docType;
        }
        
        public boolean getCopyIds()
        {
            return copyIds;
        }
        
        public void setCopyIds(boolean copyIds)
        {
            this.copyIds = copyIds;
        }
        
        public CopyMoveWindow(String id, final String docType, String docName,
                String docPath, final boolean copyFlag)
        {
            super(id);
            this.docType = docType;
            this.docName = docName.toString();
            this.docPath = docPath.toString();
            final String docOldName = docName.toString();
            final String docOldPath = docPath.toString();
            final FeedbackPanel feedback = new FeedbackPanel("feedback");
            feedback.setOutputMarkupId(true);
            add(feedback);
            Form copyForm = new Form("copyForm")
            {

                @Override
                protected void onValidate()
                {
                    if (copyFlag)
                    {
                        if (docOldName.equals(getDocName())
                                && docOldPath.equals(getDocPath()))
                        {
                            error(docType
                                    + " can't be copied with same name at same location");
                        }
                    } else
                    {
                        if (docOldName.equals(getDocName())
                                && docOldPath.equals(getDocPath()))
                        {
                            error(docType
                                    + " can't be  moved with same name at same location");
                        }
                    }
                }

            };
            copyForm.setOutputMarkupId(true);
            copyForm.add(new Label("docType-label", "Doc Type"));
            TextField typeField = new TextField("docType", new PropertyModel(
                    this, "docType"));
            typeField.setRequired(true);
            copyForm.add(typeField);
            copyForm.add(new Label("docName-label", "Document Name"));
            TextField nameField = new TextField("docName", new PropertyModel(
                    this, "docName"));
            nameField.setRequired(true);
            copyForm.add(nameField);
            
            AjaxCheckBox copyIdsChkBox = new AjaxCheckBox("copyIds", new PropertyModel(this, "copyIds")) 
            {
                @Override
                protected void onUpdate(AjaxRequestTarget target)
                {
                }
            };
            copyForm.add(copyIdsChkBox);
            
            AjaxButton copyButton = new AjaxButton("copy",new ResourceModel("common.copy"),copyForm)
            {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form)
                {
                    docAction(true, docOldName, getDocName(), docOldPath, getDocPath(), getCopyIds());
                    ((ModalWindow) CopyMoveWindow.this.getParent()).close(target);
                }

                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form)
                {
                    target.addComponent(feedback);
                }
            };
            
            AjaxButton moveButton = new AjaxButton("move",new ResourceModel("common.move"), copyForm)
            {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form)
                {
                    docAction(false, docOldName, getDocName(), docOldPath, getDocPath(), getCopyIds());
                    ((ModalWindow) CopyMoveWindow.this.getParent()).close(target);
                }

                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form)
                {
                    target.addComponent(feedback);
                }
            };
            
            copyButton.setVisibilityAllowed(true);
            moveButton.setVisibilityAllowed(true);
            copyForm.add(copyButton);
            copyForm.add(moveButton);
            
            if (copyFlag)
            {
                copyButton.setVisible(true);
                moveButton.setVisible(false);
            }
            else
            {
                copyButton.setVisible(false);
                moveButton.setVisible(true);
            }
            
            setCopyIds(!copyFlag);
            
            // Adding menu tree node
            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new SiteTreeNode("Root", "/", FileType.Folder, false));
            retrieveCopyFolders("/", rootNode);
            copyTreeNode = new DefaultTreeModel(rootNode);
            
            final LinkTree copyTree = new LinkTree("copyTree", new PropertyModel(this, "copyTreeNode"))
            {
                @Override
                protected void onNodeLinkClicked(Object node, BaseTree tree,
                        AjaxRequestTarget target)
                {
                    DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
                    SiteTreeNode siteNode = (SiteTreeNode) treeNode.getUserObject();
                    boolean needToUpdate = false;
                    
                    if (!tree.getTreeState().isNodeExpanded(treeNode))
                    {
                        if (siteNode.getDocType() == SiteTreeNode.FileType.Folder
                                && !siteNode.isLoaded())
                        {
                            retrieveCopyFolders(siteNode.getNodePath(), treeNode);
                            siteNode.setLoaded(false);
                            needToUpdate = true;
                        }
                        
                        this.getTreeState().expandNode(treeNode);
                    }
                    else
                    {
                        tree.getTreeState().collapseNode(treeNode);
                    }
                    
                    this.getTreeState().selectNode(treeNode, true);
                    
                    if (needToUpdate)
                    {
                        this.updateTree();
                    }
                    
                    setDocPath(siteNode.getNodePath());
                }
            };
            
            copyTree.getTreeState().expandNode(copyTreeNode);
            
            // Initially select the root node in the tree
            copyTree.getTreeState().selectNode(rootNode, true);
            setDocPath(((SiteTreeNode) rootNode.getUserObject()).getNodePath());
            
            add(copyTree);
            add(copyForm);
        }
        
        private void docAction(boolean copy, String oldName, String newName, String oldPath, String newPath, boolean copyIds)
        {
            FileType docType = getUserSelectedNode().getDocType();
            
            if (getUserSelectedNode().getDocType() == FileType.Folder)
            {
                Folder sourceFolder = getJetspeedFolder(oldPath);
                Node sourceParentNode = sourceFolder.getParent();
                
                try
                {
                    String destFolderPath = newPath + JETSPEED_PATH_SEPRATOR + newName;
                    getServiceLocator().getPageManager().deepCopyFolder(sourceFolder, destFolderPath, getFolderOwner(sourceFolder));
                    
                    Folder destFolder = getServiceLocator().getPageManager().getFolder(destFolderPath);
                    if (destFolder.getParent() != null)
                    {
                        setFolderTreeNodeLoadedByPath(destFolder.getParent(), false);
                    }
                    
                    if (!copy)
                    {
                        folderAction(sourceFolder, REMOVE_ACTION);
                        
                        if (sourceParentNode != null)
                        {
                            setFolderTreeNodeLoadedByPath(sourceParentNode, false);
                        }
                    }
                }
                catch (Exception e)
                {
                    log.error("Failed to copy folder.", e);
                }
            }
            else if (docType == FileType.Page)
            {
                Page sourcePage = getJetspeedPage(oldPath);
                Node sourceParentNode = sourcePage.getParent();
                
                try
                {
                    Page newPage = getServiceLocator().getPageManager().copyPage(sourcePage, newPath + JETSPEED_PATH_SEPRATOR + newName, copyIds);
                    PageAction(newPage, SAVE_ACTION);
                    
                    if (newPage.getParent() != null)
                    {
                        setFolderTreeNodeLoadedByPath(newPage.getParent(), false);
                    }
                    
                    if (!copy)
                    {
                        PageAction(sourcePage, REMOVE_ACTION);
                        
                        if (sourceParentNode != null)
                        {
                            setFolderTreeNodeLoadedByPath(sourceParentNode, false);
                        }
                    }
                }
                catch (NodeException e)
                {
                    log.error("Failed to copy page.", e);
                }
            }
            else if (docType == FileType.Link)
            {
                Link sourceLink = getJetspeedLink(oldPath);
                Node sourceParentNode = sourceLink.getParent();
                
                try
                {
                    Link newLink = getServiceLocator().getPageManager().copyLink(sourceLink, newPath + JETSPEED_PATH_SEPRATOR + newName);
                    linkAction(newLink, SAVE_ACTION);
                    
                    if (newLink.getParent() != null)
                    {
                        setFolderTreeNodeLoadedByPath(newLink.getParent(), false);
                    }
                    
                    if (!copy)
                    {
                        linkAction(sourceLink, REMOVE_ACTION);
                        
                        if (sourceParentNode != null)
                        {
                            setFolderTreeNodeLoadedByPath(sourceParentNode, false);
                        }
                    }
                }
                catch (NodeException e)
                {
                    log.error("Failed to copy link.", e);
                }
            }
        }
    }

    private class ExportObject implements Serializable
    {

        private String filePath;

        private String contentType;

        /**
         * @param filePath
         * @param contentType
         */
        public ExportObject(String filePath, String contentType)
        {
            super();
            this.filePath = filePath;
            this.contentType = contentType;
        }

        /**
         * @return the filePath
         */
        public String getFilePath()
        {
            return filePath;
        }

        /**
         * @return the contentType
         */
        public String getContentType()
        {
            return contentType;
        }
    }
    
    protected void controlTabs()
    {
        SiteTreeNode node = getUserSelectedNode();
        controlTabs(node);
    }
    
    protected void controlTabs(final SiteTreeNode node)
    {
        TabbedPanel tabs = (TabbedPanel) get("tabs");
        tabs.getTabs().clear();
        ITab tempTab;
        tempTab = new AbstractTab(new Model("Information"))
        {

            public Panel getPanel(String panelId)
            {
                return new InformationTab(panelId, document);
            }
        };
        tabs.getTabs().add(tempTab);
        tempTab = new AbstractTab(new Model("Metadata"))
        {

            public Panel getPanel(String panelId)
            {
                return new MetaDataTab(panelId, document);
            }
        };
        tabs.getTabs().add(tempTab);
        tempTab = new AbstractTab(new Model("Security"))
        {

            public Panel getPanel(String panelId)
            {
                return new SecurityTab(panelId, document);
            }
        };
        tabs.getTabs().add(tempTab);
        if (node.getDocType().equals(FileType.Folder))
        {
            tempTab = new AbstractTab(new Model("Document ordering"))
            {

                public Panel getPanel(String panelId)
                {
                    return new DocumentOrderingTabPanel(panelId, document);
                }
            };
            tabs.getTabs().add(tempTab);
        }
        if (node.getDocType().equals(FileType.Folder)
                || node.getDocType().equals(FileType.Page))
        {
            tempTab = new AbstractTab(new Model("Menus"))
            {

                public Panel getPanel(String panelId)
                {
                    return new MenuTabPanel(panelId, document);
                }
            };
            tabs.getTabs().add(tempTab);
        }
        tempTab = new AbstractTab(new Model("Import/Export"))
        {

            public Panel getPanel(String panelId)
            {
                return new ImportDataTab(panelId, document);
            }
        };
        tabs.getTabs().add(tempTab);
        tabs.setSelectedTab(0);
    }

    protected void insertMetadata(JetspeedDocumentMetaData metaData, Node node)
    {
        Locale locale = new Locale(metaData.getLanguage());
        node.getMetadata().addField(locale, metaData.getName(),
                metaData.getValue());
    }

    protected void updateMetadata(JetspeedDocumentMetaData metaData,
            JetspeedDocumentMetaData oldMetaData, Node node)
    {
        Collection cfields = node.getMetadata()
                .getFields(oldMetaData.getName());
        if (cfields == null || cfields.size() == 0)
        {
            insertMetadata(metaData, node);
            return;
        }
        boolean found = false;
        Iterator fields = cfields.iterator();
        while (fields.hasNext())
        {
            LocalizedField field = (LocalizedField) fields.next();
            if (areFieldsSame(field.getName(), oldMetaData.getName())
                    && areFieldsSame(field.getLocale().toString(), oldMetaData
                            .getLanguage()))
            {
                field.setName(metaData.getName());
                field.setLocale(new Locale(metaData.getLanguage()));
                field.setValue(metaData.getValue());
                found = true;
                break;
            }
        }
        if (!found) insertMetadata(metaData, node);
    }

    protected void removeMetadata(JetspeedDocumentMetaData metaData, Node node)
    {
        Collection cfields = node.getMetadata().getFields(metaData.getName());
        Collection allFields = node.getMetadata().getFields();
        if (cfields == null || cfields.size() == 0) { return; }
        boolean found = false;
        Iterator fields = cfields.iterator();
        while (fields.hasNext())
        {
            LocalizedField field = (LocalizedField) fields.next();
            if (areFieldsSame(field.getName(), metaData.getName())
                    && areFieldsSame(field.getLocale().toString(), metaData
                            .getLanguage()))
            {
                cfields.remove(field);
                if (allFields.remove(field))
                {
                    node.getMetadata().setFields(allFields);
                }
                found = true;
                break;
            }
        }
    }

    protected boolean isBlank(String field)
    {
        if (field == null || field.trim().length() == 0) return true;
        return false;
    }

    protected boolean isFieldModified(String paramValue, String prevValue)
    {
        if (paramValue == null)
        {
            if (prevValue == null)
                return false;
            else
                return true;
        } else
        {
            if (prevValue == null) return true;
            if (prevValue.equals(paramValue))
                return false;
            else
                return true;
        }
    }

    protected boolean areFieldsSame(String f1, String f2)
    {
        return !isFieldModified(f1, f2);
    }

    protected boolean isBooleanModified(String paramValue, boolean prevValue)
    {
        if (paramValue == null)
        {
            if (prevValue == false)
                return false;
            else
                return true;
        } else
        {
            if (prevValue == false)
                return true;
            else
                return false;
        }
    }

    protected void insertSecurityReference(String name, String kind, Node node)
    {
        if (node.getSecurityConstraints() == null)
        {
            SecurityConstraints cons = node.newSecurityConstraints();
            node.setSecurityConstraints(cons);
        }
        if (kind.equals("Owner"))
        {
            node.getSecurityConstraints().setOwner(name);
        } else
        {
            List refs = node.getSecurityConstraints()
                    .getSecurityConstraintsRefs();
            if (refs.contains(name)) return;
            refs.add(name);
        }
        return;
    }

    protected void updateSecurityReference(String name, String oldName,
            String kind, Node node)
    {
        if (node.getSecurityConstraints() == null)
        {
            SecurityConstraints cons = node.newSecurityConstraints();
            node.setSecurityConstraints(cons);
        }
        List refs = node.getSecurityConstraints().getSecurityConstraintsRefs();
        if (refs == null || refs.size() == 0)
        {
            insertSecurityReference(name, kind, node);
        }
        boolean found = false;
        if (kind.equals("Owner"))
        {
            node.getSecurityConstraints().setOwner(name);
            found = true;
        } else
        {
            for (int ix = 0; ix < refs.size(); ix++)
            {
                String ref = (String) refs.get(ix);
                if (areFieldsSame(ref, oldName))
                {
                    refs.set(ix, name);
                    found = true;
                    break;
                }
            }
        }
        if (!found) insertSecurityReference(name, kind, node);
        return;
    }

    protected void removeSecurityReference(String name, Node node)
    {
        if (node.getSecurityConstraints() != null)
        {
            String kind = "";
            if (kind.equals("Owner"))
            {
                node.getSecurityConstraints().setOwner(null);
            } else
            {
                List refs = node.getSecurityConstraints()
                        .getSecurityConstraintsRefs();
                if (!refs.contains(name)) return; // nothing to do
                refs.remove(name);
            }
        }
    }

    /**
     * @return the userFolder
     */
    protected String getUserFolder()
    {
        return userFolder;
    }

    /**
     * @param userFolder
     *            the userFolder to set
     */
    protected void setUserFolder(String userFolder)
    {
        this.userFolder = userFolder;
    }

    protected String determineRootFolder()
    {
        PortletRequest request = ((AbstractAdminWebApplication) getApplication()).getPortletRequest();
        String jsroot = request.getParameter(TREE_ROOT);
        if (StringUtils.isEmpty(jsroot))
        {
            jsroot = request.getPreferences().getValue(TREE_ROOT,"/");
        }
        return jsroot;
    }

    private void securityConstraintAction(String action, String constraint,
            String oldConstraintName)
    {
        SiteTreeNode node = getUserSelectedNode();
        Node jetspeedNode = null;
        PageManager pageManager = getServiceLocator().getPageManager();
        if (node.getDocType() == SiteTreeNode.FileType.Folder)
        {
            jetspeedNode = (Node) getJetspeedFolder(node.getNodePath());
            if (action.equals(ADD_ACTION) || action.equals(SAVE_ACTION))
            {
                updateSecurityReference(constraint, oldConstraintName, "",
                        jetspeedNode);
            } else if (action.equals(REMOVE_ACTION))
            {
                removeSecurityReference(constraint, jetspeedNode);
            }
            folderAction((Folder) jetspeedNode, SAVE_ACTION);
        } else if (node.getDocType() == SiteTreeNode.FileType.Page)
        {
            jetspeedNode = (Node) getJetspeedPage(node.getNodePath());
            if (action.equals(ADD_ACTION) || action.equals(SAVE_ACTION))
            {
                updateSecurityReference(constraint, oldConstraintName, "",
                        jetspeedNode);
            } else if (action.equals(REMOVE_ACTION))
            {
                removeSecurityReference(constraint, jetspeedNode);
            }
            PageAction((Page) jetspeedNode, SAVE_ACTION);
        } else if (node.getDocType() == SiteTreeNode.FileType.Link)
        {
            jetspeedNode = (Node) getJetspeedLink(node.getNodePath());
            if (action.equals(ADD_ACTION) || action.equals(SAVE_ACTION))
            {
                updateSecurityReference(constraint, oldConstraintName, "",
                        jetspeedNode);
            } else if (action.equals(REMOVE_ACTION))
            {
                removeSecurityReference(constraint, jetspeedNode);
            }
            linkAction((Link) jetspeedNode, SAVE_ACTION);
        }
        populateDocument(node);
    }

    private void metaDataAction(String action,
            JetspeedDocumentMetaData metaData,
            JetspeedDocumentMetaData oldMetaData)
    {
        SiteTreeNode node = getUserSelectedNode();
        Node jetspeedNode = null;
        PageManager pageManager = getServiceLocator().getPageManager();
        if (node.getDocType() == SiteTreeNode.FileType.Folder)
        {
            jetspeedNode = (Node) getJetspeedFolder(node.getNodePath());
            if (action.equals(ADD_ACTION) || action.equals(SAVE_ACTION))
            {
                updateMetadata(metaData, oldMetaData, jetspeedNode);
            } else if (action.equals(REMOVE_ACTION))
            {
                removeMetadata(metaData, jetspeedNode);
            }
            folderAction((Folder) jetspeedNode, SAVE_ACTION);
        } else if (node.getDocType() == SiteTreeNode.FileType.Page)
        {
            jetspeedNode = (Node) getJetspeedPage(node.getNodePath());
            if (action.equals(ADD_ACTION) || action.equals(SAVE_ACTION))
            {
                updateMetadata(metaData, oldMetaData, jetspeedNode);
            } else if (action.equals(REMOVE_ACTION))
            {
                removeMetadata(metaData, jetspeedNode);
            }
            PageAction((Page) jetspeedNode, SAVE_ACTION);
        } else if (node.getDocType() == SiteTreeNode.FileType.Link)
        {
            jetspeedNode = (Node) getJetspeedLink(node.getNodePath());
            if (action.equals(ADD_ACTION) || action.equals(SAVE_ACTION))
            {
                updateMetadata(metaData, oldMetaData, jetspeedNode);
            } else if (action.equals(REMOVE_ACTION))
            {
                removeMetadata(metaData, jetspeedNode);
            }
            linkAction((Link) jetspeedNode, SAVE_ACTION);
        }
        populateDocument(node);
    }

    private DefaultMutableTreeNode populateUserTree(String userName)
    {
        DefaultMutableTreeNode rootNode = null;
        rootNode = retrieveFolders("/_user/" + userName, rootNode);
        treeRoot = new DefaultTreeModel(rootNode);
        return rootNode;
    }

    private DefaultMutableTreeNode populateTree()
    {
        DefaultMutableTreeNode rootNode = null;
        rootNode = retrieveFolders(determineRootFolder(), rootNode);
        treeRoot = new DefaultTreeModel(rootNode);
        return rootNode;
    }

    private DefaultMutableTreeNode populateCopyTree()
    {
        DefaultMutableTreeNode rootNode = null;
        rootNode = retrieveCopyFolders("/", rootNode);
        treeRoot = new DefaultTreeModel(rootNode);
        return rootNode;
    }
    
    private DefaultMutableTreeNode retrieveFolders(String folderPath, DefaultMutableTreeNode rootNode)
    {
        return retrieveFolders(folderPath, rootNode, true);
    }
    
    private DefaultMutableTreeNode retrieveFolders(String folderPath, DefaultMutableTreeNode rootNode, boolean cleanBeforeRetrieve)
    {
        try
        {
            Link link;
            Folder folder;
            DefaultMutableTreeNode tmpNode;
            Page page;
            Folder rootfolder = getServiceLocator().getPageManager().getFolder(folderPath);
            
            if (rootNode == null)
            {
                rootNode = new DefaultMutableTreeNode(new SiteTreeNode(rootfolder, true));
            }
            
            if (cleanBeforeRetrieve)
            {
                rootNode.removeAllChildren();
            }
            
            Iterator folders = rootfolder.getFolders().iterator();
            while (folders.hasNext())
            {
                folder = (Folder) folders.next();
                
                if (rootfolder.getPath().equals("/_user"))
                {
                    if (folder.getName().startsWith("template"))
                    {
                        rootNode.add(new DefaultMutableTreeNode(new SiteTreeNode(folder)));
                    }
                }
                else
                {
                    rootNode.add(new DefaultMutableTreeNode(new SiteTreeNode(folder)));
                }
            }
            
            Iterator pages = rootfolder.getPages().iterator();
            while (pages.hasNext())
            {
                page = (Page) pages.next();
                tmpNode = new DefaultMutableTreeNode(new SiteTreeNode(page));
                tmpNode.setAllowsChildren(false);
                rootNode.add(tmpNode);
            }
            
            Iterator links = rootfolder.getLinks().iterator();
            while (links.hasNext())
            {
                link = (Link) links.next();
                tmpNode = new DefaultMutableTreeNode(new SiteTreeNode(link));
                tmpNode.setAllowsChildren(false);
                rootNode.add(tmpNode);
            }
        }
        catch (Exception e)
        {
            log.error("Failed to retrieve folders ", e);
        }
        
        return rootNode;
    }

    private DefaultMutableTreeNode retrieveCopyFolders(String folderPath,
            DefaultMutableTreeNode rootNode)
    {
        try
        {
            Folder folder;
            Folder rootfolder = getServiceLocator().getPageManager().getFolder(
                    folderPath);
            if (rootNode == null)
            {
                rootNode = new DefaultMutableTreeNode(new SiteTreeNode(
                        rootfolder, true));
            }
            Iterator folders = rootfolder.getFolders().iterator();
            while (folders.hasNext())
            {
                folder = (Folder) folders.next();
                if (rootfolder.getPath().equals("/_user"))
                {
                    if (folder.getName().startsWith("template"))
                    {
                        rootNode.add(new DefaultMutableTreeNode(
                                new SiteTreeNode(folder)));
                    }
                } else
                {
                    rootNode.add(new DefaultMutableTreeNode(new SiteTreeNode(
                            folder)));
                }
            }
        }
        catch (Exception e)
        {
            log.error("Failed to retrieve folders ", e);
        }
        return rootNode;
    }

    private List<String> getPortletDecorators()
    {
        if (portletDecorators == null)
        {
            portletDecorators = new ArrayList<String>(getServiceLocator().getDecorationFactory().getPortletDecorations(null));
        }
        return portletDecorators;
    }

    private List<String> getPageDecorators()
    {
        if (pageDecorators == null)
        {
            pageDecorators = new ArrayList<String>(getServiceLocator().getDecorationFactory().getPageDecorations(null));
        }
        return pageDecorators;
    }

    private List<String> getThemes()
    {
        if (pageThemes == null)
        {
            pageThemes = new ArrayList<String>(getServiceLocator().getDecorationFactory().getDesktopPageDecorations(null));
        }
        return pageThemes;
    }

    private List<String> getTargetList()
    {
        if (targetList == null)
        {
            targetList = new ArrayList<String>();
            targetList.add("new");
            targetList.add("self");
            targetList.add("top");
            targetList.add("parent");
        }
        return targetList;
    }

    private List<String> getConstraintsDef()
    {
        if (constraintsDefs == null)
        {
            constraintsDefs = new ArrayList<String>();
            Iterator constraintsDefIterator;
            try
            {
                constraintsDefIterator = getServiceLocator().getPageManager()
                        .getPageSecurity().getSecurityConstraintsDefs()
                        .iterator();
                while (constraintsDefIterator.hasNext())
                {
                    constraintsDefs
                            .add(((SecurityConstraintsDef) constraintsDefIterator
                                    .next()).getName());
                }
            }
            catch (UnsupportedDocumentTypeException e)
            {
                log.error("Unsupported document type.", e);
            }
            catch (DocumentNotFoundException e)
            {
                log.error("Document is not found.", e);
            }
            catch (NodeException e)
            {
                log.error("Unexpected exception.", e);
            }

        }
        return constraintsDefs;
    }

    private void populateDocument(SiteTreeNode node)
    {
        try
        {
            if (node.getDocType() == SiteTreeNode.FileType.Folder)
            {
                document = new JetspeedDocument(getServiceLocator()
                        .getPageManager().getFolder(node.getNodePath()));
            } else if (node.getDocType() == SiteTreeNode.FileType.Page)
            {
                document = new JetspeedDocument(getServiceLocator()
                        .getPageManager().getPage(node.getNodePath()));
            } else if (node.getDocType() == SiteTreeNode.FileType.Link)
            {
                document = new JetspeedDocument(getServiceLocator()
                        .getPageManager().getLink(node.getNodePath()));
            }
        }
        catch (Exception e)
        {
            log.error("Failed populate document.", e);
        }
    }

    private void metaOrderAction(JetspeedDocument document, String choices)
    {
        SiteTreeNode node = getUserSelectedNode();
        PageManager pageManger = getServiceLocator().getPageManager();
        List<String> documentOrder = null;
        if (node != null)
        {
            if (choices != null)
            {
                documentOrder = Arrays.asList(choices.split(","));
            }
            if (node.getDocType() == SiteTreeNode.FileType.Folder)
            {
                Folder folder = getJetspeedFolder(node.getNodePath());
                folder.setDocumentOrder(documentOrder);
                folderAction(folder, SAVE_ACTION);
            }
        }
    }

    private void excuteAction(JetspeedDocument document, String action)
    {
        SiteTreeNode node = getUserSelectedNode();
        PageManager pageManger = getServiceLocator().getPageManager();
        if (node != null)
        {
            if (node.getDocType() == SiteTreeNode.FileType.Folder)
            {
                Folder folder = getJetspeedFolder(node.getNodePath());
                if (action.equals("save"))
                {
                    folder.setTitle(document.getTitle());
                    folder.setShortTitle(document.getShortTitle());
                    folder.setDefaultPage(document.getPage());
                    folder.setDefaultDecorator(document.getPageDecorator(),
                            Fragment.LAYOUT);
                    folder.setDefaultDecorator(document.getPortletDecorator(),
                            Fragment.PORTLET);
                    folder.setSkin(document.getDesktopTheme());
                    folder.setHidden(document.isHidden());
                    folderAction(folder, SAVE_ACTION);
                } else if (action.equals("remove"))
                {
                    folderAction(folder, REMOVE_ACTION);
                }
            } else if (node.getDocType() == SiteTreeNode.FileType.Page)
            {
                Page page = getJetspeedPage(node.getNodePath());
                if (action.equals("save"))
                {
                    page.setTitle(document.getTitle());
                    page.setShortTitle(document.getShortTitle());
                    page.setDefaultDecorator(document.getPageDecorator(),
                            Fragment.LAYOUT);
                    page.setDefaultDecorator(document.getPortletDecorator(),
                            Fragment.PORTLET);
                    page.setSkin(document.getDesktopTheme());
                    page.setHidden(document.isHidden());
                    PageAction(page, SAVE_ACTION);
                } else if (action.equals("remove"))
                {
                    PageAction(page, REMOVE_ACTION);
                }
            } else if (node.getDocType() == SiteTreeNode.FileType.Link)
            {
                Link link = getJetspeedLink(node.getNodePath());
                if (action.equals("save"))
                {
                    link.setTitle(document.getTitle());
                    link.setShortTitle(document.getShortTitle());
                    link.setUrl(document.getUrl());
                    link.setTitle(document.getTitle());
                    link.setHidden(document.isHidden());
                    linkAction(link, SAVE_ACTION);
                } else if (action.equals("remove"))
                {
                    linkAction(link, REMOVE_ACTION);
                }
            }
        }
    }

    private String fileExt(String fileName)
    {
        int extIndex = fileName.lastIndexOf(".");
        if (extIndex > 0) { return fileName.substring(extIndex + 1, fileName
                .length()); }
        return "";
    }

    private ExportObject exportJetspeedtObject(SiteTreeNode siteNode,
            boolean recursive, String userName)
    {
        String downloadPath = "";
        String contentType = "text/xml";
        boolean success = true;
        ExportObject jetspeedObject = null;
        String objectPath = siteNode.getNodePath();
        if (!cleanUserFolder(userName)) success = false;
        if (success)
        {
            PageManager pageManager = getServiceLocator().getPageManager();
            PageManager castorPageManager = getServiceLocator()
                    .getCastorPageManager();
            try
            {
                if (siteNode.getDocType() == SiteTreeNode.FileType.Folder)
                {
                    Folder folder = pageManager.getFolder(siteNode
                            .getNodePath());
                    if (recursive)
                    {
                        PortalSiteManagerUtil
                                .importFolder(castorPageManager, folder,
                                        userName, getRealPath(folder.getPath()), true);
                    } else
                    {
                        Folder destFolder = castorPageManager.copyFolder(
                                folder, PortalSiteManagerUtil.getUserFolder(
                                        userName, true)
                                        + siteNode.getNodeName());
                        castorPageManager.updateFolder(destFolder);
                    }
                } else if (siteNode.getDocType() == SiteTreeNode.FileType.Page)
                {
                    objectPath = PortalSiteManagerUtil
                            .getParentPath(objectPath);
                    Folder folder = pageManager.getFolder(objectPath);
                    Page page = folder.getPage(siteNode.getNodeName());
                    Page destPage = castorPageManager.copyPage(page,
                            PortalSiteManagerUtil.getUserFolder(userName, true)
                                    + siteNode.getNodeName(),
                                    true);
                    castorPageManager.updatePage(destPage);
                } else if (siteNode.getDocType() == SiteTreeNode.FileType.Link)
                {
                    objectPath = PortalSiteManagerUtil
                            .getParentPath(objectPath);
                    Folder folder = pageManager.getFolder(objectPath);
                    Link link = folder.getLink(siteNode.getNodeName());
                    Link destLink = castorPageManager.copyLink(link,
                            PortalSiteManagerUtil.getUserFolder(userName, true)
                                    + siteNode.getNodeName());
                    castorPageManager.updateLink(destLink);
                }
                String link = userName + "_" + siteNode.getNodeName();
                if (siteNode.getDocType() == SiteTreeNode.FileType.Folder)
                {
                    contentType = "application/zip";
                    link = userName + ".zip";
                }
                downloadPath = PortalSiteManagerUtil.getDownloadLink(siteNode
                        .getNodeName(), userName, siteNode.getDocType()
                        .toString());
                jetspeedObject = new ExportObject(downloadPath, contentType);
            }
            catch (Exception e)
            {
                log.error("Failed to export site node.", e);
            }
        }
        return jetspeedObject;
    }

    private String getTempFolder(String userName)
    {
        String dir = System.getProperty("java.io.tmpdir");
        String path = System.getProperty("file.separator");
        File file = new File(dir + path + userName);
        file.mkdir();
        return dir + path + userName;
    }

    private static final void copyInputStream(InputStream in, OutputStream out)
            throws IOException
    {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) >= 0)
            out.write(buffer, 0, len);
        in.close();
        out.close();
    }

    private boolean unzipfile(String file, String destination, String sepreator)
    {
        Enumeration entries;
        String filePath = "";
        ZipFile zipFile = null;
        
        try
        {
            zipFile = new ZipFile(destination + sepreator + file);
            entries = zipFile.entries();
            while (entries.hasMoreElements())
            {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                filePath = destination + sepreator + entry.getName();
                createPath(filePath);
                
                InputStream input = null;
                OutputStream output = null;
                
                try
                {
                    input = zipFile.getInputStream(entry);
                    output = new FileOutputStream(filePath);
                    IOUtils.copy(input, output);
                }
                finally
                {
                    IOUtils.closeQuietly(output);
                    IOUtils.closeQuietly(input);
                }
            }
            return true;
        }
        catch (IOException ioe)
        {
            log.error("Unexpected IO exception.", ioe);
            return false;
        }
        finally
        {
            if (zipFile != null)
            {
                try
                {
                    zipFile.close();
                }
                catch (IOException ignore)
                {
                }
            }
        }
    }

    private void createPath(String filePath)
    {
        String parentPath = "";
        File file = new File(filePath);
        File parent = new File(file.getParent());
        if (!parent.exists())
        {
            parentPath = parent.getPath();
            createPath(parentPath);
            parent.mkdir();
        }
    }

    private Folder importFolders(PageManager pageManager,
            PageManager castorPageManager, Folder srcFolder, String userName,
            String destination, boolean copyIds) throws JetspeedException
    {
        Folder dstFolder = lookupFolder(castorPageManager, srcFolder.getPath());
        dstFolder = pageManager.copyFolder(srcFolder, destination);
        pageManager.updateFolder(dstFolder);
        String newPath = "";
        Iterator pages = srcFolder.getPages().iterator();
        while (pages.hasNext())
        {
            Page srcPage = (Page) pages.next();
            Page dstPage = lookupPage(castorPageManager, srcPage.getPath());
            newPath = destination + getRealPath(srcPage.getPath());
            dstPage = pageManager.copyPage(srcPage, newPath, copyIds);
            pageManager.updatePage(dstPage);
        }
        Iterator links = srcFolder.getLinks().iterator();
        while (links.hasNext())
        {
            Link srcLink = (Link) links.next();
            Link dstLink = lookupLink(castorPageManager, srcLink.getPath());
            newPath = destination + getRealPath(srcLink.getPath());
            dstLink = pageManager.copyLink(srcLink, newPath);
            pageManager.updateLink(dstLink);
        }
        Iterator folders = srcFolder.getFolders().iterator();
        while (folders.hasNext())
        {
            Folder folder = (Folder) folders.next();
            newPath = destination + getRealPath(folder.getPath());
            importFolders(pageManager, castorPageManager, folder, userName,
                    newPath, copyIds);
        }
        return dstFolder;
    }

    private Page lookupPage(PageManager castorPageManager, String path)
    {
        try
        {
            return castorPageManager.getPage(path);
        } catch (Exception e)
        {
            return null;
        }
    }

    private Link lookupLink(PageManager castorPageManager, String path)
    {
        try
        {
            return castorPageManager.getLink(path);
        } catch (Exception e)
        {
            return null;
        }
    }

    private Folder lookupFolder(PageManager castorPageManager, String path)
    {
        try
        {
            return castorPageManager.getFolder(path);
        } catch (Exception e)
        {
            return null;
        }
    }

    private String getRealPath(String path)
    {
        int index = path.lastIndexOf("/");
        if (index > 0) { return path.substring(index); }
        return path;
    }

    private boolean cleanUserFolder(String userName)
    {
        boolean success = false;
        synchronized (this)
        {
            String tmpdir = System.getProperty("java.io.tmpdir");
            String path = System.getProperty("file.separator");
            String folder = tmpdir + path + userName;
            File dir = new File(folder);
            if (dir.exists())
            {
                success = deleteDir(dir);
            }
            success = dir.mkdir();
        }
        return success;
    }

    private boolean deleteDir(File dir)
    {
        if (dir.exists())
        {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++)
            {
                if (files[i].isDirectory())
                {
                    deleteDir(files[i]);
                } else
                {
                    files[i].delete();
                }
            }
        }
        return (dir.delete());
    }
    
    private void setFolderTreeNodeLoadedByPath(final Node node, boolean loaded)
    {
        DefaultMutableTreeNode destParentTreeNode = getTreeNodeByPath(node.getPath());
        SiteTreeNode siteTreeNode = (SiteTreeNode) destParentTreeNode.getUserObject();
        siteTreeNode.setLoaded(loaded);
    }
    
    private DefaultMutableTreeNode getTreeNodeByPath(String path)
    {
        PortalTree tree = (PortalTree) getPage().get("siteTree");
        TreeModel treeModel = tree.getModelObject();
        List<DefaultMutableTreeNode> treeNodeList = new ArrayList<DefaultMutableTreeNode>();
        findTreeNodeByPath((DefaultMutableTreeNode) treeModel.getRoot(), path, treeNodeList, 1);
        return (treeNodeList.isEmpty() ? null : treeNodeList.get(0));
    }
    
    private void findTreeNodeByPath(DefaultMutableTreeNode node, String path, List<DefaultMutableTreeNode> treeNodeList, int maxSize)
    {
        SiteTreeNode siteTreeNode = (SiteTreeNode) node.getUserObject();
        
        if (siteTreeNode != null && path.equals(siteTreeNode.getNodePath()))
        {
            treeNodeList.add(node);
        }
        
        if (treeNodeList.size() >= maxSize)
        {
            return;
        }
        
        for (Enumeration children = node.children(); children.hasMoreElements(); )
        {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
            findTreeNodeByPath(child, path, treeNodeList, maxSize);
            
            if (treeNodeList.size() >= maxSize)
            {
                return;
            }
        }
    }
    
    private DefaultMutableTreeNode getSelectedNode()
    {
        PortalTree tree = (PortalTree) getPage().get("siteTree");
        Collection selectedNodes = tree.getTreeState().getSelectedNodes();
        DefaultMutableTreeNode treeNode = null;
        
        if (selectedNodes != null)
        {
            Iterator selectedNode = selectedNodes.iterator();
            while (selectedNode.hasNext())
            {
                treeNode = (DefaultMutableTreeNode) selectedNode.next();
                break;
            }
        }
        
        if (treeNode == null)
        {
            TreeModel treeModel = tree.getModelObject();
            
            if (treeModel != null && treeModel.getRoot() != null)
            {
                treeNode = (DefaultMutableTreeNode) treeModel.getRoot();
            }
        }

        if (treeNode == null)
        {
            treeNode = new DefaultMutableTreeNode(new SiteTreeNode("root", "/", FileType.Folder));
        }
        
        return treeNode;
    }
    
    private DefaultMutableTreeNode getMenuSelectedNode()
    {
        Collection selectedNodes = menuTree.getTreeState().getSelectedNodes();
        DefaultMutableTreeNode treeNode = null;
        if (selectedNodes != null)
        {
            Iterator selectedNode = selectedNodes.iterator();
            while (selectedNode.hasNext())
            {
                treeNode = (DefaultMutableTreeNode) selectedNode.next();
                break;
            }
            if (treeNode == null)
            {
                treeNode = new DefaultMutableTreeNode(new MenuTreeNode("Menus",null));
            }
        }
        return treeNode;
    }
    
    private SiteTreeNode getUserSelectedNode()
    {
        return (SiteTreeNode) getSelectedNode().getUserObject();
    }
    
    private void getSubFoldersPage(JetspeedDocument document)
    {
        List<String> pageFolder = new ArrayList<String>();
        List<String> documentOrder = document.getDocumentOrder();
        Folder folder;
        try
        {
            folder = getJetspeedFolder(document.getPath());
            Iterator folders = folder.getFolders().iterator();
            while (folders.hasNext())
            {
                pageFolder.add(((Folder) folders.next()).getName());
            }
            Iterator pages = folder.getPages().iterator();
            while (pages.hasNext())
            {
                pageFolder.add(((Page) pages.next()).getName());
            }
            Iterator links = folder.getLinks().iterator();
            while (links.hasNext())
            {
                pageFolder.add(((Link) links.next()).getName());
            }
            for (String documentName : pageFolder)
            {
                if (!documentOrder.contains(documentName))
                {
                    documentOrder.add(documentName);
                }
            }
        }
        catch (InvalidFolderException e)
        {
            log.error("Invalid folder.", e);
        }
        catch (NodeException e)
        {
            log.error("Unexpected exception.", e);
        }
    }

    private void getMenus(JetspeedDocument document,
            DefaultMutableTreeNode menuNode)
    {
        MenuDefinition definitaion = null;
        DefaultMutableTreeNode menu = null;
        List menuDefinitions = null;
        if (getUserSelectedNode().getDocType() == FileType.Folder)
        {
            menuDefinitions = getJetspeedFolder(document.getPath())
                    .getMenuDefinitions();
        } else
        {
            menuDefinitions = getJetspeedPage(document.getPath())
                    .getMenuDefinitions();
        }
        if (menuDefinitions == null || menuDefinitions.size() == 0) { return; }
        Iterator menuItreator = menuDefinitions.iterator();
        while (menuItreator.hasNext())
        {
            definitaion = (MenuDefinition) menuItreator.next();
            menu = new DefaultMutableTreeNode(new MenuTreeNode(definitaion.getName(),document.getPath(),document.getType(),getServiceLocator()));
            menuNode.add(menu);
        }
    }

    private void getMenuElements(MenuDefinition definition, String type)
    {
        List elements = new ArrayList();
        if (definition.getMenuElements() == null
                || definition.getMenuElements().size() == 0)
        {
            elements = Collections.EMPTY_LIST;
        } else
        {
            Iterator menuElements = definition.getMenuElements().iterator();
            Object menuElement = null;
            while (menuElements.hasNext())
            {
                menuElement = menuElements.next();
                if (type.equals(MenuElement.MENU_ELEMENT_TYPE)
                        && menuElement instanceof MenuDefinition)
                {
                    elements.add(menuElement);
                } else if (type.equals(MenuElement.OPTION_ELEMENT_TYPE)
                        && menuElement instanceof MenuOptionsDefinition)
                {
                    elements.add(menuElement);
                } else if (type.equals(MenuElement.SEPARATOR_ELEMENT_TYPE)
                        && menuElement instanceof MenuSeparatorDefinition)
                {
                    elements.add(menuElement);
                } else if (type.equals(INCLUDES)
                        && menuElement instanceof MenuIncludeDefinition)
                {
                    elements.add(menuElement);
                } else if (type.equals(EXCLUDES)
                        && menuElement instanceof MenuExcludeDefinition)
                {
                    elements.add(menuElement);
                }
            }
        }
        setMenuOption(elements);
    }

    private void updateList(List menuDefinitions, MenuDefinition olddefinition,
            MenuDefinition definition)
    {
        menuDefinitions.remove(definition);
        menuDefinitions.add(definition);
    }

    protected Object copyMenuElement(String type, Object srcElement)
    {
        PageManager pageManger = getServiceLocator().getPageManager();
        if (srcElement instanceof MenuDefinition)
        {
            // create the new menu element and copy attributes
            MenuDefinition source = (MenuDefinition) srcElement;
            MenuDefinition menu = null;
            if (type.equals(PAGE_NODE_TYPE))
            {
                menu = pageManger.newPageMenuDefinition();
            } else if (type.equals(FOLDER_NODE_TYPE))
            {
                menu = pageManger.newFolderMenuDefinition();
            }
            menu.setDepth(source.getDepth());
            menu.setName(source.getName());
            menu.setOptions(source.getOptions());
            menu.setOrder(source.getOrder());
            menu.setPaths(source.isPaths());
            menu.setProfile(source.getProfile());
            menu.setRegexp(source.isRegexp());
            menu.setShortTitle(source.getShortTitle());
            menu.setSkin(source.getSkin());
            menu.setTitle(source.getTitle());
            // copy locale specific metadata
            menu.getMetadata().copyFields(source.getMetadata().getFields());
            // recursively copy menu elements
            List elements = source.getMenuElements();
            if (elements != null)
            {
                List copiedElements = new ArrayList();
                Iterator elementsIter = elements.iterator();
                while (elementsIter.hasNext())
                {
                    Object element = elementsIter.next();
                    Object copiedElement = copyMenuElement(type, element);
                    if (copiedElement != null)
                    {
                        copiedElements.add(copiedElement);
                    }
                }
                menu.setMenuElements(copiedElements);
            }
            return menu;
        } else if (srcElement instanceof MenuExcludeDefinition)
        {
            // create the new menu exclude element and copy attributes
            MenuExcludeDefinition source = (MenuExcludeDefinition) srcElement;
            MenuExcludeDefinition menuExclude = null;
            if (type.equals(PAGE_NODE_TYPE))
            {
                menuExclude = pageManger.newPageMenuExcludeDefinition();
            } else if (type.equals(FOLDER_NODE_TYPE))
            {
                menuExclude = pageManger.newFolderMenuExcludeDefinition();
            }
            menuExclude.setName(source.getName());
            return menuExclude;
        } else if (srcElement instanceof MenuIncludeDefinition)
        {
            // create the new menu include element and copy attributes
            MenuIncludeDefinition source = (MenuIncludeDefinition) srcElement;
            MenuIncludeDefinition menuInclude = null;
            if (type.equals(PAGE_NODE_TYPE))
            {
                menuInclude = pageManger.newPageMenuIncludeDefinition();
            } else if (type.equals(FOLDER_NODE_TYPE))
            {
                menuInclude = pageManger.newFolderMenuIncludeDefinition();
            }
            menuInclude.setName(source.getName());
            menuInclude.setNest(source.isNest());
            return menuInclude;
        } else if (srcElement instanceof MenuOptionsDefinition)
        {
            // create the new menu options element and copy attributes
            MenuOptionsDefinition source = (MenuOptionsDefinition) srcElement;
            MenuOptionsDefinition menuOptions = null;
            if (type.equals(PAGE_NODE_TYPE))
            {
                menuOptions = pageManger.newPageMenuOptionsDefinition();
            } else if (type.equals(FOLDER_NODE_TYPE))
            {
                menuOptions = pageManger.newFolderMenuOptionsDefinition();
            }
            menuOptions.setDepth(source.getDepth());
            menuOptions.setOptions(source.getOptions());
            menuOptions.setOrder(source.getOrder());
            menuOptions.setPaths(source.isPaths());
            menuOptions.setProfile(source.getProfile());
            menuOptions.setRegexp(source.isRegexp());
            menuOptions.setSkin(source.getSkin());
            return menuOptions;
        } else if (srcElement instanceof MenuSeparatorDefinition)
        {
            // create the new menu separator element and copy attributes
            MenuSeparatorDefinition source = (MenuSeparatorDefinition) srcElement;
            MenuSeparatorDefinition menuSeparator = null;
            if (type.equals(PAGE_NODE_TYPE))
            {
                menuSeparator = pageManger.newPageMenuSeparatorDefinition();
            } else if (type.equals(FOLDER_NODE_TYPE))
            {
                menuSeparator = pageManger.newFolderMenuSeparatorDefinition();
            }
            menuSeparator.setSkin(source.getSkin());
            menuSeparator.setTitle(source.getTitle());
            menuSeparator.setText(source.getText());
            // copy locale specific metadata
            menuSeparator.getMetadata().copyFields(
                    source.getMetadata().getFields());
            return menuSeparator;
        }
        return null;
    }

    private String getNodeType()
    {
        String nodeType = FOLDER_NODE_TYPE;
        if (getUserSelectedNode().getDocType() == FileType.Page)
        {
            nodeType = PAGE_NODE_TYPE;
        }
        return nodeType;
    }

    /**
     * @return the menuDefinition
     */
    protected MenuDefinition getMenuDefinition()
    {
        return (MenuDefinition)getPortletRequest().getPortletSession().getAttribute("menuDefinition");
    }    
    /**
     * @param menuDefinition
     *            the menuDefinition to set
     */
    protected void setMenuDefinition(MenuDefinition menuDefinition)
    {
            
        getPortletRequest().getPortletSession().setAttribute("menuDefinition", menuDefinition);
    }

    protected void menuActions(String action, String options, OptionsDefinitionBean element)
    {
        SiteTreeNode node = getUserSelectedNode();
        MenuDefinition definition = getMenuDefinition();
        MenuOptionsDefinition oldElement = getOptionsDefinition(options);
        MenuOptionsDefinition newElement = getOptionsDefinition(element);
        if (node.getDocType() == FileType.Folder)
        {
            Folder folder = getJetspeedFolder(node.getNodePath());
            List menuList = folder.getMenuDefinitions();
            if (action.equals(SAVE_ACTION))
            {
                menuList.remove(definition);
                if(definition.getMenuElements()!=null)
                {
                    definition.getMenuElements().remove(oldElement);
                    definition.getMenuElements().add(newElement);
                }else{
                    List elements =  new ArrayList();
                    elements.add(newElement);
                    definition.setMenuElements(elements);
                }
                menuList.add(definition);
            }
            else if (action.equals(REMOVE_ACTION))
            {
                menuList.remove(definition);
                definition.getMenuElements().remove(oldElement);
                menuList.add(definition);
            }
            folder.setMenuDefinitions(menuList);
            folderAction(folder, SAVE_ACTION);
        }
        else if (node.getDocType() == FileType.Page)
        {
            Page page = getJetspeedPage(node.getNodePath());
            List menuList = page.getMenuDefinitions();
            if (action.equals(SAVE_ACTION))
            {
                menuList.remove(definition);
                if(definition.getMenuElements()!=null)
                {
                    definition.getMenuElements().remove(oldElement);
                    definition.getMenuElements().add(newElement);
                }else{
                    List elements =  new ArrayList();
                    elements.add(newElement);
                    definition.setMenuElements(elements);
                }
                menuList.add(definition);
            }
            else if (action.equals(REMOVE_ACTION))
            {
                menuList.remove(definition);
                definition.getMenuElements().remove(oldElement);
                menuList.add(definition);
            }
            page.setMenuDefinitions(menuList);
            PageAction(page, SAVE_ACTION);
        }
        setMenuDefinition(definition);
        getMenuElements(definition, MenuElement.OPTION_ELEMENT_TYPE);
    }

    protected void menuActions(String action, String name, JetspeedMenuDefinition element)
    {
        MenuDefinition oldElement = getDefinition(name);
        SiteTreeNode node = getUserSelectedNode();
        MenuDefinition newElement = getMenuDefinition(document.getType(), element);
        if (oldElement != null && oldElement.getMenuElements() != null)
        {
            newElement.setMenuElements(oldElement.getMenuElements());
        }
        if (node.getDocType() == FileType.Folder)
        {
            Folder folder = getJetspeedFolder(node.getNodePath());
            List menuList = folder.getMenuDefinitions();
            if(menuList==null)
            {
                menuList = new LinkedList();
            }
            if (action.equals(SAVE_ACTION))
            {
                menuList.remove(oldElement);
                menuList.add(newElement);
            }
            else if (action.equals(REMOVE_ACTION))
            {
                menuList.remove(oldElement);
            }
            else if (action.equals(ADD_ACTION))
            {
                menuList.add(newElement);
            }
            folder.setMenuDefinitions(menuList);
            folderAction(folder, SAVE_ACTION);
        }
        else if (node.getDocType() == FileType.Page)
        {
            Page page = getJetspeedPage(node.getNodePath());
            List menuList = page.getMenuDefinitions();
            if (action.equals(SAVE_ACTION))
            {
                menuList.remove(oldElement);
                menuList.add(newElement);
            }
            else if (action.equals(REMOVE_ACTION))
            {
                menuList.remove(oldElement);
            }
            else if (action.equals(ADD_ACTION))
            {
                menuList.add(newElement);
            }
            page.setMenuDefinitions(menuList);
            PageAction(page, SAVE_ACTION);
        }
        setMenuDefinition(newElement);
    }

    protected void menuActions(String action, String name, SeparatorDefinitionBean element)
    {
        SiteTreeNode node = getUserSelectedNode();
        MenuDefinition definition = getMenuDefinition();
        MenuSeparatorDefinition oldElement = getSeparatorDefinition(element.getText());
        MenuSeparatorDefinition newElement = getSeparatorDefinition(element);
        if (node.getDocType() == FileType.Folder)
        {
            Folder folder = getJetspeedFolder(node.getNodePath());
            List menuList = folder.getMenuDefinitions();
            if (action.equals(SAVE_ACTION))
            {
                menuList.remove(definition);
                if(definition.getMenuElements()!=null)
                {
                    definition.getMenuElements().remove(oldElement);
                    definition.getMenuElements().add(newElement);
                }else{
                    List elements =  new ArrayList();
                    elements.add(newElement);
                    definition.setMenuElements(elements);
                }
                menuList.add(definition);
            }
            else if (action.equals(REMOVE_ACTION))
            {
                menuList.remove(definition);
                definition.getMenuElements().remove(oldElement);
                menuList.add(definition);
            }
            folder.setMenuDefinitions(menuList);
            folderAction(folder, SAVE_ACTION);
        }
        else if (node.getDocType() == FileType.Page)
        {
            Page page = getJetspeedPage(node.getNodePath());
            List menuList = page.getMenuDefinitions();
            if (action.equals(SAVE_ACTION))
            {
                menuList.remove(definition);
                if(definition.getMenuElements()!=null)
                {
                    definition.getMenuElements().remove(oldElement);
                    definition.getMenuElements().add(newElement);
                }else{
                    List elements =  new ArrayList();
                    elements.add(newElement);
                    definition.setMenuElements(elements);
                }
                menuList.add(definition);
            }
            else if (action.equals(REMOVE_ACTION))
            {
                menuList.remove(definition);
                definition.getMenuElements().remove(oldElement);
                menuList.add(definition);
            }
            page.setMenuDefinitions(menuList);
            PageAction(page, SAVE_ACTION);
        }
        setMenuDefinition(definition);
        getMenuElements(definition, MenuElement.SEPARATOR_ELEMENT_TYPE);
    }

    protected void menuActions(String action, String name, IncludesDefinitionBean element)
    {
        SiteTreeNode node = getUserSelectedNode();
        MenuDefinition definition = getMenuDefinition();
        MenuIncludeDefinition oldElement = getIncludesDefinition(name);
        MenuIncludeDefinition newElement = getIncludesDefinition(element);
        if (node.getDocType() == FileType.Folder)
        {
            Folder folder = getJetspeedFolder(node.getNodePath());
            List menuList = folder.getMenuDefinitions();
            if (action.equals(SAVE_ACTION))
            {
                menuList.remove(definition);
                if(definition.getMenuElements()!=null)
                {
                    definition.getMenuElements().remove(oldElement);
                    definition.getMenuElements().add(newElement);
                }else{
                    List elements =  new ArrayList();
                    elements.add(newElement);
                    definition.setMenuElements(elements);
                }
                menuList.add(definition);
            }
            else if (action.equals(REMOVE_ACTION))
            {
                menuList.remove(definition);
                definition.getMenuElements().remove(oldElement);
                menuList.add(definition);
            }
            folder.setMenuDefinitions(menuList);
            folderAction(folder, SAVE_ACTION);
        }
        else if (node.getDocType() == FileType.Page)
        {
            Page page = getJetspeedPage(node.getNodePath());
            List menuList = page.getMenuDefinitions();
            if (action.equals(SAVE_ACTION))
            {
                menuList.remove(definition);
                if(definition.getMenuElements()!=null)
                {
                    definition.getMenuElements().remove(oldElement);
                    definition.getMenuElements().add(newElement);
                }else{
                    List elements =  new ArrayList();
                    elements.add(newElement);
                    definition.setMenuElements(elements);
                }
                menuList.add(definition);
            }
            else if (action.equals(REMOVE_ACTION))
            {
                menuList.remove(definition);
                definition.getMenuElements().remove(oldElement);
                menuList.add(definition);
            }
            page.setMenuDefinitions(menuList);
            PageAction(page, SAVE_ACTION);
        }
        setMenuDefinition(definition);
        getMenuElements(definition, INCLUDES);
    }

    protected void menuActions(String action, String name, ExcludesDefinitionBean element)
    {
        SiteTreeNode node = getUserSelectedNode();
        MenuDefinition definition = getMenuDefinition();
        MenuExcludeDefinition oldElement = getExcludeDefinition(name);
        MenuExcludeDefinition newElement = getExcludesDefinition(element);
        if (node.getDocType() == FileType.Folder)
        {
            Folder folder = getJetspeedFolder(node.getNodePath());
            List menuList = folder.getMenuDefinitions();
            if (action.equals(SAVE_ACTION))
            {
                menuList.remove(definition);
                if(definition.getMenuElements()!=null)
                {
                    definition.getMenuElements().remove(oldElement);
                    definition.getMenuElements().add(newElement);
                }else{
                    List elements =  new ArrayList();
                    elements.add(newElement);
                    definition.setMenuElements(elements);
                }
                menuList.add(definition);
            }
            else if (action.equals(REMOVE_ACTION))
            {
                menuList.remove(definition);
                definition.getMenuElements().remove(oldElement);
                menuList.add(definition);
            }
            folder.setMenuDefinitions(menuList);
            folderAction(folder, SAVE_ACTION);
        }
        else if (node.getDocType() == FileType.Page)
        {
            Page page = getJetspeedPage(node.getNodePath());
            List menuList = page.getMenuDefinitions();
            if (action.equals(SAVE_ACTION))
            {
                menuList.remove(definition);
                if(definition.getMenuElements()!=null)
                {
                    definition.getMenuElements().remove(oldElement);
                    definition.getMenuElements().add(newElement);
                }else{
                    List elements =  new ArrayList();
                    elements.add(newElement);
                    definition.setMenuElements(elements);
                }
                menuList.add(definition);
            }
            else if (action.equals(REMOVE_ACTION))
            {
                menuList.remove(definition);
                definition.getMenuElements().remove(oldElement);
                menuList.add(definition);
            }
            page.setMenuDefinitions(menuList);
            PageAction(page, SAVE_ACTION);
        }
        setMenuDefinition(definition);
        getMenuElements(definition, EXCLUDES);
    }

    protected Page getJetspeedPage(String pagePath)
    {
        Page page = null;
        try
        {
            page = getServiceLocator().getPageManager().getPage(pagePath);
        }
        catch (PageNotFoundException e)
        {
            log.error("Page is not found: {}", pagePath);
            return null;
        }
        catch (InvalidFolderException e)
        {
            log.error("Invalid folder path: {}", pagePath);
            return null;
        }
        catch (NodeException e)
        {
            log.error("Unexpected exception.", e);
            return null;
        }
        return page;
    }

    protected Link getJetspeedLink(String pagePath)
    {
        Link link = null;
        try
        {
            link = getServiceLocator().getPageManager().getLink(pagePath);
        }
        catch (PageNotFoundException e)
        {
            log.error("Page is not found: {}", pagePath);
            return null;
        }
        catch (InvalidFolderException e)
        {
            log.error("Invalid folder path: {}", pagePath);
            return null;
        } 
        catch (NodeException e)
        {
            log.error("Unexpected exception.", e);
            return null;
        }
        catch (DocumentNotFoundException e)
        {
            log.error("Document is not found: {}", pagePath);
            return null;
        }
        return link;
    }

    protected Folder getJetspeedFolder(String folderPath)
    {
        Folder folder = null;
        try
        {
            folder = getServiceLocator().getPageManager().getFolder(folderPath);
        }
        catch (FolderNotFoundException e)
        {
            log.error("Folder is not found: {}", folderPath);
            return null;
        }
        catch (InvalidFolderException e)
        {
            log.error("Invalid folder path: {}", folderPath);
            return null;
        }
        catch (NodeException e)
        {
            log.error("Unexpected exception.", e);
            return null;
        }
        return folder;
    }
    
    protected void invalidatePortalSiteSessionContext()
    {
        getPortalRequestContext().setSessionAttribute(PortalReservedParameters.PORTAL_SITE_SESSION_CONTEXT_ATTR_KEY, null);
    }
    
    protected boolean folderAction(Folder folder, String action)
    {
        if (action.equals(SAVE_ACTION))
        {
            try
            {
                getServiceLocator().getPageManager().updateFolder(folder);
                invalidatePortalSiteSessionContext();
            }
            catch (FolderNotUpdatedException e)
            {
                log.error("Folder is not updated.", e);
                return false;
            }
            catch (Exception e)
            {
                log.error("Unexpected exception.", e);
                return false;
            }
        }
        else if (action.equals(REMOVE_ACTION))
        {
            try
            {
                getServiceLocator().getPageManager().removeFolder(folder);
                invalidatePortalSiteSessionContext();
            }
            catch (FolderNotRemovedException e)
            {
                log.error("Folder is not removed.", e);
                return false;
            }
            catch (Exception e)
            {
                log.error("Unexpected exception.", e);
                return false;
            }
        }
        return true;
    }

    protected boolean PageAction(Page page, String action)
    {
        if (action.equals(SAVE_ACTION))
        {
            try
            {
                getServiceLocator().getPageManager().updatePage(page);
                invalidatePortalSiteSessionContext();
            }
            catch (Exception e)
            {
                log.error("Unexpected exception.", e);
                return false;
            }
        }
        else if (action.equals(REMOVE_ACTION))
        {
            try
            {
                getServiceLocator().getPageManager().removePage(page);
                invalidatePortalSiteSessionContext();
            }
            catch (Exception e)
            {
                log.error("Unexpected exception.", e);
                return false;
            }
        }
        return true;
    }

    protected boolean linkAction(Link link, String action)
    {
        if (action.equals(SAVE_ACTION))
        {
            try
            {
                getServiceLocator().getPageManager().updateLink(link);
                invalidatePortalSiteSessionContext();
            }
            catch (Exception e)
            {
                log.error("Unexpected exception.", e);
                return false;
            }
        }
        else if (action.equals(REMOVE_ACTION))
        {
            try
            {
                getServiceLocator().getPageManager().removeLink(link);
                invalidatePortalSiteSessionContext();
            }
            catch (Exception e)
            {
                log.error("Unexpected exception.", e);
                return false;
            }
        }
        return true;
    }

    /**
     * @return the menuOptions
     */
    public List getMenuOption()
    {
        return this.menuOptions;
    }

    /**
     * @param menuOptions
     *            the menuOptions to set
     */
    public void setMenuOption(List menuOptions)
    {
        this.menuOptions = menuOptions;
    }

    private void controlMenuTabs(final boolean nodeSelected)
    {
        ITab tab = null;
        menuTabs.clear();
        
        if (nodeSelected)
        {
            tab = new AbstractTab(new Model("Info"))
            {

                public Panel getPanel(String panelId)
                {
                    return new MenuInfoPanel(panelId, document,
                            getMenuDefinition());
                }
            };
            menuTabs.add(tab);
            tab = new AbstractTab(new Model("Options"))
            {

                public Panel getPanel(String panelId)
                {
                    return new MenuOptionsPanel(panelId, document,
                            getMenuDefinition());
                }
            };
            menuTabs.add(tab);
            tab = new AbstractTab(new Model("Separator"))
            {

                public Panel getPanel(String panelId)
                {
                    return new MenuSeparatorPanel(panelId, document,
                            getMenuDefinition());
                }
            };
            menuTabs.add(tab);
            tab = new AbstractTab(new Model("Includes"))
            {

                public Panel getPanel(String panelId)
                {
                    return new MenuIncludesPanel(panelId, document,
                            getMenuDefinition());
                }
            };
            menuTabs.add(tab);
            tab = new AbstractTab(new Model("Excludes"))
            {

                public Panel getPanel(String panelId)
                {
                    return new MenuExlcudesPanel(panelId, document,
                            getMenuDefinition());
                }
            };
            menuTabs.add(tab);
        }
    }

    /**
     * @return the menuTabs
     */
    public List<ITab> getMenuTab()
    {
        return menuTabs;
    }
    
    private MenuOptionsDefinition getOptionsDefinition(String options)
    {
        List elements = getMenuDefinition().getMenuElements();
        if (elements != null)
        {
            for (int index = 0; index < elements.size(); index++)
            {
                Object element = elements.get(index);
                if (element instanceof MenuOptionsDefinition)
                {
                    if (((MenuOptionsDefinition) element).getOptions().equals(options))
                    {
                        return (MenuOptionsDefinition) element;
                    }
                }
            }
        }
        return null;
    }

    private MenuSeparatorDefinition getSeparatorDefinition(String options)
    {
        List elements = getMenuDefinition().getMenuElements();
        if (elements != null)
        {
            for (int index = 0; index < elements.size(); index++)
            {
                Object element = elements.get(index);
                if (element instanceof MenuSeparatorDefinition)
                {
                    if (((MenuSeparatorDefinition) element).getText().equals(options))
                    {
                        return (MenuSeparatorDefinition) element;
                    }
                }
            }
        }
        return null;
    }

    private MenuExcludeDefinition getExcludeDefinition(String options)
    {
        List elements = getMenuDefinition().getMenuElements();
        if (elements != null)
        {
            for (int index = 0; index < elements.size(); index++)
            {
                Object element = elements.get(index);
                if (element instanceof MenuExcludeDefinition)
                {
                    if (((MenuExcludeDefinition) element).getName().equals(options))
                    {
                        return (MenuExcludeDefinition) element;
                    }
                }
            }
        }
        return null;
    }

    private MenuIncludeDefinition getIncludesDefinition(String options)
    {
        List elements = getMenuDefinition().getMenuElements();
        if (elements != null)
        {
            for (int index = 0; index < elements.size(); index++)
            {
                Object element = elements.get(index);
                if (element instanceof MenuIncludeDefinition)
                {
                    if (((MenuIncludeDefinition) element).getName().equals(options))
                    {
                        return (MenuIncludeDefinition) element;
                    }
                }
            }
        }
        return null;
    }
    
    public MenuDefinition getDefinition(String name)
    {
        MenuDefinition definition = null;
        boolean found = true;
        try
        {
            if (document.getType().equals(PortalSiteManager.FOLDER_NODE_TYPE))
            {
                return getMenu(getServiceLocator().getPageManager().getFolder(document.getPath()).getMenuDefinitions(),name);
            }
            else if (document.getType().equals(PortalSiteManager.PAGE_NODE_TYPE))
            {
                return getMenu(getServiceLocator().getPageManager().getPage(document.getPath()).getMenuDefinitions(),name);
            }
        }
        catch (PageNotFoundException e)
        {
            found = false;
        }
        catch (FolderNotFoundException e)
        {
            found = false;
        }
        catch (InvalidFolderException e)
        {
            found = false;
        }
        catch (NodeException e)
        {
            found = false;
        }
        catch (Exception e)
        {
            found = false;
        }
        if (!found)
        {
            if (document.getType().equals(PortalSiteManager.PAGE_NODE_TYPE))
            {
                definition = getServiceLocator().getPageManager().newPageMenuDefinition();
            }
            else if (document.getType().equals(PortalSiteManager.FOLDER_NODE_TYPE))
            {
                definition = getServiceLocator().getPageManager().newFolderMenuDefinition();
            }
        }
        return definition;
    }

    private MenuDefinition getMenu(List menuDefinitions,String name)
    {
        for (int index = 0; index < menuDefinitions.size(); index++)
        {
            MenuDefinition definition = (MenuDefinition) menuDefinitions.get(index);
            if (definition.getName().equals(name))
            {
                return definition;
            }
        }
        if (document.getType().equals(PortalSiteManager.PAGE_NODE_TYPE))
        {
            return getServiceLocator().getPageManager().newPageMenuDefinition();
        }
        return getServiceLocator().getPageManager().newFolderMenuDefinition();
    }
    
    private MenuDefinition getMenuDefinition(String type,JetspeedMenuDefinition menuDefinition)
    {        
        MenuDefinition definition = null;
        if (document.getType().equals(PortalSiteManager.PAGE_NODE_TYPE))
        {
            definition =  getServiceLocator().getPageManager().newPageMenuDefinition();
        }else{
            definition = getServiceLocator().getPageManager().newFolderMenuDefinition();    
        }
        definition.setDepth(menuDefinition.getDepth());
        definition.setName(menuDefinition.getName());
        definition.setOptions(menuDefinition.getOptions());
        definition.setOrder(menuDefinition.getOrder());
        definition.setPaths(menuDefinition.isPaths());
        definition.setRegexp(menuDefinition.isRegexp());
        definition.setTitle(menuDefinition.getTitle());
        definition.setShortTitle(menuDefinition.getShortTitle());
        definition.setSkin(menuDefinition.getSkin());
        definition.setProfile(menuDefinition.getProfile());        
        return definition;
    }
    
    private MenuSeparatorDefinition getSeparatorDefinition(SeparatorDefinitionBean separator)
    {        
        MenuSeparatorDefinition definition = null;
        if (document.getType().equals(PortalSiteManager.PAGE_NODE_TYPE))
        {
            definition =  getServiceLocator().getPageManager().newPageMenuSeparatorDefinition();
        }else{
            definition = getServiceLocator().getPageManager().newFolderMenuSeparatorDefinition();    
        }
        definition.setText(separator.getText());
        definition.setTitle(separator.getTitle());
        return definition;
    }
    
    private MenuExcludeDefinition getExcludesDefinition(ExcludesDefinitionBean excludes)
    {        
        MenuExcludeDefinition definition = null;
        if (document.getType().equals(PortalSiteManager.PAGE_NODE_TYPE))
        {
            definition =  getServiceLocator().getPageManager().newPageMenuExcludeDefinition();
        }else{
            definition = getServiceLocator().getPageManager().newFolderMenuExcludeDefinition();    
        }
        definition.setName(excludes.getName());
        return definition;
    }
    
    private MenuIncludeDefinition getIncludesDefinition(IncludesDefinitionBean includes)
    {        
        MenuIncludeDefinition  definition = null;
        if (document.getType().equals(PortalSiteManager.PAGE_NODE_TYPE))
        {
            definition =  getServiceLocator().getPageManager().newPageMenuIncludeDefinition();
        }else{
            definition = getServiceLocator().getPageManager().newFolderMenuIncludeDefinition();    
        }
        definition.setName(includes.getName());
        definition.setNest(includes.isNest());
        return definition;
    }
    
    private MenuOptionsDefinition getOptionsDefinition(OptionsDefinitionBean options)
    {        
        MenuOptionsDefinition definition = null;
        if (document.getType().equals(PortalSiteManager.PAGE_NODE_TYPE))
        {
            definition =  getServiceLocator().getPageManager().newPageMenuOptionsDefinition();
        }else{
            definition = getServiceLocator().getPageManager().newFolderMenuOptionsDefinition();    
        }
        definition.setDepth(options.getDepth());
        definition.setOptions(options.getOptions());
        definition.setOrder(options.getOrder());
        definition.setPaths(options.isPaths());
        definition.setRegexp(options.isRegexp());
        definition.setSkin(options.getSkin());
        definition.setProfile(options.getProfile());          
        return definition;
    }
    
    private SiteTreeNode getInitSiteTreeNode()
    {
        SiteTreeNode siteTreeNode = null;
        
        PortletRequest portletRequest = getPortletRequest();
        String pathParam = portletRequest.getParameter(PORTAL_SITE_MANAGER_INIT_NODE_PATH_PARAM);
        
        if (pathParam == null)
        {
            pathParam = getPreference(PORTAL_SITE_MANAGER_INIT_NODE_PATH_PARAM);
            
            if (pathParam == null)
            {
                pathParam = getInitParam(PORTAL_SITE_MANAGER_INIT_NODE_PATH_PARAM);
            }
        }
        
        if (pathParam != null)
        {
            String type = PAGE_NODE_TYPE;
            String typeParam = null;
            
            if (typeParam == null)
            {
                typeParam = getPreference(PORTAL_SITE_MANAGER_INIT_NODE_TYPE_PARAM);
                
                if (typeParam == null)
                {
                    typeParam = getInitParam(PORTAL_SITE_MANAGER_INIT_NODE_TYPE_PARAM);
                }
            }
            
            if (typeParam != null)
            {
                type = typeParam;
            }
            
            PageManager pageManager = getServiceLocator().getPageManager();
            DefaultMutableTreeNode treeRootNode = null;
            
            try
            {
                if (PAGE_NODE_TYPE.equals(type))
                {
                    Page page = pageManager.getPage(pathParam);
                    siteTreeNode = new SiteTreeNode(page.getName(), page.getPath(), SiteTreeNode.FileType.Page, true);
                    treeRootNode = new DefaultMutableTreeNode(new SiteTreeNode(page));
                }
                else if (FOLDER_NODE_TYPE.equals(type))
                {
                    Folder folder = pageManager.getFolder(pathParam);
                    siteTreeNode = new SiteTreeNode(folder.getName(), folder.getPath(), SiteTreeNode.FileType.Folder, true);
                    treeRootNode = new DefaultMutableTreeNode(new SiteTreeNode(folder, true));
                }
                else if (LINK_NODE_TYPE.equals(type))
                {
                    Link link = pageManager.getLink(pathParam);
                    siteTreeNode = new SiteTreeNode(link.getName(), link.getPath(), SiteTreeNode.FileType.Link, true);
                    treeRootNode = new DefaultMutableTreeNode(new SiteTreeNode(link));
                }
            }
            catch (Exception e)
            {
                log.error("Failed to retrieve the init site tree node on " + pathParam, e);
            }
            
            if (treeRootNode != null)
            {
                treeRoot = new DefaultTreeModel(treeRootNode);
            }
        }
        
        if (siteTreeNode == null)
        {
            siteTreeNode = new SiteTreeNode("ROOT", "/", SiteTreeNode.FileType.Folder, true);
        }
        
        return siteTreeNode;
    }
    
    private String getFolderOwner(final Folder folder)
    {
        String owner = null;
        
        SecurityConstraints constraints = folder.getSecurityConstraints();
        
        if (constraints != null)
        {
            owner = constraints.getOwner();
        }
        
        return owner;
    }
}   
