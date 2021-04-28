package com.google;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;

@WebServlet(
    name = "HelloAppEngine",
    urlPatterns = {"/upload"}
)
public class HelloAppEngine extends HttpServlet {
	String category = "People";
	private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) 
      throws IOException, ServletException {		
	   
	  
	  System.out.println("Inside do post");
	  
	//Create dataStore instance
      DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();

      String userID = (String)req.getParameter("userID");

      ArrayList<String> photoID = new ArrayList<String>(Arrays.asList(req.getParameterValues("imageID")[0].split(",")));
      ArrayList<String> imageLinks = new ArrayList<String>(Arrays.asList(req.getParameterValues("imageLinks")[0].split(",")));

	  
	  Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(req);
	  System.out.println(blobs.toString());
      List<BlobKey> blobKeys = blobs.get("fileName");
      blobKeys.forEach( (k)->System.out.println("Added by anu" + k));

      if(blobKeys == null || blobKeys.isEmpty()) {
          res.sendRedirect("/");
      } else {
      	byte[] blobBytes = getBlobBytes(blobKeys.get(0));
  		List<EntityAnnotation> imageLabels = getImageLabels(blobBytes);
  		String imageUrl = getUploadedFileUrl(blobKeys.get(0));
  		req.setAttribute("imageUrl", imageUrl);
  		req.setAttribute("imageLabels", imageLabels);
  		RequestDispatcher dispatcher = getServletContext()
  			      .getRequestDispatcher("/displayLabels.jsp");
  			    dispatcher.forward(req, res);	
      } 
    }
  private List<EntityAnnotation> getImageLabels(byte[] imgBytes) throws IOException {
	  	 //boolean landscape =false ;
	  
		ByteString byteString = ByteString.copyFrom(imgBytes);
		Image image = Image.newBuilder().setContent(byteString).build();

		Feature feature = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
		Feature featureLandmark = Feature.newBuilder().setType(Feature.Type.LANDMARK_DETECTION).build();
		Feature featureFace = Feature.newBuilder().setType(Feature.Type.FACE_DETECTION).build();
		Feature featureObject = Feature.newBuilder().setType(Feature.Type.OBJECT_LOCALIZATION).build();
		
		
		AnnotateImageRequest request =
				AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(image).build();
		
		AnnotateImageRequest request1 =
				AnnotateImageRequest.newBuilder().addFeatures(featureLandmark).setImage(image).build();
		
		AnnotateImageRequest request2 =
				AnnotateImageRequest.newBuilder().addFeatures(featureFace).setImage(image).build();
		AnnotateImageRequest request3 =
				AnnotateImageRequest.newBuilder().addFeatures(featureObject).setImage(image).build();
		

		    
		List<AnnotateImageRequest> requests = new ArrayList<>();
		requests.add(request);
		requests.add(request1);
		requests.add(request2);
		requests.add(request3);
		

		ImageAnnotatorClient client = ImageAnnotatorClient.create();
		
		BatchAnnotateImagesResponse batchResponse = client.batchAnnotateImages(requests);
		client.close();
		List<AnnotateImageResponse> imageResponses = batchResponse.getResponsesList();
		//System.out.println("Added by anu" + imageResponses);
		AnnotateImageResponse imageResponseLabel = imageResponses.get(0);
		AnnotateImageResponse imageResponseLandmark = imageResponses.get(1);
		AnnotateImageResponse imageResponseFace = imageResponses.get(2);
		AnnotateImageResponse imageResponseObject = imageResponses.get(3);
		int count = imageResponseFace.getFaceAnnotationsCount();
		System.out.println("Added by ANU faces count"+ count);
		boolean landscape = false;
//		
//		imageResponseLabel.getLabelAnnotationsList().forEach( (k)-> 
//				{ if(k.getDescription().contains("Landscape"))
//						{
//							System.out.println("Added by ANU  inside if landscape");
//							landscape = true;
//						}; 
//				} );
		for(int i=0;i<imageResponseLabel.getLabelAnnotationsList().size();i++)
		{
			if(imageResponseLabel.getLabelAnnotationsList().get(i).getDescription().contains("Landscape"))
			{
				landscape = true;
			}
		}
		
		
		if(imageResponseLandmark.getLandmarkAnnotationsCount()>=1)
			category = "Landmark";
		else if(imageResponseFace.getFaceAnnotationsCount()>=1) 
		{
			if(imageResponseFace.getFaceAnnotationsCount()>=4)
				category = "Grouped People";
			else
				category = "People";
		}
		else if(landscape == true) 
		{
			category = "Landscape";
		}
		
			
		
		
		
		System.out.println("Added by ANU labels: "+ imageResponseLabel.getLabelAnnotationsList());
		System.out.println("Added by ANU faces category : "+ category);
		
		
//		 Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
//
//		    // The kind for the new entity
//		    String kind = "image";
//		    // The name/ID for the new entity
//		    String name = "id";
//		    // The Cloud Datastore key for the new entity
//		    Key taskKey = datastore.newKeyFactory().setKind(kind).newKey(name);
//
//		    // Prepares the new entity
//		    Entity task = Entity.newBuilder(taskKey)
//		        .set("category", category,"label", category)
//		        .build();
//
//		    // Saves the entity
//		    datastore.put(task);
//
//		    System.out.printf("Saved %s: %s%n", task.getKey().getName(), task.getString("category"));
//
//		    //Retrieve entity
//		    Entity retrieved = datastore.get(taskKey);
//
//		    System.out.printf("Retrieved %s: %s%n", taskKey.getName(), retrieved.getString("category"));
		


		if (imageResponseLabel.hasError()) {
			System.err.println("Error getting image labels: " + imageResponseLabel.getError().getMessage());
			return null;
		}
		
		return imageResponseLabel.getLabelAnnotationsList();
	}
  
  private String getUploadedFileUrl(BlobKey blobKey){
		ImagesService imagesService = ImagesServiceFactory.getImagesService();
		ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);
		System.out.println("Added by anu" + imagesService.getServingUrl(options));
		return imagesService.getServingUrl(options);
	}
  
  private byte[] getBlobBytes(BlobKey blobKey) throws IOException {
		BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
		ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();

		int fetchSize = BlobstoreService.MAX_BLOB_FETCH_SIZE;
		long currentByteIndex = 0;
		boolean continueReading = true;
		while (continueReading) {
			byte[] b = blobstoreService.fetchData(blobKey, currentByteIndex, currentByteIndex + fetchSize - 1);
			outputBytes.write(b);
			if (b.length < fetchSize) {
				continueReading = false;
			}

			currentByteIndex += fetchSize;
		}

		return outputBytes.toByteArray();
	}

}
