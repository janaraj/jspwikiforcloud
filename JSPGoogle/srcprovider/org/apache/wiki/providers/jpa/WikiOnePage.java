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
package org.apache.wiki.providers.jpa;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;
 
@NamedQueries({ @NamedQuery(name = "FindPageContent", query = "SELECT P FROM WikiOnePage P WHERE P.pageKey = :1 AND P.version = :2") })
@Entity
public class WikiOnePage {
  
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;

    @Basic(optional = false)
    private long pageKey;

    @Basic(optional = false)
    private int version;
    
    @Basic(optional = false)
    private Text content;

    public void setPageKey(long pageKey) {
        this.pageKey = pageKey;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getContent() {
        return content.getValue();
    }

    public void setContent(String content) {
        this.content = new Text(content);
    }

}
