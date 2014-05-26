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
package org.apache.jetspeed.portlets.selector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.jetspeed.CommonPortletServices;
import org.apache.jetspeed.JetspeedActions;
import org.apache.jetspeed.PortalReservedParameters;
import org.apache.jetspeed.components.portletpreferences.PortletPreferencesProvider;
import org.apache.jetspeed.components.portletregistry.FailedToStorePortletDefinitionException;
import org.apache.jetspeed.components.portletregistry.PortletRegistry;
import org.apache.jetspeed.headerresource.HeaderResource;
import org.apache.jetspeed.layout.PageLayoutComponent;
import org.apache.jetspeed.om.page.ContentFragment;
import org.apache.jetspeed.om.page.ContentPage;
import org.apache.jetspeed.om.portlet.InitParam;
import org.apache.jetspeed.om.portlet.LocalizedField;
import org.apache.jetspeed.om.portlet.PortletApplication;
import org.apache.jetspeed.om.portlet.PortletDefinition;
import org.apache.jetspeed.om.portlet.Preference;
import org.apache.jetspeed.om.portlet.Preferences;
import org.apache.jetspeed.page.PageManager;
import org.apache.jetspeed.portlets.CategoryInfo;
import org.apache.jetspeed.portlets.PortletInfo;
import org.apache.jetspeed.portlets.dojo.AbstractAdminDojoVelocityPortlet;
import org.apache.jetspeed.portlets.util.PortletApplicationUtils;
import org.apache.jetspeed.profiler.Profiler;
import org.apache.jetspeed.request.RequestContext;
import org.apache.jetspeed.search.ParsedObject;
import org.apache.jetspeed.search.SearchEngine;
import org.apache.jetspeed.security.SecurityAccessController;
import org.apache.velocity.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CategoryPortletSelector selects categories organized by categories
 * 
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: $
 */
public class CategoryPortletSelector extends AbstractAdminDojoVelocityPortlet implements Comparator
{
    
    static final Logger logger = LoggerFactory.getLogger(CategoryPortletSelector.class);
    
    public final String[] DEFAULT_IMAGES = new String[]
    {
            "images/portlets/applications-development.png",
            "images/portlets/applications-system.png",
            "images/portlets/applications-other.png",
            "images/portlets/linux.png"
    };

    public final static String PORTLET_ICON = "portlet-icon";
    protected final static String PORTLETS = "category.selector.portlets";
    protected final static String CATEGORIES = "category.selector.categories";
    protected final static String PAGE = "category.selector.page";
    protected final static String LAYOUT = "category.selector.layout";
    
    private final String JSPAGE = "jspage";
    private final String JSLAYOUT = "jslayoutid";
	private final String CATEGORY = "category";
	private final String PAGENUMNER = "pageNumber";
	private final String FILTER = "filter";
    
    protected PortletPreferencesProvider preferenceService;	
    protected PortletRegistry registry;
    protected SearchEngine searchEngine;
    protected Profiler profiler;
    protected PageLayoutComponent pageLayoutComponent;
    protected SecurityAccessController securityAccessController;
    protected Random rand;
    
    public void init(PortletConfig config)
    throws PortletException 
    {
        super.init(config);
        PortletContext context = getPortletContext();                
        registry = (PortletRegistry)context.getAttribute(CommonPortletServices.CPS_REGISTRY_COMPONENT);
        if (null == registry)
        {
            throw new PortletException("Failed to find the Portlet Registry on portlet initialization");
        }        
        searchEngine = (SearchEngine)context.getAttribute(CommonPortletServices.CPS_SEARCH_COMPONENT);
        if (null == searchEngine)
        {
            throw new PortletException("Failed to find the Search Engine on portlet initialization");
        }
        securityAccessController = (SecurityAccessController)context.getAttribute(CommonPortletServices.CPS_SECURITY_ACCESS_CONTROLLER);
        if (null == securityAccessController)
        {
            throw new PortletException("Failed to find the Security Access Controller on portlet initialization");
        }
        profiler = (Profiler)context.getAttribute(CommonPortletServices.CPS_PROFILER_COMPONENT);
        if (null == profiler)
        {
            throw new PortletException("Failed to find the Profiler on portlet initialization");
        }        
        pageLayoutComponent = (PageLayoutComponent)context.getAttribute(CommonPortletServices.CPS_PAGE_LAYOUT_COMPONENT);
        if (null == pageLayoutComponent)
        {
            throw new PortletException("Failed to find the PageLayoutComponent on portlet initialization");
        }        
        preferenceService = (PortletPreferencesProvider)context.getAttribute(CommonPortletServices.CPS_PORTLET_PREFERENCES_PROVIDER);
        if (null == preferenceService)
        {
            throw new PortletException("Failed to find the Prefs Service on portlet initialization");
        }
        rand = new Random( 19580427 );
    }
    
    public void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException
    {
    	String category = "All";
    	String pageNumber = "";	
		String row = "";		
		String columns = "";
		int portletPerPages=0;
    	PortletPreferences prefs = request.getPreferences();
		category = request.getParameter(CATEGORY);
		pageNumber = request.getParameter(PAGENUMNER);
		String filter = request.getParameter(FILTER);
		columns = prefs.getValue("Columns", "4");
		row = prefs.getValue("Rows", "6");
		portletPerPages = Integer.parseInt(columns) * Integer.parseInt(row);
		if (category == null || category.equals("")) category = "All";
		if (pageNumber == null || pageNumber.equals("")) pageNumber = "1";
		if (filter == null ||  filter.equals("")) filter = null;
		CategoryResult result = getPortlets(request, category, pageNumber, portletPerPages, filter);
        this.getContext(request).put("Columns", columns);
        this.getContext(request).put("Rows", row);
        this.getContext(request).put("pageNumber", pageNumber);
        this.getContext(request).put("category", category);
        this.getContext(request).put("portlets", mergePortletCount(result.getList(),request));
        this.getContext(request).put("Count", new Integer(result.getResultSize()));
        this.getContext(request).put("categories", retrieveCategories(request));
        processPage(request);
        super.doView(request, response);
    }
    
    protected ContentPage getPage(RenderRequest request) throws Exception
    {
        String path = request.getParameter(JSPAGE);
        RequestContext requestContext = (RequestContext) 
            request.getAttribute(PortalReservedParameters.REQUEST_CONTEXT_ATTRIBUTE);        
        return requestContext.locatePage(profiler, pageLayoutComponent, path);
    }    
    
    protected void processPage(RenderRequest request)
    {
        String page = request.getParameter(JSPAGE);
        if (page == null || page.equals(""))
        {
            page = (String)request.getPortletSession().getAttribute(PAGE);
        }
        else
        {
            request.getPortletSession().setAttribute(PAGE, page);
        }
        String layout = request.getParameter(JSLAYOUT);
        this.getContext(request).put(JSPAGE, page); 
        if (layout == null || layout.equals(""))
        {
            layout = (String)request.getPortletSession().getAttribute(LAYOUT);
        }
        else
        {
            request.getPortletSession().setAttribute(LAYOUT, layout);
        }
        this.getContext(request).put(JSLAYOUT, layout);                
    }
    
    public List retrieveCategories(RenderRequest request)
    throws PortletException
    {
        List categories = (List)request.getPortletSession().getAttribute(CATEGORIES);
        if (categories != null)
        {
            return categories;
        }
        Locale locale = request.getLocale();        
        categories = new ArrayList();
        PortletPreferences prefs = request.getPreferences();
        String cats = prefs.getValue("Categories", null);
        if (cats == null)
        {
            throw new PortletException("No categories defined, please add categories via edit mode.");
        }
        StringTokenizer catTokenizer = new StringTokenizer(cats, ",");
        while (catTokenizer.hasMoreTokens())
        {
            String name = catTokenizer.nextToken().trim();
            CategoryInfo cat = new CategoryInfo(name);
            String keywords = prefs.getValue("Keywords:" + name, null);
            if (keywords != null)
            {
                StringTokenizer keyTokenizer = new StringTokenizer(keywords, ",");
                StringBuffer searchString = new StringBuffer();
                int count = 0;
                while (keyTokenizer.hasMoreTokens())
                {
                    String keyword = keyTokenizer.nextToken().trim();
                    if (count > 0)
                    {
                        searchString.append(" | ");
                    }
                    searchString.append(keyword);
                    count++;
                }
                if (count > 0)
                {
                    Iterator portlets = searchEngine.search(searchString.toString()).getResults().iterator();
                    while (portlets.hasNext())
                    {
                        PortletDefinition portlet = 
                            getPortletFromParsedObject((ParsedObject)portlets.next());
                        PortletInfo portletInfo = filterPortlet(portlet, locale, request);
                        if (portletInfo != null)
                        {
                            cat.addPortlet(portletInfo);
                        }                    
                    }                
                    Collections.sort(cat.getPortlets(), this);
                    categories.add(cat);
                }
            }
        }
        request.getPortletSession().setAttribute(CATEGORIES, categories);        
        return categories;
    }
    
    public List retrievePortlets(RenderRequest request, String filter)
    {
        List portletsList = (List)request.getPortletSession().getAttribute(PORTLETS);
        if (filter == null && portletsList != null)
        {
            return portletsList;
        }        
        Iterator portlets = null;
        List list = new ArrayList();
        Locale locale = request.getLocale();                
        if (filter == null)
            portlets = registry.getAllPortletDefinitions().iterator();
        else
            portlets = searchEngine.search(filter).getResults().iterator();
        
        while (portlets.hasNext())
        {
            PortletDefinition portlet = null;
            if (filter == null)
                portlet = (PortletDefinition)portlets.next();
            else
                portlet = getPortletFromParsedObject((ParsedObject)portlets.next());
            
            PortletInfo portletInfo = filterPortlet(portlet, locale, request);
            if (portletInfo != null)
            {
                list.add(portletInfo);
            }
        }            
        Collections.sort(list, this);
        if(filter == null ) request.getPortletSession().setAttribute(PORTLETS, list);
        return list;
    }
    

    private List mergePortletCount(List sysPortlets,
			RenderRequest request) {
		List list = new ArrayList();
		int sPortletCnt = 0;
		Iterator iterator;
		String usrPortletName = null;
		PortletInfo tmpPortletInfo = null;
		String portletName;
		try 
		{
			Map usrPortlet = getUserPortlet(getPage(request));
			sPortletCnt = sysPortlets.size();
			for (int si = 0; si < sPortletCnt; si++) {
				tmpPortletInfo = (PortletInfo)((PortletInfo)sysPortlets.get(si)).clone(); 
				portletName = tmpPortletInfo.getName();
				iterator = usrPortlet.keySet().iterator();
				while (iterator.hasNext()) {
					usrPortletName = (String) iterator.next();
					if (usrPortletName.equalsIgnoreCase(portletName)) {
						tmpPortletInfo.setCount(((Integer)usrPortlet.get(usrPortletName)).intValue());
					}
				}
				list.add(tmpPortletInfo);
				tmpPortletInfo = null;
			}
		} 
		catch (Exception e) 
		{
			logger.error("Unexpected error during retrieving portlet infos.", e);
		}
		return list;
	}

    protected boolean filterByRole(PortletDefinition portlet, RenderRequest request)
    {
        boolean doFilter = false;
        Collection c = portlet.getMetadata().getFields("selector.conditional.role");
        if (c != null) 
        {
            Iterator it = c.iterator();
            if (it.hasNext()) 
            {
                LocalizedField roleField = (LocalizedField) it.next();
                String role = roleField.getValue();
                if (role != null)
                {
                    if (role.equals("*"))
                        doFilter = true;
                    else
                    {
                        return (!request.isUserInRole(role));
                    }
                }
            }
        }
        return doFilter;
    }
    
    /**
     * Filters portlets being added to the based on security checks and layout criteria
     * 
     * @param portlet
     * @return null if filtered, otherwise PortletInfo to be added to list
     */
    protected PortletInfo filterPortlet(PortletDefinition portlet, Locale locale, RenderRequest request)
    {
        if (portlet == null)
            return null;
        if (filterByRole(portlet, request))
        {
            return null; 
        }        
        // Do not display Jetspeed Layout Applications
        PortletApplication pa = (PortletApplication)portlet.getApplication();
        if (pa.isLayoutApplication())
            return null;
        
        // SECURITY filtering
        String uniqueName = pa.getName() + "::" + portlet.getPortletName();
        if (securityAccessController.checkPortletAccess(portlet, JetspeedActions.MASK_VIEW))
        {
            InitParam param = portlet.getInitParam(PORTLET_ICON);
            String image;
            if (param != null)
            {                
                //String relativeImagePath = param.getValue();
                //String context = muta.getWebApplicationDefinition().getContextRoot();
                // Have to use a supported icon in jetspeed, otherwise image can be out of skew
                String  imagePath = param.getParamValue();
                if (imagePath == null)
                {
                    image = DEFAULT_IMAGES[rand.nextInt(DEFAULT_IMAGES.length)];
                }
                else
                {
                    if (-1 == imagePath.indexOf("/"))
                        image = "images/portlets/" + param.getParamValue();
                    else
                        image = param.getParamValue();
                }
            }
            else
            {
                image = DEFAULT_IMAGES[rand.nextInt(DEFAULT_IMAGES.length)];
            }
            return new PortletInfo(uniqueName, cleanup(portlet.getDisplayNameText(locale)), cleanup(portlet.getDescriptionText(locale)), image);
        }
        return null;
    }
    
    protected String cleanup(String str)
    {
        if (str == null)
            return str;
        return str.replaceAll("\r|\n|\"|\'", "");
    }
    
    protected PortletDefinition getPortletFromParsedObject(ParsedObject po)
    {
        boolean found = false;
        String name = "";
        Map fields = po.getFields();
        if(fields != null)
        {
            Object id = fields.get("ID");
    
            if(id != null)
            {
                if(id instanceof Collection)
                {
                    Collection coll = (Collection)id;
                    name = (String) coll.iterator().next();
                }
                else
                {
                    name = (String)id;
                }
            }
            
            if(po.getType().equals("portlet"))
            {
                Object pa = fields.get("portlet_application");
                String paName = "";
                if(pa != null)
                {
                    if(id instanceof Collection)
                    {
                        Collection coll = (Collection)pa;
                        paName = (String) coll.iterator().next();
                    }
                    else
                    {
                        paName = (String)pa;
                    }
                }
                name = paName + "::" + name;
                found = true;
            }
        }
        if (found == false)
            return null;
        
        return registry.getPortletDefinitionByUniqueName(name);
    }
        
    
    public void processAction(ActionRequest request,
            ActionResponse actionResponse) throws PortletException, IOException
    {
        if (request.getPortletMode() == PortletMode.EDIT)
        {
            String removes = request.getParameter("jsRemovedCats");
            String modifiedCats = request.getParameter("jsModifiedCats");
            String modifiedKeys = request.getParameter("jsModifiedKeys");
            String addedCats = request.getParameter("jsAddedCats");
            String addedKeys = request.getParameter("jsAddedKeys");
            String columns = request.getParameter("Columns");
            String rows = request.getParameter("Rows");
            String cats = request.getParameter("jsCats");
            String page = request.getParameter(JSPAGE);
            PortletApplication pa = registry.getPortletApplication("j2-admin");
            String portletName = this.getPortletName();
            PortletDefinition portlet = (PortletDefinition) PortletApplicationUtils.getPortletOrClone(pa, portletName);
            Preferences prefs = portlet.getPortletPreferences();
            boolean updated = updateNumericPref("Columns", columns, 10, prefs);
            updated = updated | updateNumericPref("Rows", rows, 100, prefs);
                        
            // process removes first
            if (!isEmpty(removes))
            {
                StringTokenizer tokenizer = new StringTokenizer(removes, ",");
                while (tokenizer.hasMoreTokens())
                {
                    String name = tokenizer.nextToken().trim();
                    updated = updated | removePref("Keywords:" + name, prefs);
                }                
            }
            
            // process adds
            if (!isEmpty(addedCats))
            {
                StringTokenizer keyTokenizer = new StringTokenizer(addedKeys, "|");
                StringTokenizer tokenizer = new StringTokenizer(addedCats, ",");
                while (tokenizer.hasMoreTokens() && keyTokenizer.hasMoreTokens())
                {
                    String name = tokenizer.nextToken().trim();
                    String keys = keyTokenizer.nextToken().trim();
                    updated = updated | addPref("Keywords:" + name, keys, prefs);
                }                
            }
            
            // process updates
            if (!isEmpty(modifiedCats))
            {
                StringTokenizer keyTokenizer = new StringTokenizer(modifiedKeys, "|");
                StringTokenizer tokenizer = new StringTokenizer(modifiedCats, ",");
                while (tokenizer.hasMoreTokens())
                {
                    String name = tokenizer.nextToken().trim();
                    String keys = keyTokenizer.nextToken().trim();                    
                    updated = updated | modifyPref("Keywords:" + name, keys, prefs);
                }                
            }
                                    
            try
            {
                if (updated)
                {
                    // process category string list
                    // sort it first
                    StringTokenizer catTokenizer = new StringTokenizer(cats, ",");
                    List sorted = new ArrayList();
                    while (catTokenizer.hasMoreTokens())
                    {
                        String name = catTokenizer.nextToken().trim();
                        sorted.add(name);
                    }
                    Collections.sort(sorted);
                    Iterator si = sorted.iterator();
                    StringBuffer temp = new StringBuffer();
                    int count = 0;
                    while (si.hasNext())
                    {
                        String name = (String)si.next();
                        if (count > 0)
                        {
                            temp.append(",");
                        }
                        temp.append(name);
                        count++;
                    }
                    cats = temp.toString();
                    modifyPref("Categories", cats, prefs);
                    
                    // finally save it all
                    //registry.savePortletDefinition(portlet);
                    preferenceService.storeDefaults(portlet, prefs);                    
                }
            }
            catch (Exception e)
            {
                throw new PortletException("Failed to update portlet", e);
            }
            PortletSession session = request.getPortletSession();
            session.removeAttribute(PORTLETS);
            session.removeAttribute(CATEGORIES);
            actionResponse.setPortletMode(PortletMode.VIEW);
            actionResponse.setRenderParameter(JSPAGE, page);
        }
        else
        {            
        	String reset = request.getParameter("reset");        
            if (reset != null && reset.equals("true"))
            {
                PortletSession session = request.getPortletSession();
                session.removeAttribute(PORTLETS);
                session.removeAttribute(CATEGORIES);
            }
            String category = request.getParameter(CATEGORY);
            if (category != null)
                actionResponse.setRenderParameter(CATEGORY, category);
            String pageNumber = request.getParameter(PAGENUMNER);
            if (pageNumber != null)
                actionResponse.setRenderParameter(PAGENUMNER, pageNumber);                
            String filter = request.getParameter(FILTER);
            if (filter != null)
                actionResponse.setRenderParameter(FILTER, filter);
            String pagePath = request.getParameter(JSPAGE);
            if (pagePath != null)
                actionResponse.setRenderParameter(JSPAGE, pagePath);            
        }
    }

    private boolean addPref(String prefName, String keywords, Preferences prefs)
    {
        Preference pref = prefs.getPortletPreference(prefName);
        if (pref == null)
        {
            pref = prefs.addPreference(prefName);
            pref.setReadOnly(false);
            pref.addValue(keywords);            
        }
        else
        {
            return modifyPref(prefName, keywords, prefs);           
        }        
        return true;
    }
    
    private boolean modifyPref(String prefName, String keywords, Preferences prefs)
    {
        Preference pref = prefs.getPortletPreference(prefName);
        if (pref == null)
        {
            return addPref(prefName, keywords, prefs);
        }
        else
        {
            List<String> values = pref.getValues();
            if (!values.get(0).equals(keywords))
            {
                values.set(0, keywords);
                return true;
            }
        }
        return false;
    }
    
    private boolean removePref(String prefName, Preferences prefs)
    {
        List<Preference> prefs2 = prefs.getPortletPreferences();
        for(Preference pref : prefs2)
        {
            if (pref.getName().equals(prefName))
            {
                prefs2.remove(pref);
                return true;
            }
        }
        return false;
    }
    
    private boolean updateNumericPref(String prefName, String param, int max, Preferences prefs)
    {
        if (!isEmpty(param))
        {
            int val = 4;
            try
            {
                val = Integer.parseInt(param);
            }
            catch (NumberFormatException e)
            {
                return false;                
            }
            if (val > max)
                return false;
            Preference pref = (Preference) prefs.getPortletPreference(prefName);
            List<String> values = pref.getValues();
            if(!values.get(0).equals(param))
            {
                values.set(0, param);
                return true;
            }                           
        }
        return false;
    }
    private boolean isEmpty(String param)
    {
        if (param == null)
            return true;
        param = param.trim();
        if (param.length() == 0)
            return true;
        return false;
    }

    public int compare(Object obj1, Object obj2)
    {
        PortletInfo portlet1 = (PortletInfo)obj1;
        PortletInfo portlet2 = (PortletInfo)obj2;
        String name1 = portlet1.getDisplayName();
        String name2 = portlet2.getDisplayName();
        name1 = (name1 == null) ? "unknown" : name1;
        name2 = (name2 == null) ? "unknown" : name2;
        return name1.compareTo(name2);
    }
        
    protected void includeHeaderContent(HeaderResource headerResource)
    {
        headerResource.dojoAddCoreLibraryRequire( "dojo.widget.Dialog" );
        headerResource.dojoAddCoreLibraryRequire( "dojo.widget.Button" );
        headerResource.dojoAddCoreLibraryRequire( "dojo.widget.ContentPane" );
        headerResource.dojoAddCoreLibraryRequire( "dojo.widget.LayoutContainer" );
        headerResource.dojoAddModuleLibraryRequire( "jetspeed.selector" );
    }

    /* (non-Javadoc)
     * @see org.apache.portals.bridges.velocity.GenericVelocityPortlet#doEdit(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    public void doEdit(RenderRequest request, RenderResponse response) throws PortletException, IOException
    {
        Context context = getContext(request);
        PortletPreferences prefs = request.getPreferences();
        String cats = prefs.getValue("Categories", null);
        List categories = new ArrayList();
        if (cats != null)
        {
            StringTokenizer catTokenizer = new StringTokenizer(cats, ",");
            while (catTokenizer.hasMoreTokens())
            {
                String name = catTokenizer.nextToken().trim();
                CategoryInfo cat = new CategoryInfo(name);
                String keys = prefs.getValue("Keywords:" + name, "");
                cat.setKeywords(keys);
                categories.add(cat);
            }
        }
        context.put("categories", categories);
        context.put("Rows", prefs.getValue("Rows", "5"));
        context.put("Columns", prefs.getValue("Columns", "4"));
        processPage(request);
        super.doEdit(request, response);
    }
    
    private CategoryResult getPortlets(RenderRequest request,
			String category, String pageNumber, int portletPerPages,String filter) {
		List list = new ArrayList();
		List tmpList = null;
		List catList = null;
		Iterator iterator = null;
		int portletCount = 0;
		CategoryInfo catInfo = null;
		int strtCnt = getStartRow(pageNumber, portletPerPages);
		int endCnt = getEndRow(pageNumber, portletPerPages);
		try 
		{			
			if (category.equalsIgnoreCase("all")) {				
				tmpList = retrievePortlets(request, filter);
				portletCount = tmpList.size();
				if (endCnt > portletCount)
					endCnt = portletCount;
				for (int index = strtCnt; index < endCnt; index++) {
					list.add(tmpList.get(index));
				}
			} else if (category.equalsIgnoreCase("search")) {
				tmpList = retrievePortlets(request, filter);
				portletCount = tmpList.size();
				if (endCnt > portletCount)
					endCnt = portletCount;
				for (int index = strtCnt; index < endCnt; index++) {
					list.add(tmpList.get(index));
				}
			} else {
				tmpList =  retrieveCategories(request);
				iterator = tmpList.iterator();
				while (iterator.hasNext()) {
					catInfo = (CategoryInfo) iterator.next();
					if (catInfo.getName().equalsIgnoreCase(category)) {
						catList = catInfo.getPortlets();
						break;
					}
				}
				portletCount = catList.size();
				if (endCnt > portletCount)
					endCnt = portletCount;
				for (int index = strtCnt; index < endCnt; index++) {
					list.add(catList.get(index));
				}
			}
		}
		catch (Exception e) 
		{
			logger.error("Unexpected error during retrieving portlet infos.", e);
		}
		return new CategoryResult(list, portletCount);
	}
    
    
	private int getStartRow(String sPageNumber, int portletPerPages) {
		int iPageNumber = Integer.parseInt(sPageNumber);
		return (iPageNumber - 1) * portletPerPages;
	}

	private int getEndRow(String sPageNumber,int portletPerPages) {
		int iPageNumber = Integer.parseInt(sPageNumber);
		return iPageNumber * portletPerPages;
	}
	private Map getUserPortlet(ContentPage page) {
		int portletCount = 0;
		Map map = new HashMap();
		try 
		{
			ContentFragment fragment;
			Iterator it = page.getRootFragment().getFragments().iterator();
			while (it.hasNext()) {
				fragment = (ContentFragment) it.next();
				if (map.containsKey(fragment.getName())) {
					portletCount =((Integer) map.get(fragment.getName())).intValue();				
					map.put(fragment.getName(), new Integer(portletCount + 1));					
				} else {
					map.put(fragment.getName(), new Integer(1));
				}
			}
		}
		catch (Exception e) 
		{
		    logger.error("Unexpected error during retrieving fragments information.", e);
		}
		return map;
	}

    class CategoryResult {
		List list;

		int resultSize;

		/**
		 * @param list
		 * @param resultSize
		 */
		public CategoryResult(List list, int resultSize) {
			this.list = list;
			this.resultSize = resultSize;
		}

		/**
		 * @return the list
		 */
		public List getList() {
			return list;
		}

		/**
		 * @return the resultSize
		 */
		public int getResultSize() {
			return resultSize;
		}
	}
}
