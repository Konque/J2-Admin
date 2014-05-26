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
import java.util.Map;

import org.apache.jetspeed.om.portlet.PortletApplication;
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
 * Provides Data to sortable and detachable list views for Portlet Applications
 * 
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: ApplicationDataProvider.java 772093 2009-05-06 08:06:42Z taylor $
 */
public class ApplicationDataProvider extends SortableDataProvider<ApplicationBean> implements IDataProvider<ApplicationBean>
{
    public enum AppOrderBy { NAME_ASC, NAME_DESC, VERSION_ASC, VERSION_DESC, PATH_ASC, PATH_DESC }
    private JetspeedServiceLocator serviceLocator;
    private boolean searchMode = false;
    private List<ApplicationBean> portletApps = null;
    private List<ApplicationBean> searchResults = null;
    private AppOrderBy orderBy = AppOrderBy.NAME_ASC;
    private static final long serialVersionUID = 1L;
    private final static Logger log = LoggerFactory.getLogger(ApplicationDataProvider.class);
    
    public ApplicationDataProvider(JetspeedServiceLocator locator)
    {
        this.serviceLocator = locator;
        setSort("name", true);
        
        refresh();
    }
    
    public Iterator<ApplicationBean> iterator(int first, int count)
    {
        int last = first + count;
        if (last > portletApps.size())
            last = portletApps.size() - 1;
        if (last < 0)
            last = 0;
        return portletApps.subList(first, last).iterator();
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
            if (portletApps == null)
                return 0;
            return portletApps.size();
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
                portletApps = searchResults;
                return;
            }
            searchMode = false;
        }
        else
        {
            portletApps = new ArrayList<ApplicationBean>();
            Collection<PortletApplication> apps = serviceLocator.getPortletRegistry().getPortletApplications();
            for (PortletApplication app : apps)
            {
                boolean isRunning = serviceLocator.getPortletFactory().isPortletApplicationRegistered(app);
                portletApps.add(new ApplicationBean(app, isRunning));
            }
            sort(portletApps, this.orderBy);
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
    
    @SuppressWarnings("unchecked")
    public void searchApplications(String search)
    {
        try
        {
            if (search == null)
            {
                searchMode = false;
                return;
            }
            searchResults = new ArrayList<ApplicationBean>();
            SearchResults results = serviceLocator.getSearchEngine().search(search);
            if (results.size() > 0)
            {
                for (ParsedObject po : results.getResults())
                {
                    Map<String, String> fields = po.getFields();
                    if(fields != null && po.getType().equals(ParsedObject.OBJECT_TYPE_PORTLET_APPLICATION))
                    {
                        String paName;
                        Object id = fields.get(ParsedObject.ID);
                        if (id instanceof Collection)
                        {
                            Collection<String> coll = (Collection<String>) id;
                            paName =  coll.iterator().next();
                        }
                        else
                        {
                            paName = (String) id;
                        }
                        PortletApplication pa = serviceLocator.getPortletRegistry().getPortletApplication(paName);
                        if (pa != null)
                        {
                            boolean isRunning = serviceLocator.getPortletFactory().isPortletApplicationRegistered(pa);                            
                            searchResults.add(new ApplicationBean(pa, isRunning));
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
            log.error("Registry Search failure: ", e);
        }        
    }

    public void sort(List<ApplicationBean> list, AppOrderBy orderBy)
    {
        Collections.sort(list, new AppComparator(orderBy));
    }
    
    public void sort()
    {
        this.sort(this.portletApps, this.orderBy);
    }
    
    public class AppComparator implements Comparator<ApplicationBean>
    {
        AppOrderBy orderBy;
        
        public AppComparator(AppOrderBy order)
        {
            this.orderBy = order;
        }
        
        public int compare(ApplicationBean app1, ApplicationBean app2)
        {
            String p1 = "";
            String p2 = "";
            if (orderBy == AppOrderBy.NAME_ASC)
            {
                p1 = app1.getApplicationName();
                p2 = app2.getApplicationName();
            }
            else if (orderBy == AppOrderBy.NAME_DESC)
            {
                p2 = app1.getApplicationName();
                p1 = app2.getApplicationName();
            }
            else if (orderBy == AppOrderBy.VERSION_ASC)
            {
                p1 = app1.getVersion();
                p2 = app2.getVersion();
            }
            else if (orderBy == AppOrderBy.VERSION_DESC)
            {
                p2 = app1.getVersion();
                p1 = app2.getVersion();
            }
            else if (orderBy == AppOrderBy.PATH_ASC)
            {
                p1 = app1.getPath();
                p2 = app2.getPath();
            }
            else if (orderBy == AppOrderBy.PATH_DESC)
            {
                p2 = app1.getPath();
                p1 = app2.getPath();
            }
            p1 = (p1 == null) ? "" : p1;
            p2 = (p2 == null) ? "" : p2;
            return p1.compareToIgnoreCase(p2);            
        }
    }
    
    public IModel<ApplicationBean> model(ApplicationBean object)
    {
        return new Model<ApplicationBean>((ApplicationBean)object);
    }
    
    public AppOrderBy getOrderBy()
    {
        return orderBy;
    }
    
    public void setOrderBy(AppOrderBy orderBy)
    {
        this.orderBy = orderBy;
    }    
}                    

