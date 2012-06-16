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
package org.apache.wiki;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wiki.spring.BeanHolder;

public class CreateModuleManager {

	private static final Log log = LogFactory.getLog(CreateModuleManager.class);

	private final WikiEngine engine;
	private final Properties props;
	private final String beanName;
	private final WikiProvider wProvider;
	private boolean initialized = false;

	public CreateModuleManager(WikiEngine engine, Properties props,
			String beanName) {
		this.beanName = beanName;
		this.engine = engine;
		this.props = props;
		this.wProvider = null;
	}

	public CreateModuleManager(WikiEngine engine, Properties props,
			WikiProvider wProvider) {
		this.beanName = null;
		this.engine = engine;
		this.props = props;
		this.wProvider = wProvider;
	}

	public WikiProvider getBeanObject() {
		WikiProvider w;
		if (wProvider != null) {
			w = wProvider;
		} else {
			w = (WikiProvider) BeanHolder.getObject(beanName);
			if (w == null) {
				log.fatal("Null bean " + beanName);
			}
		}
		try {
			w.initialize(engine, props);
		} catch (WikiException e) {
			log.fatal(beanName + " initializa", e);
		}
		return w;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

}
