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

import org.apache.jetspeed.om.folder.MenuOptionsDefinition;

/**
 * @author <a href="mailto:vkumar@apache.org">Vivek Kumar</a>
 * @version $Id$
 */
public class OptionsDefinitionBean implements Serializable
{
    private String options;
    private int depth;
    private boolean paths;
    private boolean regexp;
    private String profile;
    private String order;
    private String skin;

    public OptionsDefinitionBean()
    {
        
    }
    public OptionsDefinitionBean(MenuOptionsDefinition definition)
    {
        this.options = definition.getOptions();
        this.depth = definition.getDepth();
        this.paths = definition.isPaths();
        this.regexp = definition.isRegexp();
        this.skin = definition.getSkin();
        this.profile = definition.getProfile();
        this.order = definition.getOrder();

    }

    /**
     * @return the options
     */
    public String getOptions()
    {
        return options;
    }

    /**
     * @param options
     *            the options to set
     */
    public void setOptions(String options)
    {
        this.options = options;
    }

    /**
     * @return the depth
     */
    public int getDepth()
    {
        return depth;
    }

    /**
     * @param depth
     *            the depth to set
     */
    public void setDepth(int depth)
    {
        this.depth = depth;
    }

    /**
     * @return the paths
     */
    public boolean isPaths()
    {
        return paths;
    }

    /**
     * @param paths
     *            the paths to set
     */
    public void setPaths(boolean paths)
    {
        this.paths = paths;
    }

    /**
     * @return the regexp
     */
    public boolean isRegexp()
    {
        return regexp;
    }

    /**
     * @param regexp
     *            the regexp to set
     */
    public void setRegexp(boolean regexp)
    {
        this.regexp = regexp;
    }

    /**
     * @return the profile
     */
    public String getProfile()
    {
        return profile;
    }

    /**
     * @param profile
     *            the profile to set
     */
    public void setProfile(String profile)
    {
        this.profile = profile;
    }

    /**
     * @return the order
     */
    public String getOrder()
    {
        return order;
    }

    /**
     * @param order
     *            the order to set
     */
    public void setOrder(String order)
    {
        this.order = order;
    }

    /**
     * @return the skin
     */
    public String getSkin()
    {
        return skin;
    }

    /**
     * @param skin
     *            the skin to set
     */
    public void setSkin(String skin)
    {
        this.skin = skin;
    }
}
