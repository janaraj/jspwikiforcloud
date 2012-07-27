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

import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.wiki.auth.NoSuchPrincipalException;
import org.apache.wiki.auth.WikiPrincipal;
import org.apache.wiki.auth.WikiSecurityException;
import org.apache.wiki.auth.user.AbstractUserDatabase;
import org.apache.wiki.auth.user.DuplicateUserException;
import org.apache.wiki.auth.user.UserProfile;
import org.apache.wiki.providers.jpa.UserEntBlob;
import org.apache.wiki.util.Serializer;

public class WikiBlobUserDatabase extends AbstractUserDatabase {

    private static class User implements Serializable {

        private String loginName;

        private String fullName;

        private String email;

        private String password;

        private String wikiName;

        private String uId;

        private Date created;

        private Date modified;

        private Date LockExpiry;

        private String sAttributes;

        String getLoginName() {
            return loginName;
        }

        void setLoginName(String loginName) {
            this.loginName = loginName;
        }

        String getFullName() {
            return fullName;
        }

        void setFullName(String fullName) {
            this.fullName = fullName;
        }

        String getEmail() {
            return email;
        }

        void setEmail(String email) {
            this.email = email;
        }

        String getPassword() {
            return password;
        }

        void setPassword(String password) {
            this.password = password;
        }

        String getWikiName() {
            return wikiName;
        }

        void setWikiName(String wikiName) {
            this.wikiName = wikiName;
        }

        Date getCreated() {
            return created;
        }

        void setCreated(Date created) {
            this.created = created;
        }

        Date getModified() {
            return modified;
        }

        void setModified(Date modified) {
            this.modified = modified;
        }

        String getuId() {
            return uId;
        }

        void setuId(String uId) {
            this.uId = uId;
        }

        public Map<String, Serializable> getAttributes() throws IOException {
            Map<String, Serializable> hMap = new HashMap<String, Serializable>();
            if (sAttributes != null) {
                hMap = (Map<String, Serializable>) Serializer
                        .deserializeFromBase64(sAttributes);
            }
            return hMap;
        }

        public void setAttributes(Map<String, Serializable> attributes)
                throws IOException {
            if (attributes == null || attributes.isEmpty()) {
                sAttributes = null;
                return;
            }
            String s = Serializer.serializeToBase64(attributes);
            sAttributes = s;
        }

        public Date getLockExpiry() {
            return LockExpiry;
        }

        public void setLockExpiry(Date lockExpiry) {
            LockExpiry = lockExpiry;
        }
    }

    private Collection<User> toUsers(UserEntBlob u)
            throws WikiSecurityException {
        return BlobUtil.toObjects(u);
    }

    private void toBlobUser(UserEntBlob u, Collection<User> uList)
            throws WikiSecurityException {
        BlobUtil.toBlob(u, uList);
    }

    enum FindMethod {
        LOGINNAME, UID, EMAIL, FULLNAME, WIKINAME;
    };

    private abstract class GetUser extends ECommand {

        private UserEntBlob uEnt;
        protected Collection<User> uList = null;
        protected User u;

        protected GetUser(boolean transact) {
            super(transact);
        }

        protected void readUser(EntityManager eF) throws WikiSecurityException {
            if (eF == null) {
                return;
            }
            uEnt = ECommand.getSingleObject(eF, "GetUser");
            uList = toUsers(uEnt);
        }

        protected void saveUser(EntityManager eF) throws WikiSecurityException {
            if (uEnt == null) {
                uEnt = new UserEntBlob();
            }
            toBlobUser(uEnt, uList);
            eF.persist(uEnt);
        }

        protected void findByName(EntityManager eF, FindMethod m, String s)
                throws WikiSecurityException {
            readUser(eF);
            for (User us : uList) {
                String val = null;
                switch (m) {
                case LOGINNAME:
                    val = us.getLoginName();
                    break;
                case UID:
                    val = us.getuId();
                    break;
                case EMAIL:
                    val = us.getEmail();
                    break;
                case FULLNAME:
                    val = us.getFullName();
                    break;
                case WIKINAME:
                    val = us.getWikiName();
                    break;
                }
                if (val != null && (val.toUpperCase().equals(s.toUpperCase()))) {
                    u = us;
                    break;
                }
            }
        }

        protected UserProfile toUserProfile(User u)
                throws WikiSecurityException {
            UserProfile user = newProfile();
            user.setCreated(u.getCreated());
            user.setEmail(u.getEmail());
            user.setFullname(u.getFullName());
            user.setLastModified(u.getModified());
            user.setLoginName(u.getLoginName());
            user.setPassword(u.getPassword());
            user.setUid(u.getuId());
            user.setWikiName(u.getWikiName());
            user.setLockExpiry(u.getLockExpiry());
            try {
                user.getAttributes().putAll(u.getAttributes());
            } catch (IOException e) {
                log.error("Deserializable attributes", e);
                throw new WikiSecurityException(e.getMessage());
            }
            return user;
        }

        protected void toUser(User e, UserProfile profile)
                throws WikiSecurityException {
            try {
                e.setAttributes(profile.getAttributes());
            } catch (IOException e1) {
                log.error("Serializable attributes", e1);
                throw new WikiSecurityException(e1.getMessage());
            }
            e.setEmail(profile.getEmail());
            e.setFullName(profile.getFullname());
            e.setLockExpiry(profile.getLockExpiry());
            e.setModified(getToday());
            // do not change password
            if (profile.getPassword() != null) {
              String cryptedPassword = getHash(profile.getPassword());
              e.setPassword(cryptedPassword);
              log.trace("User: " + profile.getLoginName() + ". Password has been changed.");
            } else {
                log.trace("User: " + profile.getLoginName() + ". Modify profile without password chaning.");
            }
            e.setuId(profile.getUid());
            e.setWikiName(profile.getWikiName());
        }

    }

    private class DeleteByLoginName extends GetUser {

        private final String loginName;

        DeleteByLoginName(String loginName) {
            super(true);
            this.loginName = loginName;
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            findByName(eF, FindMethod.LOGINNAME, loginName);
            if (u != null) {
                uList.remove(u);
            }
            saveUser(eF);
        }

    }

    @Override
    public void deleteByLoginName(String loginName)
            throws NoSuchPrincipalException, WikiSecurityException {
        DeleteByLoginName dCommand = new DeleteByLoginName(loginName);
        dCommand.runCommand();
    }

    private class GetAll extends GetUser {

        protected GetAll() {
            super(false);
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            readUser(eF);
        }

        Collection<User> getUList() {
            return uList;
        }

    }

    @Override
    public Principal[] getWikiNames() throws WikiSecurityException {
        GetAll command = new GetAll();
        command.runCommand();
        Set<Principal> principals = new HashSet<Principal>();
        for (User u : command.getUList()) {
            Principal principal = new WikiPrincipal(u.getWikiName(),
                    WikiPrincipal.WIKI_NAME);
            principals.add(principal);
        }
        return principals.toArray(new Principal[principals.size()]);
    }

    private class FindUser extends GetUser {

        protected final String s;
        private UserProfile user;
        protected final FindMethod m;
        boolean notFound = false;

        FindUser(String s, FindMethod m) {
            super(false);
            this.s = s;
            this.m = m;
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            findByName(eF, m, s);
            if (u == null) {
                // throw new NoSuchPrincipalException(s);
                notFound = true;
                return;
            }
            user = toUserProfile(u);
        }

        UserProfile getUser() {
            return user;
        }

    }

    @Override
    public UserProfile findByUid(String uid) throws NoSuchPrincipalException {
        return findUser(uid, FindMethod.UID);
    }

    class RenameCommand extends GetUser {
        private final String loginName;
        private final String newName;
        private boolean duplicatedError = false;

        RenameCommand(String loginName, String newName) {
            super(true);
            this.loginName = loginName;
            this.newName = newName;
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            findByName(eF, FindMethod.LOGINNAME, newName);
            if (u != null) {
                duplicatedError = true;
                return;
            }
            findByName(null, FindMethod.LOGINNAME, loginName);
            if (u == null) {
                throw new NoSuchPrincipalException(loginName);
            }
            u.setLoginName(newName);
            saveUser(eF);
        }

    }

    @Override
    public void rename(String loginName, String newName)
            throws NoSuchPrincipalException, DuplicateUserException,
            WikiSecurityException {

        RenameCommand command = new RenameCommand(loginName, newName);
        command.runCommand();
        if (command.duplicatedError) {
            throw new DuplicateUserException(newName);
        }
    }

    private UserProfile findUser(String index, FindMethod m)
            throws NoSuchPrincipalException {
        FindUser command = new FindUser(index, m);
        command.runCommand();
        if (command.notFound) {
            throw new NoSuchPrincipalException(index);
        }
        return command.getUser();

    }

    @Override
    public UserProfile findByEmail(String index)
            throws NoSuchPrincipalException {
        return findUser(index, FindMethod.EMAIL);
    }

    @Override
    public UserProfile findByFullName(String index)
            throws NoSuchPrincipalException {
        return findUser(index, FindMethod.FULLNAME);
    }

    @Override
    public UserProfile findByLoginName(String index)
            throws NoSuchPrincipalException {
        return findUser(index, FindMethod.LOGINNAME);
    }

    @Override
    public UserProfile findByWikiName(String index)
            throws NoSuchPrincipalException {
        return findUser(index, FindMethod.WIKINAME);
    }

    private class SaveUser extends GetUser {
        private final UserProfile profile;

        SaveUser(UserProfile profile) {
            super(true);
            this.profile = profile;
        }

        @Override
        protected void runCommand(EntityManager eF)
                throws WikiSecurityException {
            findByName(eF, FindMethod.LOGINNAME, profile.getLoginName());
            if (u == null) {
                u = new User();
                u.setCreated(getToday());
                u.setLoginName(profile.getLoginName());
                uList.add(u);
            }
            toUser(u, profile);
            saveUser(eF);
        }
    }

    @Override
    public void save(UserProfile profile) throws WikiSecurityException {
        SaveUser command = new SaveUser(profile);
        command.runCommand();
    }

}
