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
package org.apache.wiki.providers;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wiki.AbstractWikiProvider;
import org.apache.wiki.QueryItem;
import org.apache.wiki.SearchMatcher;
import org.apache.wiki.SearchResult;
import org.apache.wiki.SearchResultComparator;
import org.apache.wiki.WikiPage;
import org.apache.wiki.auth.WikiSecurityException;
import org.apache.wiki.providers.jpa.AttachmentEnt;
import org.apache.wiki.providers.jpa.AttachmentOneEnt;
import org.apache.wiki.providers.jpa.WikiObject;
import org.apache.wiki.providers.jpa.WikiOnePage;
import org.apache.wiki.providers.jpa.WikiPageBlob;
import org.apache.wiki.util.Serializer;

public class WikiPageBlobProvider extends AbstractWikiProvider implements
        WikiPageProvider {

    private static final Log log = LogFactory
            .getLog(WikiPageBlobProvider.class);

    // TODO: should be taken as a configuration parameter
    /** Time out to recognize new version of the page or continuation. */
    private static final int getContinuationEditTimeout = 15 * 1000;


    private Collection<PageUtil.WikiVersion> toVersions(WikiPageBlob u)
            throws WikiSecurityException {
        return BlobUtil.toObjects(u);
    }

    private abstract class PageCommand extends ECommand {

        protected Collection<WikiPageBlob> bList;
        protected final WikiPage page;
        protected String pageName;
        protected final int version;
        protected WikiPageBlob w;
        protected PageUtil.WikiVersion v;
        protected Collection<PageUtil.WikiVersion> vList;
        protected WikiOnePage pa;

        PageCommand(boolean trans, WikiPage page) {
            super(trans);
            this.page = page;
            this.pageName = page.getName();
            this.version = page.getVersion();
        }

        protected void setW(WikiPageBlob w) {
            this.w = w;
            this.pageName = w.getPageName();
        }

        PageCommand(boolean trans, String pageName) {
            super(trans);
            this.page = null;
            this.version = WikiPageProvider.LATEST_VERSION;
            this.pageName = pageName;
        }

        PageCommand(boolean trans) {
            super(trans);
            this.page = null;
            this.pageName = null;
            this.version = WikiPageProvider.LATEST_VERSION;
        }

        PageCommand(boolean trans, String pageName, int version) {
            super(trans);
            this.page = null;
            this.version = version;
            this.pageName = pageName;
        }

        protected WikiPage toWikiPage() throws WikiSecurityException {
            WikiPage ww = new WikiPage(pageName);
            PageUtil.toWikiPage(ww, v);
            return ww;
        }

        protected void getAllPages(EntityManager eF) {
            Query q = ECommand.getQuery(eF, "FindListOfPages");
            bList = q.getResultList();
        }

        protected void getPage(EntityManager eF) throws WikiSecurityException {
            w = ECommand.getSingleObject(eF, "FindPage", pageName);
            if (w == null) {
                vList = new ArrayList<PageUtil.WikiVersion>();
            } else {
                vList = toVersions(w);
            }
        }

        protected void findVersion() {
            v = PageUtil.findVersion(vList, version);
        }

        protected void findLatest() {
            v = PageUtil.findLatest(vList);
        }

        protected void savePage(EntityManager eF) throws WikiSecurityException {
            if (w == null) {
                w = new WikiPageBlob();
                w.setPageName(pageName);
            }
            PageUtil.toBlobPage(w, vList);
            eF.persist(w);
        }

        protected void getContent(EntityManager eF) {
            long l = w.getKey().getId();
            pa = ECommand.getSingleObject(eF, "FindPageContent", l, v.version);
        }

        protected void saveContent(EntityManager eF, String content) {
            if (pa == null) {
                pa = new WikiOnePage();
                pa.setPageKey(w.getKey().getId());
                pa.setVersion(v.version);
            }
            pa.setContent(content);
            eF.persist(pa);
        }
    }

    private class PutPageDirectCommand extends PageCommand {

        private final String content;

        PutPageDirectCommand(WikiPage page, String text) {
            super(true, page);
            this.content = text;
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            getPage(eF);
            findVersion();
            if (v == null) {
                v = new PageUtil.WikiVersion();
                vList.add(v);
            }
            PageUtil.toVersion(v, page);
            v.version = page.getVersion();
            savePage(eF);
            commit();
            getContent(eF);
            saveContent(eF, content);
        }

    }

    private class PutPageCommand extends PageCommand {

        private final String content;

        PutPageCommand(WikiPage page, String text) {
            super(true, page);
            this.content = text;
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            getPage(eF);
            findLatest();

            String previousAuthor = "";
            Date previousModified = new Date(0l);
            if (v != null) {
                previousModified = v.changetime;
                previousAuthor = v.changeBy;
            }
            /**
             * If same author and saved again within continuationEditTimeout,
             * save by directly overwriting current version. Create version, if
             * not in continuationEdit, or if migrating, or if page is
             * non-existent
             */
            boolean isDifferentAuthor = page.getAuthor() == null
                    || page.getAuthor().equals("")
                    || !page.getAuthor().equals(previousAuthor);
            boolean isContinuationEditTimeExpired = System.currentTimeMillis() > getContinuationEditTimeout
                    + (previousModified != null ? previousModified.getTime()
                            : 0);

            boolean createVersion = !pageExists(page.getName())
                    || isDifferentAuthor || isContinuationEditTimeExpired;

            if (createVersion) {
                int version = 0;
                if (v != null) {
                    version = v.version;
                }
                // Insert page
                v = new PageUtil.WikiVersion();
                v.version = version;
                v.version++;
                vList.add(v);
                PageUtil.toVersion(v, page);
            }
            v.changetime = getToday();
            savePage(eF);
            commit();
            getContent(eF);
            saveContent(eF, content);
        }

    }

    @Override
    public void putPageText(WikiPage page, String text)
            throws ProviderException {
        ECommand e = null;
        if (page.isPutdirect()) {
            e = new PutPageDirectCommand(page, text);
        } else {
            e = new PutPageCommand(page, text);
        }
        e.runCommand();
    }

    private class PageExists extends PageCommand {

        private boolean exists;

        PageExists(String page) {
            super(false, page);
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            getPage(eF);
            exists = (w != null);
        }

    }

    @Override
    public boolean pageExists(String page) {
        PageExists pa = new PageExists(page);
        try {
            pa.runCommand();
        } catch (ProviderException e) {
            log.error(page + " pageExists", e);
            return false;
        }
        return pa.exists;
    }

    private class FindPages extends PageCommand {
        private final QueryItem[] query;

        Collection res;

        FindPages(QueryItem[] query) {
            super(false);
            this.query = query;
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            res = new TreeSet(new SearchResultComparator());
            SearchMatcher matcher = new SearchMatcher(query);
            getAllPages(eF);
            for (WikiPageBlob ww : bList) {
                setW(ww);
                vList = toVersions(w);
                findLatest();
                getContent(eF);
                try {
                    SearchResult comparison = matcher.matchPageContent(
                            w.getPageName(), pa.getContent());
                    if (comparison != null) {
                        res.add(comparison);
                    }
                } catch (IOException e) {
                    log.fatal(e);
                    throw new WikiSecurityException(e.getMessage());
                }
            }
        }
    }

    @Override
    public Collection findPages(QueryItem[] query) {
        FindPages pa = new FindPages(query);
        try {
            pa.runCommand();
        } catch (ProviderException e) {
            log.error("findPages", e);
            return null;
        }
        return pa.res;
    }

    private class FindPageInfo extends PageCommand {

        WikiPage pa = null;

        FindPageInfo(String page, int version) {
            super(false, page, version);
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            getPage(eF);
            findVersion();
            if (v == null) {
                return;
            }
            pa = toWikiPage();
        }

    }

    @Override
    public WikiPage getPageInfo(String page, int version)
            throws ProviderException {
        FindPageInfo pa = new FindPageInfo(page, version);
        pa.runCommand();
        return pa.pa;
    }

    private class FindAllPages extends PageCommand {

        Collection<WikiPage> col = new ArrayList<WikiPage>();

        FindAllPages() {
            super(false);
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            getAllPages(eF);
            for (WikiPageBlob ww : bList) {
                setW(ww);
                vList = toVersions(w);
                findLatest();
                col.add(toWikiPage());
            }
        }

    }

    @Override
    public Collection<WikiPage> getAllPages() throws ProviderException {
        FindAllPages pa = new FindAllPages();
        pa.runCommand();
        return pa.col;
    }

    @Override
    public Collection<WikiPage> getAllChangedSince(Date date) {
        // TODO: correct
        return new ArrayList<WikiPage>();
    }

    private class CountAllPages extends PageCommand {

        int co;

        CountAllPages() {
            super(false);
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            getAllPages(eF);
            co = bList.size();
        }

    }

    @Override
    public int getPageCount() throws ProviderException {
        CountAllPages pa = new CountAllPages();
        pa.runCommand();
        return pa.co;
    }

    private class VersionHistory extends PageCommand {

        List<WikiPage> col = new ArrayList<WikiPage>();

        VersionHistory(String page) {
            super(false, page);
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            getPage(eF);
            for (PageUtil.WikiVersion vv : vList) {
                v = vv;
                col.add(toWikiPage());
            }
        }

    }

    @Override
    public List<WikiPage> getVersionHistory(String page)
            throws ProviderException {
        VersionHistory pa = new VersionHistory(page);
        pa.runCommand();
        return pa.col;
    }

    private class PageText extends PageCommand {

        String content;

        PageText(String page, int version) {
            super(false, page, version);
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            getPage(eF);
            findVersion();
            if (v == null) {
                content = null;
                return;
            }
            getContent(eF);
            content = pa.getContent();
        }

    }

    @Override
    public String getPageText(String page, int version)
            throws ProviderException {
        PageText pa = new PageText(page, version);
        pa.runCommand();
        return pa.content;
    }

    private class RemoveVersion extends PageCommand {

        RemoveVersion(String page, int version) {
            super(true, page, version);
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            getPage(eF);
            findVersion();
            getContent(eF);
            vList.remove(v);
            if (vList.isEmpty()) {
                eF.remove(w);
            } else {
                savePage(eF);
            }
            getContent(eF);
            eF.remove(pa);
        }

    }

    @Override
    public void deleteVersion(String pageName, int version)
            throws ProviderException {
        RemoveVersion ve = new RemoveVersion(pageName, version);
        ve.runCommand();
    }

    private class DeletePage extends PageCommand {

        DeletePage(String pageName) {
            super(true, pageName);
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            getPage(eF);
            for (PageUtil.WikiVersion vv : vList) {
                v = vv;
                getContent(eF);
                eF.remove(pa);
            }
            eF.remove(w);
        }

    }

    @Override
    public void deletePage(String pageName) throws ProviderException {
        DeletePage pa = new DeletePage(pageName);
        pa.runCommand();
    }

    private class MovePage extends PageCommand {

        private final String newPage;

        MovePage(String pageName, String newPage) {
            super(true, pageName);
            this.newPage = newPage;
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            getPage(eF);
            w.setPageName(newPage);
            eF.persist(w);
        }
    }

    @Override
    public void movePage(String from, String to) throws ProviderException {

        MovePage pa = new MovePage(from, to);
        pa.runCommand();
    }

    private class RemoveWikiContent extends ECommand {

        RemoveWikiContent() {
            super(true);
        }

        private void removeEntities(EntityManager eF, Class cl) {
            String query = "SELECT P FROM " + cl.getName() + " P";
            Query q = eF.createQuery(query);
            while (true) {
                List li = q.getResultList();
                if (li.isEmpty()) {
                    break;
                }
                Object o = li.get(0);
                eF.remove(o);
                commit();
            }
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            removeEntities(eF, AttachmentEnt.class);
            removeEntities(eF, AttachmentOneEnt.class);
            removeEntities(eF,WikiObject.class);
            removeEntities(eF,WikiOnePage.class);
            removeEntities(eF,WikiPageBlob.class);            
        }

    }

    @Override
    public void clearWiki() throws ProviderException {
        RemoveWikiContent co = new RemoveWikiContent();
        co.runCommand();
    }

}
