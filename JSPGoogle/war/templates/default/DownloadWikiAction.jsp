<%@ page import="java.io.InputStream"%>
<%@ page import="java.io.Reader"%>
<%@ page import="java.io.InputStreamReader"%>
<%@ page import="java.io.ByteArrayInputStream"%>
<%@ page import="javax.servlet.ServletOutputStream"%>
<%@ page import="org.apache.commons.logging.*"%>
<%@ page import="org.apache.wiki.downup.IDownloadWiki"%>
<%@ page import="org.apache.wiki.spring.BeanHolder"%>
<%@ page import="org.apache.wiki.*"%>
<%@ page import="java.io.BufferedReader"%>
<%@ page errorPage="/Error.jsp"%>
<%@ page import="java.util.*"%>
<%@ page
	import="org.apache.commons.fileupload.*,org.apache.commons.fileupload.servlet.ServletFileUpload,org.apache.commons.fileupload.disk.DiskFileItemFactory,org.apache.commons.io.FilenameUtils,java.util.*,java.io.File,java.lang.Exception"%>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki"%>

<%!Log log = LogFactory.getLog("DownloadWiki.jsp");%>
<%
    String returnPage = null;
    ServletFileUpload upload = new ServletFileUpload();
    FileItemIterator iterator = upload.getItemIterator(request);
    while (iterator.hasNext()) {
        FileItemStream fItem = iterator.next();
        if (fItem.isFormField()) {
            String fieldName = fItem.getFieldName();
            log.trace("Got a form field: " + fieldName);
            if (fieldName.equals("returnPage")) {
                InputStream ins = fItem.openStream();
                InputStreamReader is = new InputStreamReader(ins);
                BufferedReader br = new BufferedReader(is);
                returnPage = br.readLine();
            }
        }
    } // while

    log.trace("Return page=" + returnPage);
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
    // for some reason it does not work, I do not understand why
    WikiActionResult res = new WikiActionResult();
    res.setSuccess(true);
    res.setMessageId("downloadwiki.downloadwiki.resultok");
    request.setAttribute(WikiEngine.WIKIACTIONRESULT, res);

    ServletContext context = pageContext.getServletContext();
    RequestDispatcher rd = context.getRequestDispatcher(returnPage);
    rd.forward(request, response);
%>
