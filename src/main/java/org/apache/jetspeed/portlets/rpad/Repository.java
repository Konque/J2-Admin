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
package org.apache.jetspeed.portlets.rpad;

import java.io.Serializable;
import java.util.List;

public interface Repository extends Serializable
{
    public void init();

    public String getName();
    
    public String getConfigPath();

    public void setAvailable(boolean b);

    public boolean isAvailable();

    public PortletApplication getPortletApplication(String groupId,
            String artifactId, String version, String packaging);

    public List<PortletApplication>  getPortletApplications();

    public String toXMLString();
}
