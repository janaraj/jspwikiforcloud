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

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;

/**
 * @author hotel 
 * 
 */
@Entity
@NamedQueries({
		@NamedQuery(name = "GetListOfPages", query = "SELECT P FROM WikiPageEnt P WHERE P.name= :1 ORDER BY P.version desc"),
		@NamedQuery(name = "GetListOfAllPages", query = "SELECT P FROM WikiPageEnt P ORDER BY P.name asc"),
		@NamedQuery(name = "GetPageVersion", query = "SELECT P FROM WikiPageEnt P WHERE P.name= :1 AND P.version = :2"),
		@NamedQuery(name = "GetPagesAfterDate", query = "SELECT P FROM WikiPageEnt P WHERE P.changetime > :1")
})
public class WikiPageEnt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key key;

	@Basic(optional=false)
	private int version;

	@Basic(optional=false)
	private String name;

	@Basic(optional=false)
	private Date changetime;

	@Basic(optional=false)
	private String changeBy;

	private String changeNote;
	
	private Text content;

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getChangetime() {
		return changetime;
	}

	public void setChangetime(Date changetime) {
		this.changetime = changetime;
	}

	public String getChangeBy() {
		return changeBy;
	}

	public void setChangeBy(String changeBy) {
		this.changeBy = changeBy;
	}

	public String getChangeNote() {
		return changeNote;
	}

	public void setChangeNote(String changeNote) {
		this.changeNote = changeNote;
	}

	public Text getContent() {
		return content;
	}

	public void setContent(Text content) {
		this.content = content;
	}

	public void setContent(String text) {
		setContent(new Text(text));

	}

}