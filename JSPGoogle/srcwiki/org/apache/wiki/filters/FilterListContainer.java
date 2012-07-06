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
package org.apache.wiki.filters;

import org.apache.wiki.util.PriorityList;

public class FilterListContainer {

    private final PriorityList m_pageFilters = new PriorityList();

    public void addPageFilter(PageFilter f, int priority)
            throws IllegalArgumentException {
        if (f == null) {
            throw new IllegalArgumentException(
                    "Attempt to provide a null filter - this should never happen.  Please check your configuration (or if you're a developer, check your own code.)");
        }

        m_pageFilters.add(f, priority);
    }

    public PriorityList getFilterList() {
        return m_pageFilters;
    }

}
