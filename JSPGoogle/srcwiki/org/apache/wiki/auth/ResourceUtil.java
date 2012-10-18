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
package org.apache.wiki.auth;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.spring.BeanHolder;

public class ResourceUtil {

	private static final Log log = LogFactory.getLog(ResourceUtil.class);

	private ResourceUtil() {
	}

	/**
	 * Looks up and obtains a configuration file inside the WEB-INF folder of a
	 * wiki webapp.
	 * 
	 * @param engine
	 *            the wiki engine
	 * @param name
	 *            the file to obtain, <em>e.g.</em>, <code>jspwiki.policy</code>
	 * @return the URL to the file
	 */
	public static URL findConfigFile(WikiEngine engine, String name) {
		// Try creating an absolute path first
		File defaultFile = null;
		String rootPath = BeanHolder.getRootURL();
		if (rootPath != null) {
			defaultFile = new File(rootPath + "/WEB-INF/" + name);
		}
		if (defaultFile != null && defaultFile.exists()) {
			try {
				return defaultFile.toURI().toURL();
			} catch (MalformedURLException e) {
				// Shouldn't happen, but log it if it does
				log.warn("Malformed URL: " + e.getMessage());
			}

		}

		// Ok, the absolute path didn't work; try other methods

		URL path = null;

		if (engine.getServletContext() != null) {
			try {
				// create a tmp file of the policy loaded as an InputStream and
				// return the URL to it
				//
				InputStream is = engine.getServletContext()
						.getResourceAsStream(name);
				File tmpFile = File.createTempFile("temp." + name, "");
				tmpFile.deleteOnExit();

				OutputStream os = new xjava.io.FileOutputStream(tmpFile);

				byte[] buff = new byte[1024];

				while (is.read(buff) != -1) {
					os.write(buff);
				}

				os.close();

				path = tmpFile.toURI().toURL();

			} catch (MalformedURLException e) {
				// This should never happen unless I screw up
				log.fatal("Your code is b0rked.  You are a bad person.");
			} catch (IOException e) {
				log.error("failed to load security policy from " + name
						+ ",stacktrace follows", e);
			}
		}
		return path;
	}

}
