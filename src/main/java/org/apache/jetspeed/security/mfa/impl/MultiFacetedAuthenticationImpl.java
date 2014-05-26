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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jetspeed.security.mfa.CaptchaBean;
import org.apache.jetspeed.security.mfa.MultiFacetedAuthentication;
import org.apache.jetspeed.security.mfa.TextToSpeechBean;
import org.apache.jetspeed.security.mfa.util.ServerData;

/* license not compatible? 
import javax.sound.sampled.AudioFileFormat;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.audio.AudioPlayer;
import com.sun.speech.freetts.audio.SingleFileAudioPlayer;
*/

/**
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id: $
 */
public class MultiFacetedAuthenticationImpl implements MultiFacetedAuthentication
{
    protected final static Log log = LogFactory.getLog(MultiFacetedAuthenticationImpl.class);
        
    private String captchasRealPath;
    private String ttsRealPath;
    private String backgroundRealPath;
    private String rootPath;
    
    private CaptchaConfiguration captchaConfig;
    private TTSConfiguration ttsConfig;
    private ResourceRemovalCache removalCache;
    private byte[] background = null;
    
    public MultiFacetedAuthenticationImpl(Configuration cc, Configuration tc)
    {
        this(cc, tc, null);
    }
    
    public MultiFacetedAuthenticationImpl(Configuration cc, Configuration tc, String rootPath)
    {
        // captcha configuration
        this.captchaConfig = new CaptchaConfiguration(cc);        
        // text-to-speech configuration
        this.ttsConfig = new TTSConfiguration(tc);

        this.rootPath = rootPath;
        captchasRealPath = concatenatePaths(rootPath, captchaConfig.getDirectory());
        ttsRealPath = concatenatePaths(rootPath, ttsConfig.getDirectory());
        backgroundRealPath = concatenatePaths(rootPath, captchaConfig.getImageBackground());
        if (this.captchaConfig.isUseImageBackground())
        {
            loadBackground();
        }
        
        // image removal scanner
        this.removalCache = new ResourceRemovalCache(captchaConfig.getScanRateSeconds(), captchaConfig.getTimetoliveSeconds());
        this.removalCache.setDaemon(true);
        try
        {
            this.removalCache.start();
        }
        catch (java.lang.IllegalThreadStateException e)
        {
            log.error("Exception starting scanner", e);
        }        
    }

    private void loadBackground()
    {
        InputStream is = null;
        try
        {
            is = new FileInputStream(backgroundRealPath);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            drain(is, bytes);
            background = bytes.toByteArray();
        }
        catch (IOException e)
        {
            this.captchaConfig.setUseImageBackground(false);
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException ee) 
                {}
            }
        }        
    }
        
    public void destroy()
    {
        this.removalCache.shutdown();
    }
    
    public CaptchaBean createCaptcha(HttpServletRequest request)
    {
    	return createCaptcha(request, null);
    }
    
    public CaptchaBean createCaptcha(HttpServletRequest request, String text)
    {
    	CaptchaBean captcha;
    	if ( text == null )
    		captcha = new CaptchaBeanImpl(captchaConfig);       
    	else
    		captcha = new CaptchaBeanImpl(captchaConfig, text);

        if (captchaConfig.isUseImageBackground())
        {
            captcha.setBackgroundImage(this.background);
        }        
        FileOutputStream fs = null;
        ServerData url = new ServerData(request);
        String imageUrl =  url.getBasePath() + url.getContextPath() + captchaConfig.getDirectory() + "/" + captcha.getImageId() + captchaConfig.getImageFormat();        
        captcha.setImageURL(imageUrl);
        String imagePath = this.captchasRealPath + "/" + captcha.getImageId() + captchaConfig.getImageFormat();
        
        captcha.init();
        
        try
        {
            fs = new FileOutputStream(imagePath);
            byte[] data = captcha.getImageBytes();
            fs.write(data);
            this.removalCache.insert(new RemovableResource(captcha.getImageId(), imagePath));
        } 
        catch (IOException e)
        {
            log.error("Unexpected error during writing captch image.", e);
            imageUrl = "";
        } 
        finally
        {
            try
            {
                if (fs != null)
                    fs.close();
            } catch (IOException e)
            {}
        }
        
        return captcha;
    }
    
    public TextToSpeechBean createTextToSpeech(HttpServletRequest request, String text)
    {
        TextToSpeechBean tts = new TextToSpeechBeanImpl(text);
        ServerData url = new ServerData(request);
        String audioUrl =  url.getBasePath() + url.getContextPath() + ttsConfig.getDirectory() + "/" + tts.getAudioId() + ttsConfig.getImageFormat();        
        tts.setAudioURL(audioUrl);
        play(tts);
        return tts;
    }
    
    public void play(TextToSpeechBean ttsBean)
    {
/* // TODO: LICENSE        
        boolean phonetic = true;
        String imageText = ttsBean.getText();
        if (imageText == null || imageText.trim().length() == 0)
        {
            log.error(this.getClass().getName() + " no text to write "
                    + imageText);
            return;
        }
        // build the full text string
        imageText = imageText.trim();
        StringBuffer buf = new StringBuffer(ttsConfig.getConfiguration().getString("YOUR_SECURE_IMAGE_TEXT_IS"));
        for (int i = 0; i < imageText.length(); i++)
        {
            char c = imageText.charAt(i);
            if (Character.isUpperCase(c))
            {
                buf.append(ttsConfig.getConfiguration().getString("capital"));
            } 
            else if (Character.isDigit(c))
            {
                buf.append(ttsConfig.getConfiguration().getString("number"));
            }
            buf.append("'" + c + "' ");
            if (phonetic)
            {
                String phon = TTSHelper.getPhonetic(c, ttsConfig.getConfiguration());
                if (phon != null)
                {
                    // problems with a as in
                    buf.append(ttsConfig.getConfiguration().getString("AS_IN") + phon);
                }
            }
            buf.append(" ,, ");
        }
        String textToSave = buf.toString();
        // create voice
        Voice voice = VoiceManager.getInstance().getVoice(ttsConfig.getVoiceName());
        if (voice == null)
        {
            log.error(this.getClass().getName() + " Cannot load a voice named " + ttsConfig.getVoiceName());
            return;
        }
        // Allocate the resources for the voice.
        voice.setVerbose(ttsConfig.isDebug());
        voice.setPitch(ttsConfig.getPitch());
        voice.setRate(ttsConfig.getRate());
        voice.allocate();

        // Create output file
        String audioPath = this.ttsRealPath + "/" + ttsBean.getAudioId() + ttsConfig.getImageFormat();               
        AudioFileFormat.Type type = TTSHelper.getAudioType(audioPath); 
        AudioPlayer audioPlayer = new SingleFileAudioPlayer(TTSHelper.getBaseName(audioPath), type);
        voice.setAudioPlayer(audioPlayer);

         // Synthesize speech based on whether or not build is successful
        voice.startBatch();
        if (!voice.speak(textToSave))
        {
            log.error(this.getClass().getName()
                    + " Cannot save text to speach ");
        }
        voice.endBatch();

        // cleanup. Be a good citizen.
        audioPlayer.close();

        voice.deallocate();
        */
    }

    static final int BLOCK_SIZE = 4096;

    public static void drain(InputStream r, OutputStream w) throws IOException
    {
        byte[] bytes = new byte[BLOCK_SIZE];
        try
        {
            int length = r.read(bytes);
            while (length != -1)
            {
                if (length != 0)
                {
                    w.write(bytes, 0, length);
                }
                length = r.read(bytes);
            }
        } finally
        {
            bytes = null;
        }
    }
    
    private static String concatenatePaths(String root, String rel)
    {
        if (root == null)
        {
            return rel;
        }        
        if (rel.startsWith("/"))
        {
            return root + rel.substring(1);
        }
        else
        {
            return root + rel;
        }        
    }
    
    
}