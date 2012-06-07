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

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wiki.IObjectPersist;
import org.apache.wiki.providers.WikiPageProvider;
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
		WikiSubject su = (WikiSubject) getObject("wikiuser");
		log.trace("get Subject wikiuser : " + su);
		return su;
	}

	public static IObjectPersist getObjectPersist() {
		Object o = getObject("objectProvider");
		return (IObjectPersist) o;
	}
	
	public static  WikiPageProvider getPageProvider() {
		Object o = getObject("pageProvider");
		return (WikiPageProvider) o;
	}


}
