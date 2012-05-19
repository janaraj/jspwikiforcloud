package org.apache.wiki.providers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.apache.wiki.NoRequiredPropertyException;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.auth.WikiPrincipal;
import org.apache.wiki.auth.WikiSecurityException;
import org.apache.wiki.auth.authorize.Group;
import org.apache.wiki.auth.authorize.GroupDatabase;
import org.apache.wiki.providers.jpa.GroupEnt;
import org.apache.wiki.providers.jpa.GroupMember;

public class WikiGaeGroupDatabase implements GroupDatabase {

	private final Logger log = Logger.getLogger(WikiGaeGroupDatabase.class);

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

	@Override
	public void initialize(WikiEngine engine, Properties props)
			throws NoRequiredPropertyException, WikiSecurityException {
		wikiName = engine.getApplicationName();

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
			GroupEnt ge = findGroup(eF);
			if (ge == null) {
				ge = new GroupEnt();
				ge.setCreated(getToday());
				ge.setCreator(modifier.getName());
				ge.setName(g.getName());
			}
			ge.setModifier(modifier.getName());
			ge.setModified(getToday());
			Collection<GroupMember> mList = new ArrayList<GroupMember>();
			for (Principal p : g.members()) {
				GroupMember me = new GroupMember();
				me.setMemberName(p.getName());
				mList.add(me);
			}
			ge.setMemberList(mList);
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
			if (g.getMemberList() != null)
				for (GroupMember me : g.getMemberList()) {
					Principal member = new WikiPrincipal(me.getMemberName());
					gr.add(member);
				}
			gList[i++] = gr;
		}
		return gList;
	}
}
