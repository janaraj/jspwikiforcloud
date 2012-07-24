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

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.apache.wiki.util.Serializer;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;

@Entity
@NamedQueries({
		@NamedQuery(name = "FindUserByWikiName", query = "SELECT P FROM UserEnt P WHERE P.wikiName = :1"),
		@NamedQuery(name = "FindUserByFullName", query = "SELECT P FROM UserEnt P WHERE P.fullName = :1"),
		@NamedQuery(name = "FindUserByeMail", query = "SELECT P FROM UserEnt P WHERE P.email = :1"),
		@NamedQuery(name = "FindUserByUid", query = "SELECT P FROM UserEnt P WHERE P.uId = :1"),
		@NamedQuery(name = "FindUserByLoginName", query = "SELECT P FROM UserEnt P WHERE P.loginName = :1"),
		@NamedQuery(name = "AllUsers", query = "SELECT P FROM UserEnt P") })
public class UserEnt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key key;

	@Basic(optional = false)
	private String loginName;

	@Basic
	private String fullName;

	@Basic
	private String email;

	@Basic
	private String password;

	@Basic
	private String wikiName;

	@Basic
	private String uId;

	@Basic(optional = false)
	private Date created;

	@Basic(optional = false)
	private Date modified;

	@Basic
	private Date LockExpiry;

	@Basic
	private Text sAttributes;

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getWikiName() {
		return wikiName;
	}

	public void setWikiName(String wikiName) {
		this.wikiName = wikiName;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public String getuId() {
		return uId;
	}

	public void setuId(String uId) {
		this.uId = uId;
	}

	public Map<String, Serializable> getAttributes() throws IOException {
		Map<String, Serializable> hMap = new HashMap<String, Serializable>();
		if (sAttributes != null) {
            hMap = (Map<String, Serializable>) Serializer.deserializeFromBase64( sAttributes.getValue() );
		}
		return hMap;
	}

	public void setAttributes(Map<String, Serializable> attributes) throws IOException {
		if (attributes == null || attributes.isEmpty()) { sAttributes = null; return; }
		String s = Serializer.serializeToBase64( attributes);
		sAttributes = new Text(s);		
	}

	public Date getLockExpiry() {
		return LockExpiry;
	}

	public void setLockExpiry(Date lockExpiry) {
		LockExpiry = lockExpiry;
	}

}
