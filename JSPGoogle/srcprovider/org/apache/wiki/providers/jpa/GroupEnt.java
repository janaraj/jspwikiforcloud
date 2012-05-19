package org.apache.wiki.providers.jpa;

import java.util.Collection;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.google.appengine.api.datastore.Key;

@Entity
@NamedQueries({
	@NamedQuery(name = "FindGroup", query = "SELECT P FROM GroupEnt P WHERE P.name= :1"),
	@NamedQuery(name = "AllGroups", query = "SELECT P FROM GroupEnt P")
})

public class GroupEnt {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key key;
	
	@Basic(optional=false)
	private String name;
	
	@Basic
	private String creator;
	
	@Basic(optional=false)
	private Date created;
	
	@Basic
	private String modifier;
	
	@Basic(optional=false)
	private Date modified;

	@Lob
	private Collection<GroupMember> memberList;
	
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

	public Collection<GroupMember> getMemberList() {
		return memberList;
	}

	public void setMemberList(Collection<GroupMember> memberList) {
		this.memberList = memberList;
	}


}
