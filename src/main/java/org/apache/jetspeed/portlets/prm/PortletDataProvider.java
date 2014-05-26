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
package org.apache.jetspeed.portlets.prm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.jetspeed.om.portlet.PortletApplication;
import org.apache.jetspeed.om.portlet.PortletDefinition;
import org.apache.jetspeed.portlets.JetspeedServiceLocator;
import org.apache.jetspeed.search.ParsedObject;
import org.apache.jetspeed.search.SearchResults;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides Data to sortable and detachable list views for Portlet lists
 * 
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: PortletDataProvider.java 1089610 2011-04-06 20:08:17Z woonsan $
 */
public class PortletDataProvider extends SortableDataProvider<PortletDefinitionBean> implements IDataProvider<PortletDefinitionBean>
{
    public enum PortletOrderBy { NAME_ASC, NAME_DESC, DISPLAY_NAME_ASC, DISPLAY_NAME_DESC }
    private JetspeedServiceLocator serviceLocator;    
    private boolean searchMode = false;
    private String appName;
    private List<PortletDefinitionBean> portlets = null;
    private List<PortletDefinitionBean> searchResults = null;
    private Locale locale;
    private static final long serialVersionUID = 1L;
    private final static Logger log = LoggerFactory.getLogger(PortletDataProvider.class);
    private PortletOrderBy orderBy = PortletOrderBy.DISPLAY_NAME_ASC;
    
    public PortletDataProvider(String defaultAppName, Locale locale, JetspeedServiceLocator locator)
    {
        this.serviceLocator = locator;
        this.locale = locale;
        this.appName = defaultAppName;
        setSort("name", true);

        refresh();
    }
    
    public Iterator<PortletDefinitionBean> iterator(int first, int count)
    {
        int last = first + count;
        if (last > portlets.size())
            last = portlets.size() - 1;
        if (last < 0)
            last = 0;
        return portlets.subList(first, last).iterator();
    }

    public int size()
    {
        if (searchMode)
        {
            if (searchResults == null)
                return 0;
            return searchResults.size();
        }
        else
        {
            if (portlets == null)
                return 0;
            return portlets.size();
        }
    }
    
    /**
     * @see org.apache.wicket.model.IDetachable#detach()
     */
    public void detach()
    {
        super.detach();        
    }

    public void refresh()
    {
        if (searchMode)
        {
            if (searchResults != null)
            {
                portlets = searchResults;
                return;
            }
            searchMode = false;
        }
        else
        {
            if (getAppName() == null) {
                changeAppName("j2-admin");
            }

            portlets = new ArrayList<PortletDefinitionBean>();
            PortletApplication app = serviceLocator.getPortletRegistry().getPortletApplication(getAppName());
            if (app != null)
            {
                for (PortletDefinition def : app.getPortlets())
                {
                    portlets.add(new PortletDefinitionBean(def, getAppName(), locale));
                }
                for (PortletDefinition def : app.getClones())
                {
                    portlets.add(new PortletDefinitionBean(def, getAppName(), locale));
                }
            }
            sort(portlets, this.orderBy);            
        }
    }

    public void setSearchMode(boolean mode)
    {
        this.searchMode = mode;
    }
    
    public boolean getSearchMode()
    {
        return this.searchMode;
    }
    
    public IModel<PortletDefinitionBean> model(PortletDefinitionBean object)
    {
        return new Model<PortletDefinitionBean>(object);
    }

    public void sort(List<PortletDefinitionBean> list, PortletOrderBy orderBy)
    {
        Collections.sort(list, new PortletComparator(orderBy));
    }
    
    public class PortletComparator implements Comparator<PortletDefinitionBean>
    {
        PortletOrderBy orderBy;
        
        public PortletComparator(PortletOrderBy order)
        {
            this.orderBy = order;
        }
        
        public int compare(PortletDefinitionBean portlet1, PortletDefinitionBean portlet2)
        {
            String p1 = "";
            String p2 = "";
            if (orderBy == PortletOrderBy.NAME_ASC)
            {
                p1 = portlet1.getName();
                p2 = portlet2.getName();
            }
            else if (orderBy == PortletOrderBy.NAME_DESC)
            {
                p2 = portlet1.getName();
                p1 = portlet2.getName();
            }
            else if (orderBy == PortletOrderBy.DISPLAY_NAME_ASC)
            {
                p1 = portlet1.getDisplayName();
                p2 = portlet2.getDisplayName();
            }
            else if (orderBy == PortletOrderBy.DISPLAY_NAME_DESC)
            {
                p2 = portlet1.getDisplayName();
                p1 = portlet2.getDisplayName();
            }
            p1 = (p1 == null) ? "" : p1;
            p2 = (p2 == null) ? "" : p2;
            return p1.compareToIgnoreCase(p2);            
        }
    }
    
    @SuppressWarnings("unchecked")    
    public void searchPortlets(String search)
    {
        searchPortlets(search, true, true);
    }
    
    @SuppressWarnings("unchecked")    
    public void searchPortlets(String search, boolean withOriginalPortlets, boolean withClonedPortlets)
    {
        try
        {
            if (search == null)
            {
                searchMode = false;
                return;
            }
            searchResults = new ArrayList<PortletDefinitionBean>();
            SearchResults results = serviceLocator.getSearchEngine().search(search);
            if (results.size() > 0)
            {            
                String name = "";
                for (ParsedObject po : results.getResults())
                {
                    Map<String, String> fields = po.getFields();
                    if (fields != null && po.getType().equals(ParsedObject.OBJECT_TYPE_PORTLET))
                    {
                        Object id = fields.get(ParsedObject.ID);                
                        Object pa = fields.get(ParsedObject.OBJECT_TYPE_PORTLET_APPLICATION);
                        String paName = "";
                        if(pa != null)
                        {
                            if(id instanceof Collection)
                            {
                                Collection<String> coll = (Collection<String>) id;
                                paName = (String) coll.iterator().next();
                            }
                            else
                            {
                                paName = (String)pa;
                            }
                        }
                        name = paName + "::" + id;
                        PortletDefinition pd = serviceLocator.getPortletRegistry().getPortletDefinitionByUniqueName(name);
                        if (pd != null)
                        {
                            if ((pd.isClone() && withClonedPortlets) || (!pd.isClone() && withOriginalPortlets))
                            {
                                searchResults.add(new PortletDefinitionBean(pd, paName, locale));
                            }
                        }                        
                    }
                }
            }
            searchMode = true;            
            sort(searchResults, this.orderBy);                
            refresh();
        }
        catch (Exception e)
        {
            
        }
    }

    public PortletOrderBy getOrderBy()
    {
        return orderBy;
    }
    
    public void setOrderBy(PortletOrderBy orderBy)
    {
        this.orderBy = orderBy;
    }

	public void changeAppName(String appName) 
	{
		this.appName = appName;
		
		refresh();
	}

	public String getAppName() 
	{
		return appName;
	}    
	
    public void sort()
    {
        this.sort(this.portlets, this.orderBy);
    }

    
}
