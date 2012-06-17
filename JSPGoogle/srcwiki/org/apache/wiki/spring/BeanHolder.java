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
package org.apache.wiki.spring;

import java.util.Locale;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wiki.IObjectPersist;
import org.apache.wiki.PageManager;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiSession;
import org.apache.wiki.auth.AuthenticationManager;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.auth.UserManager;
import org.apache.wiki.auth.authorize.GroupManager;
import org.apache.wiki.event.WikiEventManager;
import org.apache.wiki.security.WikiSubject;
import org.springframework.web.context.WebApplicationContext;

public class BeanHolder {

	private BeanHolder() {
	}

	private static Log log = LogFactory.getLog(BeanHolder.class);

	public static Object getObject(String name) {
		WebApplicationContext apx = ApplicationContextHolder.getContext();
		log.trace("get subject context : " + apx + " long="
				+ apx.getStartupDate());
		return apx.getBean(name);
	}

	public static ServletContext getServletContext() {
		WebApplicationContext apx = ApplicationContextHolder.getContext();
		return apx.getServletContext();
	}

	public static WikiSubject getSubject() {
		WikiSession se = getWikiSession();
		log.trace("getSubject = " + se.getWikiSubject());
		return se.getWikiSubject();
	}

	public static IObjectPersist getObjectPersist() {
		Object o = getObject("objectProvider");
		return (IObjectPersist) o;
	}

	public static WikiEngine getWikiEngine() {
		WikiEngine engine = (WikiEngine) BeanHolder.getObject("wikiEngine");
		return engine;
	}

	public static Locale getLocale() {
		return ApplicationContextHolder.getLocale();
	}

	public static WikiSession getWikiSession() {
		WikiSession se = (WikiSession) BeanHolder.getObject("wikiSession");
		return se;
	}

	public static WikiEventManager getWikiManager() {
		WikiEventManager se = (WikiEventManager) BeanHolder
				.getObject("wikiEventManager");
		return se;
	}

	public static GroupManager getGroupManager() {
		GroupManager ge = (GroupManager) BeanHolder.getObject("groupManager");
		return ge;
	}

	public static UserManager getUserManager() {
		UserManager ge = (UserManager) BeanHolder.getObject("userManager");
		return ge;
	}

	public static AuthenticationManager getAuthenticationManager() {
		AuthenticationManager ge = (AuthenticationManager) BeanHolder
				.getObject("authenticationManager");
		return ge;
	}

	public static AuthorizationManager getAuthorizationManager() {
		AuthorizationManager ge = (AuthorizationManager) BeanHolder
				.getObject("authorizationManager");
		return ge;
	}

	public static PageManager getPageManager() {
		PageManager ge = (PageManager) BeanHolder.getObject("pageManager");
		return ge;
	}

}
