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

/**
 * Portlet Info
 * 
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: $
 */
public class PortletInfo implements Serializable 
{
    /**
     * 
     */
    String name;
    String displayName;
    String description;
    String image;
    int count;
    
	public PortletInfo(String name, String displayName, String description)
    {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
    }

    public PortletInfo(String name, String displayName, String description, String image)
    {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.image = image;
    }
    
    public PortletInfo(String name, String displayName, String description, String image,int count)
    {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.image = image;
        this.count = count;
    }
    
    
    /**
     * @return Returns the description.
     */
    public String getDescription()
    {
        return description;
    }
    /**
     * @return Returns the displayName.
     */
    public String getDisplayName()
    {
        return displayName;
    }
    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return name;
    }

    
    /**
     * @return Returns the image.
     */
    public String getImage()
    {
        return image;
    }

    
    /**
     * @param image The image to set.
     */
    public void setImage(String image)
    {
        this.image = image;
    }
    /**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}

	public Object clone() throws CloneNotSupportedException {
		return new PortletInfo(this.name,this.displayName,this.description,this.image,this.count);
	}
	
	
}