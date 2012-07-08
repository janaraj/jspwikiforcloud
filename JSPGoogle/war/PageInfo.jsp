<%@ page import="org.apache.commons.logging.*" %>
<%@ page import="org.apache.wiki.*" %>
<%@ page import="org.apache.wiki.util.*" %>
<%@ page errorPage="/Error.jsp" %>
<%@ page import="org.apache.wiki.spring.BeanHolder" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>

<%! 
    Log log = LogFactory.getLog("JSPWiki"); 
%>

<%
    WikiEngine wiki = WikiEngine.getInstance( getServletConfig() );
    // Create wiki context and check for authorization
    WikiContext wikiContext = wiki.createContext( request, WikiContext.INFO );
    if(!wikiContext.hasAccess( response )) return;
    String pagereq = wikiContext.getName();
    
    WatchDog w = wiki.getCurrentWatchDog();
    try{
    w.enterState("Generating INFO response",60);
    
    // Set the content type and include the response content
    response.setContentType("text/html; charset="+wiki.getContentEncoding() );
    String contentPage = BeanHolder.getTemplateManager().findJSP( pageContext,
                                                            wikiContext.getTemplate(),
                                                            "ViewTemplate.jsp" );
%><wiki:Include page="<%=contentPage%>" />

<% } finally { w.exitState(request.getSession()); } %>