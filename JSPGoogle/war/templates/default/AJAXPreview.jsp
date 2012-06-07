<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="org.apache.commons.logging.*" %>
<%@ page import="org.apache.wiki.*" %>
<%@ page import="org.apache.wiki.auth.*" %>
<%@ page import="org.apache.wiki.auth.permissions.*" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%! 
  public void jspInit()
  {
    wiki = WikiEngine.getInstance( getServletConfig() );
  }
  Log log = LogFactory.getLog("JSPWikiSearch");
  WikiEngine wiki;
%>
<%
  // Copied from a top-level jsp -- which would be a better place to put this 
  WikiContext wikiContext = wiki.createContext( request, WikiContext.VIEW );
  if( !wikiContext.hasAccess( response ) ) return;

  response.setContentType("text/html; charset="+wiki.getContentEncoding() );
  
  String wikimarkup = request.getParameter( "wikimarkup" );
%>
<wiki:Translate><%= wikimarkup %></wiki:Translate>