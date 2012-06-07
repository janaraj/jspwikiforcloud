package org.apache.wiki;

import java.util.Collection;
import java.util.Properties;

import org.apache.wiki.modules.WikiModuleInfo;

public abstract class AbstractWikiProvider implements WikiProvider {

	protected WikiEngine m_engine;
	protected Properties m_properties;

	@Override
	public void initialize(WikiEngine engine, Properties properties)
			throws WikiException {
		this.m_engine = engine;
		this.m_properties = properties;
	}

	@Override
	public String getProviderInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public void initializeProvider() {		
	}



}
