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
package org.apache.wiki.tags;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.jstl.fmt.LocaleSupport;

import org.apache.wiki.WikiActionResult;
import org.apache.wiki.WikiEngine;

public class ActionResultTag extends WikiTagBase {

    @Override
    public int doWikiStartTag() throws Exception {
        WikiActionResult res = (WikiActionResult) pageContext.getRequest()
                .getAttribute(WikiEngine.WIKIACTIONRESULT);
        JspWriter out = pageContext.getOut();
        if (res == null) {
            return SKIP_BODY;
        }
        String localizeMessage = LocaleSupport.getLocalizedMessage(pageContext,
                res.getMessageId());
        String htmlstart, htmlend;
        if (res.isSuccess()) {
            htmlstart = "<h3>";
            htmlend = "</h3>";
        } else {
            htmlstart = "<h3><div class='error'>";
            htmlend = "</div></h3>";
        }
        out.print(htmlstart + localizeMessage + (res.getMessage() == null ? ""
                : " " + res.getMessage()) + htmlend);
        out.flush();
        return SKIP_BODY;
    }

}
