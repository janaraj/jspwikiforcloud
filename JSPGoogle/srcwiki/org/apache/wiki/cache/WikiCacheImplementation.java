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
package org.apache.wiki.cache;

import java.util.HashMap;
import java.util.Map;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

/**
 * Google App Engine environment. Maps to Google App Engin memcache
 * 
 * @author perseus
 * 
 */
public class WikiCacheImplementation implements IWikiCache {

    private final Cache cache;

    public WikiCacheImplementation() throws CacheException {
        Map props = new HashMap();
        CacheFactory cacheFactory = CacheManager.getInstance()
                .getCacheFactory();
        cache = cacheFactory.createCache(props);
    }

    @Override
    public Object getObject(String key) {
        Object o = cache.get(key);
        return o;
    }

    @Override
    public void setObject(String key, Object o) {
        cache.put(key, o);
    }

}
