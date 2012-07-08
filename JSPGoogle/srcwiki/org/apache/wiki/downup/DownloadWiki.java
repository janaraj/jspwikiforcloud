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

import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wiki.AbstractWikiProvider;
import org.apache.wiki.PageManager;
import org.apache.wiki.WikiPage;
import org.apache.wiki.providers.ProviderException;

@SuppressWarnings("serial")
public class DownloadWiki extends AbstractWikiProvider implements IDownloadWiki {

    private static final Log log = LogFactory.getLog(DownloadWiki.class);

    private PageManager pageManager;

    public void setPageManager(PageManager pageManager) {
        this.pageManager = pageManager;
    }

    private void addAttr(StringBuffer sb, String tagName, String... attr) {
        sb.append("<" + tagName);
        for (int i = 0; i < attr.length; i++) {
            String attrName = attr[i++];
            String attrVal = attr[i];
            sb.append(" " + attrName + "=\"" + attrVal + "\"");
        }
        sb.append('>');
    }

    private void addTag(StringBuffer sb, String tag, boolean beg) {
        sb.append((beg ? "<" : "</") + tag + ">");
    }

    @Override
    public Reader provideReader() {
        try {
            Collection<WikiPage> pList = pageManager.getAllPages();
            StringBuffer sb = new StringBuffer();
            addTag(sb, IWikiTag.WIKI, true);
            for (WikiPage page : pList) {
                sb.append('\n');
                addAttr(sb, IWikiTag.PAGE, IWikiTag.NAME, page.getName(), IWikiTag.WIKI,
                        page.getWiki());
                List<WikiPage> verList = pageManager.getVersionHistory(page
                        .getName());
                for (WikiPage ver : verList) {
                    sb.append('\n');
                    addAttr(sb, IWikiTag.VER, IWikiTag.AUTHOR, ver.getAuthor(), IWikiTag.VER, ""
                            + ver.getVersion(), IWikiTag.DATE, ver.getLastModified()
                            .toString());
                    Map<String, String> prop = ver.getAttributes();
                    Set<Map.Entry<String, String>> mSet = prop.entrySet();
                    for (Map.Entry<String, String> e : mSet) {
                        String key = e.getKey();
                        String val = e.getValue();
                        addAttr(sb, "property", "key", key, "value", val);
                        sb.append("</property>");
                    }
                    String content = pageManager.getPageText(ver.getName(),
                            ver.getVersion());
                    addTag(sb,IWikiTag.CONTENT,true);
                    sb.append("<![CDATA[");
                    sb.append(content);
                    sb.append("]]>");
                    addTag(sb,IWikiTag.CONTENT,false);
                    addTag(sb,IWikiTag.VER,false);
                    sb.append('\n');
                }
                sb.append("</page>");
                sb.append('\n');
            }
            addTag(sb, IWikiTag.WIKI, false);
            return new StringReader(sb.toString());
        } catch (ProviderException e) {
            log.error(e);
            return null;
        }
    }

}
