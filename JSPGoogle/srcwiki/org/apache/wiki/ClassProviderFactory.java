package org.apache.wiki;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.wiki.util.ClassUtil;

public class ClassProviderFactory {

	private static Logger log = Logger.getLogger(ClassProviderFactory.class);

	@SuppressWarnings("unchecked")
	public static <T extends WikiProvider> T construct(WikiEngine engine,
			Properties props, String pro) {
		String classname;
		T m_provider = null;
		try {
			classname = WikiEngine.getRequiredProperty(props, pro);
			log.debug("Page provider class: '" + classname + "'");
			Class<T> providerclass = ClassUtil.findClass(
					"org.apache.wiki.providers", classname);

			m_provider = (T) providerclass.newInstance();
			log.debug("Initializing page provider class " + m_provider);
			m_provider.initialize(engine, props);
		} catch (NoRequiredPropertyException e) {
			log.error(e);
		} catch (ClassNotFoundException e) {
			log.error(e);
		} catch (InstantiationException e) {
			log.error(e);
		} catch (IllegalAccessException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		}
		return m_provider;
	}
}
