<%@ page import="org.apache.commons.logging.*" %>
<%@ page import="org.apache.wiki.*" %>
<%@ page import="org.apache.wiki.filters.*" %>
<%@ page import="java.util.Date" %>
<%@ page import="org.apache.wiki.ui.EditorManager" %>
<%@ page errorPage="/Error.jsp" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%! 
    Log log = LogFactory.getLog("JSPWiki"); 
%>

<%
    WikiEngine wiki = WikiEngine.getInstance( getServletConfig() );
    // Create wiki context and check for authorization
    WikiContext wikiContext = wiki.createContext( request, WikiContext.PREVIEW );
    if(!wikiContext.hasAccess( response )) return;
    String pagereq = wikiContext.getName();

    pageContext.setAttribute( EditorManager.ATTR_EDITEDTEXT,
                              session.getAttribute( EditorManager.REQ_EDITEDTEXT ),
                              PageContext.REQUEST_SCOPE );

    String lastchange = SpamFilter.getSpamHash( wikiContext.getPage(), request );

    pageContext.setAttribute( "lastchange",
                              lastchange,
                              PageContext.REQUEST_SCOPE );
   
    // Set the content type and include the response content
    response.setContentType("text/html; charset="+wiki.getContentEncoding() );
    String contentPage = wiki.getTemplateManager().findJSP( pageContext,
                                                            wikiContext.getTemplate(),
                                                            "ViewTemplate.jsp" );
%><wiki:Include page="<%=contentPage%>" />

