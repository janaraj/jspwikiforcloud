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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wiki.WikiContext;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.spring.BeanHolder;
import org.apache.wiki.ui.Editor;
import org.apache.wiki.ui.EditorManager;

/**
 * Iterates through editors.
 * 
 * @since 2.4.12
 */

public class EditorIteratorTag extends IteratorTag {
	private static final long serialVersionUID = 0L;

	static private final Log log = LogFactory.getLog(EditorIteratorTag.class);

	public final int doStartTag() {
		m_wikiContext = WikiContext.findContext(pageContext);

		WikiEngine engine = m_wikiContext.getEngine();
		EditorManager mgr = BeanHolder.getEditorManager();

		String[] editorList = mgr.getEditorList();

		Collection<Editor> editors = new ArrayList<Editor>();

		for (int i = 0; i < editorList.length; i++) {
			editors.add(new Editor(m_wikiContext, editorList[i]));
		}
		setList(editors);

		return super.doStartTag();
	}
}
