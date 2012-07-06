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

import java.util.ArrayList;

import org.apache.wiki.ReferenceManager;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiException;
import org.apache.wiki.WikiPage;
import org.apache.wiki.filters.FilterListContainer;
import org.apache.wiki.filters.FilterManager;
import org.apache.wiki.render.RenderingManager;
import org.apache.wiki.search.SearchManager;

public class ReferenceFactoryProvider {

    private final FilterManager fManager;
    private final SearchManager sManager;
    private final ReferenceManager rManager;
    private final RenderingManager rendManager;

    public ReferenceFactoryProvider(WikiEngine engine) throws WikiException {
        FilterListContainer fContainer = BeanHolder.getFiltrListContainer();
        fManager = new FilterManager(engine);
        fManager.setFiltrListContainer(fContainer);
        fManager.initializeProvider();

        sManager = new SearchManager(engine);
        sManager.initializeProvider();

        rManager = new ReferenceManager(engine);
        rManager.initializeProvider();

        fContainer.addPageFilter(rManager, -1001);
        fContainer.addPageFilter(sManager, -1002);
        
        sManager.initializeSearchManager(fManager);

        rendManager = new RenderingManager(engine);
        rendManager.initRendering(fManager);

        // initialize ReferenceManager
        // the only way to avoid nested dependency
        // unfortunately : while running initlizaPager it can call FilterManager
        // but FilterManager requires ReferenceManager to initialize (look
        // above)
        // so cannot run it is a part of the constructor
        ArrayList<WikiPage> pages = new ArrayList<WikiPage>();
        pages.addAll(BeanHolder.getPageManager().getAllPages());
        pages.addAll(BeanHolder.getAttachmentManager().getAllAttachments());
        rManager.initializePages(fManager, rendManager, pages);
    }

    public FilterManager getfManager() {
        return fManager;
    }

    public SearchManager getsManager() {
        return sManager;
    }

    public ReferenceManager getrManager() {
        return rManager;
    }

    public RenderingManager getRendManager() {
        return rendManager;
    }

}
