package org.apache.wiki.providers;

import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.wiki.NoRequiredPropertyException;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.auth.NoSuchPrincipalException;
import org.apache.wiki.auth.WikiPrincipal;
import org.apache.wiki.auth.WikiSecurityException;
import org.apache.wiki.auth.user.AbstractUserDatabase;
import org.apache.wiki.auth.user.DuplicateUserException;
import org.apache.wiki.auth.user.UserProfile;
import org.apache.wiki.providers.jpa.UserEnt;

public class WikiGaeUserDatabase extends AbstractUserDatabase {

	private abstract class FindUser extends ECommand {

		private final String searchName;
		private final String queryName;

		FindUser(boolean transact,String queryName , String searchName) {
			super(transact);
			this.queryName = queryName;
			this.searchName = searchName;
		}

		protected UserEnt findUser(EntityManager eF) {
			log.debug("Find user: " + queryName + " " + searchName);
			Query query = getQuery(eF, queryName, searchName);
			try {
				UserEnt g = (UserEnt) query.getSingleResult();
				return g;
			} catch (NoResultException e) {
				return null;
			}
		}

	}
	
	private abstract class FindUserByLoginName extends FindUser {
		
		FindUserByLoginName(boolean transact,String loginName) {
			super(transact,"FindUserByLoginName",loginName);
		}
		
	}
	
	private class DeleteUserByLoginName extends FindUserByLoginName {
		
		DeleteUserByLoginName(String loginName) {
			super(true,loginName);
		}

		@Override
		protected void runCommand(EntityManager eF) {
			UserEnt e = findUser(eF);
			if (e != null) {
				eF.remove(e);
			}			
		}
	}
	
	
	@Override
	public void deleteByLoginName(String loginName)
			throws NoSuchPrincipalException, WikiSecurityException {
		DeleteUserByLoginName command = new DeleteUserByLoginName(loginName);
		command.runCommand();		
	}
	
	private class ReadAll extends ECommand {
		
		Collection<UserEnt> uList;
		
		ReadAll() {
			super(false);
		}

		@Override
		protected void runCommand(EntityManager eF) {
			Query q = getQuery(eF,"AllUsers");
			uList = q.getResultList();
		}
	}
	

	@Override
	public Principal[] getWikiNames() throws WikiSecurityException {
		ReadAll command = new ReadAll();
        Set<Principal> principals = new HashSet<Principal>();
        for (UserEnt u : command.uList) {
        	Principal principal = new WikiPrincipal( u.getWikiName(), WikiPrincipal.WIKI_NAME );
            principals.add( principal );
        }
        return principals.toArray( new Principal[principals.size()] );
	}
	
	
	private class FindUserByVal extends FindUser {
		
		UserEnt u;

		FindUserByVal(String queryName, String searchName) {
			super(false, queryName, searchName);
		}

		@Override
		protected void runCommand(EntityManager eF) {
			u = findUser(eF);
		}
		
	}
	
	
	private UserProfile findUser(String queryName, String val) throws NoSuchPrincipalException {
		FindUserByVal command = new FindUserByVal(queryName,val);
		command.runCommand();
		if (command.u == null) { throw new NoSuchPrincipalException(val); }
		UserProfile user = newProfile();
		user.setCreated(command.u.getCreated());
		user.setEmail(command.u.getEmail());
		user.setFullname(command.u.getFullName());
		user.setLastModified(command.u.getModified());
		user.setLoginName(command.u.getLoginName());
		user.setPassword(command.u.getPassword());
		user.setUid(command.u.getuId());
		user.setWikiName(command.u.getWikiName());
		user.setLockExpiry(command.u.getLockExpiry());
        user.getAttributes().putAll( command.u.getAttributes());
		return user;
	}

	@Override
	public UserProfile findByUid(String uid) throws NoSuchPrincipalException {
		return findUser("FindUserByUid", uid);
	}
	
	private class RenameCommand extends FindUserByLoginName {
		
		private final String newName;
		
		RenameCommand(String loginName, String newName) {
			super(true, loginName);
			this.newName = newName;
		}

		@Override
		protected void runCommand(EntityManager eF) {
			UserEnt u = findUser(eF);
			if (u != null) {
				u.setLoginName(newName);
				eF.persist(u);
			}
			
		}
	}
	
	private class FindName extends FindUserByLoginName {

		UserEnt u;
		
		FindName(String newName) {
			super(false,newName);
		}

		@Override
		protected void runCommand(EntityManager eF) {
			u = findUser(eF);			
		}
	}
	

	@Override
	public void rename(String loginName, String newName)
			throws NoSuchPrincipalException, DuplicateUserException,
			WikiSecurityException {
		FindName findCommand = new FindName(newName);
		findCommand.runCommand();
		if (findCommand.u != null) { throw new DuplicateUserException(newName); }
		RenameCommand renameCommand = new RenameCommand(loginName,newName);
		renameCommand.runCommand();
	}

	@Override
	public UserProfile findByEmail(String index)
			throws NoSuchPrincipalException {
		return findUser("FindUserByeMail", index);
	}

	@Override
	public UserProfile findByFullName(String index)
			throws NoSuchPrincipalException {
		return findUser("FindUserByFullName", index);
	}

	@Override
	public UserProfile findByLoginName(String index)
			throws NoSuchPrincipalException {
		return findUser("FindUserByLoginName", index);
	}

	@Override
	public UserProfile findByWikiName(String index)
			throws NoSuchPrincipalException {
		return findUser("FindUserByWikiName", index);
	}

	@Override
	public void initialize(WikiEngine engine, Properties props)
			throws NoRequiredPropertyException {
		// TODO Auto-generated method stub
		
	}
	
	private class SaveCommand extends FindUserByLoginName {
		
		private final UserProfile profile;
		
		SaveCommand(UserProfile profile) {
			super(true,profile.getLoginName());
			this.profile = profile;
		}

		@Override
		protected void runCommand(EntityManager eF) {
			UserEnt e = findUser(eF);
			if (e == null) {
				e = new UserEnt();
				e.setCreated(getToday());
				e.setLoginName(profile.getLoginName());
			}
			e.setAttributes(profile.getAttributes());
			e.setEmail(profile.getEmail());
			e.setFullName(profile.getFullname());
			e.setLockExpiry(profile.getLockExpiry());
			e.setModified(getToday());
			e.setPassword(profile.getPassword());
			e.setuId(profile.getUid());
			e.setWikiName(profile.getWikiName());
			eF.persist(e);
		}
	}

	@Override
	public void save(UserProfile profile) throws WikiSecurityException {
		SaveCommand saveCommand = new SaveCommand(profile);
		saveCommand.runCommand();		
	}

}
