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
package org.apache.wiki.security;

import java.security.AccessControlException;
import java.security.Permission;

public class WikiAccessController {
	
	private WikiAccessController() { }
		
	    public static void checkPermission(Permission perm)
	   		 throws AccessControlException {
	    	// do nothing now
//            AccessController.checkPermission( permission );

	       }

	}

