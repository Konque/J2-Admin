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

/**
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: $
 */
public interface CaptchaBean
{
    void init();
    
    /**
     * Get the challenge captcha string that is encoded into an image
     * @return
     */
    String getChallengeId();
    
    /**
     * Get the image name string for the captcha 
     * @return
     */
    String getImageId();
    
    /**
     * Get the image content as a byte array
     * @return
     */
    byte[] getImageBytes();
    
    /**
     * Get the full URL of the image resource
     * 
     * @return
     */
    String getImageURL();
    void setImageURL(String url);
    
    void setBackgroundImage(byte[] image);
}