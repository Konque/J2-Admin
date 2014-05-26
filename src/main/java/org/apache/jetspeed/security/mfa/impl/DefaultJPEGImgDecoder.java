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

import java.awt.image.BufferedImage;
import java.io.InputStream;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.jetspeed.security.mfa.JPEGImgDecoder;

public class DefaultJPEGImgDecoder implements JPEGImgDecoder
{
    private static Class jpegCodecClazz;
    
    public BufferedImage decodeAsBufferedImage(InputStream input) throws Exception
    {
        if (jpegCodecClazz == null)
        {
            jpegCodecClazz = Class.forName("com.sun.image.codec.jpeg.JPEGCodec");
        }
        
        Object decoder = MethodUtils.invokeStaticMethod(jpegCodecClazz, "createJPEGDecoder", input);
        
        return (BufferedImage) MethodUtils.invokeMethod(decoder, "decodeAsBufferedImage", null);
    }
    
}
