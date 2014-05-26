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
package org.apache.jetspeed.portlets.rpad.portlet.deployer.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import org.apache.commons.io.IOUtils;
import org.apache.jetspeed.deployment.DeploymentException;
import org.apache.jetspeed.deployment.DeploymentManager;
import org.apache.jetspeed.deployment.DeploymentStatus;
import org.apache.jetspeed.portlets.rpad.PortletApplication;
import org.apache.jetspeed.portlets.rpad.portlet.deployer.PortletDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JetspeedPortletDeployer implements PortletDeployer
{
    private static final long serialVersionUID = 1L;
    
    /**
     * Logger for this class
     */
    private static final Logger log = LoggerFactory.getLogger(JetspeedPortletDeployer.class);
    private int status;
    private String message = "Ready";
    private long startTime = 0;

    public JetspeedPortletDeployer()
    {
        status = READY;
    }

    public int getStatus()
    {
        return status;
    }

    synchronized public void deploy(PortletApplication portlet, DeploymentManager manager)
    {
        if (status != READY)
        {
            return;
        }
        DeployerThread deployer = new DeployerThread();
        deployer.setDeploymentManager(manager);
        deployer.setPortletApplication(portlet);
        try
        {
            deployer.start();
        }
        catch (Exception e)
        {
            log.error("Could not start deployment process.", e);
        }
    }

    public class DeployerThread extends Thread
    {
        private DeploymentManager deploymentManager;
        private PortletApplication portletApplication;

        /*
         * (non-Javadoc)
         * @see java.lang.Thread#run()
         */
        public void run()
        {
            status = DEPLOYING;
            setMessage("Start deploying");
            try
            {
                startTime = Calendar.getInstance().getTimeInMillis();
                if (getDeploymentManager() != null)
                {
                    String binaryUrl = portletApplication.getBinaryUrl();
                    if (binaryUrl != null && !binaryUrl.equals(""))
                    {
                        setMessage("Start dowloading from " + binaryUrl);
                        
                        URL targetURL = null;
                        InputStream is = null;
                        
                        File targetFile = null;
                        OutputStream os = null;
                        BufferedOutputStream bos = null;
                        
                        try
                        {
                            targetURL = new URL(portletApplication.getBinaryUrl());
                            is = targetURL.openStream();
                            
                            File tempFile = File.createTempFile("rpad_", "." + portletApplication.getPackaging());
                            os = new FileOutputStream(tempFile);
                            bos = new BufferedOutputStream(os);
                            
                            // Commons IO's IOUtils#copy() method buffers the input internally.
                            IOUtils.copy(is, bos);
                            
                            bos.close();
                            bos = null;
                            is.close();
                            is = null;
                            
                            try
                            {
                                targetFile = new File(tempFile.getParentFile(), portletApplication.getArtifactId() + "." + portletApplication.getPackaging());
                                tempFile.renameTo(targetFile);
                            }
                            catch (Exception e)
                            {
                                targetFile = tempFile;
                            }
                            
                            setMessage(portletApplication.getName() + " deploying start");
                            
                            if (getDeploymentManager().deploy(targetFile).getStatus() == DeploymentStatus.STATUS_OKAY)
                            {
                                log.info(portletApplication.getName() + " was deployed.");
                                setMessage(portletApplication.getName() + " was deployed.");
                            }
                            else
                            {
                                setMessage(portletApplication.getName() + " was deployed.");
                                log.error("Could not deploy " + portletApplication.getName());
                            }
                        }
                        catch (MalformedURLException e)
                        {
                            setMessage("Malformed url: " + binaryUrl);
                            log.error(e.getMessage(), e);
                        }
                        catch (FileNotFoundException e)
                        {
                            setMessage("download fail from from " + binaryUrl);
                            log.error(e.getMessage(), e);
                        }
                        catch (IOException e)
                        {
                            setMessage("download fail from from " + binaryUrl);
                            log.error(e.getMessage(), e);
                        }
                        catch (DeploymentException e)
                        {
                            setMessage("download fail from from " + binaryUrl);
                            log.error(e.getMessage(), e);
                        }
                        finally
                        {
                            if (bos != null)
                            {
                                try { bos.close(); } catch (Exception ce) { }
                            }
                            if (os != null)
                            {
                                try { os.close(); } catch (Exception ce) { }
                            }
                            if (is != null)
                            {
                                try { is.close(); } catch (Exception ce) { }
                            }
                        }
                        
                        if (targetFile != null && targetFile.exists())
                        {
                            targetFile.delete();
                        }
                    }
                    else
                    {
                        setMessage("The target url is invalid. The path is " + binaryUrl);
                        log.error("The target url is invalid. The path is " + binaryUrl);
                    }
                }
                else
                {
                    log.error("Could not find the deployment manager.");
                }
            }
            catch (Exception e)
            {
                setMessage(e.getMessage());
                log.error("Unexpected exception.", e);
            }
            finally
            {
                setMessage("Ready");
                status = READY;
            }
        }

        /**
         * @return the portletApplication
         */
        public PortletApplication getPortletApplication()
        {
            return portletApplication;
        }

        /**
         * @param portletApplication
         *            the portletApplication to set
         */
        public void setPortletApplication(PortletApplication portletApplication)
        {
            this.portletApplication = portletApplication;
        }

        /**
         * @return the startTime
         */
        public long getStartTime()
        {
            return startTime;
        }

        /**
         * @return the deploymentManager
         */
        public DeploymentManager getDeploymentManager()
        {
            return deploymentManager;
        }

        /**
         * @param deploymentManager
         *            the deploymentManager to set
         */
        public void setDeploymentManager(DeploymentManager deploymentManager)
        {
            this.deploymentManager = deploymentManager;
        }
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}
