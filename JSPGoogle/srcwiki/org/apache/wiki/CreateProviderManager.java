package org.apache.wiki;

import java.util.Properties;

public class CreateProviderManager<T extends WikiProvider> {
	
	private final CreateModuleManager pManager;
	
	public CreateProviderManager(WikiEngine engine, Properties props,
			String beanName) {
		pManager = new CreateModuleManager(engine,props,beanName);
	}
	
	public T getManager() {
		@SuppressWarnings("unchecked")
		T ma = (T) pManager.getBeanObject();
		if (!pManager.isInitialized()) {
			ma.initializeProvider();
			pManager.setInitialized(true);
		}
		return ma;
	}


}
