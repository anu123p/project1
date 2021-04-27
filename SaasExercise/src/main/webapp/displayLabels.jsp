<%@ page
	import="com.google.appengine.api.blobstore.BlobstoreServiceFactory"%>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService"%>
<%@ page import="com.google.cloud.vision.v1.EntityAnnotation"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.List"%>
<%
	BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>Label Detection</title>
  </head>
  
  <style>
  
table {
  font-family: arial, sans-serif;
  border-collapse: collapse;
  width: 100%;
}

td, th {
  border: 1px solid #dddddd;
  text-align: left;
  padding: 8px;
}

tr:nth-child(even) {
  background-color: #dddddd;
}
</style>

<body style="background-color:#FFDFDF;">

<center>

			<h1><span>Label Description</span></h1> <br>
			<br>
		
			<h3><span>IMAGE</span></h3><br>
			
			<img src="<%=request.getAttribute("imageUrl")%>"> <br>

		<%
			List<EntityAnnotation> imageLabels = (List<EntityAnnotation>) request.getAttribute("imageLabels");
		%>
		
			</br><h2><span>Labels Results from Google Cloud Vision API</span></h2></br> 
				<table style="width:80%" >
				<thead>
    
					<tr>
				
						<th scope="col" ><b><i>LABEL DESCRIPTION</i></b></th>
						<th scope="col" ><b><i>LABEL SCORE</i></b></th>

					</tr>
</thead>
 <tbody>
					<c:forEach items="${imageLabels}" var="label">
						<tr>
							<td>${label.getDescription()}</td>
							<td>${label.getScore()}</td>
						</tr>
					</c:forEach>
</tbody>
				</table>

			</td>
		</tr>
	</table>
	</center>

</body>
</html>