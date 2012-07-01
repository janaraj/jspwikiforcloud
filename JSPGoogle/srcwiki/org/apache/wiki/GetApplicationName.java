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
package org.apache.wiki;

import java.util.Properties;

import org.apache.wiki.parser.MarkupParser;
import org.apache.wiki.spring.BeanHolder;

public class GetApplicationName {

    public static String getApplicationName() {

        Properties prop = BeanHolder.getWikiProperties();
        String appName = TextUtil.getStringProperty(prop,
                WikiEngine.PROP_APPNAME, Release.APPNAME);

        return MarkupParser.cleanLink(appName);
    }

}
