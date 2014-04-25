<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki"%>
<%@ page import="org.apache.wiki.*"%>
<%@ page import="org.apache.wiki.auth.*"%>
<%@ page import="org.apache.wiki.ui.progress.*"%>
<%@ page import="org.apache.wiki.auth.permissions.*"%>
<%@ page import="java.security.Permission"%>
<%@ page import="org.apache.wiki.spring.BeanHolder"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setLocale value="${prefs.Language}" />
<fmt:setBundle basename="templates.default" />
<%
    WikiEngine wiki = BeanHolder.getWikiEngine();
    WikiContext c = WikiContext.findContext(pageContext);
    String returnPage = request.getServletPath() + "?"
            + request.getQueryString();
    String progressId = BeanHolder.getProgressManager()
            .getNewProgressIdentifier();
    String downloadWikiAction = BeanHolder.getTemplateManager()
            .findJSP(pageContext, c.getTemplate(),
                    "DownloadWikiAction.jsp");
%>
<div id="downloadwikiid">

	<h3>
		<fmt:message key="downloadwiki.download.header" />
	</h3>

   <fmt:message key="downloadwiki.download.info" />
   <p>
   <a class="wikiform" href="/DownloadWiki?nextpage=DownloadWiki.jsp?action=downloadwiki" accesskey="" title="Download">Download</a>    	

	<wiki:ActionResult />


</div>


