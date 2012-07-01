<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki"%>
<%@ page import="org.apache.wiki.*"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page import="org.apache.wiki.spring.BeanHolder"%>
<%@ page import="com.jsp.util.localize.LocaleSupport"%>
<fmt:setLocale value="${prefs.Language}" />
<fmt:setBundle basename="templates.default" />
<%
	WikiEngine wiki = BeanHolder.getWikiEngine();
	WikiContext c = WikiContext.findContext(pageContext);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html id="top" xmlns="http://www.w3.org/1999/xhtml"
	xmlns:jspwiki="http://www.jspwiki.org">

<head>
<title><fmt:message key="downloadwiki.title">
		<fmt:param>
			<wiki:Variable var="applicationname" />
		</fmt:param>
	</fmt:message></title>
<wiki:Include page="commonheader.jsp" />
<meta name="robots" content="noindex,nofollow" />
</head>

<body>

	<div id="wikibody" class="${prefs.Orientation}">

		<wiki:Include page="Header.jsp" />

		<div id="content">

			<div id="page">

				<wiki:TabbedSection defaultTab="download">
					<wiki:Tab id="download"
						title='<%=LocaleSupport.getLocalizedMessage(pageContext,
							"downloadwiki.download.tab")%>'>
						<wiki:Include page="DownloadWikiTab.jsp" />
					</wiki:Tab>

					<wiki:Tab id="info"
						title='<%=LocaleSupport.getLocalizedMessage(pageContext,
							"downloadwiki.info.tab")%>'>
						<wiki:Include page="DownloadWikiInfo.jsp" />
					</wiki:Tab>

				</wiki:TabbedSection>

			</div>

			<wiki:Include page="Favorites.jsp" />

			<div class="clearbox"></div>
		</div>

		<wiki:Include page="Footer.jsp" />

	</div>
</body>

</html>