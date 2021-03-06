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
import java.io.InputStream;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wiki.AbstractWikiProvider;
import org.apache.wiki.PageManager;
import org.apache.wiki.WikiPage;
import org.apache.wiki.providers.ProviderException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class UploadWiki extends AbstractWikiProvider implements IUploadWiki {

    private static final Log log = LogFactory.getLog(UploadWiki.class);

    private PageManager pageManager;
    private String errMess;

    public void setPageManager(PageManager pageManager) {
        this.pageManager = pageManager;
    }

    private class Handler extends DefaultHandler {

        private boolean wikitag = true;
        private boolean page = false;
        private boolean ver = false;
        private String pageName;
        private String wikiName;
        private String author;
        private Date date;
        private int pVer;
        private StringBuffer sb;

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {

            if (qName.equals(IWikiTag.WIKI)) {
                wikitag = true;
            }
            if (!wikitag) {
                return;
            }
            if (qName.equals(IWikiTag.PAGE)) {
                page = true;
                pageName = attributes.getValue(IWikiTag.NAME);
                wikiName = attributes.getValue(IWikiTag.WIKI);
            }
            if (!page) {
                return;
            }
            if (qName.equals(IWikiTag.VER)) {
                ver = true;
                author = attributes.getValue(IWikiTag.AUTHOR);
                date = new Date(attributes.getValue(IWikiTag.DATE));
                String s = attributes.getValue(IWikiTag.VER);
                if (s != null) {
                    pVer = Integer.parseInt(s);
                }
            }
            if (!ver) {
                return;
            }
            if (qName.equals(IWikiTag.CONTENT)) {
                sb = new StringBuffer();
            }
        }

        @Override
        public void characters(char ch[], int start, int length)
                throws SAXException {
            if (sb != null) {
                sb.append(ch, start, length);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            if (qName.equals(IWikiTag.VER))
                try {
                    // put page direct
                    WikiPage page = new WikiPage(pageName,true);
                    page.setAuthor(author);
                    page.setVersion(pVer);
                    page.setLastModified(date);
                    log.info("Add page: " + pageName + " " + author + " version=" + pVer); 
                    pageManager.putPageText(page, sb.toString());

                } catch (ProviderException e) {
                    log.fatal(e);
                    errMess = e.getMessage();
                }
        }
    }

    @Override
    public String uploadWiki(InputStream in) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        errMess = null;
        try {
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(in, new Handler());
            return errMess;
        } catch (ParserConfigurationException e) {
            log.fatal(e);
            return e.getMessage();
        } catch (SAXException e) {
            log.fatal(e);
            return e.getMessage();
        } catch (IOException e) {
            log.fatal(e);
            return e.getMessage();
        }

    }

}
