/*
 * Copyright 2012 stanislawbartkowski@gmail.com 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package org.apache.wiki;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wiki.providers.ProviderException;
import org.apache.wiki.providers.WikiPageProvider;
import org.apache.wiki.spring.BeanHolder;

public class RemoveWikiNotWise extends HttpServlet {
    
    private static Log log = LogFactory.getLog(RemoveWikiNotWise.class);

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
    
        log.info("Start removing wiki content");
        WikiPageProvider iProvider = BeanHolder.getWikiPageProvider();
        try {
            iProvider.clearWiki();
        } catch (ProviderException e) {
            log.error("Error while removing wiki content", e);
            throw new ServletException(e.getMessage());
        }
        log.info("Wiki content removed successfully");
//        String uri = req.getRequestURL().toString();
        WikiEngine wiki = WikiEngine.getInstance(getServletConfig());
        String uri = wiki.getBaseURL();
        res.sendRedirect(uri);
    }


}
