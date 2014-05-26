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

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.IModel;

/**
 * PortletOddEvenItem to show rows with different colors.
 * 
 * @author <a href="mailto:woonsan@apache.org">Woonsan Ko</a>
 * @version $Id: $
 */
public class PortletOddEvenItem extends OddEvenItem
{
	private static final long serialVersionUID = 1L;

    private String evenClass = "portlet-section-body";
    private String oddClass = "portlet-section-alternate";
       
	public PortletOddEvenItem(String id, int index, IModel model)
	{
		this(id, index, model, null, null);
	}

	public PortletOddEvenItem(String id, int index, IModel model, String evenClass, String oddClass)
	{
		super(id, index, model);
        
        if (evenClass != null)
        {
            this.evenClass = evenClass;
        }
        
        if (oddClass != null)
        {
            this.oddClass = oddClass;
        }
	}
    
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		tag.put("class", (getIndex() % 2 == 0) ? this.oddClass : this.evenClass);
	}
}