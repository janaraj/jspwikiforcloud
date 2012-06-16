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
package org.apache.wiki.tags;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiPage;
import org.apache.wiki.providers.ProviderException;

/**
 * Includes the body in case there is no such page available.
 * 
 * @since 2.0
 */
public class NoSuchPageTag extends WikiTagBase {
	private static final long serialVersionUID = 0L;

	private static Log log = LogFactory.getLog(NoSuchPageTag.class);

	private String m_pageName;

	public void initTag() {
		super.initTag();
		m_pageName = null;
	}

	public void setPage(String name) {
		m_pageName = name;
	}

	public String getPage() {
		return m_pageName;
	}

	public int doWikiStartTag() throws IOException, ProviderException {
		WikiEngine engine = m_wikiContext.getEngine();
		WikiPage page;

		log.debug("NoSuchPageTage pagename = " + m_pageName);

		if (m_pageName == null) {
			page = m_wikiContext.getPage();
		} else {
			page = engine.getPage(m_pageName);
		}

		// System.out.println("Checking "+page);

		if (page != null
				&& engine.pageExists(page.getName(), page.getVersion())) {
			log.debug("NoSuchPageTage pagename SKIP_BODY");
			return SKIP_BODY;
		}
		log.debug("NoSuchPageTage pagename BODY_INCLUDE");
		return EVAL_BODY_INCLUDE;
	}
}
