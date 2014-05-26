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
package org.apache.jetspeed.portlets.wicket.component;

import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

/**
 * TextFieldPropertyColumn to show selection column with textbox.
 * 
 * @author <a href="mailto:woonsan@apache.org">Woonsan Ko</a>
 * @version $Id: $
 */
public class TextFieldPropertyColumn extends PropertyColumn 
{
	private static final long serialVersionUID = 1L;
    
    public TextFieldPropertyColumn(IModel displayModel, String propertyExpressions) 
    {
        super(displayModel, propertyExpressions);
    }
    
    public void populateItem(Item item, String componentId, IModel model) 
    {
        item.add(new TextFieldPanel(item, componentId, model));
    }
    
    public class TextFieldPanel extends Panel 
    {
        public TextFieldPanel(final Item item, final String componentId, final IModel model) 
        {
            super(componentId);
            TextField textField = new TextField("textField", new PropertyModel(model.getObject(), getPropertyExpression()));
            add(textField);
        }
    }

}