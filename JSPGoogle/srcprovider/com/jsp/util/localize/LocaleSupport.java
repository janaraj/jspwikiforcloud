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

package com.jsp.util.localize;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import org.apache.taglibs.standard.tag.common.fmt.BundleSupport;

public class LocaleSupport {

	/**
	 * Replacement for LocaleSupport.getLocalizedMessage method in JSP pages
	 * Before calling set fmt:setLocale and fmt: setBundle
	 */

	private LocaleSupport() {

	}

	/**
	 * Get localized message (also MessageFormated) from I18N bundle
	 * 
	 * @param pageContext
	 *            PageContext
	 * @param key
	 *            Message key
	 * @param params
	 *            List of parameters for MessageFormat (if exists)
	 * @return localized message
	 */
	public static String getLocalizedMessage(PageContext pageContext,
			String key, String... params) {

		LocalizationContext loca = BundleSupport
				.getLocalizationContext(pageContext);
		ResourceBundle bundle = loca.getResourceBundle();
		String message = bundle.getString(key);
		if (params.length == 0) {
			return message;
		}
		String mess = MessageFormat.format(message, params);
		return mess;
	}

	public static String getLocalizedMessage(PageContext pageContext,
			String key, Object[] params) {
		String message = getLocalizedMessage(pageContext, key);
		if (params == null || params.length == 0) {
			return message;
		}
		String mess = MessageFormat.format(message, params);
		return mess;
	}
}
