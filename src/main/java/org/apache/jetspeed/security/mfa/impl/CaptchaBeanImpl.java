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

import org.apache.jetspeed.security.mfa.CaptchaBean;

/**
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id$
 */
public class CaptchaBeanImpl implements CaptchaBean
{
    private CaptchaImageResource cis;
    private String imageId;
    private String challengeId;
    private String imageURL;
    
    public CaptchaBeanImpl(CaptchaConfiguration config)
    {
        this.cis = new CaptchaImageResource(config);
        this.challengeId = cis.getChallengeId();
        this.imageId = CaptchaImageResource.randomString(7, 9);
    }
    
    public CaptchaBeanImpl(CaptchaConfiguration config, String text)
    {
    	this.cis = new CaptchaImageResource(config, text);
    	this.challengeId = cis.getChallengeId();
    	this.imageId = CaptchaImageResource.randomString(7, 9);
    }
    
    public void setBackgroundImage(byte[] background)
    {
        this.cis.setBackgroundImage(background);
    }
    
    public void init()
    {
        this.cis.init();
    }    
    
    public String getChallengeId()
    {
        return challengeId;
    }
    
    public String getImageId()
    {
        return imageId;
    }    
        
    public byte[] getImageBytes()
    {
        return cis.getImageBytes();
    }
    
    public String getImageURL()
    {
        return imageURL;
    }
    
    public void setImageURL(String url)
    {
        this.imageURL = url;
    }
    
}