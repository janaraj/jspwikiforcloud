<%@ page isErrorPage="true" %>
<%@ page import="org.apache.commons.logging.*" %>
<%@ page import="org.apache.wiki.*" %>
<%@ page import="org.apache.wiki.tags.WikiTagBase" %>
<%@ page import="org.apache.wiki.spring.BeanHolder" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%! 
    Log log = LogFactory.getLog("JSPWiki"); 
%>
<%
    WikiEngine wiki = WikiEngine.getInstance( getServletConfig() );
    WikiContext wikiContext = wiki.createContext( request, 
                                                  WikiContext.MESSAGE );

    // Stash the wiki context and message text
    request.setAttribute( WikiTagBase.ATTR_CONTEXT, wikiContext );
    request.setAttribute( "message", request.getParameter("message"));

    // Set the content type and include the response content
    response.setContentType("text/html; charset="+wiki.getContentEncoding() );
    String contentPage = BeanHolder.getTemplateManager().findJSP( pageContext,
                                                            wikiContext.getTemplate(),
                                                            "ViewTemplate.jsp" );

%><wiki:Include page="<%=contentPage%>" />