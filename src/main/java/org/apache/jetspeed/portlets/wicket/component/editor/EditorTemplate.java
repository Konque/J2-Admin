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
package org.apache.jetspeed.portlets.wicket.component.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jetspeed.portlets.wicket.component.JavascriptEventConfirmation;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;

/**
 * 
 * @author Ruben Alexander de Gooijer
 *
 * @param <T>
 */
public abstract class EditorTemplate<T> extends Panel
{
    private static final long serialVersionUID = 1L;

    private List<SelectableModel<T>> rows;
    
    public static final String ITEMS_FRAGMENT_ID = "itemFragment";
    public static final String NEW_FRAGMENT_ID = "newFragment";
    
    public EditorTemplate(String id)
    {
        super(id);
    }
    
    public EditorTemplate initLayout()
    {
        rows = new ArrayList<SelectableModel<T>>();
        
        RefreshingView<T> data = new RefreshingView<T>("data") 
        {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected Iterator<IModel<T>> getItemModels()
            {
                return EditorTemplate.this.getItemModels();
            }

            @Override
            protected void populateItem(Item<T> item)
            {
                T object = item.getModelObject();
                
                final SelectableModel<T> model = rowModel(object);
                rows.add(model);
                
                Fragment fragment = new Fragment("items", ITEMS_FRAGMENT_ID, this);
                EditorTemplate.this.buildItems(fragment, model.getObject());
                
                //We cannot use the PropertyModel here. The SelectableObject might implement IModel
                //this causes the PropertyModel to resolve the getChecked method to the object contained inside the SelectableObject,
                //which should not happen.
                item.add(new CheckBox("select", new IModel<Boolean>() {
                    public Boolean getObject()
                    {
                        return model.getSelected();
                    }

                    public void setObject(Boolean object)
                    {
                        model.setSelected(object);
                    }

                    public void detach()
                    {
                        
                    }
                }));
                item.add(fragment);
            }
        };
        
        Form form = new Form("form");
        
        form.add(data);
        
        Fragment newFragment = new Fragment("new", NEW_FRAGMENT_ID, this);
        buildNew(newFragment);
        form.add(newFragment);
        
        form.add(new Image("add", new ResourceReference(EditorTemplate.class, "add.png")));

        WebMarkupContainer footer = new WebMarkupContainer("footer");
        Button saveButton = saveButton("save");
        saveButton.setDefaultModel(new ResourceModel("pam.details.action.save"));
        footer.add(saveButton);
        Button deleteBtn = deleteButton("delete");
        deleteBtn.add(new JavascriptEventConfirmation("onclick", new ResourceModel("pam.details.action.delete.confirm")));
        footer.add(deleteBtn);
        footer.add(new AttributeModifier("colspan", true, new Model<Integer>(Integer.valueOf(getColumnCount()))));
        
        form.add(footer);
        add(form);
        
        return this;
    }
    
    protected Button deleteButton(String componentId)
    {
        return new Button(componentId, new ResourceModel("pam.details.action.delete")) {
            @Override
            public void onSubmit() {
                List<IModel<T>> removeList = new ArrayList<IModel<T>>();
                for(SelectableModel<T> field : rows) {
                    if(field.getSelected()) {
                        removeList.add(field);
                    }
                }
                
                delete(removeList.toArray(new IModel[removeList.size()]));
            }
        };
    }
    
    public final SelectableModel<T> rowModel(T object)
    {
        IModel<T> model = getNewRowModel(object);
        return new SelectableModel<T>(model);
    }
    
    public abstract int getColumnCount();
    
    public abstract IModel<T> getNewRowModel(T object);
    
    public abstract Iterator<IModel<T>> getItemModels();
    public abstract void delete(IModel<T>[] fields);
    protected abstract Button saveButton(String componentId);
    
    public abstract void buildItems(Fragment fragment, T model);
    public abstract void buildNew(Fragment fragment);
}
