<%@ page import="org.apache.commons.logging.*"%>
<%@ page import="org.apache.wiki.*"%>
<%@ page import="org.apache.wiki.tags.WikiTagBase"%>
<%@ page import="org.apache.wiki.spring.BeanHolder" %>
<%@ page errorPage="/Error.jsp"%>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki"%>
<%!
	Log log = LogFactory.getLog("JSPWiki");%>
<%
	WikiEngine wiki = WikiEngine.getInstance(getServletConfig());
    String action = request.getParameter("action");

    WikiContext wikiContext;
    if (action.equals("downloadwiki")) {
	  wikiContext = wiki.createContext(request, WikiContext.DOWNLOADWIKI);
    }
    else {
  	  wikiContext = wiki.createContext(request, WikiContext.UPLOADWIKI);        
    }
	pageContext.setAttribute(WikiTagBase.ATTR_CONTEXT, wikiContext,
			PageContext.REQUEST_SCOPE);

	response.setContentType("text/html; charset="
			+ wiki.getContentEncoding());
	String contentPage = BeanHolder.getTemplateManager().findJSP(pageContext,
			wikiContext.getTemplate(), "DownloadWikiTemplate.jsp");
	log.debug("Download wiki content is: " + contentPage);
%><wiki:Include page="<%=contentPage%>" />