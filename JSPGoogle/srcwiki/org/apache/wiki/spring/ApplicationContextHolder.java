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
import java.util.Properties;

import org.springframework.web.context.WebApplicationContext;

public class ApplicationContextHolder {

	/**
	 * ApplicationContext holder by means of ThreadLocal
	 */
	private static final ThreadLocal<WebApplicationContext> contextSession = new ThreadLocal<WebApplicationContext>();
	private static final ThreadLocal<Locale> locale = new ThreadLocal<Locale>();
	private static final ThreadLocal<Properties> properties = new ThreadLocal<Properties>();
	private static final ThreadLocal<Boolean> useJAAS = new ThreadLocal<Boolean>();
    private static final ThreadLocal<String> appName = new ThreadLocal<String>();

	private ApplicationContextHolder() {
	}

	/** Access to spring wired beans. */
	static WebApplicationContext getContext() {
		WebApplicationContext at = contextSession.get();
		return at;
	}

	/** Access to spring wired beans. */
	static void setContext(WebApplicationContext atx) {
		contextSession.set(atx);
	}

	static Locale getLocale() {
		return locale.get();
	}

	static void setLocale(Locale value) {
		locale.set(value);
	}

	static Properties getProperties() {
		return properties.get();
	}

	static void setProperties(Properties prop) {
		properties.set(prop);
	}

	static Boolean getUseJASS() {
		return useJAAS.get();
	}

	static void setJAAS(Boolean b) {
		useJAAS.set(b);
	}
	
	static String getAppName() {
	    return appName.get();
	}
	
	static void setAppName(String s) {
	    appName.set(s);
	}

}