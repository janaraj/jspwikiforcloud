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
package org.apache.wiki.providers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wiki.AbstractWikiProvider;
import org.apache.wiki.auth.WikiPrincipal;
import org.apache.wiki.auth.WikiSecurityException;
import org.apache.wiki.auth.authorize.Group;
import org.apache.wiki.auth.authorize.GroupDatabase;
import org.apache.wiki.providers.jpa.GroupEnt;
import org.apache.wiki.providers.jpa.GroupMember;

public class WikiGaeGroupDatabase extends AbstractWikiProvider implements
		GroupDatabase {

	private final Log log = LogFactory.getLog(WikiGaeGroupDatabase.class);

	private String wikiName;

	private abstract class FindGroup extends ECommand {

		private final String groupName;

		FindGroup(boolean transact, String groupName) {
			super(transact);
			this.groupName = groupName;
		}

		protected GroupEnt findGroup(EntityManager eF) {
			log.debug("Find group: " + groupName);
			Query query = getQuery(eF, "FindGroup", groupName);
			try {
				GroupEnt g = (GroupEnt) query.getSingleResult();
				return g;
			} catch (NoResultException e) {
				return null;
			}
		}

	}

	private class DeleteCommand extends FindGroup {

		DeleteCommand(String groupName) {
			super(true, groupName);
		}

		@Override
		protected void runCommand(EntityManager eF) {
			GroupEnt g = findGroup(eF);
			if (g == null) {
				return;
			}
			eF.remove(g);
		}

	}

	@Override
	public void delete(Group group) throws WikiSecurityException {
		log.debug("Delete group " + group.getName());
		DeleteCommand command = new DeleteCommand(group.getName());
		command.runCommand();
	}

	@SuppressWarnings("unchecked")
	private Collection<GroupMember> getGroupMembers(GroupEnt g)
			throws IOException, ClassNotFoundException {
		ByteArrayInputStream in = new ByteArrayInputStream(g.getMemberList());
		ObjectInputStream i = new ObjectInputStream(in);
		Collection<GroupMember> c = (Collection<GroupMember>) i.readObject();
		return c;
	}

	private class SaveCommand extends FindGroup {

		private final Group g;
		private final Principal modifier;

		SaveCommand(Group g, Principal modifier) {
			super(true, g.getName());
			this.g = g;
			this.modifier = modifier;
		}

		@Override
		protected void runCommand(EntityManager eF) {
			try {
				prunCommand(eF);
			} catch (IOException e) {
				log.error(e);
			} catch (ClassNotFoundException e) {
				log.error(e);
			}
		}

		private void prunCommand(EntityManager eF) throws IOException,
				ClassNotFoundException {
			GroupEnt ge = findGroup(eF);
			Collection<GroupMember> c;
			if (ge == null) {
				ge = new GroupEnt();
				ge.setCreated(getToday());
				ge.setCreator(modifier.getName());
				ge.setName(g.getName());
				c = new ArrayList<GroupMember>();
			} else {
				c = getGroupMembers(ge);
			}
			ge.setModifier(modifier.getName());
			ge.setModified(getToday());
			for (Principal p : g.members()) {
				String na = p.getName();
				boolean found = false;
				for (GroupMember g : c) {
					if (g.getMemberName().equals(na)) {
						found = true;
						break;
					}
				}
				if (!found) {
					GroupMember me = new GroupMember();
					me.setMemberName(p.getName());
					me.setAddDate(getToday());
					c.add(me);
				}
			} // for
			ByteArrayOutputStream ou = new ByteArrayOutputStream();
			ObjectOutputStream o = new ObjectOutputStream(ou);
			o.writeObject(c);
			o.flush();
			ge.setMemberList(ou.toByteArray());
			eF.persist(ge);
		}

	}

	@Override
	public void save(Group group, Principal modifier)
			throws WikiSecurityException {
		if (group.getName() == null) {
			return;
		}
		log.debug("Save group " + group.getName());
		SaveCommand command = new SaveCommand(group, modifier);
		command.runCommand();
	}

	private class ReadGroup extends ECommand {

		ReadGroup() {
			super(false);
		}

		Collection<GroupEnt> g;

		@SuppressWarnings("unchecked")
		@Override
		protected void runCommand(EntityManager eF) {
			Query query = getQuery(eF, "AllGroups");
			g = query.getResultList();
			if (g != null) {
				// touch collection while it is open
				g.isEmpty();
			}
		}

	}

	@Override
	public Group[] groups() throws WikiSecurityException {
		ReadGroup command = new ReadGroup();
		command.runCommand();
		if (command.g.isEmpty()) {
			return new Group[0];
		}
		int no = 0;
		for (GroupEnt g : command.g) {
			if (g.getName() == null) {
				continue;
			}
			no++;
		}
		Group[] gList = new Group[no];
		int i = 0;
		for (GroupEnt g : command.g) {
			if (g.getName() == null) {
				continue;
			}
			Group gr = new Group(g.getName(), wikiName);
			gr.setCreated(g.getCreated());
			gr.setCreator(g.getCreator());
			gr.setLastModified(g.getModified());
			gr.setModifier(g.getModifier());
			if (g.getMemberList() != null) {
				Collection<GroupMember> c;
				try {
					c = getGroupMembers(g);
				} catch (IOException e) {
					throw new WikiSecurityException(
							"Cannot read group memebers", e);
				} catch (ClassNotFoundException e) {
					throw new WikiSecurityException(
							"Cannot read group memebers", e);
				}
				for (GroupMember me : c) {
					Principal member = new WikiPrincipal(me.getMemberName());
					gr.add(member);
				}
				gList[i++] = gr;
			}
		}
		return gList;
	}

	@Override
	public String getProviderInfo() {
		// TODO Auto-generated method stub
		return null;
	}
}
