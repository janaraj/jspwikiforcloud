package org.apache.wiki.providers.jpa;

import java.io.Serializable;
import java.util.Date;

public class GroupMember implements Serializable {
	
	private String memberName;
	
	private Date addDate;

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	public Date getAddDate() {
		return addDate;
	}

	public void setAddDate(Date addDate) {
		this.addDate = addDate;
	}		
}

