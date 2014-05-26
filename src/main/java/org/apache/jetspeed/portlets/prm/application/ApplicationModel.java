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
package org.apache.jetspeed.portlets.prm.application;

import org.apache.jetspeed.portlets.prm.ApplicationBean;
import org.apache.wicket.model.LoadableDetachableModel;

/**
 * Provides a model for a single portlet application bean
 * 
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: ApplicationModel.java 769862 2009-04-29 18:26:19Z woonsan $
 */
class ApplicationModel extends LoadableDetachableModel<ApplicationBean>
{
    private static final long serialVersionUID = 1L;
    private ApplicationBean pa;

    /**
     * @param c
     */
    public ApplicationModel(ApplicationBean pa)
    {
        this.pa = pa;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return pa.getApplicationName().hashCode();
    }

    /**
     * used for dataview with ReuseIfModelsEqualStrategy item reuse strategy
     * 
     * @see org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        else if (obj == null)
        {
            return false;
        }
        else if (obj instanceof ApplicationModel)
        {
            ApplicationModel other = (ApplicationModel) obj;
            return pa.getApplicationName().equals(other.getModelBean().getApplicationName());
        }
        return false;
    }

    public ApplicationBean getModelBean()
    {
        return this.pa;
    }

    /**
     * @see org.apache.wicket.model.LoadableDetachableModel#load()
     */
    protected ApplicationBean load()
    {
        return pa;
    }
}