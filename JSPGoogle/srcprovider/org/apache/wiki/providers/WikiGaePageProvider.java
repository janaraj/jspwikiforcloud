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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.apache.wiki.NoRequiredPropertyException;
import org.apache.wiki.QueryItem;
import org.apache.wiki.SearchMatcher;
import org.apache.wiki.SearchResult;
import org.apache.wiki.SearchResultComparator;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiPage;
import org.apache.wiki.providers.jpa.WikiPageEnt;

/**
 * Implementation of WikiPageProvider for Google App Engine Java JPA
 * <p>
 * The implementation is based on JDBC Page Provider
 * </p>
 * 
 * @author stanislawbartkowski@gmail.com
 * 
 */
@SuppressWarnings("serial")
public class WikiGaePageProvider implements WikiPageProvider {

	private WikiEngine m_engine;

	private final Logger log = Logger.getLogger(WikiGaePageProvider.class);

	// TODO: should be taken as a configuration parameter
	/** Time out to recognize new version of the page or continuation. */
	private final int getContinuationEditTimeout = 15 * 1000;

	private class RetResult {
		WikiPageEnt ent = null;
		WikiPage page = null;

		/**
		 * Converts WikiPage entity to Page class
		 * <p>
		 * Source: ent
		 * </p>
		 * <p>
		 * Destination: page
		 * </p>
		 */
		void toPage() {
			page = new WikiPage(m_engine, ent.getName());
			page.setVersion(ent.getVersion());
			page.setLastModified(ent.getChangetime());
			page.setAuthor(ent.getChangeBy());
			if (ent.getChangeNote() != null) {
				page.setAttribute(WikiPage.CHANGENOTE, ent.getChangeNote());
			}
		}
	}

	@Override
	public void initialize(WikiEngine engine, Properties properties)
			throws NoRequiredPropertyException, IOException {
		m_engine = engine;
	}

	private void debug(String message) {
		log.debug(message);
	}

	private void error(String message, Throwable t) {
		log.error(message, t);
	}

	@Override
	public String getProviderInfo() {
		return "Google App Engine provider";
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<WikiPageEnt> queryNamed(EntityManager eF, String namedQuery,
			String... Params) {
		debug("Named query : " + namedQuery);
		Query query = ECommand.getQuery(eF, namedQuery, Params);
		List results = query.getResultList();
		debug("Numer of rows: " + results.size());
		return results;
	}

	private void nextW(RetResult ret, WikiPageEnt w) {
		if (ret.ent == null) {
			ret.ent = w;
		} else {
			if (ret.ent.getVersion() < w.getVersion()) {
				ret.ent = w;
			}
		}
	}

	private RetResult getCurrentPageInfo(EntityManager eF, String pageName) {
		List<WikiPageEnt> pList = queryNamed(eF, "GetListOfPages", pageName);
		RetResult ret = new RetResult();
		for (WikiPageEnt w : pList) {
			nextW(ret, w);
		}
		if (ret.ent != null) {
			ret.toPage();
		}
		return ret;
	}

	private class EPutPageText extends ECommand {

		private final WikiPage page;
		private final String text;

		EPutPageText(WikiPage page, String text) {
			super(true);
			this.page = page;
			this.text = text;
		}

		@Override
		protected void runCommand(EntityManager eF) {
			String previousAuthor = "";
			Date previousModified = new Date(0l);
			int version = 0;

			RetResult ret = getCurrentPageInfo(eF, page.getName());
			WikiPage latest = ret.page;
			if (latest != null) {
				version = latest.getVersion();
				previousModified = latest.getLastModified();
				previousAuthor = latest.getAuthor();
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
					+ previousModified.getTime();

			boolean createVersion = !pageExists(page.getName())
					|| isDifferentAuthor || isContinuationEditTimeExpired;
			if (ret.ent == null) {
				ret.ent = new WikiPageEnt();
			}

			if (createVersion) {
				// Insert page
				if (!pageExists(page.getName())) {
					page.setVersion(1);
				} else {
					page.setVersion(page.getVersion() + 1);
				}
				if (page.getLastModified() != null) {
					page.setLastModified(page.getLastModified());
				} else {
					page.setLastModified(getToday());
				}

				debug("Create page version: " + page);
				// Insert the version into database
				ret.ent = new WikiPageEnt();
				ret.ent.setVersion(page.getVersion());
				ret.ent.setChangetime(page.getLastModified());
			} else {
				// Update page
				ret.ent.setChangetime(getToday());
				ret.ent.setVersion(version);
			}
			ret.ent.setName(page.getName());
			ret.ent.setChangeBy(page.getAuthor());
			ret.ent.setContent(text);
			ret.ent.setChangeNote((String) page
					.getAttribute(WikiPage.CHANGENOTE));
			EntityTransaction tran = eF.getTransaction();
			tran.begin();
			eF.persist(ret.ent);
			tran.commit();

		}
	}

	@Override
	public void putPageText(WikiPage page, String text)
			throws ProviderException {
		ECommand e = new EPutPageText(page, text);
		e.runCommand();
	}

	private class PageExists extends ECommand {

		private final String page;
		boolean result;

		PageExists(String page) {
			super(false);
			this.page = page;
		}

		@Override
		protected void runCommand(EntityManager eF) {
			List<WikiPageEnt> pList = queryNamed(eF, "GetListOfPages", page);
			result = !pList.isEmpty();
		}

	}

	@Override
	public boolean pageExists(String page) {
		PageExists e = new PageExists(page);
		e.runCommand();
		return e.result;
	}

	private class FindPages extends ECommand {

		private final QueryItem[] query;
		Collection res;

		FindPages(QueryItem[] query) {
			super(false);
			this.query = query;
		}

		@Override
		protected void runCommand(EntityManager eF) {
			res = new TreeSet(new SearchResultComparator());
			SearchMatcher matcher = new SearchMatcher(m_engine, query);
			Collection<RetResult> lre = getAllPagesW(eF);
			for (RetResult w : lre) {
				SearchResult comparison;
				try {
					comparison = matcher.matchPageContent(w.ent.getName(),
							w.ent.getContent().toString());
					if (comparison != null) {
						res.add(comparison);
					}
				} catch (IOException e) {
					error("Failed to read", e);
				}
			}
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Collection findPages(QueryItem[] query) {
		FindPages e = new FindPages(query);
		e.runCommand();
		return e.res;
	}

	public RetResult getPageInfoW(EntityManager eF, String page, int version) {
		// GetPageVersion
		RetResult re;
		if (version == WikiPageProvider.LATEST_VERSION) {
			re = getCurrentPageInfo(eF, page);
		} else {
			Query query = eF.createNamedQuery("GetPageVersion");
			query.setParameter(1, page);
			query.setParameter(2, version);
			re = new RetResult();
			try {
				re.ent = (WikiPageEnt) query.getSingleResult();
			} catch (NoResultException e) {
				// expected
			}
		}
		if (re.ent != null) {
			re.toPage();
		}
		return re;
	}

	private class GetPageInfo extends ECommand {

		private final String page;
		private final int version;
		RetResult re;

		GetPageInfo(String page, int version) {
			super(false);
			this.page = page;
			this.version = version;
		}

		@Override
		protected void runCommand(EntityManager eF) {
			re = getPageInfoW(eF, page, version);
		}

	}

	@Override
	public WikiPage getPageInfo(String page, int version)
			throws ProviderException {
		GetPageInfo r = new GetPageInfo(page, version);
		r.runCommand();
		return r.re.page;
	}

	private Collection<RetResult> getPagesW(List<WikiPageEnt> pList) {
		Collection<RetResult> li = new ArrayList<RetResult>();
		RetResult ret = null;
		for (WikiPageEnt w : pList) {
			if (ret != null) {
				if (ret.ent.getName().equals(w.getName())) {
					nextW(ret, w);
					continue;
				} else {
					ret.toPage();
					li.add(ret);
				}
			}
			ret = new RetResult();
			ret.ent = w;
		} // for
		if (ret != null) {
			ret.toPage();
			li.add(ret);
		}
		return li;
	}

	private Collection<RetResult> getAllPagesW(EntityManager eF) {
		List<WikiPageEnt> pList = queryNamed(eF, "GetListOfAllPages");
		return getPagesW(pList);
	}

	private class GetAllPages extends ECommand {

		Collection<WikiPage> li;
		
		GetAllPages() {
			super(false);
		}

		@Override
		protected void runCommand(EntityManager eF) {
			li = new ArrayList<WikiPage>();
			Collection<RetResult> lre = getAllPagesW(eF);
			for (RetResult re : lre) {
				li.add(re.page);
			}
		}

	}

	@SuppressWarnings("rawtypes")
	@Override
	public Collection getAllPages() throws ProviderException {
		GetAllPages e = new GetAllPages();
		e.runCommand();
		return e.li;
	}

	private class GetAllChangesSince extends ECommand {

		private final Date date;
		Collection res;

		GetAllChangesSince(Date date) {
			super(false);
			this.date = date;
		}

		@Override
		protected void runCommand(EntityManager eF) {
			Query query = eF.createNamedQuery("GetPagesAfterDate");
			query.setParameter(1, date);
			List<WikiPageEnt> pList = query.getResultList();
			res = getPagesW(pList);
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Collection getAllChangedSince(Date date) {
		GetAllChangesSince g = new GetAllChangesSince(date);
		g.runCommand();
		return g.res;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public int getPageCount() throws ProviderException {
		Collection col = getAllPages();
		return col.size();
	}

	private class GetVersionHistory extends ECommand {

		private final String page;
		List res;

		GetVersionHistory(String page) {
			super(false);
			this.page = page;
		}

		@Override
		protected void runCommand(EntityManager eF) {
			List<WikiPageEnt> pList = queryNamed(eF, "GetListOfPages", page);
			RetResult ret = new RetResult();
			res = new ArrayList<WikiPage>();
			for (WikiPageEnt w : pList) {
				ret.ent = w;
				ret.toPage();
				res.add(ret.page);
			}

		}

	}

	@SuppressWarnings("rawtypes")
	@Override
	public List getVersionHistory(String page) throws ProviderException {
		GetVersionHistory g = new GetVersionHistory(page);
		g.runCommand();
		return g.res;
	}

	private class GetPageText extends ECommand {

		private final String page;
		private final int version;
		String res;

		GetPageText(String page, int version) {
			super(false);
			this.page = page;
			this.version = version;
		}

		@Override
		protected void runCommand(EntityManager eF) {
			RetResult re = getPageInfoW(eF, page, version);
			res = null;
			if (re.ent != null) {
				res = re.ent.getContent().getValue();
			}
		}

	}

	@Override
	public String getPageText(String page, int version)
			throws ProviderException {
		GetPageText ge = new GetPageText(page, version);
		ge.runCommand();
		return ge.res;
	}

	private class DeleteVersion extends ECommand {

		private final String page;
		private final int version;
		String res;

		DeleteVersion(String page, int version) {
			super(true);
			this.page = page;
			this.version = version;
		}

		@Override
		protected void runCommand(EntityManager eF) {
			RetResult re = getPageInfoW(eF, page, version);
			if (re.ent == null) {
				return;
			}
			eF.remove(re.ent);
		}
	}

	@Override
	public void deleteVersion(String pageName, int version)
			throws ProviderException {
		DeleteVersion de = new DeleteVersion(pageName, version);
		de.runCommand();
	}

	private class DeletePage extends ECommand {

		private final String pageName;

		DeletePage(String pageName) {
			super(false);
			this.pageName = pageName;
		}

		@Override
		protected void runCommand(EntityManager eF) {
			List<WikiPageEnt> pList = queryNamed(eF, "GetListOfPages", pageName);
			EntityTransaction tran = eF.getTransaction();
			for (WikiPageEnt w : pList) {
				tran.begin();
				eF.remove(w);
				tran.commit();
			}
		}

	}

	@Override
	public void deletePage(String pageName) throws ProviderException {
		DeletePage de = new DeletePage(pageName);
		de.runCommand();
	}

	private class MovePage extends ECommand {

		private final String from;
		private final String to;

		MovePage(String from, String to) {
			super(false);
			this.from = from;
			this.to = to;
		}

		@Override
		protected void runCommand(EntityManager eF) {
			List<WikiPageEnt> pList = queryNamed(eF, "GetListOfPages", from);
			EntityTransaction tran = eF.getTransaction();
			for (WikiPageEnt w : pList) {
				tran.begin();
				w.setName(to);
				eF.persist(w);
				tran.commit();
			}
		}

	}

	@Override
	public void movePage(String from, String to) throws ProviderException {
		MovePage mo = new MovePage(from, to);
		mo.runCommand();

	}

}
