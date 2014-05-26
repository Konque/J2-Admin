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
package org.apache.jetspeed.portlets.content;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.portlet.PortletURL;

import org.apache.portals.applications.webcontent.rewriter.MutableAttributes;
import org.apache.portals.applications.webcontent.rewriter.WebContentRewriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebContentRewriter that overlays page navigation onto portal content paths.
 * 
 * @author <a href="mailto:rwatler@apache.org">Randy Watler</a>
 * @version $Id$
 */
public class DynamicWebContentRewriter extends WebContentRewriter
{
    private final static Logger log = LoggerFactory.getLogger(DynamicWebContentRewriter.class);

    private final static Pattern ONCLICK_LOCATION_PATTERN = Pattern.compile("[.]location *= *'([^']*)'");
    private final static Pattern STYLE_URL_PATTERN = Pattern.compile("url\\( *\"([^\"]*)\" *\\)");
    
    protected String basePortalPath;
    
    public String getBasePortalPath()
    {
        return basePortalPath;
    }

    public void setBasePortalPath(String basePortalPath)
    {
        this.basePortalPath = basePortalPath;
        if (log.isDebugEnabled())
        {
            log.debug("basePortalPath: "+basePortalPath);
        }
    }
        
    /* (non-Javadoc)
     * @see org.apache.portals.applications.webcontent.rewriter.AbstractRewriter#setBaseUrl(java.lang.String)
     */
    public void setBaseUrl(String base)
    {
        super.setBaseUrl(base);
        if (log.isDebugEnabled())
        {
            log.debug("baseUrl: "+base);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.portals.applications.webcontent.rewriter.WebContentRewriter#setActionURL(javax.portlet.PortletURL)
     */
    public void setActionURL(PortletURL action)
    {
        super.setActionURL(action);
        if (log.isDebugEnabled())
        {
            log.debug("actionURL: "+action);
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.portals.applications.webcontent.rewriter.WebContentRewriter#rewriteUrl(java.lang.String, java.lang.String, java.lang.String, org.apache.portals.applications.webcontent.rewriter.MutableAttributes)
     */
    public String rewriteUrl(String url, String tag, String attribute, MutableAttributes otherAttributes)
    {
        String rewrittenUrl = url;
        if (tag.equalsIgnoreCase("A") && attribute.equalsIgnoreCase("href"))
        {
            // redirect navigation through portal content urls
            rewrittenUrl = portalURL(url);
        }
        else if (tag.equalsIgnoreCase("FORM") && attribute.equalsIgnoreCase("action"))                
        {
            // redirect forms through portal as action
            getActionURL().setParameter(ACTION_PARAMETER_URL, webContentURL(url));
            String httpMethod = otherAttributes.getValue("method");
            if (httpMethod != null)
            {
                getActionURL().setParameter(ACTION_PARAMETER_METHOD, httpMethod);
            }
            rewrittenUrl = getActionURL().toString();
        }
        else if (attribute.equalsIgnoreCase("onclick"))                
        {
            // redirect javascript click navigation through portal content urls
            String javascript = url;
            StringBuilder rewrittenJavascript = new StringBuilder();
            int rewrittenIndex = 0;
            Matcher matcher = ONCLICK_LOCATION_PATTERN.matcher(javascript);
            while (matcher.find())
            {
                rewrittenJavascript.append(javascript.substring(rewrittenIndex,matcher.start(1)));
                rewrittenJavascript.append(portalURL(matcher.group(1)));
                rewrittenIndex = matcher.end(1);
            }
            if (rewrittenIndex > 0)
            {
                rewrittenJavascript.append(javascript.substring(rewrittenIndex));
                rewrittenUrl = rewrittenJavascript.toString();
            }
        }
        else
        {
            // access assets directly externally from portal 
            rewrittenUrl = webContentURL(url);            
        }

        if (log.isDebugEnabled())
        {
            log.debug("rewriteUrl: "+url+" -> "+rewrittenUrl);
        }
        return rewrittenUrl;
    }
    
    /* (non-Javadoc)
     * @see org.apache.portals.applications.webcontent.rewriter.BasicRewriter#rewriteText(java.lang.String, java.lang.String)
     */
    public String rewriteText(String tag, String text)
    {
        String rewrittenText = null;
        if (tag.equalsIgnoreCase("STYLE"))
        {
            StringBuilder rewrittenStyle = new StringBuilder();
            int rewrittenIndex = 0;
            Matcher matcher = STYLE_URL_PATTERN.matcher(text);
            while (matcher.find())
            {
                rewrittenStyle.append(text.substring(rewrittenIndex,matcher.start(1)));
                rewrittenStyle.append(webContentURL(matcher.group(1)));
                rewrittenIndex = matcher.end(1);
            }
            if (rewrittenIndex > 0)
            {
                rewrittenStyle.append(text.substring(rewrittenIndex));
                rewrittenText = rewrittenStyle.toString();
            }
        }
        
        if (rewrittenText != null)
        {
            if (log.isDebugEnabled())
            {
                String logText = text.replace('\n', ' ').replace('\r', ' ');
                String logRewrittenText = rewrittenText.replace('\n', ' ').replace('\r', ' ');
                log.debug("rewriteText: "+logText+" -> "+logRewrittenText);
            }
        }
        return rewrittenText;
    }

    protected String webContentURL(String url)
    {
        // form absolute web content URL
        if (!url.startsWith("http://") && !url.startsWith("https://"))
        {
            if (url.startsWith("/"))
            {
                // get site root base url
                String baseRootUrl = baseRootURL(getBaseUrl());
                // append site relative url to base url
                url = baseRootUrl+url.substring(1);
            }
            else
            {
                // strip "./" prefix from url
                while (url.startsWith("./"))
                {
                    url = url.substring(2);
                }
                // get base url 
                String baseUrl = baseURL(getBaseUrl(), false);
                // strip "../" prefix from url
                while (url.startsWith("../"))
                {
                    url = url.substring(3);
                    baseUrl = baseURL(baseUrl, true);
                }
                // append relative url to base url
                url = baseUrl+url;
            }
        }
        return url;
    }

    protected String portalURL(String url)
    {
        // derive content relative url if necessary
        if (url.startsWith("http://") || url.startsWith("https://"))
        {
            // get base url 
            String baseUrl = baseURL(getBaseUrl(), false);
            // strip base url to create relative url
            if (url.startsWith(baseUrl))
            {
                return url.substring(baseUrl.length());
            }
            // compute relative url from base if possible
            String baseRootUrl = baseRootURL(getBaseUrl());
            if (url.startsWith(baseRootUrl))
            {
                url = url.substring(baseRootUrl.length());
                String remainingBasePath = baseUrl.substring(baseRootUrl.length());
                int remainingBasePathIndex = remainingBasePath.indexOf('/');
                while (remainingBasePathIndex != -1)
                {
                    url = "../"+url;
                    remainingBasePathIndex = remainingBasePath.indexOf('/', remainingBasePathIndex+1);
                }
            }
        }
        else if (url.startsWith("/"))
        {
            // get base url 
            String baseUrl = baseURL(getBaseUrl(), false);
            // compute relative url from root relative url
            String baseRootUrl = baseRootURL(getBaseUrl());
            url = url.substring(1);
            String remainingBasePath = baseUrl.substring(baseRootUrl.length());
            int remainingBasePathIndex = remainingBasePath.indexOf('/');
            while (remainingBasePathIndex != -1)
            {
                url = "../"+url;
                remainingBasePathIndex = remainingBasePath.indexOf('/', remainingBasePathIndex+1);
            }
        }
        else
        {
            // strip "./" prefix from url
            while (url.startsWith("./"))
            {
                url = url.substring(2);
            }            
        }
        // make portal site relative url from content relative urls
        if (!url.startsWith("http://") && !url.startsWith("https://"))
        {
            String basePortalPath = getBasePortalPath();
            if (!basePortalPath.endsWith("/"))
            {
                basePortalPath += "/";
            }
            url = basePortalPath+url;
        }
        return url;
    }
    
    protected static String baseURL(String baseUrl, boolean force)
    {
        boolean folderUrl = baseUrl.endsWith("/");
        if (!folderUrl || force)
        {
            int baseRootUrlIndex = baseUrl.indexOf('/');
            baseRootUrlIndex = baseUrl.indexOf('/', baseRootUrlIndex+1);
            baseRootUrlIndex = baseUrl.indexOf('/', baseRootUrlIndex+1);
            int baseUrlIndex = (folderUrl ? baseUrl.lastIndexOf('/', baseUrl.length()-2) : baseUrl.lastIndexOf('/'));
            if ((baseRootUrlIndex != -1) || (baseUrlIndex > baseUrlIndex))
            {
                baseUrl = baseUrl.substring(0, baseUrlIndex+1);
            }
            else if (!folderUrl)
            {
                baseUrl += "/";
            }
        }
        return baseUrl;
    }

    protected static String baseRootURL(String baseUrl)
    {
        int baseRootUrlIndex = baseUrl.indexOf('/');
        baseRootUrlIndex = baseUrl.indexOf('/', baseRootUrlIndex+1);
        baseRootUrlIndex = baseUrl.indexOf('/', baseRootUrlIndex+1);
        return ((baseRootUrlIndex != -1) ? baseUrl : baseUrl.substring(0, baseRootUrlIndex))+"/";
    }
}
