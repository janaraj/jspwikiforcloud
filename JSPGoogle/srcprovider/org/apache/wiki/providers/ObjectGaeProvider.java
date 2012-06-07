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
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wiki.IObjectPersist;
import org.apache.wiki.NoRequiredPropertyException;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.providers.jpa.WikiObject;

/**
 * Implementation of IObjectPersist plugin. The purpose is to set object data in
 * the data store , not as the files on the file system
 */

@SuppressWarnings("serial")
public class ObjectGaeProvider implements IObjectPersist {

	private final Log log = LogFactory.getLog(ObjectGaeProvider.class);

	@Override
	public void initialize(WikiEngine engine, Properties properties)
			throws NoRequiredPropertyException, IOException {
	}

	@Override
	public String getProviderInfo() {
		return this.getClass().getSimpleName();
	}

	/**
	 * Helper for ECommand. Keeps pDir and pName parameters.
	 * 
	 * @author stanislawbartkowski@gmail.com
	 * 
	 */
	private abstract class EObject extends ECommand {

		/** pDir parameter. */
		protected final String pDir;
		/** pName parameter. */
		protected final String pName;

		/**
		 * Constructor
		 * 
		 * @param pDir
		 *            pDir (can be null)
		 * @param pName
		 *            pName data parameter
		 */
		EObject(boolean transact, String pDir, String pName) {
			super(transact);
			this.pDir = pDir;
			this.pName = pName;
		}

		/**
		 * Gets WikiObject related to pDir and pName
		 * 
		 * @param eF
		 *            EntityManager
		 * @return WikiObject (or null if not exists)
		 */
		protected WikiObject getW(EntityManager eF) {
			Query q = ECommand.getQuery(eF, "GetObject", pDir, pName);
			Object o = null;
			try {
				o = q.getSingleResult();
			} catch (NoResultException e) {
				// expected
			}
			return (WikiObject) o;
		}
	}

	/**
	 * Gets WikiObject and bytes
	 * 
	 * @author stanislawbartkowski@gmail.com
	 * 
	 */
	private class GetObject extends EObject {

		/** output parameter. */
		ByteArrayInputStream b = null;

		GetObject(String pDir, String pName) {
			super(false, pDir, pName);
		}

		@Override
		protected void runCommand(EntityManager eF) {
			// Get object
			WikiObject w = getW(eF);
			if (w == null) {
				// if not exist then set b as null
				return;
			}
			// set b ByteArrayInputStream filled with bytes read
			b = new ByteArrayInputStream(w.getObject().getBytes());
		}
	}

	@Override
	public ObjectInputStream constructInput(String pDir, String pName) {

		// Get object from datastore
		GetObject o = new GetObject(pDir, pName);
		o.runCommand();
		if (o.b == null) {
			// if not exist return null
			return null;
		}
		try {
			// if exist returns ObjectInputStream
			return new ObjectInputStream(o.b);
		} catch (IOException e) {
			log.error(e);
			return null;
		}
	}

	/**
	 * Implements of ObjectOutputStream.
	 * <p>
	 * Purpose: Override close method for saving bytes in the datastore.
	 * </p>
	 * 
	 * @author stanislawbartkowski@gmail.com
	 * 
	 */
	private class BlobOutputStream extends ObjectOutputStream {

		private final String pDir;
		private final String pName;
		private final ByteArrayOutputStream o;

		BlobOutputStream(ByteArrayOutputStream o, String pDir, String pName)
				throws IOException {
			super(o);
			this.pDir = pDir;
			this.pName = pName;
			this.o = o;
		}

		@Override
		public void close() throws IOException {
			super.close();
			// o contains all bytes. Save bytes in the datastore.
			new SaveObject(pDir, pName, o).runCommand();
		}
	}

	/**
	 * Command to persist bytes related to pDir and pName
	 * 
	 * @author stanislawbartkowski@gmail.com
	 * 
	 */
	private class SaveObject extends EObject {
		private final ByteArrayOutputStream o;

		SaveObject(String pDir, String pName, ByteArrayOutputStream o) {
			super(true, pDir, pName);
			this.o = o;
		}

		@Override
		protected void runCommand(EntityManager eF) {
			WikiObject w = getW(eF);
			if (w == null) {
				// if not exists then create
				w = new WikiObject();
				w.setpDir(pDir);
				w.setpName(pName);
			}
			w.setBytes(o.toByteArray());
			// the same command for inserting new record and modyfying existing
			eF.persist(w);
		}
	}

	/**
	 * Command to delete bytes related to pDir and pName if exists
	 * 
	 * @author stanislawbartkowski@gmail.com
	 * 
	 */

	private class RemoveObject extends EObject {

		RemoveObject(String pDir, String pName) {
			super(true, pDir, pName);
		}

		@Override
		protected void runCommand(EntityManager eF) {
			WikiObject w = getW(eF);
			if (w == null) {
				// if not exists do nothing
				return;
			}
			// remove
			eF.remove(w);
		}

	}

	@Override
	public ObjectOutputStream constructOutput(String pDir, String pName,
			boolean delete) {
		if (delete) {
			new RemoveObject(pDir, pName).runCommand();
			return null;
		}
		try {
			return new BlobOutputStream(new ByteArrayOutputStream(), pDir,
					pName);
		} catch (IOException e) {
			log.error(e);
			return null;
		}
	}

}
