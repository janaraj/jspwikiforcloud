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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wiki.auth.WikiSecurityException;
import org.apache.wiki.providers.jpa.AbstractEntBlob;

class BlobUtil {

    private BlobUtil() {

    }

    private static final Log log = LogFactory.getLog(BlobUtil.class);

    static <T> Collection<T> toObjects(AbstractEntBlob g)
            throws WikiSecurityException {
        if (g == null) {
            return new ArrayList<T>();
        }
        ByteArrayInputStream in = new ByteArrayInputStream(g.getContent());
        try {
            ObjectInputStream i = new ObjectInputStream(in);
            Collection<T> c = (Collection<T>) i.readObject();
            return c;
        } catch (IOException e) {
            log.fatal(e);
            throw new WikiSecurityException(e.getMessage());
        } catch (ClassNotFoundException e) {
            log.fatal(e);
            throw new WikiSecurityException(e.getMessage());
        }
    }

    static <T> void toBlob(AbstractEntBlob g, Collection<T> cList)
            throws WikiSecurityException {
        ByteArrayOutputStream ou = new ByteArrayOutputStream();
        try {
            ObjectOutputStream o = new ObjectOutputStream(ou);
            o.writeObject(cList);
            o.flush();
        } catch (IOException e) {
            log.fatal(e);
            throw new WikiSecurityException(e.getMessage());
        }
        g.setContent(ou.toByteArray());
    }

}
