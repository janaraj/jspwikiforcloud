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
	String progressId = c.getEngine().getProgressManager()
			.getNewProgressIdentifier();
	String downloadWikiAction = BeanHolder.getTemplateManager().findJSP( pageContext,
            c.getTemplate(),
            "DownloadWikiAction.jsp" );
%>
<div id="downloadwikiid">

	<h3>
		<fmt:message key="downloadwiki.download.header" />
	</h3>
	<form
		action="<%=downloadWikiAction%>"
		class="wikiform" id="uploadform" method="post"
		enctype="multipart/form-data" accept-charset="<wiki:ContentEncoding/>"
		onsubmit="return Wiki.submitUpload(this, '<%=progressId%>');">
		
		<p>
		<fmt:message key="downloadwiki.download.info" />
		</p>
		
        <input type="submit" name="upload" id="upload" value="<fmt:message key='downloadwiki.download.submit'/>" />
        <input type="hidden" name="action" value="upload" />
        <div id="progressbar"><div class="ajaxprogress"></div></div>
        <br>
	</form>

</div>


