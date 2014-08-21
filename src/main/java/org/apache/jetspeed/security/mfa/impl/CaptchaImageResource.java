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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.TimeZone;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.jetspeed.security.mfa.JPEGImgDecoder;
import org.apache.jetspeed.security.mfa.MFA;
import org.apache.jetspeed.security.mfa.MultiFacetedAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: try to find a javax.imageio equivalent and not use Sun classes
 * @author <a href="mailto:taylor@apache.org">David Sean Taylor</a>
 * @version $Id$
 */
public final class CaptchaImageResource
{
    private static final long serialVersionUID = 1L;
    
    static final Logger logger = LoggerFactory.getLogger(CaptchaImageResource.class);

    private String challengeId;
    private  List charAttsList;
    private int height = 0;
    private int width = 0;
    private byte[] background = null;
    private BufferedImage image = null;
    private CaptchaConfiguration config;

    /** Transient image data so that image only needs to be generated once per VM */
    private transient SoftReference imageData;

    /**
     * Construct.
     */
    public CaptchaImageResource(CaptchaConfiguration config)
    {        
        this(config, null);
    }

    /**
     * Construct.
     * 
     * @param challengeId
     *            The id of the challenge
     */
    public CaptchaImageResource(CaptchaConfiguration config, String challengeId)
    {
        if (challengeId == null)
            this.challengeId = randomString(config.getTextMinlength(), config.getTextMaxlength());
        else
            this.challengeId = challengeId;
        this.config = config;
        this.background = null;
    }

    public void setBackgroundImage(byte[] background)
    {
        this.background = background;
    }
    
    /**
     * Gets the id for the challenge.
     * 
     * @return The the id for the challenge
     */
    public final String getChallengeId()
    {
        return challengeId;
    }

    /**
     * Causes the image to be redrawn the next time its requested.
     * 
     * @see wicket.Resource#invalidate()
     */
    public final void invalidate()
    {
        imageData = null;
    }

    /**
     * 
     */
    public void saveTo(OutputStream target) throws IOException
    {
        byte[] data = getImageData();
        target.write(data);
    }

    public byte[] getImageBytes()
    {
        try
        {
            return getImageData();
        }
        catch (IOException e)
        {
            logger.error("Unexpected exception during getImageBytes().", e);
        }
        return null;
    }

    /**
     * @throws IOException
     * @see wicket.markup.html.image.resource.DynamicImageResource#getImageData()
     */
    protected final byte[] getImageData() throws IOException
    {
        // get image data is always called in sync block
        byte[] data = null;
        if (imageData != null)
        {
            data = (byte[]) imageData.get();
        }
        if (data == null)
        {
            data = render();
            imageData = new SoftReference(data);
        }
        return data;
    }

    private Font getFont(String fontName)
    {
        return new Font(fontName, config.getFontStyle(), config.getFontSize());
    }

    public void init() 
    {
        boolean emptyBackground = true;
        if (config.isUseImageBackground() && background != null)
        {
            ByteArrayInputStream is = new ByteArrayInputStream(background);
            JPEGImgDecoder decoder = new DefaultJPEGImgDecoder();
            try
            {
                this.image = decoder.decodeAsBufferedImage(is);
                this.width = image.getWidth();
                this.height = image.getHeight();
                emptyBackground = false;
            }
            catch (Exception e)
            {
                emptyBackground = true;
            }
        }
        if (emptyBackground)
        {
            this.width = config.getTextMarginLeft() * 2;
            this.height = config.getTextMarginBottom() * 6;
        }
        char[] chars = challengeId.toCharArray();
        charAttsList = new ArrayList();
        TextLayout text = null;
        AffineTransform textAt = null;
        String []fontNames = config.getFontNames();
        for (int i = 0; i < chars.length; i++)
        {
            // font name
            String fontName = (fontNames.length == 1) ? fontNames[0] : fontNames[randomInt(0, fontNames.length)];
                        
            // rise
            int rise = config.getTextRiseRange();
            if (rise > 0)
            {
                rise = randomInt(config.getTextMarginBottom(), config.getTextMarginBottom() + config.getTextRiseRange());
            }

            if (config.getTextShear() > 0.0 || config.getTextRotation() > 0)
            {
                // rotation
                double dRotation = 0.0;
                if (config.getTextRotation() > 0)
                {
                    dRotation = Math.toRadians(randomInt(-(config.getTextRotation()), config.getTextRotation()));
                }
                
                // shear
                double shearX = 0.0;
                double shearY = 0.0;
                if (config.getTextShear() > 0.0)
                {
                    Random ran = new Random();
                    shearX = ran.nextDouble() * config.getTextShear();
                    shearY = ran.nextDouble() * config.getTextShear();
                }
                CharAttributes cf = new CharAttributes(chars[i], fontName, dRotation, rise, shearX, shearY);
                charAttsList.add(cf);
                text = new TextLayout(chars[i] + "", getFont(fontName),
                        new FontRenderContext(null, config.isFontAntialiasing(), false));
                textAt = new AffineTransform();
                if (config.getTextRotation() > 0)
                    textAt.rotate(dRotation);
                if (config.getTextShear() > 0.0)
                    textAt.shear(shearX, shearY);                
            }
            else
            {
                CharAttributes cf = new CharAttributes(chars[i], fontName, 0, rise, 0.0, 0.0);
                charAttsList.add(cf);                
            }
            if (emptyBackground)
            {
                Shape shape = text.getOutline(textAt);
//                this.width += text.getBounds().getWidth();
                this.width += (int) shape.getBounds2D().getWidth();
                this.width += config.getTextSpacing() + 1;
                if (this.height < (int) shape.getBounds2D().getHeight() + rise)
                {
                    this.height = (int) shape.getBounds2D().getHeight() + rise;
                }
            }
        }
        if (emptyBackground)
        {
            this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D gfx = (Graphics2D) this.image.getGraphics();
            gfx.setBackground(Color.WHITE);
            gfx.clearRect(0, 0, width, height);            
        }
    }
    
    /**
     * Renders this image
     * 
     * @return The image data
     */
    private final byte[] render() throws IOException
    {       
        Graphics2D gfx = (Graphics2D) this.image.getGraphics();
        if (config.isFontAntialiasing())
            gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int curWidth = config.getTextMarginLeft();
        FontRenderContext ctx = new FontRenderContext(null, config.isFontAntialiasing(), false);        
        for (int i = 0; i < charAttsList.size(); i++)
        {
            CharAttributes cf = (CharAttributes) charAttsList.get(i);
            TextLayout text = new TextLayout(cf.getChar() + "", getFont(cf.getName()), ctx); //gfx.getFontRenderContext());
            AffineTransform textAt = new AffineTransform();
            textAt.translate(curWidth, this.height - cf.getRise());
            if (cf.getRotation() != 0)
            {
                textAt.rotate(cf.getRotation());
            }
            if (cf.getShearX() > 0.0)
                textAt.shear(cf.getShearX(), cf.getShearY());
            Shape shape = text.getOutline(textAt);
            curWidth += shape.getBounds().getWidth() + config.getTextSpacing();
            if (config.isUseImageBackground())
                gfx.setColor(Color.BLACK);
            else
                gfx.setXORMode(Color.BLACK);
            gfx.fill(shape);
        }
        if (config.isEffectsNoise())
        {
            noiseEffects(gfx, image);
        }
        if (config.isUseTimestamp())
        {
            if (config.isEffectsNoise())
                gfx.setColor(Color.WHITE);
            else
                gfx.setColor(Color.BLACK);

            TimeZone tz = TimeZone.getTimeZone(config.getTimestampTZ());
            Calendar cal = new GregorianCalendar(tz);            
            SimpleDateFormat formatter;
            if (config.isUseTimestamp24hr())
                formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z");            
            else
                formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a, z");
            formatter.setTimeZone (tz);
            Font font = gfx.getFont();
            Font newFont = new Font(font.getName(), font.getStyle(), config.getTimestampFontSize());
            gfx.setFont(newFont);
            gfx.drawString(formatter.format(cal.getTime()), config.getTextMarginLeft() * 4, this.height - 1);
        }
        
        return toImageData(image);
    }

    protected void noiseEffects(Graphics2D gfx, BufferedImage image)
    {
        // XOR circle
        int dx = randomInt(width, 2 * width);
        int dy = randomInt(width, 2 * height);
        int x = randomInt(0, width / 2);
        int y = randomInt(0, height / 2);

        gfx.setXORMode(Color.GRAY);
        if (config.isFontSizeRandom())
            gfx.setStroke(new BasicStroke(randomInt(config.getFontSize() / 8, config.getFontSize() / 2)));
        else
            gfx.setStroke(new BasicStroke(config.getFontSize()));
            
        gfx.drawOval(x, y, dx, dy);

        WritableRaster rstr = image.getRaster();
        int[] vColor = new int[3];
        int[] oldColor = new int[3];
        Random vRandom = new Random(System.currentTimeMillis());
        // noise
        for (x = 0; x < width; x++)
        {
            for (y = 0; y < height; y++)
            {
                rstr.getPixel(x, y, oldColor);

                // hard noise
                vColor[0] = 0 + (int) (Math.floor(vRandom.nextFloat() * 1.03) * 255);
                // soft noise
                vColor[0] = vColor[0]
                        ^ (170 + (int) (vRandom.nextFloat() * 80));
                // xor to image
                vColor[0] = vColor[0] ^ oldColor[0];
                vColor[1] = vColor[0];
                vColor[2] = vColor[0];

                rstr.setPixel(x, y, vColor);
            }
        }
    }

    /**
     * @param image
     *            The image to turn into data
     * @return The image data for this dynamic image
     */
    protected byte[] toImageData(final BufferedImage image) throws IOException
    {
        // Create output stream
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        String format = config.getImageFormat().substring(1);
        // Get image writer for format
        // FIXME: config.getImageFormat()
        final ImageWriter writer = (ImageWriter) ImageIO
                .getImageWritersByFormatName(format).next();

        // Write out image
        writer.setOutput(ImageIO.createImageOutputStream(out));
        writer.write(image);

        // Return the image data
        return out.toByteArray();
    }

    /**
     * This class is used to encapsulate all the filters that a character will
     * get when rendered. The changes are kept so that the size of the shapes
     * can be properly recorded and reproduced later, since it dynamically
     * generates the size of the captcha image. The reason I did it this way is
     * because none of the JFC graphics classes are serializable, so they cannot
     * be instance variables here. If anyone knows a better way to do this,
     * please let me know.
     */
    private static final class CharAttributes implements Serializable
    {

        private static final long serialVersionUID = 1L;

        private char c;

        private String name;

        private int rise;

        private double rotation;

        private double shearX;

        private double shearY;

        CharAttributes(char c, String name, double rotation, int rise,
                double shearX, double shearY)
        {
            this.c = c;
            this.name = name;
            this.rotation = rotation;
            this.rise = rise;
            this.shearX = shearX;
            this.shearY = shearY;
        }

        char getChar()
        {
            return c;
        }

        String getName()
        {
            return name;
        }

        int getRise()
        {
            return rise;
        }

        double getRotation()
        {
            return rotation;
        }

        double getShearX()
        {
            return shearX;
        }

        double getShearY()
        {
            return shearY;
        }
    }

    private static int randomInt(int min, int max)
    {
        return (int) (Math.random() * (max - min) + min);
    }

    public static String randomString(int min, int max)
    {
        int num = randomInt(min, max);
        byte b[] = new byte[num];
        for (int i = 0; i < num; i++)
            b[i] = (byte) randomInt('a', 'z');
        return new String(b);
    }

    private static String randomWord()
    {
        final String words[] =
        { "Albert", "Barber", "Charlie", "Daniel", "Edward", "Flower",
                "Georgia", "Lawrence", "Michael", "Piper", "Stanley"};

        return words[randomInt(0, words.length)];
    }
 
    public static void main(String args[])
    {
        String configLocation = "./WebContent/WEB-INF/mfa.properties";
        String ttsLocation = "./WebContent/WEB-INF/tts.properties";
        
        PropertiesConfiguration config = new PropertiesConfiguration();
        PropertiesConfiguration tconfig = new PropertiesConfiguration();
        Properties x = new Properties();
        try
        {
            InputStream is = new FileInputStream(configLocation);
            config.load(is);
            is.close();
            InputStream tis = new FileInputStream(ttsLocation);
            tconfig.load(tis);
            tis.close();            
            MultiFacetedAuthentication mfa = new MultiFacetedAuthenticationImpl(config, tconfig);
            MFA.setInstance(mfa);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        
        CaptchaConfiguration captchaConfig = new CaptchaConfiguration(config);
        CaptchaImageResource captcha = new CaptchaImageResource(captchaConfig);
        
        InputStream is = null;
        try
        {
            is = new FileInputStream("./WebContent/images/jetspeedlogo98.jpg");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            MultiFacetedAuthenticationImpl.drain(is, bytes);
            byte[] background = bytes.toByteArray();
            captcha.setBackgroundImage(background);            
        }
        catch (IOException e)
        {
            captchaConfig.setUseImageBackground(false);
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
        
        captcha.init();
        FileOutputStream fs = null;
        try
        {
            fs = new FileOutputStream("/data/result.jpg");
            byte[] data = captcha.getImageBytes();
            fs.write(data);
        }
        catch (IOException e)
        {
            logger.error("Unexpected exception during writing captcha image.", e);
        } 
        finally
        {
            try
            {
                if (fs != null) fs.close();
            } catch (IOException e)
            {
            }
        }

    }
    
}
