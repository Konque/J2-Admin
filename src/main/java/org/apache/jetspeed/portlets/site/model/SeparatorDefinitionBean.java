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

import org.apache.jetspeed.om.folder.MenuSeparatorDefinition;

/**
 * @author <a href="mailto:vkumar@apache.org">Vivek Kumar</a>
 * @version $Id$
 */
public class SeparatorDefinitionBean implements Serializable
{
    private String text;
    private String title;
    
    public SeparatorDefinitionBean()
    {
        
    }
    public SeparatorDefinitionBean(MenuSeparatorDefinition separatorDefinition)
    {
        this.text = separatorDefinition.getText();
        this.title = separatorDefinition.getTitle();
    }
    /**
     * @return the text
     */
    public String getText()
    {
        return text;
    }
    /**
     * @param text the text to set
     */
    public void setText(String text)
    {
        this.text = text;
    }
    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }
    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }
    
}
