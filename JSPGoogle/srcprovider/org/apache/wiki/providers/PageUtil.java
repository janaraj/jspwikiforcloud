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
package org.apache.wiki.providers;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wiki.WikiPage;
import org.apache.wiki.auth.WikiSecurityException;
import org.apache.wiki.providers.jpa.AbstractEntBlob;
import org.apache.wiki.util.Serializer;

class PageUtil {

    private PageUtil() {

    }

    private static final Log log = LogFactory.getLog(PageUtil.class);

    static class WikiVersion implements Serializable {

        int version;

        Date changetime;

        String changeBy;

        String attributes;
        
        long fileSize;

    }

    static void toVersion(WikiVersion v, WikiPage page)
            throws WikiSecurityException {
        v.changeBy = page.getAuthor();
        v.changetime = page.getLastModified();
        v.fileSize = page.getSize();
        Map<String, String> attributes = page.getAttributes();
        if (attributes == null || attributes.isEmpty()) {
            v.attributes = null;
            return;
        }
        Map<String, Serializable> sMap = new HashMap<String, Serializable>();
        sMap.putAll(attributes);
        try {
            v.attributes = Serializer.serializeToBase64(sMap);
        } catch (IOException e) {
            log.error(e);
            throw new WikiSecurityException(e.getMessage());
        }
    }

    static void toWikiPage(WikiPage ww, WikiVersion v)
            throws WikiSecurityException {
        ww.setVersion(v.version);
        ww.setLastModified(v.changetime);
        ww.setAuthor(v.changeBy);
        ww.setSize(v.fileSize);
        Map<String, Serializable> hMap = new HashMap<String, Serializable>();
        if (v.attributes != null) {
            try {
                hMap = (Map<String, Serializable>) Serializer
                        .deserializeFromBase64(v.attributes);
            } catch (IOException e1) {
                log.fatal(e1);
                throw new WikiSecurityException(e1.getMessage());
            }
        }
        for (Entry<String, Serializable> e : hMap.entrySet()) {
            ww.setAttribute(e.getKey(), e.getValue().toString());
        }
    }

    static WikiVersion findLatest(Collection<WikiVersion> vList) {
        WikiVersion v = null;
        for (WikiVersion vv : vList) {
            if ((v == null) || (v.version < vv.version)) {
                v = vv;
            }
        }
        return v;
    }

    static WikiVersion findVersion(Collection<WikiVersion> vList, int version) {
        if (version == WikiPageProvider.LATEST_VERSION) {
            return findLatest(vList);
        }
        WikiVersion v = null;
        for (WikiVersion vv : vList) {
            if (vv.version == version) {
                v = vv;
                break;
            }
        }
        return v;
    }

    static Collection<WikiVersion> toVersions(AbstractEntBlob u)
            throws WikiSecurityException {
        return BlobUtil.toObjects(u);
    }

    static void toBlobPage(AbstractEntBlob u, Collection<WikiVersion> uList)
            throws WikiSecurityException {
        BlobUtil.toBlob(u, uList);
    }

}
