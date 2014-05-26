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

import org.apache.jetspeed.om.portlet.DisplayName;
import org.apache.wicket.IClusterable;

public class DisplayNameBean  implements IClusterable
{
    private static final long serialVersionUID = 1L;
    
    private DisplayName displayName;
    
    public DisplayNameBean(DisplayName displayName)
    {
        this.displayName = displayName;
    }
    
    public String getLocaleString()
    {
        return displayName.getLocale().toString();
    }
    
    public Locale getLocale()
    {
        return displayName.getLocale();
    }
    
    public String getDisplayName()
    {
        return displayName.getDisplayName();
    }

    public void setDisplayName(String dispName)
    {
        displayName.setDisplayName(dispName);
    }
    
}
