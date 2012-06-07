package org.apache.wiki.security;

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

import java.io.Serializable;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WikiSubject implements Serializable {

	private Set<Principal> principals = new HashSet<Principal>();
	private static final Log log = LogFactory.getLog(WikiSubject.class);
	private int no = 0;

	@Override
	public String toString() {
		String s = "WikiSubject (+ " + no + ") : ";
		Iterator<Principal> i = principals.iterator();
		while (i.hasNext()) {
			s += i.next() + " ";
		}
		return s;
	}

	public WikiSubject() {
		log.debug("Construct WikiSubject");
	}

	public Set<Principal> getPrincipals() {
		no++;
		return principals;
	}

	public <T extends Principal> Set<T> getPrincipals(Class<T> c) {

		if (c == null)
			throw new NullPointerException("invalid null Class provided");

		// always return an empty Set instead of null
		// so LoginModules can add to the Set if necessary
		Iterator<Principal> ite = principals.iterator();
		Set<T> outSet = new HashSet<T>();
		while (ite.hasNext()) {
			Principal ip = ite.next();
			if (c.isAssignableFrom(ip.getClass())) {
				outSet.add((T) ip);
			}
		}
		return outSet;
	}

	public static <T> T doAsPrivileged(final WikiSubject subject,
			final PrivilegedAction<T> action) {
		// Subject sub = new Subject(false,subject.getPrincipals(),new
		// HashSet<Principal>(),new HashSet<Principal>());
		// return Subject.doAsPrivileged(sub, action, null);
		return action.run();
	}

}
