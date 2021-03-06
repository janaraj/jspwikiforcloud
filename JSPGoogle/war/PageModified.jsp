<%@ page import="org.apache.commons.logging.*" %>
<%@ page import="org.apache.commons.lang.*" %>
<%@ page import="org.apache.wiki.*" %>
<%@ page import="org.apache.wiki.ui.EditorManager" %>
<%@ page errorPage="/Error.jsp" %>
<%@ page import="org.apache.wiki.spring.BeanHolder" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>

<%! 
    Log log = LogFactory.getLog("JSPWiki");
%>

<%
    WikiEngine wiki = WikiEngine.getInstance( getServletConfig() );
    // Create wiki context and check for authorization
    WikiContext wikiContext = wiki.createContext( request, WikiContext.CONFLICT );
    if(!wikiContext.hasAccess( response )) return;
    String pagereq = wikiContext.getName();

    String usertext = (String)session.getAttribute( EditorManager.REQ_EDITEDTEXT );

    // Make the user and conflicting text presentable for display.
    usertext = StringEscapeUtils.escapeXml( usertext );
    usertext = TextUtil.replaceString( usertext, "\n", "<br />" );

    String conflicttext = wiki.getText(pagereq);
    conflicttext = StringEscapeUtils.escapeXml( conflicttext );
    conflicttext = TextUtil.replaceString( conflicttext, "\n", "<br />" );

    pageContext.setAttribute( "conflicttext",
                              conflicttext,
                              PageContext.REQUEST_SCOPE );

    log.info("Page concurrently modified "+pagereq);
    pageContext.setAttribute( "usertext",
                              usertext,
                              PageContext.REQUEST_SCOPE );

    // Set the content type and include the response content
    response.setContentType("text/html; charset="+wiki.getContentEncoding() );
    String contentPage = BeanHolder.getTemplateManager().findJSP( pageContext,
                                                            wikiContext.getTemplate(),
                                                            "ViewTemplate.jsp" );
%><wiki:Include page="<%=contentPage%>" />

