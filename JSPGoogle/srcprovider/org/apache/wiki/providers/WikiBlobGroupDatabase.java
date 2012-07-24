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
import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.persistence.EntityManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wiki.AbstractWikiProvider;
import org.apache.wiki.auth.WikiPrincipal;
import org.apache.wiki.auth.WikiSecurityException;
import org.apache.wiki.auth.authorize.Group;
import org.apache.wiki.auth.authorize.GroupDatabase;
import org.apache.wiki.providers.jpa.GroupEntBlob;
import org.apache.wiki.providers.jpa.GroupMember;
import org.apache.wiki.spring.BeanHolder;

public class WikiBlobGroupDatabase extends AbstractWikiProvider implements
        GroupDatabase {

    private final Log log = LogFactory.getLog(WikiBlobGroupDatabase.class);

    private static class OneGroup implements Serializable {

        private String name;

        private String creator;

        private Date created;

        private String modifier;

        private Date modified;

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

    private Collection<OneGroup> toGroups(GroupEntBlob g)
            throws WikiSecurityException {
        return BlobUtil.toObjects(g);
    }

    private void toGroupBlob(GroupEntBlob g, Collection<OneGroup> cList)
            throws WikiSecurityException {
        BlobUtil.toBlob(g, cList);
    }

    private abstract class SaveOrDeleteCommand extends ECommand {
        protected final Group gr;
        protected GroupEntBlob gEnt;
        protected Collection<OneGroup> cList;
        protected OneGroup fGr;

        SaveOrDeleteCommand(Group gr) {
            super(true);
            this.gr = gr;
        }

        protected void findGroup(EntityManager eF) throws WikiSecurityException {
            gEnt = ECommand.getSingleObject(eF, "GetGroup");
            cList = toGroups(gEnt);
            fGr = null;
            for (OneGroup o : cList) {
                if (gr.getName().equals(o.getName())) {
                    fGr = o;
                    break;
                }
            }
        }

        protected void saveGroup(EntityManager eF) throws WikiSecurityException {
            if (gEnt == null) {
                gEnt = new GroupEntBlob();
            }
            toGroupBlob(gEnt, cList);
            eF.persist(gEnt);
        }

    }

    private class DeleteGroup extends SaveOrDeleteCommand {

        private DeleteGroup(Group gr) {
            super(gr);
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            findGroup(eF);
            if (fGr != null) {
                cList.remove(fGr);
            }
            saveGroup(eF);
        }
    }

    @Override
    public void delete(Group group) throws WikiSecurityException {
        DeleteGroup dCommand = new DeleteGroup(group);
        dCommand.runCommand();
    }

    public class SaveGroup extends SaveOrDeleteCommand {

        private final Principal modifier;

        protected SaveGroup(Group group, Principal modifier) {
            super(group);
            this.modifier = modifier;
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            findGroup(eF);
            Collection<GroupMember> mem;
            if (fGr == null) {
                mem = new ArrayList<GroupMember>();
                fGr = new OneGroup();
                fGr.setCreated(getToday());
                fGr.setCreator(modifier.getName());
                fGr.setName(gr.getName());
                fGr.setMemberList(mem);
                cList.add(fGr);
            } else {
                mem = fGr.getMemberList();
            }
            fGr.setModifier(modifier.getName());
            fGr.setModified(getToday());
            for (Principal p : gr.members()) {
                String na = p.getName();
                boolean found = false;
                for (GroupMember g : mem) {
                    if (g.getMemberName().equals(na)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    GroupMember me = new GroupMember();
                    me.setMemberName(p.getName());
                    me.setAddDate(getToday());
                    mem.add(me);
                }
            } // for
            saveGroup(eF);
        }
    }

    @Override
    public void save(Group group, Principal modifier)
            throws WikiSecurityException {
        SaveGroup saveCommand = new SaveGroup(group, modifier);
        saveCommand.runCommand();
    }

    private class GetGroup extends ECommand {

        private GroupEntBlob g;

        protected GetGroup() {
            super(false);
        }

        public GroupEntBlob getG() {
            return g;
        }

        @Override
        protected void runCommand(EntityManager eF) {
            g = ECommand.getSingleObject(eF, "GetGroup");
        }

    }

    @Override
    public Group[] groups() throws WikiSecurityException {
        GetGroup gCommand = new GetGroup();
        gCommand.runCommand();
        Collection<OneGroup> c = toGroups(gCommand.getG());
        Group[] list = new Group[c.size()];
        int i = 0;
        for (OneGroup g : c) {
            Group gr = new Group(g.getName(), BeanHolder.getWikiName());
            gr.setCreated(g.getCreated());
            gr.setCreator(g.getCreator());
            gr.setLastModified(g.getModified());
            gr.setModifier(g.getModifier());
            Collection<GroupMember> mem = g.getMemberList();
            for (GroupMember me : mem) {
                Principal member = new WikiPrincipal(me.getMemberName());
                gr.add(member);
            }
            list[i++] = gr;
        }
        return list;
    }

}
