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

import java.io.Serializable;

/**
 * Stub replacement for org.apache.commons.logging
 * 
 * <pre>
 * Reason: 
 *   1) Google App Engine does not support FileOutputStream
 *   2) Log should be serializable
 * </pre>
 * 
 * Only subset of methods (used by application) is copied but can be easily
 * extended
 * 
 * @author stanislawbartkowski@gmail.com
 * 
 */

public interface Log extends Serializable {

	boolean isDebugEnabled();

	boolean isErrorEnabled();

	boolean isInfoEnabled();

	void debug(java.lang.Object message);

	void error(java.lang.Object message, java.lang.Throwable t);

	void error(java.lang.Object message);

	void warn(java.lang.Object message);

	void info(java.lang.Object message);

	void warn(java.lang.Object message, java.lang.Throwable t);

}
