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
package org.apache.jetspeed.security.mfa;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.jetspeed.security.mfa.impl.CaptchaConfiguration;
import org.apache.jetspeed.security.mfa.impl.CaptchaImageResource;
import org.apache.wicket.util.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCaptchaImageResource extends TestCase
{
    
    private static Logger log = LoggerFactory.getLogger(TestCaptchaImageResource.class);
    
    private CaptchaConfiguration captchaConfig;
    private byte [] background;
    private File tempCaptchaFile;
    
    @Override
    public void setUp() throws Exception
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        
        InputStream input = null;
        
        try 
        {
            input = Thread.currentThread().getContextClassLoader().getResourceAsStream("mfa.properties");
            config.load(input);
        } 
        finally 
        {
            IOUtils.closeQuietly(input);
        }
        
        captchaConfig = new CaptchaConfiguration(config);
        captchaConfig.setUseImageBackground(true);
        
        ByteArrayOutputStream output = null;
        
        try 
        {
            input = Thread.currentThread().getContextClassLoader().getResourceAsStream("Jetspeed_white_sm-1.jpg");
            output = new ByteArrayOutputStream();
            IOUtils.copy(input, output);
            background = output.toByteArray();
        } 
        finally 
        {
            IOUtils.closeQuietly(output);
            IOUtils.closeQuietly(input);
        }
        
        tempCaptchaFile = File.createTempFile("captcha-", ".jpg");
    }
    
    @Override
    public void tearDown() throws Exception
    {
        if (tempCaptchaFile != null)
        {
            tempCaptchaFile.delete();
        }
    }
    
    public void testCaptchaImageData() throws Exception
    {
        CaptchaImageResource cir = new CaptchaImageResource(captchaConfig);
        cir.setBackgroundImage(background);
        cir.init();
        
        OutputStream output = null;
        
        
        try 
        {
            output = new FileOutputStream(tempCaptchaFile);
            IOUtils.write(cir.getImageBytes(), output);
        } 
        finally 
        {
            IOUtils.closeQuietly(output);
        }
        
        assertTrue(tempCaptchaFile.length() > 0);
    }
    
}
