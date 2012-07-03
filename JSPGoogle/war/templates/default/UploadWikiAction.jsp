<%@ page import="java.io.InputStream"%>
<%@ page import="java.io.Reader"%>
<%@ page import="java.io.ByteArrayInputStream"%>
<%@ page import="javax.servlet.ServletOutputStream"%>
<%@ page import="org.apache.commons.logging.*"%>
<%@ page import="org.apache.wiki.downup.IDownloadWiki"%>
<%@ page import="org.apache.wiki.spring.BeanHolder"%>
<%@ page import="java.io.InputStream" %>
<%@ page import="org.apache.wiki.downup.IUploadWiki" %>
<%@ page errorPage="/Error.jsp"%>
<%@ page import="java.util.*"%>
<%@ page import="org.apache.commons.fileupload.*,org.apache.commons.fileupload.servlet.ServletFileUpload,org.apache.commons.fileupload.disk.DiskFileItemFactory,org.apache.commons.io.FilenameUtils,java.util.*,java.io.File,java.lang.Exception"%>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki"%>

<%!Log log = LogFactory.getLog("UploadWikiAction.jsp");%>
<%
  ServletContext context = pageContext.getServletContext();
  String filePath = context.getInitParameter("file-upload");  
//  String contentType = request.getContentType();

  //Create a new file upload handler
  ServletFileUpload upload = new ServletFileUpload();
  response.setContentType("text/plain");
  
  String error = null;

  //Parse the request
  FileItemIterator iterator = upload.getItemIterator(request);
  while (iterator.hasNext()) {
      FileItemStream fItem = iterator.next();
      if (fItem.isFormField()) {
          log.trace("Got a form field: " + fItem.getFieldName());
          continue;
      }
      
      String contentType = fItem.getContentType();
      String fieldName = fItem.getFieldName();
      String fileName = fItem.getName();
      log.info("Got file name: " + fileName + " start uploading");
      InputStream in = fItem.openStream();
      IUploadWiki up = BeanHolder.getUploadWiki();
      up.uploadWiki(in);
  }
%>
