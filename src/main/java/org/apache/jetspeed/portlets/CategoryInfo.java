/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.jetspeed.portlets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Category Info
 * 
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: $
 */
public class CategoryInfo implements Serializable 
{
    String name;
    String keywords;
    List portlets = new ArrayList();
    
    public CategoryInfo(String name)
    {
        this.name = name;
    }
    public CategoryInfo(String name, String keywords)
    {
        this.name = name;
        this.keywords = keywords;
    }    
    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return name;
    }
    
    public void addPortlet(PortletInfo portlet)
    {
        portlets.add(portlet);
    }
    
    public List getPortlets()
    {
        return portlets;
    }
    
    /**
     * @return Returns the keywords.
     */
    public String getKeywords()
    {
        return keywords;
    }
    
    /**
     * @param keywords The keywords to set.
     */
    public void setKeywords(String keywords)
    {
        this.keywords = keywords;
    }
    
}