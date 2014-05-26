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
import org.apache.jetspeed.om.portlet.Language;
import org.apache.wicket.IClusterable;

public class LanguageBean implements IClusterable
{
    private static final long serialVersionUID = 1L;
    
    private transient Language language;
    private Locale locale;
    
    public LanguageBean(Language language)
    {
        this.language = language;
        this.locale = language.getLocale();
    }
    
    public String getTitle()
    {
        return language.getTitle();
    }
    
    public void setTitle(String title)
    {
        language.setTitle(title);
    }

    public String getShortTitle()
    {
        return language.getShortTitle();
    }
    
    public void setShortTitle(String shortTitle)
    {
        language.setShortTitle(shortTitle);
    }

    public boolean isSupportedLocale()
    {
        return language.isSupportedLocale();
    }
    
    public void setSupportedLocale(boolean supportedLocale)
    {
        language.setSupportedLocale(supportedLocale);
    }
    
    public String getKeywords()
    {
        return language.getKeywords();
    }
    
    public void setKeywords(String keywords)
    {
        language.setKeywords(keywords);
    }
    
    public String getLocaleString()
    {
        return locale.toString();
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
        
        locale = (country == null ? new Locale(language) : new Locale(language, country));
    }
    
    public Locale getLocale()
    {
        return locale;
    }
    
}
