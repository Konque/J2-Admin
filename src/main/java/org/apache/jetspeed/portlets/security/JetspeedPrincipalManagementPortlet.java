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
package org.apache.jetspeed.portlets.security;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletPreferences;
import javax.security.auth.Subject;

import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.administration.PortalConfigurationConstants;
import org.apache.jetspeed.audit.AuditActivity;
import org.apache.jetspeed.om.folder.Folder;
import org.apache.jetspeed.om.folder.FolderNotFoundException;
import org.apache.jetspeed.page.PageManager;
import org.apache.jetspeed.page.document.NodeException;
import org.apache.jetspeed.portlets.AdminPortletWebPage;
import org.apache.jetspeed.portlets.security.PrincipalDataProvider.OrderBy;
import org.apache.jetspeed.portlets.wicket.AbstractAdminWebApplication;
import org.apache.jetspeed.portlets.wicket.component.CheckBoxPropertyColumn;
import org.apache.jetspeed.portlets.wicket.component.JavascriptEventConfirmation;
import org.apache.jetspeed.portlets.wicket.component.PortletOddEvenItem;
import org.apache.jetspeed.profiler.Profiler;
import org.apache.jetspeed.profiler.rules.PrincipalRule;
import org.apache.jetspeed.profiler.rules.ProfilingRule;
import org.apache.jetspeed.security.InvalidNewPasswordException;
import org.apache.jetspeed.security.InvalidPasswordException;
import org.apache.jetspeed.security.JetspeedPrincipal;
import org.apache.jetspeed.security.JetspeedPrincipalAssociationType;
import org.apache.jetspeed.security.JetspeedPrincipalManager;
import org.apache.jetspeed.security.JetspeedPrincipalType;
import org.apache.jetspeed.security.PasswordAlreadyUsedException;
import org.apache.jetspeed.security.PasswordCredential;
import org.apache.jetspeed.security.RoleManager;
import org.apache.jetspeed.security.SecurityAttribute;
import org.apache.jetspeed.security.SecurityException;
import org.apache.jetspeed.security.SubjectHelper;
import org.apache.jetspeed.security.User;
import org.apache.jetspeed.security.UserCredential;
import org.apache.jetspeed.security.UserManager;
import org.apache.jetspeed.security.UserSubjectPrincipal;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink.VoidCssProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vkumar <a href="vkumar@apache.org">Vivek Kumar</a>
 */
public class JetspeedPrincipalManagementPortlet extends AdminPortletWebPage
{

    static final Logger log = LoggerFactory.getLogger(JetspeedPrincipalManagementPortlet.class);

    private static final String principalParamName = "principalParam";
    
    public static final String DEFAULT_SUBSITE = "defaultSubsite";
    public static final String DEFAULT_ROLE = "defaultRole";
    public static final String REQUIRED_ROLE = "requiredRole";
    public static final String DEFAULT_PROFILE = "defaultProfile";
    public static final String NEW_USER_TEMPLATE_DIR = "newUserTemplateDirectory";
    public static final String SUB_SITE_ROOT = "subsiteRootFolder";

    private static final java.sql.Date MAX_DATE = java.sql.Date
            .valueOf("8099-01-01");

    private String principalParam;

    private JetspeedPrincipalType principalType;

    protected String searchString;

    protected boolean filtered;

    protected JetspeedPrincipal principal;
    final PrincipalDataProvider principalDataProvider;
    WebMarkupContainer group;

    protected String selectedUserName;

    public JetspeedPrincipalManagementPortlet()
    {
        super();
        List tabs;
        
        principalParam = getPreference(principalParamName);
        if (principalParam == null)
        {
            principalParam = getInitParam(principalParamName).toLowerCase();
        }
        principalType = getServiceLocator()
                .getJetspeedPrincipalManagerProvider().getPrincipalType(
                        principalParam);
       
        String filteredRole = getPreference("filteredRole", "");
        if (principalParam.equals(JetspeedPrincipalType.USER))
        {
            principalDataProvider = new PrincipalDataProvider(getManager(), getRoleManager(), getSearchString(), filteredRole, true);
        }
        else
        {
            principalDataProvider = new PrincipalDataProvider(getManager(), getSearchString());
        }
        
        ITab tab = null;
        tabs = new ArrayList();
        group = new WebMarkupContainer("group");
        group.setOutputMarkupId(true);

        
        final DataView<JetspeedPrincipal> principalView = new DataView<JetspeedPrincipal>("entries", principalDataProvider)
        {
            @Override
            protected void populateItem(Item<JetspeedPrincipal> item)
            {
                final JetspeedPrincipal user = (JetspeedPrincipal) item.getModelObject();
                Link editLink = new Link("link", item.getModel())
                {
                    @Override
                    public void onClick()
                    {
                        JetspeedPrincipal user = (JetspeedPrincipal) getModelObject();
                        setPrincipal(user);
                        controlPannels(true);
                    }
                };
                editLink.add(new Label("name", user.getName()));
                item.add(editLink);
            }
        };
        principalView.setItemsPerPage(10);
        group.add(principalView);

        OrderByLink orderLink = new OrderByLink("nameOrderLink", "name", principalDataProvider,VoidCssProvider.getInstance())
        {
            protected void onSortChanged()
            {
                if (principalDataProvider.getOrderBy() == OrderBy.NAME_ASC)
                {
                    principalDataProvider.setOrderBy(OrderBy.NAME_DESC);
                }
                else
                {
                    principalDataProvider.setOrderBy(OrderBy.NAME_ASC);
                }
                principalDataProvider.sort();                
                principalView.setCurrentPage(0);
            };
        };
        group.add(new Label("principal",new ResourceModel(principalParam)));
        orderLink.add(new Label("nameSort",new ResourceModel(principalParam)));
        group.add(orderLink);
        
        group.add(new AjaxPagingNavigator("navigator", principalView));
        
        add(group);
        Form searchForm = new Form("searchForm")
        {

            protected void onSubmit()
            {
                setPrincipal(null);
            }
        };
        TextField searchStringField = new TextField("searchString",
                new PropertyModel(this, "searchString"));
        searchForm.add(searchStringField);
        Button searchbutton = new Button("searchButton", new ResourceModel(
                "common.search"));
        searchForm.add(searchbutton);
        Button newPrincipal = new Button("newPrincipal", new ResourceModel(
                principalParam + ".new.button"))
        {

            public void onSubmit()
            {
                setPrincipal(null);
                controlPannels(false);
            }
        };
        searchForm.add(newPrincipal);
        add(searchForm);
        Label label = new Label("userLabel", new ResourceModel(principalParam
                + ".name"));
        label.setVisible(false);
        add(label);
        TextField fld = new TextField("userName", new PropertyModel(this,
                "principal.name"));
        fld.setVisible(false);
        add(fld);
        add(new TabbedPanel("tabs", tabs));
        controlPannels(false);
    }

    public void setSearchString(String searchString)
    {
        this.searchString = (searchString == null ? "" : searchString.trim());        
        principalDataProvider.refresh(getManager(),getRoleManager(),searchString);
        setPrincipal(null);
        controlPannels(false);
    }

    public String getSearchString()
    {
        return (this.searchString == null ? "" : this.searchString);
    }

    public void setFiltered(boolean filtered)
    {
        this.filtered = filtered;
    }

    public boolean getFiltered()
    {
        return getSearchString() == null ? false : true;
    }

    public String getSelectedUserName()
    {
        return this.principal.getName();
    }

    public void setPrincipal(JetspeedPrincipal principal)
    {
        this.principal = principal;
    }

    protected class UserPrincipalProfilePanel extends Panel
    {

        protected String locatorName;

        protected String ruleName;

        protected List fullRules;

        protected List userRules;

        protected boolean userEnabled;

        /**
         * @param userEnabled
         *            the userEnabled to set
         */
        public void setUserEnabled(boolean userEnabled)
        {
            this.userEnabled = userEnabled;
        }

        /**
         * @return the userEnabled
         */
        public boolean isUserEnabled()
        {
            return userEnabled;
        }

        protected UserPrincipalProfilePanel(String id)
        {
            super(id);
            if (fullRules == null || userRules == null)
            {
                refreshData();
            }
            Form profileForm = new Form("profileForm");
            add(profileForm);
            profileForm.add(new CheckBox("userEnabled", new PropertyModel(this,
                    "userEnabled")));
            profileForm.add(new Label("enabledLabel", new ResourceModel(
                    "common.enabled")));
            profileForm.add(new Button("submit", new ResourceModel(
                    principalParam + ".update.button")){
                @Override
                public void onSubmit()
                {
                    try
                    {
                        getPrincipal().setEnabled(isUserEnabled());
                        getManager().updatePrincipal(principal);
                        setPrincipal(principal);
                        principalDataProvider.refresh(getManager(),getRoleManager(),getSearchString());
                    }
                    catch (SecurityException jSx)
                    {
                        log.error("Failed to update principal.", jSx);
                    }
                }
                
            });
            profileForm.add(new Button("remove", new ResourceModel(principalParam + ".remove.button")){

                @Override
                public void onSubmit()
                {
                    try
                    {
                        getManager().removePrincipal(principal.getName());
                        setPrincipal(null);
                        controlPannels(false);
                        principalDataProvider.refresh(getManager(),getRoleManager(),getSearchString());
                    }
                    catch (SecurityException e)
                    {
                        error(e.getMessage());
                    }
                }
                
            }.add(new JavascriptEventConfirmation("onclick",new ResourceModel("action.delete.confirm"))));            
            Form userRulesForm = new Form("userRulesForm")
            {

                protected void onSubmit()
                {
                    try
                    {
                        Collection rules = getServiceLocator().getProfiler()
                                .getRulesForPrincipal(getPrincipal());
                        for (Iterator it = getUserRules().iterator(); it
                                .hasNext();)
                        {
                            Map ruleMap = (Map) it.next();
                            if (Boolean.TRUE.equals(ruleMap.get("checked")))
                            {
                                String locatorName = ((PrincipalRule) ruleMap
                                        .get("rule")).getLocatorName();
                                for (Iterator ruleIter = rules.iterator(); ruleIter
                                        .hasNext();)
                                {
                                    PrincipalRule rule = (PrincipalRule) ruleIter
                                            .next();
                                    if (rule.getLocatorName().equals(
                                            locatorName))
                                    {
                                        getServiceLocator().getProfiler()
                                                .deletePrincipalRule(rule);
                                        getServiceLocator()
                                                .getAuditActivity()
                                                .logAdminAuthorizationActivity(
                                                        getPortletRequest()
                                                                .getUserPrincipal()
                                                                .getName(),
                                                        getIPAddress(),
                                                        getSelectedPrincipal(),
                                                        AuditActivity.USER_DELETE_PROFILE,
                                                        rule.getProfilingRule()
                                                                .getId()
                                                                + "-"
                                                                + rule
                                                                        .getLocatorName(),
                                                        AdminPortletWebPage.USER_ADMINISTRATION);
                                    }
                                }
                            }
                        }
                    } catch (Exception e)
                    {
                        error(e.getMessage());
                    }
                    refreshData();
                }
            };
            SortableDataProvider dataProvider = new SortableDataProvider()
            {

                public int size()
                {
                    return getUserRules().size();
                }

                public IModel model(Object object)
                {
                    Map ruleMap = (Map) object;
                    return new Model((Serializable) ruleMap);
                }

                public Iterator iterator(int first, int count)
                {
                    return getUserRules().subList(first, first + count)
                            .iterator();
                }
            };
            IColumn[] columns =
            {
                    new CheckBoxPropertyColumn(new Model(" "), "checked"),
                    new PropertyColumn(new ResourceModel("user.ruleName"),
                            "rule.locatorName"),
                    new PropertyColumn(new ResourceModel("user.ruleValue"),
                            "rule.profilingRule")};
            DataTable userRulesDataTable = new DataTable("entries", columns,
                    dataProvider, 10)
            {

                protected Item newRowItem(String id, int index, IModel model)
                {
                    return new PortletOddEvenItem(id, index, model);
                }
            };
            userRulesDataTable.addTopToolbar(new HeadersToolbar(
                    userRulesDataTable, dataProvider));
            userRulesDataTable.addBottomToolbar(new NavigationToolbar(
                    userRulesDataTable));
            userRulesForm.add(userRulesDataTable);
            userRulesForm.add(new Button("submit", new ResourceModel(
                    "common.delete")));
            add(userRulesForm);
            Form addRuleForm = new Form("addRuleForm")
            {

                protected void onSubmit()
                {
                    String locatorName = getLocatorName();
                    if (locatorName != null && locatorName.trim().length() > 0)
                    {
                        try
                        {
                            String ruleName = getRuleName();
                            Profiler profiler = getServiceLocator().getProfiler();
                            ProfilingRule profilingRule = profiler.getRule(ruleName);
                            if (profilingRule != null)
                            {
                                profiler.setRuleForPrincipal(getPrincipal(), profilingRule, locatorName);
                            }
                            else
                            {
                                log.error("Failed to set profiling rule for the principal. Invalid profiling rule: " + ruleName);
                            }
                            getServiceLocator()
                                    .getAuditActivity()
                                    .logAdminAuthorizationActivity(
                                            getPortletRequest()
                                                    .getUserPrincipal()
                                                    .getName(),
                                            getIPAddress(),
                                            getSelectedPrincipal(),
                                            AuditActivity.USER_ADD_PROFILE,
                                            ruleName + "-" + locatorName,
                                            AdminPortletWebPage.USER_ADMINISTRATION);
                        } catch (Exception e)
                        {
                            error(e.getMessage());
                        }
                        refreshData();
                    }
                }
            };
            addRuleForm.add(new Label("userruleNamelabel", new ResourceModel(
                    "user.ruleName")));
            addRuleForm.add(new Label("userruledesclabel", new ResourceModel(
                    "user.rule.desc")));
            RequiredTextField locatorNameField = new RequiredTextField(
                    "locatorName", new PropertyModel(this, "locatorName"));
            addRuleForm.add(locatorNameField);
            addRuleForm.add(new Label("userrulelabel", new ResourceModel(
                    "user.ruleValue")));
            DropDownChoice ruleNameField = new DropDownChoice("ruleName",
                    new PropertyModel(this, "ruleName"), getFullRules());
            addRuleForm.add(ruleNameField);
            addRuleForm.add(new Button("addRule", new ResourceModel(
                    "user.rule.add")));
            add(addRuleForm);
            add(new FeedbackPanel("feedback"));
        }

        protected void refreshData()
        {
            try
            {
                if (getPrincipal() != null)
                {
                    userEnabled = getServiceLocator().getUserManager().getUser(
                            getSelectedPrincipal()).isEnabled();
                }
                this.fullRules = new ArrayList();
                this.userRules = new ArrayList();
                for (Iterator it = getServiceLocator().getProfiler().getRules()
                        .iterator(); it.hasNext();)
                {
                    ProfilingRule rule = (ProfilingRule) it.next();
                    this.fullRules.add(rule);
                }
                if (getPrincipal() != null)
                {
                    for (Iterator it = getServiceLocator().getProfiler()
                            .getRulesForPrincipal(getPrincipal()).iterator(); it
                            .hasNext();)
                    {
                        PrincipalRule rule = (PrincipalRule) it.next();
                        Map ruleMap = new HashMap();
                        ruleMap.put("rule", rule);
                        ruleMap.put("checked", Boolean.FALSE);
                        this.userRules.add(ruleMap);
                    }
                }
            }
            catch (Exception e)
            {
                log.error("Failed to add user rules.", e);
            }
        }

        public void setLocatorName(String locatorName)
        {
            this.locatorName = locatorName;
        }

        public String getLocatorName()
        {
            return this.locatorName;
        }

        public void setRuleName(String ruleName)
        {
            this.ruleName = ruleName;
        }

        public String getRuleName()
        {
            return this.ruleName;
        }

        public List getFullRules()
        {
            return this.fullRules;
        }

        public List getUserRules()
        {
            return this.userRules;
        }
    }

    protected class NewUserPrincipalPanel extends Panel
    {

        protected String locatorName;

        protected String ruleName;

        protected List fullRules;

        protected List userRules;

        String userName;

        String password;

        boolean checkpass;

        String profilingRule;

        /**
         * @return the profilingRule
         */
        public String getProfilingRule()
        {
            return profilingRule;
        }

        /**
         * @param profilingRule
         *            the profilingRule to set
         */
        public void setProfilingRule(String profilingRule)
        {
            this.profilingRule = profilingRule;
        }

        /**
         * @return the userName
         */
        public String getUserName()
        {
            return userName;
        }

        /**
         * @param userName
         *            the userName to set
         */
        public void setUserName(String userName)
        {
            this.userName = userName;
        }

        /**
         * @return the password
         */
        public String getPassword()
        {
            return password;
        }

        /**
         * @param password
         *            the password to set
         */
        public void setPassword(String password)
        {
            this.password = password;
        }

        /**
         * @return the checkpass
         */
        public boolean isCheckpass()
        {
            return checkpass;
        }

        /**
         * @param checkpass
         *            the checkpass to set
         */
        public void setCheckpass(boolean checkpass)
        {
            this.checkpass = checkpass;
        }

        protected NewUserPrincipalPanel(String id)
        {
            super(id);
            
            PortletPreferences preferences = ((AbstractAdminWebApplication)getApplication()).getPortletRequest().getPreferences();
            
            final String defaultRole = preferences.getValue(DEFAULT_ROLE ,"");
            final String requiredRole = preferences.getValue(REQUIRED_ROLE, "");
            final String defaultProfile = preferences.getValue(DEFAULT_PROFILE ,"");
            final String defaultSubsite = preferences.getValue(DEFAULT_SUBSITE ,"");
            final String templateFolder = preferences.getValue(NEW_USER_TEMPLATE_DIR, "/_user/template/");
            final String subsiteRoot = preferences.getValue(SUB_SITE_ROOT,"");
            profilingRule = defaultProfile.toString();
            
            add(new FeedbackPanel("feedback"));
            Form userForm = new Form("newUserForm");
            add(userForm);
            RequiredTextField userName = new RequiredTextField("userName",
                    new PropertyModel(this, "userName"));
            userName.add(new PrincipalNameValidator());
            userForm.add(userName);
            TextField password = new PasswordTextField("password",
                    new PropertyModel(this, "password"));
            userForm.add(password);
            CheckBox changePassword = new CheckBox("checkpass",
                    new PropertyModel(this, "checkpass"));
            userForm.add(changePassword);
            DropDownChoice profilingtRole = new DropDownChoice("profilingRule",
                    new PropertyModel(this, "profilingRule"), getProfileList());
            userForm.add(profilingtRole);
            Button newUser = new Button("addUser",new ResourceModel(principalParam +".add.button"))
            {

                @Override
                public void onSubmit()
                {
                    UserManager userManager = (UserManager)getManager();
                    JetspeedPrincipal principal = getManager().newPrincipal(
                            getUserName(), false);
                    RoleManager roleManager = ((AbstractAdminWebApplication)getApplication()).getServiceLocator().getRoleManager();
                    PageManager pageManager = ((AbstractAdminWebApplication) getApplication()).getServiceLocator().getPageManager();
                    try
                    {
                        getManager().addPrincipal(principal, null);
                        User user = userManager.getUser(getUserName());

                        if(!StringUtils.isEmpty(defaultRole))
                        {
                            roleManager.addRoleToUser(getUserName(), defaultRole);
                        }
                        if(!StringUtils.isEmpty(requiredRole))
                        {
                            roleManager.addRoleToUser(getUserName(), requiredRole);
                        }
                        
                        Profiler profiler = getServiceLocator().getProfiler();
                        
                        if (!StringUtils.isEmpty(getProfilingRule()))
                        {
                            ProfilingRule profilingRule = profiler.getRule(getProfilingRule());
                            
                            if (profilingRule != null)
                            {
                                profiler.setRuleForPrincipal(principal, profilingRule, "default");
                            }
                            else
                            {
                                log.error("Failed to set profiling rule for principal. Invalid profiling rule: " + getProfilingRule());
                            }
                        }
                        else if (!StringUtils.isEmpty(defaultProfile)) 
                        {
                            ProfilingRule defaultProfilingRule = profiler.getRule(defaultProfile);
                            
                            if (defaultProfilingRule != null)
                            {
                                profiler.setRuleForPrincipal(principal, defaultProfilingRule, "default");
                            }
                            else
                            {
                                if (log.isDebugEnabled())
                                {
                                    log.debug("Default profiling rule is not applied to the principal because the default profiling rule is not found: " + defaultProfile);
                                }
                            }
                        }
                        
                        String subSite;
                        if (!StringUtils.isEmpty(defaultSubsite))
                        {
                            user.getSecurityAttributes().getAttribute(User.JETSPEED_USER_SUBSITE_ATTRIBUTE,true).setStringValue(defaultSubsite);
                            user.getSecurityAttributes().getAttribute(User.JETSPEED_USER_SUBSITE_ATTRIBUTE, true).setStringValue(defaultSubsite);
                            subSite = subsiteRoot + defaultSubsite + Folder.USER_FOLDER + user.getName();
                        }
                        
                        else
                        {
                            subSite = Folder.USER_FOLDER + user.getName();;
                        }
                        
                        if (!StringUtils.isEmpty(templateFolder))
                        {
                            try
                            {
                                Folder source = pageManager.getFolder(templateFolder);
                                pageManager.deepCopyFolder(source, subSite, user.getName());
                            }
                            catch (FolderNotFoundException e)
                            {
                                error(e.getMessage());
                            }
                            catch (NodeException e)
                            {
                                error(e.getMessage());
                            }
                        }
                        userManager.updateUser(user);
                                                
                        PasswordCredential credential = userManager
                                .getPasswordCredential(user);
                        if (!StringUtils.isEmpty(getPassword()))
                        {
                            credential.setPassword(getPassword(), false);
                        }
                        credential.setUpdateRequired(isCheckpass());
                        userManager.storePasswordCredential(credential);
                        setPrincipal(user);
                        controlPannels(true);
                    }
                    catch (SecurityException jSx)
                    {
                        log.error("Failed to update user.", jSx);
                    }
                    principalDataProvider.refresh(getManager(),getRoleManager(),getSearchString());
                }
            };
            userForm.add(newUser);
        }
    }

    protected class NewPrincipalPanel extends Panel
    {

        protected String userName;        

        /**
         * @return the userName
         */
        public String getUserName()
        {
            return userName;
        }

        /**
         * @param userName
         *            the userName to set
         */
        public void setUserName(String userName)
        {
            this.userName = userName;
        }

        protected NewPrincipalPanel(String id)
        {
            super(id);
            
            
            
            
            Form userForm = new Form("newUserForm");
            add(userForm);      
            add(new FeedbackPanel("feedback"));
            userForm.add(new Label("userNameLabel",new ResourceModel(principalParam + ".new.button")));            
            TextField userName = new RequiredTextField("userName",new PropertyModel(this, "userName"));
            userName.add(new PrincipalNameValidator());
            userForm.add(userName);
            Button newUser = new Button("addUser",new ResourceModel(principalParam +".add.button")){
				@Override
				public void onSubmit() {
					JetspeedPrincipal principal =  getManager().newPrincipal(getUserName(),false);
					try{
						getManager().addPrincipal(principal, null);
						setPrincipal(principal);
						controlPannels(true);
						principalDataProvider.refresh(getManager(),getSearchString());
					}
				    catch (SecurityException jSx)
	                {
				        log.error("Failed to add principal.", jSx);
	                }
				}            	
            };
            userForm.add(newUser);
        }
    }

    private class PrincipalNameValidator extends AbstractValidator
    {

        public PrincipalNameValidator()
        {
        }
        @Override
        protected void onValidate(IValidatable validatable)
        {
            String userName = (String) validatable.getValue();
                if (getManager().getPrincipal(userName) != null)
                {
                    error(validatable);
                }
        }
    }

    protected class PrincipalStatusPanel extends Panel
    {

        protected boolean principalEnabled = false;

        protected String name;

        /**
         * @param principalEnabled
         *            the principalEnabled to set
         */
        public void setPrincipalEnabled(boolean principalEnabled)
        {
            this.principalEnabled = principalEnabled;
        }

        /**
         * @return the principalEnabled
         */
        public boolean isPrincipalEnabled()
        {
            return principalEnabled;
        }

        /**
         * @return the name
         */
        protected String getName()
        {
            return name;
        }        

        protected PrincipalStatusPanel(String id)
        {
            super(id);
            Form statusForm = new Form("statusForm");
            add(statusForm);
            statusForm.add(new CheckBox("principalStatus", new PropertyModel(this,
                    "principalEnabled")));
            statusForm.add(new Label("enabledLabel", new ResourceModel(
                    "common.enabled")));
            statusForm.add(new Button("submit", new ResourceModel(
                    principalParam + ".update.button")){
                @Override
                public void onSubmit()
                {
                    JetspeedPrincipal principal = getManager().getPrincipal(
                            getName());
                    try
                    {
                        principal.setEnabled(isPrincipalEnabled());
                        getManager().updatePrincipal(principal);
                        setPrincipal(principal);
                        principalDataProvider.refresh(getManager(),getSearchString());
                    } catch (SecurityException jSx)
                    {
                        error(jSx.getMessage());
                    }
                }
            });
            statusForm.add(new Button("remove", new ResourceModel(principalParam + ".remove.button")){
                @Override
                public void onSubmit()
                {
                    try
                    {
                        getManager().removePrincipal(principal.getName());
                        setPrincipal(null);
                        controlPannels(false);
                        principalDataProvider.refresh(getManager(),getSearchString());
                    }
                    catch (SecurityException e)
                    {
                        error(e.getMessage());
                    }
                }
                
            }.add(new JavascriptEventConfirmation("onclick", new ResourceModel("action.delete.confirm"))));
            add(new FeedbackPanel("feedback"));
        }

        @Override
        protected void onBeforeRender()
        {
            if (getPrincipal() != null)
            {
                this.principalEnabled = getPrincipal().isEnabled();
                this.name = getPrincipal().getName();
            }
            super.onBeforeRender();
        }
    }

    protected class PrincipalCredentialsPanel extends Panel
    {

        protected String userName;

        protected String credentialValue;

        protected boolean credentialUpdateRequired;

        protected Date lastAuthenticationDate;

        protected boolean credentialEnabled;

        protected Date credentialExpirationDate;

        protected String userExpiredFlag;

        protected UserCredential credential;

        public PrincipalCredentialsPanel(String ID)
        {
            super(ID);
            Form form = new Form("userCredentialForm")
            {

                protected void onSubmit()
                {
                    try
                    {
                        boolean passwordSet = false;
                        UserManager manager = (UserManager) getManager();
                        PasswordCredential credential = manager
                                .getPasswordCredential((User) getPrincipal());
                        if (getCredentialValue() != null
                                && getCredentialValue().trim().length() > 0)
                        {
                            credential.setPassword(null, getCredentialValue());
                            getServiceLocator()
                                    .getAuditActivity()
                                    .logAdminCredentialActivity(
                                            getPortletRequest()
                                                    .getUserPrincipal()
                                                    .getName(),
                                            getIPAddress(),
                                            getPrincipal().getName(),
                                            AuditActivity.PASSWORD_RESET,
                                            AdminPortletWebPage.USER_ADMINISTRATION);
                            passwordSet = true;
                        }
                        if (getCredentialUpdateRequired() != credential
                                .isUpdateRequired())
                        {
                            credential
                                    .setUpdateRequired(getCredentialUpdateRequired());
                            getServiceLocator()
                                    .getAuditActivity()
                                    .logAdminCredentialActivity(
                                            getPortletRequest()
                                                    .getUserPrincipal()
                                                    .getName(),
                                            getIPAddress(),
                                            getPrincipal().getName(),
                                            AuditActivity.PASSWORD_UPDATE_REQUIRED,
                                            AdminPortletWebPage.USER_ADMINISTRATION);
                        }
                        if (getCredentialEnabled() != credential.isEnabled())
                        {
                            credential.setEnabled(getCredentialEnabled());
                            String activity = (getCredentialEnabled() ? AuditActivity.PASSWORD_ENABLED
                                    : AuditActivity.PASSWORD_DISABLED);
                            getServiceLocator()
                                    .getAuditActivity()
                                    .logAdminCredentialActivity(
                                            getPortletRequest()
                                                    .getUserPrincipal()
                                                    .getName(),
                                            getIPAddress(),
                                            getPrincipal().getName(),
                                            activity,
                                            AdminPortletWebPage.USER_ADMINISTRATION);
                        }
                        String expiredFlagStr = getUserExpiredFlag();
                        if (expiredFlagStr != null)
                        {
                            if (!passwordSet
                                    && expiredFlagStr.equals("expired"))
                            {
                                java.sql.Date today = new java.sql.Date(
                                        new Date().getTime());
                                credential.setExpirationDate(today);
                                getServiceLocator()
                                        .getAuditActivity()
                                        .logAdminCredentialActivity(
                                                getPortletRequest()
                                                        .getUserPrincipal()
                                                        .getName(),
                                                getIPAddress(),
                                                getPrincipal().getName(),
                                                AuditActivity.PASSWORD_EXPIRE,
                                                AdminPortletWebPage.USER_ADMINISTRATION);
                            } else if (expiredFlagStr.equals("extend"))
                            {
                                // getUserManager().setPasswordExpiration(getUserName(),
                                // null);
                                // TODO confirm with Ate/David
                                credential.setExpirationDate(MAX_DATE);
                                getServiceLocator()
                                        .getAuditActivity()
                                        .logAdminCredentialActivity(
                                                getPortletRequest()
                                                        .getUserPrincipal()
                                                        .getName(),
                                                getIPAddress(),
                                                getPrincipal().getName(),
                                                AuditActivity.PASSWORD_EXTEND,
                                                AdminPortletWebPage.USER_ADMINISTRATION);
                            } else if (expiredFlagStr.equals("unlimited"))
                            {
                                credential.setExpirationDate(MAX_DATE);
                                getServiceLocator()
                                        .getAuditActivity()
                                        .logAdminCredentialActivity(
                                                getPortletRequest()
                                                        .getUserPrincipal()
                                                        .getName(),
                                                getIPAddress(),
                                                getPrincipal().getName(),
                                                AuditActivity.PASSWORD_UNLIMITED,
                                                AdminPortletWebPage.USER_ADMINISTRATION);
                            }
                        }
                        manager.storePasswordCredential(credential);
                    } catch (InvalidPasswordException ipe)
                    {
                        error(ipe.getMessage());
                    } catch (InvalidNewPasswordException inpe)
                    {
                        error(inpe.getMessage());
                    } catch (PasswordAlreadyUsedException paue)
                    {
                        error(paue.getMessage());
                    } catch (SecurityException e)
                    {
                        error(e.getMessage());
                    }
                    refreshData();
                    setPrincipal(getPrincipal());
                }
            };
            PasswordTextField credentialValueField = new PasswordTextField(
                    "credentialValue", new PropertyModel(this,
                            "credentialValue"));
            form.add(new Label("passwordLabel", new ResourceModel(
                    "user.login.password")));
            credentialValueField.setRequired(false);
            form.add(credentialValueField);
            CheckBox credentialUpdateRequiredField = new CheckBox(
                    "credentialUpdateRequired", new PropertyModel(this,
                            "credentialUpdateRequired"));
            form.add(new Label("changerequiredLabel", new ResourceModel(
                    "user.change.required")));
            form.add(credentialUpdateRequiredField);
            Label lastAuthenticationDateLabel = new Label(
                    "lastAuthenticationDate", new PropertyModel(this,
                            "lastAuthenticationDate"));
            form.add(new Label("lastLogonLabel", new ResourceModel(
                    "user.login.last.logon")));
            form.add(lastAuthenticationDateLabel);
            CheckBox credentialEnabledField = new CheckBox("credentialEnabled",
                    new PropertyModel(this, "credentialEnabled"));
            form.add(new Label("enabledLabel", new ResourceModel(
                    "common.enabled")));
            form.add(credentialEnabledField);
            Label credentialExpirationDateLabel = new Label(
                    "credentialExpirationDate", new PropertyModel(this,
                            "credentialExpirationDate"));
            form.add(new Label("expiresLabel", new ResourceModel(
                    "user.login.expires")));
            form.add(credentialExpirationDateLabel);
            List expiredFlagChoices = new ArrayList();
            RadioChoice userExpiredFlagField = new RadioChoice(
                    "userExpiredFlag", new PropertyModel(this,
                            "userExpiredFlag"), expiredFlagChoices);
            // TODO change to use localize values
            expiredFlagChoices.add("Active");// new
                                             // ResourceModel("user.login.expires.active").);
            expiredFlagChoices.add("Expired");// new
                                              // ResourceModel("user.login.expires.expired"));
            expiredFlagChoices.add("Extend");// new
                                             // ResourceModel("user.login.expires.extend"));
            expiredFlagChoices.add("Unlimited");// new
                                                // ResourceModel("user.login.expires.unlimited"));
            form.add(new Button("submitForm",
                    new ResourceModel("common.update")));
            form.add(userExpiredFlagField);
            add(form);
        }

        public void setCredentialValue(String credentialValue)
        {
            this.credentialValue = credentialValue;
        }

        public String getCredentialValue()
        {
            return this.credentialValue;
        }

        public void setCredentialUpdateRequired(boolean credentialUpdateRequired)
        {
            this.credentialUpdateRequired = credentialUpdateRequired;
        }

        public boolean getCredentialUpdateRequired()
        {
            return this.credentialUpdateRequired;
        }

        public void setLastAuthenticationDate(Date lastAuthenticationDate)
        {
            this.lastAuthenticationDate = lastAuthenticationDate;
        }

        public Date getLastAuthenticationDate()
        {
            return this.lastAuthenticationDate;
        }

        public void setCredentialEnabled(boolean credentialEnabled)
        {
            this.credentialEnabled = credentialEnabled;
        }

        public boolean getCredentialEnabled()
        {
            return this.credentialEnabled;
        }

        public void setCredentialExpirationDate(Date credentialExpirationDate)
        {
            this.credentialExpirationDate = credentialExpirationDate;
        }

        public Date getCredentialExpirationDate()
        {
            return this.credentialExpirationDate;
        }

        public void setUserExpiredFlag(String userExpiredFlag)
        {
            this.userExpiredFlag = userExpiredFlag;
        }

        public String getUserExpiredFlag()
        {
            return this.userExpiredFlag;
        }

        protected void onBeforeRender()
        {
            super.onBeforeRender();
            if (getPrincipal().getName() != this.userName)
            {
                refreshData();
                this.userName = getPrincipal().getName();
            }
        }

        protected void refreshData()
        {
            try
            {
                UserManager manager = (UserManager) getManager();
                credential = manager
                        .getPasswordCredential((User) getPrincipal());
                setCredentialUpdateRequired(credential.isUpdateRequired());
                setCredentialEnabled(credential.isEnabled());
                setLastAuthenticationDate(credential
                        .getLastAuthenticationDate());
                setCredentialExpirationDate(credential.getExpirationDate());
                setUserExpiredFlag(credential.isExpired() ? "expired"
                        : "active");
            }
            catch (SecurityException secExp)
            {
                log.error("Failed to refresh user credentials.", secExp);
            }
        }
    }

    protected class PrincipalAttributesPanel extends Panel
    {

        protected String userName;

        protected String userAttrName;

        protected String userAttrValue;

        protected List userAttributes;

        public PrincipalAttributesPanel(String id)
        {
            super(id);
            Form userAttrsForm = new Form("userAttrsForm");
            add(new FeedbackPanel("feedback"));
            userAttrsForm.add(new Label("attrNameLabel",new ResourceModel("common.name")));
            userAttrsForm.add(new Label("attrValueLabel",new ResourceModel("common.value")));
            add(userAttrsForm);
            PageableListView usersList = new PageableListView(
                    "attributeEntries", new PropertyModel(this,
                            "userAttributes"), 10)
            {

                protected void populateItem(ListItem item)
                {
                    final Map<String, SecurityAttribute> attributes = (Map<String, SecurityAttribute>) item
                            .getModelObject();
                    final SecurityAttribute attrib = attributes.get("value");
                    item.add(new TextField("name", new Model(attrib.getName()))
                    {

                        @Override
                        public boolean isEnabled()
                        {
                            return !attrib.isReadOnly();
                        }
                    });
                    item.add(new TextField("value", new PropertyModel<String>(attrib,"stringValue"))
                    {

                        @Override
                        public boolean isEnabled()
                        {
                            return !attrib.isReadOnly();
                        }
                    });
                    if (!attrib.isReadOnly())
                    {
                        Link deleteLink = new Link("link", item.getModel())
                        {

                            @Override
                            public void onClick()
                            {
                                try
                                {
                                    getPrincipal().getSecurityAttributes()
                                            .removeAttribute(attrib.getName());
                                    getManager()
                                            .updatePrincipal(getPrincipal());
                                }
                                catch (SecurityException e)
                                {
                                    log.error("Failed to update principal.", e);
                                }
                                setPrincipal(getPrincipal());
                                refreshData();
                            }
                        };
                        deleteLink.add(new Label("deleteLabel",
                                new ResourceModel("common.delete")));
                        item.add(deleteLink);
                    }
                }
            };
            userAttrsForm.add(usersList);
            userAttrsForm.add(new PagingNavigator("navigator", usersList));
            Button updateAttrButton = new Button("updateAttr",
                    new ResourceModel("common.update"))
            {

                public void onSubmit()
                {
                    Map<String, SecurityAttribute> attribs = getPrincipal().getSecurityAttributes().getAttributeMap() ;
                    for (Iterator it = userAttributes.iterator(); it.hasNext();)
                    {
                        Map userAttrMap = (Map) it.next();
                        String userAttrName = (String) userAttrMap.get("name");
                        String userAttrValue = ((SecurityAttribute) userAttrMap
                                .get("value")).getStringValue();
                        String oldUserAttrValue = attribs.get(userAttrName).getStringValue();
                        Map<String,SecurityAttribute> userAttributes = getPrincipal().getSecurityAttributes().getAttributeMap();
                        try
                        {
                            getPrincipal().getSecurityAttributes().getAttribute(userAttrName).setStringValue(userAttrValue);
                        }
                        catch (SecurityException e)
                        {
                            log.error("Failed to update security attribute of principal.", e);
                        }                        
                        getServiceLocator()
                                .getAuditActivity()
                                .logAdminAttributeActivity(
                                        getPrincipal().getName(),
                                        getIPAddress(),
                                        getPrincipal().getName(),
                                        AuditActivity.USER_UPDATE_ATTRIBUTE,
                                        userAttrName, oldUserAttrValue,
                                        userAttrValue,
                                        AdminPortletWebPage.USER_ADMINISTRATION);
                    }
                    try
                    {
                        getManager().updatePrincipal(getPrincipal());
                    }
                    catch (SecurityException e)
                    {
                        error(e.getMessage());
                    }
                    refreshData();
                }
            };       
            
            userAttrsForm.add(updateAttrButton);
            Form addAttrForm = new Form("addAttrForm")
            {

                protected void onSubmit()
                {
                    String userAttrName = getUserAttrName();
                    String userAttrValue = getUserAttrValue();
                    if (userAttrName != null
                            && userAttrName.trim().length() > 0)
                    {
                        // Preferences prefs = user.getUserAttributes();
                        // prefs.put(userAttrName, userAttrValue);
                        try
                        {
                            getPrincipal().getSecurityAttributes()
                                    .getAttribute(userAttrName, true)
                                    .setStringValue(userAttrValue);
                            getManager().updatePrincipal(getPrincipal());
                            getServiceLocator()
                                    .getAuditActivity()
                                    .logAdminAttributeActivity(
                                            getPrincipal().getName(),
                                            getIPAddress(),
                                            getPrincipal().getName(),
                                            AuditActivity.USER_ADD_ATTRIBUTE,
                                            userAttrName,
                                            "",
                                            userAttrValue,
                                            AdminPortletWebPage.USER_ADMINISTRATION);
                        }
                        catch (SecurityException e)
                        {
                            log.error("Failed to update security attribute of principal.", e);
                        }
                    }
                    setPrincipal(getPrincipal());
                    refreshData();
                }
            };
            add(addAttrForm);
            addAttrForm.add(new Label("nameLabel", new ResourceModel(
                    "common.name")));
            TextField userAttrNameField = new RequiredTextField("userAttrName",
                    new PropertyModel(this, "userAttrName"));
            addAttrForm.add(userAttrNameField);
            addAttrForm.add(new Label("valueLabel", new ResourceModel(
                    "common.value")));
            TextField userAttrValueField = new RequiredTextField(
                    "userAttrValue", new PropertyModel(this, "userAttrValue"));
            addAttrForm.add(userAttrValueField);
            addAttrForm.add(new Button("addAttr", new ResourceModel(
                    "common.attribute.add")));
        }

        public List getUserAttributes()
        {
            return this.userAttributes;
        }

        public void setUserAttrName(String userAttrName)
        {
            this.userAttrName = userAttrName;
        }

        public String getUserAttrName()
        {
            return this.userAttrName;
        }

        public void setUserAttrValue(String userAttrValue)
        {
            this.userAttrValue = userAttrValue;
        }

        public String getUserAttrValue()
        {
            return this.userAttrValue;
        }

        protected void onBeforeRender()
        {
            if (getPrincipal().getName() != this.userName)
            {
                refreshData();
                this.userName = getPrincipal().getName();
            }
            super.onBeforeRender();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.apache.wicket.Component#onDetach()
         */
        @Override
        protected void onDetach()
        {
            // TODO Auto-generated method stub
            super.onDetach();
        }

        protected void refreshData()
        {
            this.userAttributes = new LinkedList();
            if (getPrincipal() != null)
            {
                Map<String, SecurityAttribute> userAttribute = getPrincipal()
                        .getSecurityAttributes().getAttributeMap();
                Map item;
                String attributeKey;
                Iterator<String> attribsKeys = userAttribute.keySet()
                        .iterator();
                while (attribsKeys.hasNext())
                {
                    attributeKey = attribsKeys.next();
                    item = new HashMap();
                    item.put("name", attributeKey);
                    item.put("value", userAttribute.get(attributeKey));
                    userAttributes.add(item);
                }
            }
        }
    }

    protected class AssociationTypePanel extends Panel
    {

        protected List names = new ArrayList();

        protected List associations = new ArrayList();

        protected String associationName;

        protected JetspeedPrincipal associationPrincipal;

        protected JetspeedPrincipalAssociationType associationType;

        protected boolean associationsFrom;
        
        protected boolean admin;
        protected boolean modificationAllowed = true;
        
        /**
         * @return the associations
         */
        public List getAssociations()
        {
            return associations;
        }

        /**
         * @param associations
         *            the associations to set
         */
        public void setAssociations(List associations)
        {
            this.associations = associations;
        }

        /**
         * @return the selectedPrincipal
         */
        public JetspeedPrincipal getAssociationPrincipal()
        {
            return associationPrincipal;
        }

        /**
         * @param selectedPrincipal
         *            the selectedPrincipal to set
         */
        public void setAssociationPrincipal(JetspeedPrincipal selectedPrincipal)
        {
            this.associationPrincipal = selectedPrincipal;
        }

        /*
         * public void setAssociationPrincipal(JetspeedPrincipal
         * selectedPrincipal) { this.asstnPrincipal = selectedPrincipal; }
         */
        /**
         * @return the names
         */
        public List getNames()
        {
            return names;
        }

        /**
         * @param names
         *            the names to set
         */
        public void setNames(List names)
        {
            this.names = names;
        }

        public AssociationTypePanel(String id,
                JetspeedPrincipalAssociationType AssociationType)
        {
            super(id);
            this.associationType = AssociationType;
            associationName = AssociationType.getAssociationName();
            final String assoctionName = AssociationType.getAssociationName();
            String adminRole = getServiceLocator().getPortalConfiguration().getString(PortalConfigurationConstants.ROLES_DEFAULT_ADMIN);            
            admin = getPortletRequest().isUserInRole(adminRole);
            if (!admin && !principal.getType().getName().equals(JetspeedPrincipalType.USER))
            {
                // no non-user type principal modification is allowed if the current user itself doesn't have this principal 
                UserSubjectPrincipal currentUser = (UserSubjectPrincipal)getPortletRequest().getUserPrincipal();                
                if (!hasPrincipal(currentUser.getSubject(), principal))
                {
                    modificationAllowed = false;                    
                }                    
            }
            refreshList();
            ListView commentListView = new ListView("comments",
                    new PropertyModel(this, "associations"))
            {

                public void populateItem(final ListItem listItem)
                {
                    final JetspeedPrincipal principal = (JetspeedPrincipal) listItem
                            .getModelObject();
                    listItem.add(new Label("Name", principal.getName()));
                    boolean deleteAllowed = modificationAllowed;
                    Link deleteLink = new Link("delete")
                    {

                        @Override
                        public void onClick()
                        {
                            try
                            {
                                if (!associationsFrom)
                                {
                                    getManager().removeAssociation(principal,
                                            getPrincipal(), assoctionName);
                                } else
                                {
                                    getManager().removeAssociation(
                                            getPrincipal(), principal,
                                            assoctionName);
                                }
                                refreshList();
                            } catch (Exception e)
                            {
                                // TODO: handle exception
                            }
                        }
                    };
                    deleteLink.add(new Label("deleteLabel", new ResourceModel(
                            "common.delete")));
                    if (!admin && deleteAllowed && !principal.getType().getName().equals(JetspeedPrincipalType.USER))
                    {
                        // restrict deleting non-user type principals to only those the current user itself has
                        UserSubjectPrincipal currentUser = (UserSubjectPrincipal)getPortletRequest().getUserPrincipal();                
                        if (!hasPrincipal(currentUser.getSubject(), principal))
                        {
                            deleteAllowed = false;
                        }                    
                    }
                    if (!deleteAllowed || !modificationAllowed)
                    {
                        deleteLink.setEnabled(false);
                        deleteLink.setVisible(false);
                    }
                    listItem.add(deleteLink);
                }
            };
            if(AssociationType.getFromPrincipalType().equals(principalType))
            {
                add(new Label("principalReleation",new ResourceModel(AssociationType.getToPrincipalType().getName())));    
            }
            else
            {
                add(new Label("principalReleation",new ResourceModel(AssociationType.getFromPrincipalType().getName())));
            }
            add(commentListView);
            add(new FeedbackPanel("feedback"));
            Form assocationsForm = new Form("assocationsForm");
            add(assocationsForm);
            DropDownChoice dropDown = new DropDownChoice(
                    "associationPrincipal", new PropertyModel(this,
                            "associationPrincipal"), getNames(),
                    new ChoiceRenderer("name", "name"));
            dropDown.setRequired(true);
            assocationsForm.add(dropDown);
            Button addRelations = new Button("addRelations", new ResourceModel("common.association.add"))
            {

                @Override
                public void onSubmit()
                {
                    try
                    {
                        JetspeedPrincipal toPrincipal = getPrincipal();
                        // JetspeedPrincipal fromPrincipal =
                        // getJetspeedPrincipalManagerProvider().getManager(type).getPrincipal(getAssociationPrincipal());
                        JetspeedPrincipal fromPrincipal = getAssociationPrincipal();
                        if (!associationsFrom)
                        {
                            getManager().addAssociation(fromPrincipal,
                                    toPrincipal, associationName);
                        } else
                        {
                            getManager().addAssociation(toPrincipal,
                                    fromPrincipal, associationName);
                        }
                        associationPrincipal = null;
                        refreshList();
                    }
                    catch (SecurityException sExc)
                    {
                        log.error("Failed to add associations.", sExc);
                    }
                }
            };
            assocationsForm.add(addRelations);
        }

        private JetspeedPrincipalManager getBaseManager(
                JetspeedPrincipalType type)
        {
            return getServiceLocator().getJetspeedPrincipalManagerProvider()
                    .getManager(type);
        }

        private void refreshList()
        {
        	names.clear();
        	if (!principal.getType().equals(
                    associationType.getFromPrincipalType()))
            {
                associations = getBaseManager(
                        associationType.getFromPrincipalType())
                        .getAssociatedTo(principal.getName(),
                                principal.getType(),
                                associationType.getAssociationName());
            	if (modificationAllowed)
            	{
                    List tempNames = getBaseManager(
                            associationType.getFromPrincipalType()).getPrincipals(
                            "");
                    for (int index = 0; index < tempNames.size(); index++)
                    {
                        
                        JetspeedPrincipal tmpPrincipal = (JetspeedPrincipal)tempNames.get(index);
                        if (!(tmpPrincipal.getType().getName().equals(principal.getType().getName()) &&
                                        tmpPrincipal.getName().equals(principal.getName())))
                        {
                            names.add(tmpPrincipal);
                        }
                    }
            	}
                associationsFrom = false;
            } 
        	else
            {
            	associations = getBaseManager(
                        associationType.getToPrincipalType())
                        .getAssociatedFrom(principal.getName(),
                                principal.getType(),
                                associationType.getAssociationName());
            	if (modificationAllowed)
            	{            	    
                    List tempNames = getBaseManager(
                            associationType.getToPrincipalType()).getPrincipals("");
                    for (int index = 0; index < tempNames.size(); index++)
                    {
                        JetspeedPrincipal tmpPrincipal = (JetspeedPrincipal)tempNames.get(index);
                        if (!(tmpPrincipal.getType().getName().equals(principal.getType().getName()) &&
                                        tmpPrincipal.getName().equals(principal.getName())))
                        {
                            names.add(tmpPrincipal);
                        }
                    }
                }
                associationsFrom = true;
            }
        	if (modificationAllowed)
        	{
                for (int count = 0; count < associations.size(); count++)
                {
                    JetspeedPrincipal tmpPrincipal = (JetspeedPrincipal) associations.get(count);
                    JetspeedPrincipal listPrincipal;
                    for (int index = names.size()-1; index > -1; index--)
                    {
                        listPrincipal = (JetspeedPrincipal) names.get(index);
                        if (listPrincipal.getName().equals(tmpPrincipal.getName()))
                        {
                            names.remove(index);
                        }
                    }
                }
        	}
            if (names.size() > 0 && !admin)
            {
                // restrict creating new associations to only those the user itself belongs to
                String jptName = associationsFrom ? associationType.getToPrincipalType().getName() : associationType.getFromPrincipalType().getName();
                if (!jptName.equals(JetspeedPrincipalType.USER))
                {
                    // get all current user principals of asssignable type as restricted list
                    UserSubjectPrincipal currentUser = (UserSubjectPrincipal)getPortletRequest().getUserPrincipal();                
                    List<JetspeedPrincipal> filter = SubjectHelper.getPrincipals(currentUser.getSubject(), jptName);
                    if (filter.isEmpty())
                    {
                        names.clear();
                    }
                    else
                    {
                        for (int index = names.size()-1; index > -1; index--)
                        {
                            boolean found = false;
                            JetspeedPrincipal listPrincipal = (JetspeedPrincipal) names.get(index);
                            for (int count = 0; count < filter.size(); count++)
                            {
                                JetspeedPrincipal tmpPrincipal = filter.get(count);
                                if (listPrincipal.getName().equals(tmpPrincipal.getName()))
                                {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found)
                            {
                                names.remove(index);
                            }
                        }
                    }
                }
            }
        }
    }

    protected class PrincipalAssociationsPanel extends Panel
    {

        protected List tabs;

        /**
         * @return the selectedAssociationType
         */
        public String getSelectedAssociationType()
        {
            return selectedAssociationType;
        }

        /**
         * @param selectedAssociationType
         *            the selectedAssociationType to set
         */
        public void setSelectedAssociationType(String selectedAssociationType)
        {
            this.selectedAssociationType = selectedAssociationType;
        }

        private List<JetspeedPrincipalAssociationType> associationTypes;

        private String selectedAssociationType;

        /**
         * @return the associationTypes
         */
        public List<JetspeedPrincipalAssociationType> getAssociationTypes()
        {
            return associationTypes;
        }

        public PrincipalAssociationsPanel(String id)
        {
            super(id);
            tabs = new ArrayList();
            this.associationTypes = ((JetspeedPrincipalManager) getManager())
                    .getAssociationTypes();
            ITab tab;
            for (JetspeedPrincipalAssociationType associationType : this.associationTypes)
            {
                // if
                // (!associationType.getToPrincipalType().equals(principalType))
                // {
                final JetspeedPrincipalAssociationType tempAssociation = associationType;
                final JetspeedPrincipalType fromAssociationType = associationType
                        .getFromPrincipalType();
                final JetspeedPrincipalType toAssociationType = associationType
                        .getToPrincipalType();
                final String associationName = associationType
                        .getAssociationName();
                if (fromAssociationType.getName().equals(
                        getPrincipal().getType().getName()))
                {
                    tab = new AbstractTab(new Model(toAssociationType.getName()
                            + " - " + associationType.getAssociationName()))
                    {

                        public Panel getPanel(String panelId)
                        {
                            return new AssociationTypePanel(panelId,
                                    tempAssociation);
                        }
                    };
                } else
                {
                    tab = new AbstractTab(new Model(fromAssociationType
                            .getName()
                            + " - " + associationType.getAssociationName()))
                    {

                        public Panel getPanel(String panelId)
                        {
                            return new AssociationTypePanel(panelId,
                                    tempAssociation);
                        }
                    };
                }
                tabs.add(tab);
                // }
            }
            add(new TabbedPanel("assocTabs", tabs));
        }

        /**
         * @param AssociationTypes
         *            the AssociationTypes to set
         */
        public void setAssociationTypes(List AssociationTypes)
        {
            this.associationTypes = AssociationTypes;
        }
    }

    private String getSelectedPrincipal()
    {
        String principal = "";
        if (getPrincipal() != null)
        {
            principal = getPrincipal().getName();
        }
        return principal;
    }

    private JetspeedPrincipal getPrincipal()
    {
        return this.principal;
    }

    private List getPrincipalLists(String searchString)
    {
        return getManager().getPrincipals(searchString);
    }

    private JetspeedPrincipalManager getManager()
    {
        return getServiceLocator().getJetspeedPrincipalManagerProvider()
                .getManager(principalType);
    }

    private List getSubsites()
    {
        List nameList = null;
        try
        {
            nameList = getServiceLocator().getRoleManager().getRoleNames("");
        } catch (SecurityException e)
        {
            if (log.isErrorEnabled())
            {
                log.error("Error in getting role list");
            }
        }
        return nameList;
    }

    private List getProfileList()
    {
        return (List) getServiceLocator().getProfiler().getRules();
    }

    private List getRoleNames(String filter)
    {
        List nameList = null;
        try
        {
            nameList = getServiceLocator().getRoleManager()
                    .getRoleNames(filter);
        } catch (SecurityException e)
        {
            if (log.isErrorEnabled())
            {
                log.error("Error in getting role list");
            }
        }
        return nameList;
    }

    private void controlPannels(boolean userSelecteed)
    {
        TabbedPanel panel = (TabbedPanel) get("tabs");
        ITab tab;
        panel.getTabs().clear();
        boolean guestUserSelected;
        if (userSelecteed)
        {
            if (principalType.getName().equals(JetspeedPrincipalType.USER))
            {
                String adminRole = getServiceLocator().getPortalConfiguration().getString(PortalConfigurationConstants.ROLES_DEFAULT_ADMIN);
                boolean disableAdminEdit = true;
                try
                {
                    if (getPortletRequest().isUserInRole(adminRole) || !((RoleManager)getRoleManager()).isUserInRole(principal.getName(), adminRole))
                    {
                        disableAdminEdit = false;
                    }
                }
                catch (SecurityException e)
                {
                    // ignore
                }
                if (disableAdminEdit)
                {
                   return; 
                }
            }
            else if (principalType.getName().equals(JetspeedPrincipalType.ROLE))
            {
                String adminRole = getServiceLocator().getPortalConfiguration().getString(PortalConfigurationConstants.ROLES_DEFAULT_ADMIN);
                if (principal.getName().equals(adminRole) && !getPortletRequest().isUserInRole(adminRole))
                {                    
                    // disallow maintenance on admin role
                    return;
                }
            }
            guestUserSelected = (principalType.getName().equals(JetspeedPrincipalType.USER) && 
                            principal.getName().equals(((UserManager)getManager()).getAnonymousUser()));
            
            if (!guestUserSelected)
            {
                // if guest user: don't show status panel
                tab = new AbstractTab(new Model("Status"))
                {
                    public Panel getPanel(String panelId)
                    {
                        return new PrincipalStatusPanel(panelId);
                    }
                };
                panel.getTabs().add(tab);
            }
            tab = new AbstractTab(new Model("Associations"))
            {

                public Panel getPanel(String panelId)
                {
                    return new PrincipalAssociationsPanel(panelId);
                }
            };
            panel.getTabs().add(tab);
            tab = new AbstractTab(new Model("Attributes"))
            {

                public Panel getPanel(String panelId)
                {
                    return new PrincipalAttributesPanel(panelId);
                }
            };
            panel.getTabs().add(tab);
            if (!guestUserSelected && principalType.getName().equals(JetspeedPrincipalType.USER))
            {
                tab = new AbstractTab(new Model("Credentials"))
                {

                    public Panel getPanel(String panelId)
                    {
                        return new PrincipalCredentialsPanel(panelId);
                    }
                };
                panel.getTabs().add(tab);
            }
            if (principalType.getName().equals(JetspeedPrincipalType.USER))
            {
                tab = new AbstractTab(new Model("User Profile"))
                {

                    public Panel getPanel(String panelId)
                    {
                        return new UserPrincipalProfilePanel(panelId);
                    }
                };
                panel.getTabs().add(tab);
            } 
            panel.setSelectedTab(0);
        } else
        {
            if (principalType.getName().equals(JetspeedPrincipalType.USER))
            {
                tab = new AbstractTab(new Model("New "
                        + principalType.getName().toUpperCase()))
                {

                    public Panel getPanel(String panelId)
                    {
                        return new NewUserPrincipalPanel(panelId);
                    }
                };
            } else
            {
                tab = new AbstractTab(new Model("New "
                        + principalType.getName().toUpperCase()))
                {

                    public Panel getPanel(String panelId)
                    {
                        return new NewPrincipalPanel(panelId);
                    }
                };
            }
            panel.getTabs().add(tab);
            panel.setSelectedTab(0);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.wicket.Page#onBeforeRender()
     */
    @Override
    protected void onBeforeRender()
    {
        if (getPrincipal() != null)
        {
            Label label = (Label) get("userLabel");
            label.setVisible(true);
            TextField fl = (TextField) get("userName");
            fl.setVisible(true);
        } else
        {
            Label label = (Label) get("userLabel");
            label.setVisible(false);
            TextField fl = (TextField) get("userName");
            fl.setVisible(false);
        }
        super.onBeforeRender();
    }

    private JetspeedPrincipalManager getRoleManager()
    {
        return (JetspeedPrincipalManager) getServiceLocator().getRoleManager();
    }
    
    private static boolean hasPrincipal(Subject subject, JetspeedPrincipal jp)
    {
        Iterator<Principal> principals = subject.getPrincipals().iterator();
        while (principals.hasNext())
        {
            Principal p = principals.next();
            if (p instanceof JetspeedPrincipal && 
                ((JetspeedPrincipal)p).getType().getName().equals(jp.getType().getName()) && p.getName().equals(jp.getName()))
            {
                return true;
            }
        }
        return false;
    }
    
}
