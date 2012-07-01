<%@ page import="org.apache.commons.logging.*"%>
<%@ page import="org.apache.wiki.*"%>
<%@ page import="org.apache.wiki.tags.WikiTagBase"%>
<%@ page errorPage="/Error.jsp"%>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki"%>
<%!
	Log log = LogFactory.getLog("JSPWiki");%>
<%
	WikiEngine wiki = WikiEngine.getInstance(getServletConfig());

	WikiContext wikiContext = wiki.createContext(request, WikiContext.DOWNLOADWIKI);
	pageContext.setAttribute(WikiTagBase.ATTR_CONTEXT, wikiContext,
			PageContext.REQUEST_SCOPE);

	response.setContentType("text/html; charset="
			+ wiki.getContentEncoding());
	String contentPage = wiki.getTemplateManager().findJSP(pageContext,
			wikiContext.getTemplate(), "DownloadWikiTemplate.jsp");
	log.debug("Download wiki content is: " + contentPage);
%><wiki:Include page="<%=contentPage%>" />