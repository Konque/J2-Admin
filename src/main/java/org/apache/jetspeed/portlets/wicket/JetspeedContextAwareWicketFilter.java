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
package org.apache.jetspeed.portlets.wicket;

import org.apache.jetspeed.Jetspeed;
import org.apache.jetspeed.components.ComponentManager;
import org.apache.wicket.protocol.http.WicketFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A special wicket filter to allow access portal context class loader.
 * Wikcet framework needs serialization and deserialization sometimes.
 * If there are some objects to serialize/deserialize in a Wicket application, 
 * which is accessed via shared interface but originally created in other classloader,
 * it could cause errors because Wicket framework cannot find the proper classes
 * during deserialization within the current context classloader by default.
 * <P>
 * However, fortunately, {@link WicketFilter} provides a mechanism to customize the context classloader.
 * </P> 
 * <P>
 * <EM>NOTE: Don't use this filter outside j2-admin and it is not desirable to overuse this filter.</EM>
 * Instead of using this filter, please consider to make your models detachable.
 * It will make your application more portable and easily-deployable.
 * </P>
 * <P>
 * <EM>NOTE: This classloader switching solution will not help http session deserialization by the app server.</EM>
 * </P>
 * 
 * @version $Id: JetspeedContextAwareWicketFilter.java 772103 2009-05-06 09:01:55Z woonsan $
 */
public class JetspeedContextAwareWicketFilter extends WicketFilter
{
    
    final Logger logger = LoggerFactory.getLogger(JetspeedContextAwareWicketFilter.class);
    
    /**
     * By overriding this method of {@link WicketFilter}, we can use other classloader to
     * have Wicket deserialize some objects which were originally created by the other classloader.
     */
    @Override
    protected ClassLoader getClassLoader()
    {
        ClassLoader defaultClassLoader = super.getClassLoader();
        JetspeedContextAwareClassLoader jetspeedContextAwareClassLoader = null;
        
        ComponentManager componentManager = Jetspeed.getComponentManager();
            
        if (componentManager != null)
        {
            ClassLoader jetspeedContextClassLoader = null;
            
            try
            {
                jetspeedContextClassLoader = componentManager.getClass().getClassLoader();
            }
            catch (SecurityException e)
            {
                // The app server can disallow accessing the classloader.
                // We need to print information about this.
                logger.warn("Failed to access the portal context classloader: {}", e.toString());
                logger.warn("Without access portal context classloader, it might not available to use some admin portlets.");
            }
            
            if (jetspeedContextClassLoader != null)
            {
                jetspeedContextAwareClassLoader = new JetspeedContextAwareClassLoader(defaultClassLoader, jetspeedContextClassLoader);
            }
        }
        
        return (jetspeedContextAwareClassLoader != null ? jetspeedContextAwareClassLoader : defaultClassLoader);
    }

    private class JetspeedContextAwareClassLoader extends ClassLoader
    {
        private ClassLoader parent;
        private ClassLoader altParent;
        
        public JetspeedContextAwareClassLoader(ClassLoader parent, ClassLoader altParent)
        {
            super(parent);
            this.altParent = altParent;
        }
        
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException 
        {
            Class<?> result = null;
            
            try
            {
                result = super.loadClass(name, resolve);
            }
            catch (ClassNotFoundException cnfe)
            {
                if (altParent != null)
                {
                    result = altParent.loadClass(name);
                }
            }
            
            return result;
        }
    }
    
}
