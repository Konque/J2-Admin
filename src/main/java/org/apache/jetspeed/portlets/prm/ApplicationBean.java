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
package org.apache.jetspeed.portlets.prm;

import org.apache.jetspeed.om.portlet.PortletApplication;

/**
 * Represents a portlet application in the wicket widget
 * 
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: ApplicationBean.java 769862 2009-04-29 18:26:19Z woonsan $
 */
public class ApplicationBean extends PortletApplicationNodeBean
{   
    private static final long serialVersionUID = 1L;
    
    protected String path;
    protected String version;
    protected boolean local;
    protected boolean running;
           
    public ApplicationBean(PortletApplication pa)
    {
        this(pa, false);
    }

    public ApplicationBean(PortletApplication pa, boolean running)
    {
        super(pa.getName(), null);

        this.version = pa.getVersion();
        this.local = pa.getApplicationType() == PortletApplication.LOCAL;
        
        if (local)
        {
            this.path = "<local>";
        }
        else    
        {
            this.path = pa.getContextPath();
        }
        
        this.running = running;        
    }
    
    public String toString()
    {
        return getApplicationName();
    }
        
    public String getPath()
    {
        return path;
    }
    
    public boolean isLocal()
    {
        return local;
    }
    
    public boolean isRunning()
    {
        return running;
    }
    
    public String getVersion()
    {
        return version;
    }

}
