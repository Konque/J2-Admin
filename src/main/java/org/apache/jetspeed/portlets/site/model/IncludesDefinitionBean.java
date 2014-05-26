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
package org.apache.jetspeed.portlets.site.model;

import java.io.Serializable;

import org.apache.jetspeed.om.folder.MenuIncludeDefinition;

/**
 * @author <a href="mailto:vkumar@apache.org">Vivek Kumar</a>
 * @version $Id$
 */
public class IncludesDefinitionBean implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = -7091578820268065886L;
    private String name;
    private boolean nest;
    
    public IncludesDefinitionBean()
    {
        
    }
    public IncludesDefinitionBean(MenuIncludeDefinition definition)
    {
        this.name = definition.getName();
        this.nest = definition.isNest();
    }
    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }
    /**
     * @return the nest
     */
    public boolean isNest()
    {
        return nest;
    }
    /**
     * @param nest the nest to set
     */
    public void setNest(boolean nest)
    {
        this.nest = nest;
    }
    
}
