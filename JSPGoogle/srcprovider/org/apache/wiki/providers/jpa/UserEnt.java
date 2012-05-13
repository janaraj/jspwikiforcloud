package org.apache.wiki.providers.jpa;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import com.google.appengine.api.datastore.Key;

//create table @jspwiki.userdatabase.table@ (
//		  @jspwiki.userdatabase.email@ varchar(100),
//		  @jspwiki.userdatabase.fullName@ varchar(100),
//		  @jspwiki.userdatabase.loginName@ varchar(100) not null primary key,
//		  @jspwiki.userdatabase.password@ varchar(100),
//		  @jspwiki.userdatabase.wikiName@ varchar(100),
//		  @jspwiki.userdatabase.created@ timestamp,
//		  @jspwiki.userdatabase.modified@ timestamp
//		);
//
//		create table @jspwiki.userdatabase.roleTable@ (
//		  @jspwiki.userdatabase.loginName@ varchar(100) not null,
//		  @jspwiki.userdatabase.role@ varchar(100) not null
//		);
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
	private Map<String, Serializable> attributes;

	@OneToMany(mappedBy = "user")
	private Collection<RoleEnt> roleList;

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

	public Collection<RoleEnt> getRoleList() {
		return roleList;
	}

	public void setRoleList(Collection<RoleEnt> roleList) {
		this.roleList = roleList;
	}

	public String getuId() {
		return uId;
	}

	public void setuId(String uId) {
		this.uId = uId;
	}

	public Map<String, Serializable> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Serializable> attributes) {
		this.attributes = attributes;
	}

	public Date getLockExpiry() {
		return LockExpiry;
	}

	public void setLockExpiry(Date lockExpiry) {
		LockExpiry = lockExpiry;
	}
	
}
