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

import com.google.appengine.api.datastore.ShortBlob;

import com.google.appengine.api.datastore.Key;

@Entity
@NamedQueries({
		@NamedQuery(name = "GetObject", query = "SELECT P FROM WikiObject P WHERE P.pDir= :1 AND P.pName = :2")
})
public class WikiObject {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key key;
	
	@Basic
	private String pDir;
	
	@Basic
    private String pName;
	
	@Basic
	private ShortBlob object;

	public String getpDir() {
		return pDir;
	}

	public void setpDir(String pDir) {
		this.pDir = pDir;
	}

	public String getpName() {
		return pName;
	}

	public void setpName(String pName) {
		this.pName = pName;
	}

	public ShortBlob getObject() {
		return object;
	}

	public void setObject(ShortBlob object) {
		this.object = object;
	}
	
	public void setBytes(byte[] b) {
		setObject(new ShortBlob(b));
	}
	
}
