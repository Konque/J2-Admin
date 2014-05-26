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

import org.apache.jetspeed.spaces.Space;

public class SpaceBean extends NodeBean
{
    
	private static final long serialVersionUID = 1L;

	private String owner;
	private String description;
	private String theme;
	private String constraint;
	private boolean persisted = false;
	private String userHomePath = null;
	private String userHomeName = null;
	
    public SpaceBean()
    {
        super();
    }
    
	public SpaceBean(String name, String owner)
	{
	    super();
	    setName(name);
		this.owner = owner;
	}
	
	public SpaceBean(Space s)
	{
        super();
		setName(s.getName());
    	setPath(s.getPath());
    	setOwner(s.getOwner());
    	setTitle(s.getTitle());
    	setShortTitle(s.getShortTitle());
    	setDescription(s.getDescription());
    	setTheme(s.getTheme());
    	setSecurityConstraint(s.getSecurityConstraint());
    	persisted = true;
	}
	
	public boolean isPersisted()
	{
		return persisted;
	}
	
	public String getOwner() 
	{
		return owner;
	}
	
	public void setOwner(String owner) 
	{
		this.owner = owner;
	}
	
	public String getDescription() 
	{
		return description;
	}
	
	public void setDescription(String description) 
	{
		this.description = description;
	}
	
	public String getTheme() 
	{
		return theme;
	}
	
	public void setTheme(String theme) 
	{
		this.theme = theme;
	}
	
	public String getSecurityConstraint() 
	{
		return constraint;
	}
	
	public void setSecurityConstraint(String constraint) 
	{
		this.constraint = constraint;
	}

	public String getUserHome()
	{
		return this.userHomeName;
	}

	public void setUserHomeName(String userHomeName)
	{
		this.userHomeName = userHomeName;
	}

	public String getUserHomePath()
	{
		return this.userHomePath;
	}
	
	public void setUserHomePath(String homePath)
	{
		this.userHomePath = homePath;
	}
}
