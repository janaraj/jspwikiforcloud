package com.opensymphony.oscache.base;

import com.opensymphony.oscache.base.events.CacheEventListener;

public class Cache {

	public Cache(boolean useMemoryCaching, boolean unlimitedDiskCache,
			boolean overflowPersistence, boolean blocking,
			String algorithmClass, int capacity) {
	}

	public Cache(boolean useMemoryCaching, boolean unlimitedDiskCache,
			boolean overflowPersistence) {
		this(useMemoryCaching, unlimitedDiskCache, overflowPersistence, false,
				null, 0);
	}

	public Object getFromCache(String key, int refreshPeriod)
			throws NeedsRefreshException {
		return null;
	}

	public Object getFromCache(String key) throws NeedsRefreshException {
		return getFromCache(key, 0);
	}

	public void cancelUpdate(String key) {
	}

	public void removeEntry(String key) {
	}
	
    public void addCacheEventListener(CacheEventListener listener) {    	
    }
    
    public void addCacheEventListener(CacheEventListener listener, Class clazz) {
    	
    }

    
    public void flushPattern(String pattern) {
    }


	public void putInCache(String key, Object content) {

	}

}
