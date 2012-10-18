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
package org.apache.wiki.downup;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.spring.BeanHolder;

public class DownloadWikiServlet extends HttpServlet {

    private static Log log = LogFactory.getLog(DownloadWikiServlet.class);

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        String nextPage = req.getParameter("nextpage");
        // necessary to have wikiengine populated with properties
        WikiEngine wiki = WikiEngine.getInstance(this);
        res.setContentType("text/xml; charset=UTF-8");
        res.setHeader("Content-Disposition", "attachment;filename=Wiki.xml");
        IDownloadWiki i = BeanHolder.getDownloadWiki();
        Reader in = i.provideReader();
        PrintWriter out = res.getWriter();

        char[] outputByte = new char[4096];
        int bread;
        log.debug("Start sending content");
        // copy text content to output stream
        while ((bread = in.read(outputByte, 0, 4096)) != -1) {
            out.write(outputByte, 0, bread);
        }
        in.close();
        log.debug("Finish sending content");
        if (nextPage != null) {
            res.sendRedirect(nextPage);
        }

    }

}
