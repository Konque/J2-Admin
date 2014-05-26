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

import org.apache.commons.lang.StringUtils;
import org.apache.jetspeed.om.portlet.LocalizedField;
import org.apache.wicket.IClusterable;

public class LocalizedFieldBean implements IClusterable
{
    private static final long serialVersionUID = 1L;
    
    private transient LocalizedField field;
    
    public LocalizedFieldBean(LocalizedField field)
    {
        this.field = field;
    }
    
    public String getName()
    {
        return field.getName();
    }
    
    public void setName(String name)
    {
        field.setName(name);
    }
    
    public String getLocaleString()
    {
        return field.getLocale().toString();
    }
    
    public void setLocaleString(String localeString)
    {
        String language = null;
        String country = null;
        
        String [] tokens = StringUtils.splitPreserveAllTokens(localeString, '_');
        
        if (tokens.length > 0)
        {
            language = tokens[0];
        }
        
        if (tokens.length > 1)
        {
            country = tokens[1];
        }
        
        Locale locale = (country == null ? new Locale(language) : new Locale(language, country));
        field.setLocale(locale);
    }
    
    public String getValue()
    {
        return field.getValue();
    }

    public void setValue(String value)
    {
        field.setValue(value);
    }
    
    public LocalizedField getLocalizedField()
    {
        return this.field;
    }
    
    @Override
    public boolean equals(Object object)
    {
        if (object instanceof LocalizedFieldBean)
        {
            LocalizedFieldBean other = (LocalizedFieldBean) object;
            
            if (getName().equals(other.getName()) && getLocaleString().equals(other.getLocaleString()))
            {
                return true;
            }
        }   
    
        return false;
    }
    
}
