<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
     <%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>

<%
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
%>


<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
 <body style="background-color:#FFDFDF;">
	  <h1><span>Google Cloud Vision API</span></h1>
 <form action="<%= blobstoreService.createUploadUrl("/upload") %>" method="post" enctype="multipart/form-data">
           
              <div class="form-group files">
               
                <input type="file" name="fileName" class="form-control" multiple="">
                 <input type="submit" class="" value="Click here to Submit">
              </div>
              
            
          </form>
    <!--       
            <form action="/hello" method="post" enctype="multipart/form-data">
           
              <div class="form-group files">
               
                <input type="file" name="fileName" class="form-control" multiple="">
                 <input type="submit" class="" value="Click here to Submit">
              </div>
              
            
          </form> -->
        
    </body>
</html>