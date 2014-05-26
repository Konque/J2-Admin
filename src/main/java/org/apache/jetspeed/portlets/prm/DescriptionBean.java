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
package org.apache.jetspeed.portlets.prm;

import java.util.Locale;

import org.apache.jetspeed.om.portlet.Description;
import org.apache.wicket.IClusterable;

public class DescriptionBean  implements IClusterable
{
    private static final long serialVersionUID = 1L;
    
    private Description description;
    
    public DescriptionBean(Description description)
    {
        this.description = description;
    }
    
    public String getLocaleString()
    {
        return description.getLocale().toString();
    }
    
    public Locale getLocale()
    {
        return description.getLocale();
    }
    
    public String getDescription()
    {
        return description.getDescription();
    }

    public void setDescription(String desc)
    {
        description.setDescription(desc);
    }
    
    @Override
    public boolean equals(Object object)
    {
        if (object instanceof DescriptionBean)
        {
            DescriptionBean other = (DescriptionBean) object;
            
            if (getLocaleString().equals(other.getLocaleString()))
            {
                return true;
            }
        }   
    
        return false;
    }
    
}
