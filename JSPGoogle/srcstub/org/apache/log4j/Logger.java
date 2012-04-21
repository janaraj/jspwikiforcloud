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
package org.apache.log4j;

import java.io.Serializable;

/**
 * Stub for org.apache.log4j
 * <p>
 * Reason: not supported by Google App Engine because of FileOutputStream class
 * and also should be Serializable
 * </p>
 * Implemented by means of standard java.util.logging package
 * 
 * @author stanislawbartkowski@gmail.com
 * 
 */

@SuppressWarnings("serial")
public class Logger implements Serializable {

	/** logName attached to every log message. */
	private final String logName;

	/**
	 * Get java.util.logging.Logger
	 * <p>
	 * Important: Logger is created any time message is sent. Cannot be used as
	 * a attribute in Logger class because it is not Serializable
	 * </p>
	 * 
	 * @return
	 */
	private java.util.logging.Logger getL() {
		return java.util.logging.Logger.getLogger(logName);
	}

	private Logger(String logName) {
		this.logName = logName;
	}

	public static Logger getLogger(Class clazz) {
		return getLogger(clazz.getName());
	}

	/**
	 * Construct Logger class
	 * 
	 * @param name
	 *            Name connected to logger
	 * @return Logger
	 */
	public static Logger getLogger(String name) {
		Logger l = new Logger(name);
		return l;
	}

	public void info(Object message) {
		getL().info(message.toString());
	}

	public void error(Object message) {
		getL().log(java.util.logging.Level.SEVERE, message.toString());
	}

	public void error(Object message, Throwable t) {
		getL().log(java.util.logging.Level.SEVERE, message.toString(), t);
	}

	public void debug(Object message, Throwable t) {
		getL().log(java.util.logging.Level.FINEST,
				logName + " : " + message.toString(), t);
	}

	public void debug(Object message) {
		getL().log(java.util.logging.Level.FINEST,
				logName + " : " + message.toString());
	}

	public void fatal(Object message) {
		error(message);
	}

	public void fatal(Object message, Throwable t) {
		error(message, t);
	}

	public void info(Object message, Throwable t) {
		getL().log(java.util.logging.Level.INFO, message.toString(), t);
	}

	public boolean isInfoEnabled() {
		// TODO: implement
		return false;
	}

	public boolean isDebugEnabled() {
		// TODO: implement
		return false;
	}

	public void warn(Object message) {
		getL().log(java.util.logging.Level.WARNING, message.toString());

	}

	public void warn(Object message, Throwable t) {
		getL().log(java.util.logging.Level.WARNING, message.toString(), t);
	}

	public boolean isEnabledFor(Level level) {
		// TODO: implement
		return false;
	}

}
