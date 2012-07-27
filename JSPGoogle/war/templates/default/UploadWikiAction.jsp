<%@ page import="java.io.InputStream"%>
<%@ page import="java.io.InputStreamReader"%>
<%@ page import="java.io.Reader"%>
<%@ page import="org.apache.wiki.*"%>
<%@ page import="java.io.BufferedReader"%>
<%@ page import="java.io.ByteArrayInputStream"%>
<%@ page import="javax.servlet.ServletOutputStream"%>
<%@ page import="org.apache.commons.logging.*"%>
<%@ page import="org.apache.wiki.downup.IDownloadWiki"%>
<%@ page import="org.apache.wiki.spring.BeanHolder"%>
<%@ page import="java.io.InputStream"%>
<%@ page import="org.apache.wiki.downup.IUploadWiki"%>
<%@ page errorPage="/Error.jsp"%>
<%@ page import="java.util.*"%>
<%@ page
	import="org.apache.commons.fileupload.*,org.apache.commons.fileupload.servlet.ServletFileUpload,org.apache.commons.fileupload.disk.DiskFileItemFactory,org.apache.commons.io.FilenameUtils,java.util.*,java.io.File,java.lang.Exception"%>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki"%>

<%!Log log = LogFactory.getLog("UploadWikiAction.jsp");%>
<%
    ServletContext context = pageContext.getServletContext();
    String returnPage = request.getParameter("xreturnPage");
    //  String filePath = context.getInitParameter("file-upload");    
    //  String contentType = request.getContentType();

    //Create a new file upload handler
    ServletFileUpload upload = new ServletFileUpload();
    response.setContentType("text/plain");

    String error = null;

    //Parse the request
    FileItemIterator iterator = upload.getItemIterator(request);
    InputStream in = null;
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
        } else {
            String contentType = fItem.getContentType();
            String fieldName = fItem.getFieldName();
            String fileName = fItem.getName();
            log.info("Got file name: " + fileName + " start uploading");
            in = fItem.openStream();
            IUploadWiki up = BeanHolder.getUploadWiki();
            String errMess = up.uploadWiki(in);
            WikiActionResult res = new WikiActionResult();
            res.setSuccess(errMess == null);
            if (errMess == null) {
                res.setMessageId("downloadwiki.uploadwiki.resultok");
            } else {
                res.setMessageId("downloadwiki.uploadwiki.resulterror");
                res.setMessage(errMess);
            }
            request.setAttribute(WikiEngine.WIKIACTIONRESULT, res);
        }
    } // while

    RequestDispatcher rd = context.getRequestDispatcher(returnPage);
    rd.forward(request, response);
%>
