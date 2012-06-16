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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Key;

@Entity
@NamedQueries({
		@NamedQuery(name = "FindGroup", query = "SELECT P FROM GroupEnt P WHERE P.name= :1"),
		@NamedQuery(name = "AllGroups", query = "SELECT P FROM GroupEnt P") })
public class GroupEnt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key key;

	@Basic(optional = false)
	private String name;

	@Basic
	private String creator;

	@Basic(optional = false)
	private Date created;

	@Basic
	private String modifier;

	@Basic(optional = false)
	private Date modified;

	// @Lob
	// private Collection<GroupMember> memberList;
	@Basic(optional = false)
	private Blob memberList;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public byte[] getMemberList() {
		return memberList.getBytes();
	}

	public void setMemberList(byte[] memberList) {
		this.memberList = new Blob(memberList);
	}

}
