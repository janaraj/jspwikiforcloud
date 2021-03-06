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
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wiki.auth.WikiSecurityException;
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
    private EntityTransaction tran;

    protected ECommand(boolean transact) {
        this.transact = transact;
    }

    protected abstract void runCommand(EntityManager eF) throws WikiSecurityException;

    /**
     * Encloses command. Simple gets EntityManager, run command and close.
     * @throws ProviderException 
     */
    private void prunCommand(boolean transact) throws ProviderException {
        EntityManager eF = EMF.getF();
        tran = null;
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
            throw new ProviderException(e.getMessage()); 
        } finally {
            eF.close();
        }
    }
    
    protected void commit() {
        if (tran != null) {
            tran.commit();
            tran.begin();            
        }
        
    }

    void runCommand() throws ProviderException {
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
    static Query getQuery(EntityManager eF, String namedQuery, Object... Params) {
        Query query = eF.createNamedQuery(namedQuery);
        int i = 1;
        for (Object param : Params) {
            query.setParameter(i++, param);
        }
        return query;
    }

    static <T> T getSingleObject(EntityManager eF, String namedQuery,
            Object... Params) {
        Query q = getQuery(eF, namedQuery, Params);
        T o = null;
        try {
            o = (T) q.getSingleResult();
        } catch (NoResultException e) {
            // expected
        }
        return (T) o;
    }

}
