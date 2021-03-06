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
package org.apache.jetspeed.security.mfa.portlets;

import java.io.Serializable;

/**
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: $
 */
public class StatusMessage implements Serializable
{    
    private static final long serialVersionUID = 1;    
    private String text;
    private String type;
        
    public static final String INFO  = "portlet-msg-info";
    public static final String ERROR = "portlet-msg-error";
    public static final String ALERT = "portlet-msg-alert";
    public static final String SUCCESS = "portlet-msg-success";
    
    public StatusMessage(String text, String type)
    {
        this.text = new String(text);
        this.type = type;
    }

    public StatusMessage(String text)
    {
        this.text = new String(text);
        this.type = INFO;
    }
    
    
    
    /**
     * @return Returns the text.
     */
    public String getText()
    {
        return text;
    }
    /**
     * @param text The text to set.
     */
    public void setText(String text)
    {
        this.text = text;
    }
    /**
     * @return Returns the type.
     */
    public String getType()
    {
        return type;
    }
    /**
     * @param type The type to set.
     */
    public void setType(String type)
    {
        this.type = type;
    }
}