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
package org.apache.commons.logging;

/**
 * Stub replacement for org.apache.commons.logging
 * <p>
 * Implementation is based on stub for org.apache.log4j to avoid code
 * duplication
 * </p>
 * Only subset of functionality is implemented, methods used by the application
 * 
 * @author stanislawbartkowski@gmail.com
 * 
 */

public class LogFactory {

	/**
	 * Implementation of Log interface
	 * 
	 * @author stanislawbartkowski@gmail.com
	 * 
	 */
	@SuppressWarnings("serial")
	private static class L implements Log {

		/** Implementation is based on stub of final org.apache.log4j.Logger. */
		private final org.apache.log4j.Logger log;

		L(org.apache.log4j.Logger log) {
			this.log = log;
		}

		@Override
		public boolean isDebugEnabled() {
			// TODO: implement
			return true;
		}

		@Override
		public void debug(Object message) {
			log.debug(message);
		}

		@Override
		public void error(Object message, Throwable t) {
			log.error(message, t);
		}

		@Override
		public void error(Object message) {
			log.error(message);
		}

		@Override
		public boolean isErrorEnabled() {
			// TODO: implement
			return true;
		}

		@Override
		public void warn(Object message) {
			log.warn(message);
		}

		@Override
		public void info(Object message) {
			log.info(message);
		}

		@Override
		public void warn(Object message, Throwable t) {
			log.warn(message, t);
		}

		@Override
		public boolean isInfoEnabled() {
			// TODO: implement
			return true;
		}

	};

	public static Log getLog(java.lang.Class clazz) {
		return new L(org.apache.log4j.Logger.getLogger(clazz));
	}

}
