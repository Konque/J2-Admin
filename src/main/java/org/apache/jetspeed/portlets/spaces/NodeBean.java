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
package org.apache.jetspeed.portlets.spaces;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.page.document.Node;

public class NodeBean implements Serializable 
{
	private static final long serialVersionUID = 1L;

	private String name;
	private String path;
	private String title;
    private String shortTitle;
	
	public NodeBean()
	{
	}

	public NodeBean(Node node)
	{
		setName(node.getName());
    	setPath(node.getPath());
    	setTitle(node.getTitle());
        setShortTitle(node.getShortTitle());
	}
	
	public String getName() 
	{
		return name;
	}
	
	public void setName(String name) 
	{
		this.name = name;
	}
	
	public String getPath() 
	{
		return path;
	}
	
	public void setPath(String path) 
	{
		this.path = path;
	}
	
	public String getTitle() 
	{
		return title;
	}
	
	public void setTitle(String title) 
	{
		this.title = title;
	}
	
    public String getShortTitle() 
    {
        return shortTitle;
    }
    
    public void setShortTitle(String shortTitle) 
    {
        this.shortTitle = shortTitle;
    }
	
	@Override
	public boolean equals(Object o)
	{
	    if (!(o instanceof NodeBean))
	    {
	        return false;
	    }
	    
	    NodeBean other = (NodeBean) o;
	    return StringUtils.equals(path, other.path); 
	}
	
	@Override
	public int hashCode() 
	{
	    if (path != null)
	    {
	        return path.hashCode();
	    }
	    else
	    {
	        return super.hashCode();
	    }
	}
}
