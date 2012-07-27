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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
 
@NamedQueries({ 
    @NamedQuery(name = "FindListOfPages", query = "SELECT P FROM WikiPageBlob P"),
    @NamedQuery(name = "FindPage", query = "SELECT P FROM WikiPageBlob P WHERE P.pageName = :1 ") })
@Entity
public class WikiPageBlob extends AbstractEntBlob {
    
    @Basic(optional = false)
    private String pageName;

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }      

}
