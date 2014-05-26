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
package org.apache.jetspeed.portlets.wicket.component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.wicket.IResourceListener;
import org.apache.wicket.Response;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WicketURLEncoder;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:vkumar@apache.org">Vivek Kumar</a>
 * @version $Id:
 */
public class DynamicResourceLink extends Link implements IResourceListener
{
    
    static final Logger logger = LoggerFactory.getLogger(DynamicResourceLink.class);
    
    private ResourceProvider resourceProvider;       
    public DynamicResourceLink(String id, IModel model)
    {
        super(id, model);
        resourceProvider = (ResourceProvider)model.getObject();
    }

    public void onResourceRequested()
    {       
        onClick();
        IResourceStream resourceStream = new IResourceStream()
        {
            {
                resourceProvider.open();
            }
            /** Transient input stream to resource */
            private transient InputStream inputStream = null;
            private transient Locale locale = null;

            /**
             * @see org.apache.wicket.util.resource.IResourceStream#close()
             */
            public void close() throws IOException
            {
                resourceProvider.close();
            }

            /**
             * @see org.apache.wicket.util.resource.IResourceStream#getContentType()
             */
            
            public String getContentType()
            {
                
                return resourceProvider.getContentType();
            }

            /**
             * @see org.apache.wicket.util.resource.IResourceStream#getInputStream()
             */
            public InputStream getInputStream() throws ResourceStreamNotFoundException
            {
                if (inputStream == null)
                {
                    try
                    {                        
                        inputStream = resourceProvider.getResource();
                        
                    }
                    catch (Exception e)
                    {
                        logger.error("Failed to retrieve input stream from the resource.", e);
                    }
                }
                return inputStream;
            }

            /**
             * @see org.apache.wicket.util.watch.IModifiable#lastModifiedTime()
             */
            public Time lastModifiedTime()
            {
                return Time.valueOf(resourceProvider.getLastModified());
            }

            /**
             * @see org.apache.wicket.util.resource.IResourceStream#length()
             */
            public long length()
            {
                return resourceProvider.getLength();
            }

            /**
             * @see org.apache.wicket.util.resource.IResourceStream#getLocale()
             */
            public Locale getLocale()
            {
                return locale;
            }

            /**
             * @see org.apache.wicket.util.resource.IResourceStream#setLocale(java.util.Locale)
             */
            public void setLocale(Locale loc)
            {
                locale = loc;
            }
        };
        // Get servlet response to use when responding with resource
        final Response response = getRequestCycle().getResponse();
        // set not cacheable
        response.setLastModifiedTime(Time.valueOf(-1));
        getRequestCycle().setRequestTarget(new ResourceStreamRequestTarget(resourceStream, WicketURLEncoder.QUERY_INSTANCE.encode(resourceProvider.getName())));
    }

    protected final CharSequence getURL()
    {
        return urlFor(IResourceListener.INTERFACE);
    }

    @Override
    public void onClick()
    {        
    }
}