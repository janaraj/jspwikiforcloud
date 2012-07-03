<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki"%>
<%@ page import="org.apache.wiki.*"%>
<%@ page import="org.apache.wiki.auth.*"%>
<%@ page import="org.apache.wiki.ui.progress.*"%>
<%@ page import="org.apache.wiki.auth.permissions.*"%>
<%@ page import="java.security.Permission"%>
<%@ page import="org.apache.wiki.spring.BeanHolder"%>
<%@ page import="com.jsp.util.localize.LocaleSupport" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setLocale value="${prefs.Language}" />
<fmt:setBundle basename="templates.default" />
<%
    WikiEngine wiki = BeanHolder.getWikiEngine();
	WikiContext c = WikiContext.findContext(pageContext);
    String progressId = c.getEngine().getProgressManager().getNewProgressIdentifier();
	String uploadWikiAction = wiki.getTemplateManager().findJSP( pageContext,
            c.getTemplate(),
            "UploadWikiAction.jsp" );
	String alert = LocaleSupport.getLocalizedMessage(pageContext, "downloadwiki.upload.mustenterfilename");
%>
<div id="uploadwikitab">

	<h3>
		<fmt:message key="downloadwiki.upload.header" />
	</h3>
	
	  <form 
	     action="<%=uploadWikiAction%>"
         class="wikiform"
            id="uploadform"
        method="post"
       enctype="multipart/form-data" accept-charset="<wiki:ContentEncoding/>"
       onsubmit="return Wiki.validateFileName('<%=alert%>') &&  Wiki.submitUpload(this, '<%=progressId%>');">      
    <table>
    <tr>
      <td colspan="2"><div class="formhelp"><fmt:message key="downloadwiki.upload.info" /></div></td>
    </tr>
    <tr>
      <td><label for="attachfilename"><fmt:message key="attach.add.selectfile"/></label></td>
      <td><input type="file" name="content" id="attachfilename" size="60"/></td>
    </tr>

   <tr>
      <td></td>
      <td>
        <input type="hidden" name="page" value="<wiki:Variable var="pagename"/>" />
        <input type="submit" name="upload" id="upload" value="<fmt:message key='attach.add.submit'/>" />
        <input type="hidden" name="action" value="upload" />
        <div id="progressbar"><div class="ajaxprogress"></div></div>
      </td>
    </tr>

    </table>
  </form>

</div>


