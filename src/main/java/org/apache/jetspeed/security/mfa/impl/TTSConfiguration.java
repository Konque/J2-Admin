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

import org.apache.commons.configuration.Configuration;

/**
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id$
 */
public final class TTSConfiguration
{
    public final String DIRECTORY = "tts.directory";
    public final String IMAGE_FORMAT = "tts.image.format";
    public final String VOICE_NAME = "tts.voice.name";
    public final String DEBUG = "debug";
    public final String PITCH = "pitch";
    public final String RATE = "rate";
    
    // Text-To-Speech properties
    private String directory = "/tts";
    private String imageFormat = ".wav";
    private String voiceName = "kevin16";
    private boolean debug = false;
    private int pitch = 150;
    private int rate = 110;
    private Configuration config;
       
    public TTSConfiguration(Configuration c)
    {
        this.config = c;
        this.setDebug(config.getBoolean(DEBUG));
        this.setDirectory(config.getString(DIRECTORY));
        this.setVoiceName(config.getString(VOICE_NAME));
        this.setImageFormat(config.getString(IMAGE_FORMAT));
        this.setPitch(config.getInt(PITCH));
        this.setRate(config.getInt(RATE));
    }
    
    public boolean isDebug()
    {
        return this.debug;
    }
    
    public void setDebug(boolean debug)
    {
        this.debug = debug;
    }
    
    public String getDirectory()
    {
        return directory;
    }
    
    public void setDirectory(String ttsDir)
    {
        this.directory = ttsDir;
    }
    
    public String getImageFormat()
    {
        return imageFormat;
    }
    
    public void setImageFormat(String ttsImageFormat)
    {
        this.imageFormat = ttsImageFormat;
    }
    
    public int getPitch()
    {
        return pitch;
    }
    
    public void setPitch(int ttsPitch)
    {
        this.pitch = ttsPitch;
    }
    
    public int getRate()
    {
        return rate;
    }
    
    public void setRate(int ttsRate)
    {
        this.rate = ttsRate;
    }
    
    public String getVoiceName()
    {
        return voiceName;
    }
    
    public void setVoiceName(String ttsVoiceName)
    {
        this.voiceName = ttsVoiceName;
    }
    
    public Configuration getConfiguration()
    {
        return config;
    }
    
}