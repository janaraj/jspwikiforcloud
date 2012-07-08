/*
    JSPWiki - a JSP-based WikiWiki clone.

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.    
 */
package org.apache.wiki;

import java.io.IOException;
import java.util.Collection;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wiki.filters.FilterException;
import org.apache.wiki.filters.FilterManager;
import org.apache.wiki.parser.MarkupParser;
import org.apache.wiki.parser.WikiDocument;
import org.apache.wiki.render.RenderingManager;
import org.apache.wiki.spring.BeanHolder;

public class TextToHtml {

    private static final Log log = LogFactory.getLog(TextToHtml.class);

    public static String textToHTML(WikiContext context, String pagedata) {
        VariableManager m_variableManager = BeanHolder.getVariableManager();
        RenderingManager rManager = BeanHolder.getRenderingManager();
        FilterManager fManager = BeanHolder.getFilterManager();
        String result = "";

        boolean runFilters = "true".equals(m_variableManager.getValue(context,
                WikiEngine.PROP_RUNFILTERS, "true"));

        StopWatch sw = new StopWatch();
        sw.start();
        try {
            if (runFilters)
                pagedata = fManager.doPreTranslateFiltering(context, pagedata);

            result = rManager.getHTML(context, pagedata);

            if (runFilters)
                result = fManager.doPostTranslateFiltering(context, result);
        } catch (FilterException e) {
            // FIXME: Don't yet know what to do
        }
        sw.stop();
        if (log.isDebugEnabled())
            log.debug("Page " + context.getRealPage().getName()
                    + " rendered, took " + sw);

        return result;
    }

    /**
     * Helper method for doing the HTML translation.
     * 
     * @param context
     *            The WikiContext in which to do the conversion
     * @param pagedata
     *            The data to render
     * @param localLinkHook
     *            Is called whenever a wiki link is found
     * @param extLinkHook
     *            Is called whenever an external link is found
     * @param parseAccessRules
     *            Parse the access rules if we encounter them
     * @param justParse
     *            Just parses the pagedata, does not actually render. In this
     *            case, this methods an empty string.
     * @return HTML-rendered page text.
     */
    // important: FilterManager as a parameter
    public static String textToHTML(WikiContext context,
            FilterManager fManager, String pagedata,
            StringTransmutator localLinkHook, StringTransmutator extLinkHook,
            StringTransmutator attLinkHook, boolean parseAccessRules,
            boolean justParse) {
        RenderingManager rManager = BeanHolder.getRenderingManager();
        return textToHTML(context, fManager, rManager, pagedata, localLinkHook,
                extLinkHook, attLinkHook, parseAccessRules, justParse);
    }

    public static String textToHTML(WikiContext context,
            FilterManager fManager, RenderingManager rManager, String pagedata,
            StringTransmutator localLinkHook, StringTransmutator extLinkHook,
            StringTransmutator attLinkHook, boolean parseAccessRules,
            boolean justParse) {
        String result = "";
        VariableManager m_variableManager = BeanHolder.getVariableManager();

        if (pagedata == null) {
            log.error("NULL pagedata to textToHTML()");
            return null;
        }

        boolean runFilters = "true".equals(m_variableManager.getValue(context,
                WikiEngine.PROP_RUNFILTERS, "true"));

        try {
            StopWatch sw = new StopWatch();
            sw.start();

            if (runFilters)
                pagedata = fManager.doPreTranslateFiltering(context, pagedata);

            MarkupParser mp = rManager.getParser(context, pagedata);
            mp.addLocalLinkHook(localLinkHook);
            mp.addExternalLinkHook(extLinkHook);
            mp.addAttachmentLinkHook(attLinkHook);

            if (!parseAccessRules)
                mp.disableAccessRules();

            WikiDocument doc = mp.parse();

            //
            // In some cases it's better just to parse, not to render
            //
            if (!justParse) {
                result = rManager.getHTML(context, doc);

                if (runFilters) {
                    result = fManager.doPostTranslateFiltering(context, result);
                }
            }

            sw.stop();

            if (log.isDebugEnabled())
                log.debug("Page " + context.getRealPage().getName()
                        + " rendered, took " + sw);
        } catch (IOException e) {
            log.error("Failed to scan page data: ", e);
        } catch (FilterException e) {
            // FIXME: Don't yet know what to do
        }

        return result;
    }

    // important: FilterManager as a parameter
    public static Collection scanWikiLinks(WikiEngine engine,
            FilterManager fManager, RenderingManager rManager, WikiPage page,
            String pagedata) {
        LinkCollector localCollector = new LinkCollector();

        textToHTML(new WikiContext(engine, page), fManager, rManager, pagedata,
                localCollector, null, localCollector, false, true);

        return localCollector.getLinks();
    }

}
