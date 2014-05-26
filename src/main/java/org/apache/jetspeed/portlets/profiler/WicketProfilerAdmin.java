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
package org.apache.jetspeed.portlets.profiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.portlets.AdminPortletWebPage;
import org.apache.jetspeed.profiler.Profiler;
import org.apache.jetspeed.profiler.ProfilerException;
import org.apache.jetspeed.profiler.rules.ProfilingRule;
import org.apache.jetspeed.profiler.rules.RuleCriterion;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WicketProfilerAdmin extends AdminPortletWebPage {

    static final Logger logger = LoggerFactory.getLogger(WicketProfilerAdmin.class);
    
    protected ProfilingRule profilingRule;

    public WicketProfilerAdmin() {

        final RuleEditPanel ruleEditPanel = new RuleEditPanel("ruleEditPanel");

        Form rulesForm = new Form("rulesForm");

        // list of existing rules
        PageableListView rulesList = new PageableListView("rules", new PropertyModel(this, "rules"), 10) {

            @Override
            protected void populateItem(ListItem item) {
                final ProfilingRule rule = (ProfilingRule) item.getModelObject();
                
                item.add(new ActionPanel("ruleId", new Model(rule), new Link("link", new Model(rule)) {

                    @Override
                    public void onClick() {
                        ProfilingRule rule = (ProfilingRule) getModelObject();
                        setProfilingRule(rule);
                        ruleEditPanel.setVisible(true);
                        ruleEditPanel.hideCriterionEditPanel();
                        ruleEditPanel.ruleIdFieldReadOnly(true);
                    }
                }, "id"));
                
            }

        };
        
        rulesForm.add(rulesList);
        rulesForm.add(new PagingNavigator("rulesPaging", rulesList));
        add(rulesForm);

        // button the create a new rule
        Button newRuleButton = new Button("newRuleButton", new ResourceModel("profiler.rule.new")) {

            @Override
            public void onSubmit() {
                Profiler profiler = getServiceLocator().getProfiler();
                try
                {
                    Class defaultClass = profiler.getClass().getClassLoader().loadClass("org.apache.jetspeed.profiler.rules.impl.StandardProfilingRule");
                    setProfilingRule((ProfilingRule)defaultClass.newInstance());
                    ruleEditPanel.setVisible(true);
                    ruleEditPanel.hideCriterionEditPanel();
                    ruleEditPanel.ruleIdFieldReadOnly(false);
                }
                catch (Exception e)
                {
                    logger.error("Failed to set rule.", e);
                }
            }
            
            
            
        };
        rulesForm.add(newRuleButton);
        
        // form to edit new or selected rule
        add(ruleEditPanel);
        ruleEditPanel.setVisible(false);

    }

    public ProfilingRule getProfilingRule() {
        return profilingRule;
    }

    public void setProfilingRule(ProfilingRule profilingRule) {
        this.profilingRule = profilingRule;
    }
  
    /**
     * A panel containing a link with a label.
     */
    class ActionPanel extends Panel
    {
        public ActionPanel(String id, IModel model, Link link, String propertyExpression)
        {
            super(id, model);
            link.add(new Label("label", new PropertyModel(model.getObject(), propertyExpression)));
            add(link);
        }
    }    
    
    /**
     * The form to edit the selected profiling rule.
     *
     */
    protected class RuleEditPanel extends Panel {

        private List<String> classNames = Arrays.asList(new String[] {
                "org.apache.jetspeed.profiler.rules.impl.StandardProfilingRule",
                "org.apache.jetspeed.profiler.rules.impl.RoleFallbackProfilingRule" });
        protected RuleCriterion criterion;
        protected CriterionEditPanel criterionEditPanel;
        protected TextField ruleIdField;

        public RuleEditPanel(String id) {
            super(id);
            
            Form ruleEditForm = new Form("ruleEditForm");
            
            FeedbackPanel feedbackPanel = new FeedbackPanel("feedbackPanel");
            ruleEditForm.add(feedbackPanel);
            
            ruleEditForm.add(new Label("ruleIdLabel", new ResourceModel("profiler.rule.id")));
            ruleEditForm.add(new Label("ruleTitleLabel", new ResourceModel("profiler.rule.title")));
            ruleEditForm.add(new Label("ruleClassLabel", new ResourceModel("profiler.rule.class")));
            
            ruleIdField = new TextField("ruleIdField", new PropertyModel(WicketProfilerAdmin.this, "profilingRule.id"));
            ruleIdField.setRequired(true);            
            ruleIdField.add(new ProfileRuleValidator());
            
            TextField ruleTitleField = new TextField("ruleTitleField", new PropertyModel(WicketProfilerAdmin.this, "profilingRule.title"));
            
            DropDownChoice ruleClassField = new DropDownChoice("ruleClassField", classNames);
            ruleClassField.setModel(new PropertyModel(WicketProfilerAdmin.this, "profilingRule.classname"));
            
            Button ruleSaveButton = new Button("ruleSaveButton", new ResourceModel("profiler.rule.save")) {

                @Override
                public void onSubmit() {
                    Profiler profiler = getServiceLocator().getProfiler();
                    try {
                        profiler.storeProfilingRule(profilingRule);
                        if (!getRules().contains(profilingRule)) {
                            getRules().add(profilingRule);
                        }
                        ruleIdFieldReadOnly(true);
                    } catch (ProfilerException e) {
                        logger.error("Failed to update rule: " + profilingRule, e);
                    }
                }
                
            };

            Button ruleRemoveButton = new Button("ruleRemoveButton", new ResourceModel("profiler.rule.remove")) {

                @Override
                public void onSubmit() {
                    Profiler profiler = getServiceLocator().getProfiler();
                    try {
                        if (getRules().contains(profilingRule)) {
                            getRules().remove(profilingRule);
                        }
                        profiler.deleteProfilingRule(profilingRule);
                        profilingRule = null;
                        RuleEditPanel.this.setVisible(false);
                    } catch (ProfilerException e) {
                        logger.error("Failed to REMOVE: " + profilingRule, e);
                    }
                }
                
            };
            
            ruleEditForm.add(ruleIdField);
            ruleEditForm.add(ruleTitleField);
            ruleEditForm.add(ruleClassField);
            ruleEditForm.add(ruleSaveButton);
            ruleEditForm.add(ruleRemoveButton);

            
            // the list of criteria for the selected rule
            
            ruleEditForm.add(new Label("ruleCriteriaLabel",new ResourceModel("criteria.title")));
            
            SortableDataProvider dataProvider = new SortableDataProvider()
            {
                public int size()
                {
                    return profilingRule.getRuleCriteria().size();
                }

                public IModel model(Object object)
                {
                    RuleCriterion ruleCriterion = (RuleCriterion) object;
                    return new Model(ruleCriterion);
                }

                public Iterator iterator(int first, int count)
                {
                    return new ArrayList(profilingRule.getRuleCriteria()).subList(first, first + count).iterator();
                }
            };
            IColumn[] columns = { new AbstractColumn(new Model("Name")) {

                public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                    //cellItem.add(new CriterionLink(componentId, rowModel));
                    cellItem.add(new ActionPanel(componentId, rowModel, new Link("link", rowModel) {
                        public void onClick()
                        {
                            setCriterion((RuleCriterion) getModelObject());
                            criterionEditPanel.setVisible(true);
                        }
                    }, "name"));
                }
                
            }, 
                    new PropertyColumn(new Model("Value"), "value"),
                    new PropertyColumn(new Model("Resolver Type"), "type"),
                    new PropertyColumn(new Model("Fallback Type"), "fallbackType"),
                    new PropertyColumn(new Model("Order"), "fallbackOrder") };
            
            DataTable ruleCriteriaTable = new DataTable("ruleCriteriaTable", columns, dataProvider, 10);

            ruleCriteriaTable.addTopToolbar(new HeadersToolbar(ruleCriteriaTable, dataProvider));
            ruleCriteriaTable.addBottomToolbar(new NavigationToolbar(ruleCriteriaTable));
            ruleEditForm.add(ruleCriteriaTable);

            Button newCriterionButton = new Button("newCriterionButton",new ResourceModel("criteria.new")) {

                @Override
                public void onSubmit() {
                    
                    if(StringUtils.isEmpty(getProfilingRule().getId()))
                    {
                        error(getLocalizer().getString("ruleIdField.Required",this));
                        return;
                    }
                    
                    Profiler profiler = getServiceLocator().getProfiler();
                    try {
                        criterion = profiler.createRuleCriterion();
                        
                        criterionEditPanel.setVisible(true);
                    } catch (ClassNotFoundException e) {
                        logger.error("Could not create new rule criterion.", e);
                    }
                }
                
            };
            Form criterionForm = new Form("criterionForm");
            criterionForm.add(newCriterionButton);
            add(criterionForm);
            
            setCriterion(null);
            criterionEditPanel = new CriterionEditPanel("criterionEditPanel");
            criterionEditPanel.setOutputMarkupId(true);
            add(criterionEditPanel);

            criterionEditPanel.setVisible(false);
            
            add(ruleEditForm);
            
        }

        public RuleCriterion getCriterion() {
            return criterion;
        }

        public void setCriterion(RuleCriterion criterion) {
            this.criterion = criterion;
        }
        
        
        protected void hideCriterionEditPanel() {
            criterionEditPanel.setVisible(false);
        }

        protected void ruleIdFieldReadOnly(boolean readOnly) {
            ruleIdField.setEnabled(!readOnly);
        }
        
        /**
         * Form to edit selected rule criterion.
         *
         */
        protected class CriterionEditPanel extends Panel {

            private static final String FALLBACK_CONTINUE = "Continue";
            private static final String FALLBACK_STOP = "Stop";
            private static final String FALLBACK_LOOP = "Loop";
            
            private List<String> resolvers = Arrays.asList(new String[] {
                    "request",
                    "session",
                    "request.session",
                    "hard.coded",
                    "group.role.user",
                    "user",
                    "group",
                    "role",
                    "mediatype",
                    "country",
                    "language",
                    "roles",
                    "path",
                    "page",
                    "path.session",
                    "user.attribute",
                    "navigation",
                    "ip",
                    "hostname" });

            private List<Integer> fallbackTypes = Arrays.asList(new Integer[] {
                    new Integer(RuleCriterion.FALLBACK_CONTINUE), 
                    new Integer(RuleCriterion.FALLBACK_STOP), 
                    new Integer(RuleCriterion.FALLBACK_LOOP)
            });

            public CriterionEditPanel(String id) {
                super(id);
                
                Form criterionEditForm = new Form("criterionEditForm");

                criterionEditForm.add(new Label("criterionNameLabel", new ResourceModel("criterion.name")));
                criterionEditForm.add(new Label("criterionValueLabel", new ResourceModel("criterion.value")));
                criterionEditForm.add(new Label("criterionResolverTypeLabel", new ResourceModel("criterion.resolver")));
                criterionEditForm.add(new Label("criterionFallbackLabel", new ResourceModel("criterion.fallback.type")));
                criterionEditForm.add(new Label("criterionOrderLabel", new ResourceModel("criterion.fallback.order")));
                
                TextField criterionNameField = new TextField("criterionNameField", new PropertyModel(RuleEditPanel.this, "criterion.name"));
                criterionNameField.setRequired(true);

                TextField criterionValueField = new TextField("criterionValueField", new PropertyModel(RuleEditPanel.this, "criterion.value"));

                DropDownChoice criterionResolverTypeField = new DropDownChoice("criterionResolverTypeField", resolvers);
                criterionResolverTypeField.setModel(new PropertyModel(RuleEditPanel.this, "criterion.type"));
                criterionResolverTypeField.setRequired(true);
                
                DropDownChoice criterionFallbackField = new DropDownChoice("criterionFallbackField", fallbackTypes, new FallbackTypeChoiceRenderer());
                criterionFallbackField.setModel(new PropertyModel(RuleEditPanel.this, "criterion.fallbackType"));
                criterionFallbackField.setRequired(true);
                
                TextField criterionOrderField = new TextField("criterionOrderField", new PropertyModel(RuleEditPanel.this, "criterion.fallbackOrder"));
                criterionOrderField.setRequired(true);
                
                criterionEditForm.add(criterionNameField);
                criterionEditForm.add(criterionValueField);
                criterionEditForm.add(criterionResolverTypeField);
                criterionEditForm.add(criterionFallbackField);
                criterionEditForm.add(criterionOrderField);

                Button saveCriterionButton = new Button("saveCriterionButton",new ResourceModel("profiler.rule.save")) {

                    @Override
                    public void onSubmit() {
                        if (criterion.getValue() != null)
                        {
                            String value = criterion.getValue();
                            value = value.trim();
                            if (value.equals(""))
                            {
                                criterion.setValue(null);
                            }
                        }
                        if (!profilingRule.getRuleCriteria().contains(criterion)) {
                            profilingRule.getRuleCriteria().add(criterion);
                        }
                        try {
                            Profiler profiler = getServiceLocator().getProfiler();
                            profiler.storeProfilingRule(profilingRule);
                        }
                        catch (ProfilerException e) {
                            logger.error("Could not save rule criterion {}: {}", criterion.getName(), e.getMessage());
                        }
                    }
                    
                };
                
                Button removeCriterionButton = new Button("removeCriterionButton",new ResourceModel("criteria.remove")) {

                    @Override
                    public void onSubmit() {
                        if (profilingRule.getRuleCriteria().contains(criterion)) {
                            profilingRule.getRuleCriteria().remove(criterion);
                        }
                        try {
                            Profiler profiler = getServiceLocator().getProfiler();
                            profiler.storeProfilingRule(profilingRule);
                        }
                        catch (ProfilerException e) {
                            logger.error("Could not save rule criterion {}: {}", criterion.getName(), e.getMessage());
                        }
                    }
                    
                };
                
                criterionEditForm.add(saveCriterionButton);
                criterionEditForm.add(removeCriterionButton);
                
                add(criterionEditForm);
            }

            /**
             * ChoiceRenderer for fallback types.
             */
            private final class FallbackTypeChoiceRenderer extends ChoiceRenderer
            {
                /**
                 * Constructor.
                 */
                public FallbackTypeChoiceRenderer()
                {
                }

                /**
                 * @see org.apache.wicket.markup.html.form.IChoiceRenderer#getDisplayValue(Object)
                 */
                public Object getDisplayValue(Object object)
                {
                    Integer fallbackType = (Integer) object;
                    switch (fallbackType)
                    {
                    case RuleCriterion.FALLBACK_CONTINUE:
                        return FALLBACK_CONTINUE;
                    case RuleCriterion.FALLBACK_LOOP:
                        return FALLBACK_LOOP;
                    default:
                        return FALLBACK_STOP;
                    }
                }
            }            
        }

    }

    /**
     * @return the rules
     */
    public List<ProfilingRule> getRules()
    {
        return new ArrayList<ProfilingRule>(getServiceLocator().getProfiler().getRules());
    }
    
    private class ProfileRuleValidator extends AbstractValidator<String>{
        //private static final String ERROR_KEY = "rule.exits"; 
        @Override
        protected void onValidate(IValidatable validatable)
        {
            String ruleName = (String)validatable.getValue();
            if(getServiceLocator().getProfiler().getRule(ruleName)!=null)
            {
              error(validatable);   
            }
        }    
        
        
    }    
}
