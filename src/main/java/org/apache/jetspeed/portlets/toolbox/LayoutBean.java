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
package org.apache.jetspeed.portlets.toolbox;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.decoration.DecorationFactory;
import org.apache.jetspeed.om.page.ContentFragment;
import org.apache.jetspeed.om.page.ContentPage;
import org.apache.jetspeed.request.RequestContext;

public class LayoutBean implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String name;
    private String title;
    private String image;
    private boolean selected;
    private String layoutPortlet; 
    
    public LayoutBean(String name, String title, String image, String layoutPortlet)
    {
        this.setName(name);
        this.setTitle(title);
        this.setImage(image);
        this.setLayoutPortlet(layoutPortlet);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getImage()
    {
        return image;
    }

    public void setImage(String image)
    {
        this.image = image;
    }

    public boolean isSelected()
    {
        return selected;
    }

    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }

    public String getLayoutPortlet()
    {
        return layoutPortlet;
    }
    
    public void setLayoutPortlet(String layoutPortlet)
    {
        this.layoutPortlet = layoutPortlet;
    }
    
    @SuppressWarnings("unchecked")
    public static List<LayoutBean> retrieveLayouts(PortletRequest request, DecorationFactory decorationFactory)
    {
        List<LayoutBean> layouts = (List<LayoutBean>) request.getPortletSession().getAttribute("layouts");
        
        if (layouts == null)
        {
            layouts = new ArrayList<LayoutBean>();
            // BOZO: support 4 for now, need to localize, formalize etc
            LayoutBean single = new LayoutBean("OneColumn", "One Column", "OneColumn.jpg", "jetspeed-layouts::VelocityOneColumn");
            layouts.add(single);
            LayoutBean twoColumns = new LayoutBean("TwoColumn", "Two Columns", "TwoColumns.jpg", "jetspeed-layouts::VelocityTwoColumns");
            layouts.add(twoColumns);
            LayoutBean threeColumns = new LayoutBean("ThreeColumn", "Three Columns", "ThreeColumns.jpg", "jetspeed-layouts::VelocityThreeColumns");
            layouts.add(threeColumns);
            LayoutBean fourColumns = new LayoutBean("FourColumn", "Four Columns", "FourColumns.jpg", "jetspeed-layouts::VelocityFourColumns");
            layouts.add(fourColumns);
            request.getPortletSession().setAttribute("layouts", layouts);
        }
        
        RequestContext rc = (RequestContext) request.getAttribute(RequestContext.REQUEST_PORTALENV);
        ContentPage page = rc.getPage();
        ContentFragment layoutFragment = page.getNonTemplateRootFragment();
        String currentLayoutName = layoutFragment.getName();
        
        for (LayoutBean layout : layouts)
        {
            layout.setSelected(StringUtils.equals(currentLayoutName, layout.getLayoutPortlet()));
        }
        
        return layouts;
    }
    
}

 