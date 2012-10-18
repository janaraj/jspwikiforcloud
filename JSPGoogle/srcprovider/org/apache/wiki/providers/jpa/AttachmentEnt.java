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
        @NamedQuery(name = "GetAllAttachments", query = "SELECT P FROM AttachmentEnt P"),
        @NamedQuery(name = "GetAttachmentsForPage", query = "SELECT P FROM AttachmentEnt P WHERE P.pageName = :1"),
        @NamedQuery(name = "GetAttachmentForPageAndFileName", query = "SELECT P FROM AttachmentEnt P WHERE P.pageName = :1 AND P.fileName = :2") })
@Entity
public class AttachmentEnt extends AbstractEntBlob {

    @Basic(optional = false)
    private String pageName;

    @Basic(optional = false)
    private String fileName;
    
    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
