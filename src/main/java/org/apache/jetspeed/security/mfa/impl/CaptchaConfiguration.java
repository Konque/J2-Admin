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
 * @version $Id: $
 */
public final class CaptchaConfiguration
{
    public final String DIRECTORY = "captcha.directory";
    public final String EFFECTS_NOISE = "captcha.effects.noise";
    public final String IMAGE_BACKGROUND = "captcha.image.background";
    public final String USE_IMAGE_BACKGROUND = "captcha.image.background.use";
    public final String IMAGE_FORMAT = "catcha.image.format";
    public final String FONT_ANTIALIASING = "captcha.font.antialiasing";
    public final String FONT_SIZE = "captcha.font.size";
    public final String FONT_SIZE_RANDOM = "captcha.font.size.random";
    public final String FONT_NAMES = "captcha.font.names";
    public final String FONT_STYLE = "captcha.font.style";
    public final String SCANRATE_SECONDS = "captcha.scanrate.seconds";
    public final String TIMETOLIVE_SECONDS = "captcha.timetolive.seconds";
    public final String TEXT_MAXLENGTH = "captcha.text.maxlength";
    public final String TEXT_MARGIN_LEFT = "captcha.text.margin.left";
    public final String TEXT_MARGIN_BOTTOM = "captcha.text.margin.bottom";    
    public final String TEXT_MINLENGTH = "captcha.text.minlength";
    public final String TEXT_ROTATION = "captcha.text.rotation";
    public final String TEXT_SHEAR = "captcha.text.shear";
    public final String TEXT_RISE_RANGE = "captcha.text.rise.range";
    public final String TEXT_SPACING = "captcha.text.spacing";
    public final String TIMESTAMP = "captcha.timestamp";
    public final String TIMESTAMP_24HR = "captcha.timestamp.24hr";
    public final String TIMESTAMP_TZ = "captcha.timestamp.tz";
    public final String TIMESTAMP_FONT_SIZE = "captcha.timestamp.font.size";
    
    private String directory;
    private String directoryRealPath;
    private boolean isEffectsNoise;
    private String imageBackground;
    private boolean useImageBackground;
    private String imageFormat;
    private boolean isFontAntialiasing;
    private int fontSize;
    private boolean isFontSizeRandom;
    private String[]fontNames;
    private int fontStyle;
    private int scanRateSeconds;
    private int timetoliveSeconds;
    private int textMaxlength;
    private int textMarginLeft;
    private int textMarginBottom;
    private int textMinlength;
    private int textRotation;
    private double textShear;
    private int textSpacing;
    private int textRiseRange;
    private boolean useTimestamp;
    private boolean useTimestamp24hr;
    private String timestampTZ;
    private int timestampFontSize;
    private Configuration config;
    
    public CaptchaConfiguration(Configuration c)
    {
        this.config = c;
        setDirectory(config.getString(DIRECTORY));
        setEffectsNoise(config.getBoolean(EFFECTS_NOISE));
        setImageBackground(config.getString(IMAGE_BACKGROUND));
        setUseImageBackground(config.getBoolean(USE_IMAGE_BACKGROUND));
        setImageFormat(config.getString(IMAGE_FORMAT));
        setFontAntialiasing(config.getBoolean(FONT_ANTIALIASING));
        setFontSize(config.getInt(FONT_SIZE));
        setFontSizeRandom(config.getBoolean(FONT_SIZE_RANDOM));
        setFontNames(config.getStringArray(FONT_NAMES));
        setFontStyle(config.getInt(FONT_STYLE));
        setScanRateSeconds(config.getInt(SCANRATE_SECONDS));
        setTimetoliveSeconds(config.getInt(TIMETOLIVE_SECONDS));
        setTextMaxlength(config.getInt(TEXT_MAXLENGTH));
        setTextMarginLeft(config.getInt(TEXT_MARGIN_LEFT));
        setTextMarginBottom(config.getInt(TEXT_MARGIN_BOTTOM));
        setTextMinlength(config.getInt(TEXT_MINLENGTH));
        setTextRotation(config.getInt(TEXT_ROTATION));
        setTextShear(config.getDouble(TEXT_SHEAR));
        setTextRiseRange(config.getInt(TEXT_RISE_RANGE));
        setTextSpacing(config.getInt(TEXT_SPACING));
        setUseTimestamp(config.getBoolean(TIMESTAMP));
        setUseTimestamp24hr(config.getBoolean(TIMESTAMP_24HR));
        setTimestampTZ(config.getString(TIMESTAMP_TZ));
        setTimestampFontSize(config.getInt(TIMESTAMP_FONT_SIZE));
    }
    
    public String getDirectory()
    {
        return directory;
    }
    
    public void setDirectory(String directory)
    {
        this.directory = directory;
    }
        
    public String[] getFontNames()
    {
        return fontNames;
    }
    
    public void setFontNames(String[] fontNames)
    {
        this.fontNames = fontNames;
    }
    
    public int getFontSize()
    {
        return fontSize;
    }
    
    public void setFontSize(int fontSize)
    {
        this.fontSize = fontSize;
    }
    
    public String getImageBackground()
    {
        return imageBackground;
    }
    
    public void setImageBackground(String imageBackground)
    {
        this.imageBackground = imageBackground;
    }
    
    public String getImageFormat()
    {
        return imageFormat;
    }
    
    public void setImageFormat(String imageFormat)
    {
        this.imageFormat = imageFormat;
    }
    
    public boolean isEffectsNoise()
    {
        return isEffectsNoise;
    }
    
    public void setEffectsNoise(boolean isEffectsNoise)
    {
        this.isEffectsNoise = isEffectsNoise;
    }
    
    public boolean isFontAntialiasing()
    {
        return isFontAntialiasing;
    }
    
    public void setFontAntialiasing(boolean isFontAntialiasing)
    {
        this.isFontAntialiasing = isFontAntialiasing;
    }
    
    public int getScanRateSeconds()
    {
        return scanRateSeconds;
    }
    
    public void setScanRateSeconds(int scanRateSeconds)
    {
        this.scanRateSeconds = scanRateSeconds;
    }
    
    public int getTextMarginLeft()
    {
        return textMarginLeft;
    }
    
    public void setTextMarginLeft(int textMargin)
    {
        this.textMarginLeft = textMargin;
    }

    public int getTextMarginBottom()
    {
        return textMarginBottom;
    }
    
    public void setTextMarginBottom(int textMargin)
    {
        this.textMarginBottom = textMargin;
    }
    
    public int getTextMaxlength()
    {
        return textMaxlength;
    }
    
    public void setTextMaxlength(int textMaxlength)
    {
        this.textMaxlength = textMaxlength;
    }
    
    public int getTextMinlength()
    {
        return textMinlength;
    }
    
    public void setTextMinlength(int textMinlength)
    {
        this.textMinlength = textMinlength;
    }
    
    public int getTextRiseRange()
    {
        return textRiseRange;
    }
    
    public void setTextRiseRange(int textRiseRange)
    {
        this.textRiseRange = textRiseRange;
    }
    
    public int getTextRotation()
    {
        return textRotation;
    }
    
    public void setTextRotation(int textRotation)
    {
        this.textRotation = textRotation;
    }
    
    public int getTimetoliveSeconds()
    {
        return timetoliveSeconds;
    }
    
    public void setTimetoliveSeconds(int timetoliveSeconds)
    {
        this.timetoliveSeconds = timetoliveSeconds;
    }
    
    public boolean isUseTimestamp()
    {
        return useTimestamp;
    }
    
    public void setUseTimestamp(boolean useTimestamp)
    {
        this.useTimestamp = useTimestamp;
    }
    
    public String getDirectoryRealPath()
    {
        return directoryRealPath;
    }
    
    public void setDirectoryRealPath(String directoryRealPath)
    {
        this.directoryRealPath = directoryRealPath;
    }
    
    public int getFontStyle()
    {
        return fontStyle;
    }
    
    public void setFontStyle(int fontStyle)
    {
        this.fontStyle = fontStyle;
    }
    
    public boolean isFontSizeRandom()
    {
        return isFontSizeRandom;
    }
    
    public void setFontSizeRandom(boolean isfontSizeRandom)
    {
        this.isFontSizeRandom = isfontSizeRandom;
    }
    
    public boolean isUseImageBackground()
    {
        return useImageBackground;
    }
    
    public void setUseImageBackground(boolean useImageBackground)
    {
        this.useImageBackground = useImageBackground;
    }
    
    public double getTextShear()
    {
        return textShear;
    }
    
    public void setTextShear(double textShear)
    {
        this.textShear = textShear;
    }
    
    public int getTextSpacing()
    {
        return textSpacing;
    }
    
    public void setTextSpacing(int textSpacing)
    {
        this.textSpacing = textSpacing;
    }
    
    public boolean isUseTimestamp24hr()
    {
        return useTimestamp24hr;
    }
    
    public void setUseTimestamp24hr(boolean useTimestamp24hr)
    {
        this.useTimestamp24hr = useTimestamp24hr;
    }
    
    public int getTimestampFontSize()
    {
        return timestampFontSize;
    }
    
    public void setTimestampFontSize(int timestampFontSize)
    {
        this.timestampFontSize = timestampFontSize;
    }

    
    public String getTimestampTZ()
    {
        return timestampTZ;
    }

    
    public void setTimestampTZ(String timestampTZ)
    {
        this.timestampTZ = timestampTZ;
    }
    
}