<%@ page import="java.io.InputStream"%>
<%@ page import="java.io.Reader"%>
<%@ page import="java.io.ByteArrayInputStream"%>
<%@ page import="javax.servlet.ServletOutputStream"%>
<%@ page import="org.apache.commons.logging.*"%>
<%@ page import="org.apache.wiki.downup.IDownloadWiki"%>
<%@ page import="org.apache.wiki.spring.BeanHolder"%>
<%@ page errorPage="/Error.jsp"%>
<%@ page import="java.util.*"%>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki"%>

<%!Log log = LogFactory.getLog("DownloadWiki.jsp");%>
<%
    response.setContentType("text/xml; charset=UTF-8");
    response.setHeader("Content-Disposition",
            "attachment;filename=Wiki.xml");
    IDownloadWiki i = BeanHolder.getDownloadWiki();
    Reader in = i.provideReader();

    char[] outputByte = new char[4096];
    int bread;
    log.debug("Start sending content");
    //copy text content to output stream
    while ((bread = in.read(outputByte, 0, 4096)) != -1) {
        out.write(outputByte, 0, bread);
    }
    in.close();
    out.flush();
    log.debug("Finished.");
%>
