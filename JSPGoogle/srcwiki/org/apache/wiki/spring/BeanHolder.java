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
package org.apache.wiki.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wiki.security.WikiSubject;
import org.springframework.context.ApplicationContext;

public class BeanHolder {

	private BeanHolder() {
	}
	
	private static Log log = LogFactory.getLog(BeanHolder.class);

	public static WikiSubject getSubject() {
		ApplicationContext apx = ApplicationContextHolder.getContext();
		log.trace("get subject context : " + apx + " long=" + apx.getStartupDate());
		WikiSubject su = (WikiSubject) apx.getBean("wikiuser");
		log.trace("get Subject wikiuser : " + su);  
     	return su;
	}

}
