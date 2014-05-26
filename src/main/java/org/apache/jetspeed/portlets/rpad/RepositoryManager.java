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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryManager implements Serializable
{

    /**
     * Logger for this class
     */
    private static final Logger log = LoggerFactory.getLogger(RepositoryManager.class);

    private static boolean init = false;

    private Map<String, Repository> repositories;

    private static RepositoryManager repositoryManager;

    public RepositoryManager(Map<String, Repository> repos)
    {
        this.repositories = repos;
    }

    public void load()
    {
        Iterator<String> repoNames = repositories.keySet().iterator();
        Repository localRepository = null;
        while (repoNames.hasNext())
        {
            localRepository = (Repository) repositories.get(repoNames.next());
            localRepository.init();
        }
        init = true;
    }

    public static RepositoryManager getInstance(Map<String, Repository> repos)
    {
        if (repositoryManager == null)
        {
            repositoryManager = new RepositoryManager(repos);
        }
        return repositoryManager;
    }

    public void reload(Map<String, Repository> repos)
    {
        repositories = repos;
        repositoryManager = new RepositoryManager(repos);
        load();
    }

    public void addRepository(String name, Repository repository)
            throws RPADException
    {
        synchronized (repositories)
        {
            if (repositories.containsKey(name)) { throw new RPADException(name
                    + "exists."); }
            repositories.put(name, repository);
            store();
        }
    }

    public Repository getRepository(String name)
    {
        return (Repository) repositories.get(name);
    }

    public void removeRepository(String name) throws RPADException
    {
        synchronized (repositories)
        {
            if (!repositories.containsKey(name)) { throw new RPADException(name
                    + "does not exist."); }
            repositories.remove(name);
            store();
        }
    }

    public List<Repository> getRepositories()
    {
        if (!init) load();
        return new ArrayList(repositories.values());
    }

    public void store() throws RPADException
    {
    }

    public List<PortletApplication> getPortletApplications()
    {
        if (!init) load();
        ArrayList<PortletApplication> list = new ArrayList<PortletApplication>();
        for (Iterator i = repositories.entrySet().iterator(); i.hasNext();)
        {
            Map.Entry entry = (Map.Entry) i.next();
            Repository repo = (Repository) entry.getValue();
            if (repo.isAvailable())
            {
                List<PortletApplication> portlets = repo
                        .getPortletApplications();
                if (portlets != null)
                {
                    list.addAll(portlets);
                }
            }
        }
        return list;
    }

    public List<PortletApplication> getPortletApplications(String name)
    {
        if (!init) load();
        ArrayList<PortletApplication> list = new ArrayList<PortletApplication>();

        Repository repo = getRepository(name);
        if (repo != null && repo.isAvailable())
        {
            List<PortletApplication> portlets = repo.getPortletApplications();
            if (portlets != null)
            {
                list.addAll(portlets);
            }
        }
        return list;
    }

    // TODO search
}
