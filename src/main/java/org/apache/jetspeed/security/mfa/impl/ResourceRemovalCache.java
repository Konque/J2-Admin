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
package org.apache.jetspeed.security.mfa.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: $
 */
public class ResourceRemovalCache extends Thread
{
    protected final static Log log = LogFactory.getLog(ResourceRemovalCache.class);
    
    private boolean stopping = false;
    private long scanRateSeconds = 60;
    private long msTimeToLive = 120 * 1000;
    private Map resources = new HashMap();
    
    public ResourceRemovalCache(long scanRateSeconds, long timeToLiveSeconds)
    {
        this.scanRateSeconds = scanRateSeconds;
        this.msTimeToLive = timeToLiveSeconds * 1000;
    }
    
    public void setStopping(boolean flag)
    {
        // stop this removal thread
        synchronized (this)
        {
            if (!stopping && flag)
            {
                stopping = flag;
                notifyAll();
            }
            else
            {
                flag = false;
            }
        }
        // wait for removal thread to stop
        if (flag)
        {
            try
            {
                join(scanRateSeconds * 1000);
            }
            catch (InterruptedException ie)
            {
            }
        }
    }

    public void insert(RemovableResource r)
    {
        synchronized (resources)
        {
            resources.put(r.getKey(), r);
        }
    }
    
   public void shutdown()
   {
       // stop removal thread
       setStopping(true);
       // final traverse to delete all
       traverse(true);
   }
   
    /**
     * Run the file scanner thread
     *
     */
    public synchronized void run()
    {
        traverse(false);
    }
    
    protected synchronized void traverse(boolean deleteAll)
    {
        boolean done = false;
        try
        {
            while(!done)
            {
                try
                {
                    if (deleteAll)
                    {
                        done = true;
                    }
                    int count = 0;
                                        
                    List deleted = new LinkedList();
                    Iterator it = resources.entrySet().iterator();
                    while (it.hasNext())
                    {
                        Map.Entry e = (Map.Entry)it.next();
                        RemovableResource r = (RemovableResource)e.getValue();
                        long expired = System.currentTimeMillis() - r.getInsertedTime();
                        //System.out.println("expired = " + expired);
                        if (deleteAll || expired > this.msTimeToLive)
                        {
                            //System.out.println("*** resource Expired: " + r.getKey());
                            deleted.add(r);
                        }
                        //    System.out.println("*** resource not expired: " + r.getKey());
                    }
                    
                    it = deleted.iterator();
                    while (it.hasNext())
                    {
                        RemovableResource r = (RemovableResource)it.next();
                            try
                            {
                                // remove from file system
                                File f = new File(r.getResource());
                                if (f.exists())
                                {
                                    f.delete();
                                    //System.out.println("*** deleted : " + r.getKey());                                        
                                }
                                synchronized(resources)
                                {                            
                                    resources.remove(r.getKey());
                                }                                
                            }
                            catch (Exception e1)
                            {
                                log.error("Could not delete " + r.getResource());
                            }
                        }                        
                }
                catch (Exception e)
                {
                    log.error("FileCache Scanner: Error in iteration...", e);                    
                }

                if (!done)
                    wait(scanRateSeconds * 1000);                

                if (this.stopping)
                {
                    this.stopping = false;
                    done = true;
                }
            }
        }
        catch (InterruptedException e)
        {
            log.error("FileCacheScanner: recieved interruption, exiting.", e);
        }
    }
    
}