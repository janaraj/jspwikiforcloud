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
package org.apache.wiki.filters;

import java.util.Properties;

import org.apache.wiki.AbstractWikiProvider;
import org.apache.wiki.WikiContext;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiException;

/**
 * Provides a base implementation of a PageFilter. None of the callbacks do
 * anything, so it is a good idea for you to extend from this class and
 * implement only methods that you need.
 * 
 */
public class BasicPageFilter extends AbstractWikiProvider implements PageFilter {

	/**
	 * {@inheritDoc}
	 */
	public String preTranslate(WikiContext wikiContext, String content)
			throws FilterException {
		return content;
	}

	/**
	 * {@inheritDoc}
	 */
	public String postTranslate(WikiContext wikiContext, String htmlContent)
			throws FilterException {
		return htmlContent;
	}

	/**
	 * {@inheritDoc}
	 */
	public String preSave(WikiContext wikiContext, String content)
			throws FilterException {
		return content;
	}

	/**
	 * {@inheritDoc}
	 */
	public void postSave(WikiContext wikiContext, String content)
			throws FilterException {
	}

	/**
	 * {@inheritDoc}
	 */
	public void destroy(WikiEngine engine) {
	}

}
