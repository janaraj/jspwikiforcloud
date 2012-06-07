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

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.apache.commons.logging.Log; import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.LogFactory;
import org.apache.wiki.providers.jpa.EMF;

/**
 * Abstract class designed to enclose all commands related to Google App Engine
 * JPA
 * 
 * @author stanislawbartkowski@gmail.com
 * 
 */
abstract class ECommand {

	private final Log log = LogFactory.getLog(ECommand.class);
	private final boolean transact;

	protected ECommand(boolean transact) {
		this.transact = transact;
	}

	protected abstract void runCommand(EntityManager eF);

	/**
	 * Encloses command. Simple gets EntityManager, run command and close.
	 */
	private void prunCommand(boolean transact) {
		EntityManager eF = EMF.getF();
		EntityTransaction tran = null;
		if (transact) {
			tran = eF.getTransaction();
			tran.begin();
		}
		try {
			runCommand(eF);
			if (tran != null) {
				tran.commit();
			}
		} catch (Exception e) {
			log.fatal("JPA command", e);
			if (tran != null) {
				tran.rollback();
			}
		} finally {
			eF.close();
		}
	}

	void runCommand() {
		prunCommand(transact);
	}

	protected Date getToday() {
		return new Date();
	}

	/**
	 * Helper for getting NamedQuery and setting string parameters
	 * 
	 * @param eF
	 *            EntityManager
	 * @param namedQuery
	 *            NamedQuery names
	 * @param Params
	 *            Parameters (if any)
	 * @return Query with parameters set (if exist)
	 */
	static Query getQuery(EntityManager eF, String namedQuery, String... Params) {
		Query query = eF.createNamedQuery(namedQuery);
		int i = 1;
		for (String param : Params) {
			query.setParameter(i++, param);
		}
		return query;
	}

}
