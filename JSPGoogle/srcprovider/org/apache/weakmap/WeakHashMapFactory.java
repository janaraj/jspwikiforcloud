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
package org.apache.weakmap;

import java.util.HashMap;
import java.util.Map;

/**
 * WeakHashMap is not supported by Google App Engine. To keep compatibility a
 * Factory was created for producing WeakHashMap. For Google App Engine it
 * creates standard HashMap. For environment supporting WeakHashMap could be
 * implemented a different way.
 * 
 * @author perseus
 * 
 */

public class WeakHashMapFactory {

	/**
	 * Construct WeakHashMap. For Google App Engine implemented as standard
	 * HashMap
	 * 
	 * @return Map
	 */
	public static <K, V> Map<K, V> constructWeakHashMap() {

		return new HashMap<K, V>();

	}

}
