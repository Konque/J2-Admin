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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * SelectionImagePropertyColumn to show selection column with image icons.
 * 
 * @author <a href="mailto:woonsan@apache.org">Woonsan Ko</a>
 * @version $Id: $
 */
public abstract class SelectionImagePropertyColumn extends PropertyColumn 
{
	private static final long serialVersionUID = 1L;
    
    protected String selectedImage = "Selected.gif";
    protected String blankImage = "Blank.gif";
    
    protected String cellWidth = "1%";

    public SelectionImagePropertyColumn(IModel displayModel, String propertyExpressions) 
    {
        super(displayModel, propertyExpressions);
    }
    
    public void setSelectedImage(String selectedImage)
    {
        this.selectedImage = selectedImage;
    }
    
    public String getSelectedImage()
    {
        return this.selectedImage;
    }
    
    public void setBlankImage(String blankImage)
    {
        this.blankImage = blankImage;
    }
    
    public String getBlankImage()
    {
        return this.blankImage;
    }
    
    public void setCellWidth(String cellWidth)
    {
        this.cellWidth = cellWidth;
    }
    
    public String getCellWidth()
    {
        return this.cellWidth;
    }

    public void populateItem(Item item, String componentId, IModel model) 
    {
        item.add(new AttributeModifier("width", true, new Model(getCellWidth())));
        item.add(new ImagePanel(item, componentId, model));
    }
    
    protected boolean isSelected(Item item, String componentId, IModel model)
    {
        return false;
    }
    
    public class ImagePanel extends Panel 
    {
        public ImagePanel(final Item item, final String componentId, final IModel model) 
        {
            super(componentId);
            Image image = new Image("image", new Model(isSelected(item, componentId, model) ? getSelectedImage() : getBlankImage()));
            add(image);
        }
    }

}