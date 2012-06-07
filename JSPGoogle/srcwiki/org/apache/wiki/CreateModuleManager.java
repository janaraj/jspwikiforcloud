package org.apache.wiki;

import java.io.IOException;
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
