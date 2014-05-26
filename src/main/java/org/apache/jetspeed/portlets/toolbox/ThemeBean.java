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
import java.util.ResourceBundle;
import java.util.Set;

import javax.portlet.PortletRequest;

import org.apache.jetspeed.decoration.DecorationFactory;
import org.apache.jetspeed.decoration.LayoutDecoration;
import org.apache.jetspeed.decoration.Theme;
import org.apache.jetspeed.request.RequestContext;

public class ThemeBean implements Serializable
{
    private static final long serialVersionUID = 1L;
    public static final String ATTRIBUTE_THEMES = "jsThemes";

    private String name;
    private String title;
    private String image;
    private boolean selected = false;

    public ThemeBean(String name, String title, String image)
    {
        this.setName(name);
        this.setTitle(title);
        this.setImage(image);
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
        
    public static void clearThemesSession(PortletRequest request)
    {
        request.getPortletSession().removeAttribute(ATTRIBUTE_THEMES);    	
    }

    public static List<ThemeBean> retrieveThemes(PortletRequest request, DecorationFactory decorationFactory)
    {
    	return retrieveThemes(request, decorationFactory, null);
    }
    public static List<ThemeBean> retrieveThemes(PortletRequest request, DecorationFactory decorationFactory, String overrideTheme)
    {
        List<ThemeBean> themes = (List<ThemeBean>)request.getPortletSession().getAttribute(ATTRIBUTE_THEMES);
        if (themes != null)
        {
            return themes;
        }
        themes = new ArrayList<ThemeBean>();
        RequestContext rc = (RequestContext) request.getAttribute(RequestContext.REQUEST_PORTALENV);            
        Set<String> decorators = (Set<String>)decorationFactory.getPageDecorations(rc);
        for (String name : decorators)
        {
            LayoutDecoration decor = decorationFactory.getLayoutDecoration(name, rc);
            String compatibility = decor.getProperty("compatibility");
            if (compatibility != null && compatibility.compareTo("2.2.1") >= 0)
            {
                ResourceBundle rb = decor.getResourceBundle(rc.getLocale(), rc);
                String title = null;
                try
                {
                    title = rb.getString("title");
                }
                catch(Exception e)
                {}
                if (title == null)
                    title = decor.getName();
                String icon = decor.getProperty("icon");
                if (icon == null)
                {
                    icon = "";
                }
                ThemeBean theme = new ThemeBean(decor.getName(), title, icon);                
                Theme pageTheme = (Theme)rc.getRequest().getAttribute("org.apache.jetspeed.theme");
                if (overrideTheme == null)
                	overrideTheme = pageTheme.getPageLayoutDecoration().getName(); 
                if (overrideTheme.equals(decor.getName()))
                    theme.setSelected(true);
                themes.add(theme);
            }
        }
        request.getPortletSession().setAttribute("themes", themes);
        return themes;
    }

    public static String getDefaultTheme(PortletRequest request, DecorationFactory decorationFactory)
    {
        RequestContext rc = (RequestContext) request.getAttribute(RequestContext.REQUEST_PORTALENV);            
    	Set<String> decorators = (Set<String>)decorationFactory.getPageDecorations(rc);
        for (String name : decorators)
        {
            LayoutDecoration decor = decorationFactory.getLayoutDecoration(name, rc);
            String compatibility = decor.getProperty("compatibility");
            if (compatibility != null && compatibility.compareTo("2.2.1") >= 0)
            {
            	return decor.getName();
            }
        }
        return "jetspeed";
    }
}
