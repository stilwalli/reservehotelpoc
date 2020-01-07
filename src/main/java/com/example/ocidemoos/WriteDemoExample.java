package com.example.ocidemoos;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.http.ResteasyClientConfigurator;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageAsyncClient;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.BucketSummary;
import com.oracle.bmc.objectstorage.model.CreateBucketDetails;
import com.oracle.bmc.objectstorage.requests.CreateBucketRequest;
import com.oracle.bmc.objectstorage.requests.GetNamespaceRequest;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.requests.ListBucketsRequest;
import com.oracle.bmc.objectstorage.requests.ListBucketsRequest.Builder;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.GetNamespaceResponse;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import com.oracle.bmc.objectstorage.responses.ListBucketsResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.ws.rs.client.ClientBuilder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WriteDemoExample {
	
	private static final String CONFIG_LOCATION = "~/.oci/config";
    private static String CONFIG_PROFILE = "POC";

	public static void main(String[] args) throws Exception {
		SpringApplication.run(WriteDemoExample.class, args);
		
		String profileName = null, bucketName = null, objectName = null;
		
		if (args.length != 3) {
			profileName = CONFIG_PROFILE;
			bucketName = "compute_storage";
			objectName = "reserhotel_test.txt";
        } else {
        	profileName = args[0];
        	bucketName = args[1];
        	objectName = args[2];
        }
		
		
		
		System.out.println("Arg Length: " + args.length + " Profile: " + profileName + " Bucket Name: " + bucketName + " Object Name: " + objectName);
		
		final ConfigFileReader.ConfigFile configFile =
                ConfigFileReader.parse(CONFIG_LOCATION, profileName);
        final AuthenticationDetailsProvider provider =
                new ConfigFileAuthenticationDetailsProvider(configFile);
        
        System.setProperty(
                ClientBuilder.JAXRS_DEFAULT_CLIENT_BUILDER_PROPERTY,
                "org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder");
        
        ObjectStorage objectStorageClient =
                ObjectStorageClient.builder()
                        .additionalClientConfigurator(new ResteasyClientConfigurator())
                        .build(provider);
        
        ObjectStorageAsyncClient objectStorageAsyncClient =
                ObjectStorageAsyncClient.builder()
                        .additionalClientConfigurator(new ResteasyClientConfigurator())
                        .build(provider);
        
        objectStorageClient.setRegion(Region.US_ASHBURN_1);
        objectStorageAsyncClient.setRegion(Region.US_ASHBURN_1);
        String namespace =
                objectStorageClient
                        .getNamespace(GetNamespaceRequest.builder().build())
                        .getValue();
        
        System.out.println("Using namespace/cloudAccount: " + namespace);
        
        /*
        System.out.println("Creating the source bucket.");
        CreateBucketDetails createSourceBucketDetails =
                CreateBucketDetails.builder().compartmentId("ocid1.compartment.oc1..aaaaaaaasiotetkeonmqaneq7m6ii3334r4vy34svbcaazynf3xsf2mbbaxq").name("program1").build();
        CreateBucketRequest createSourceBucketRequest =
                CreateBucketRequest.builder()
                        .namespaceName(namespace)fff
                        .createBucketDetails(createSourceBucketDetails)
                        .build();
        objectStorageClient.createBucket(createSourceBucketRequest);
        */
        
        GetObjectRequest getObjectRequest = GetObjectRequest.builder().namespaceName(namespace).bucketName(bucketName).objectName(objectName).build();
        GetObjectResponse getObjectResponse = objectStorageClient.getObject(getObjectRequest);
        
        InputStream inputStream = getObjectResponse.getInputStream();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        
        String objectContent = result.toString(StandardCharsets.UTF_8.name());
        System.out.println("Bucket: " + bucketName + ", Object: " + objectName + " Current Content: " + objectContent);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        if (objectContent != null) {
        	objectContent = objectContent + ":" + timeStamp;
        }

        
       // Util.writeInputStream(getObjectResponse.getInputStream(), getObjectResponse.getContentType());
       // def object = Util.writeInputStream(getObjectResponse.inputStream, getObjectResponse.contentType)
        
        PutObjectRequest putObjectRequest =
                PutObjectRequest.builder()
                        .namespaceName(namespace)
                        .bucketName(bucketName)
                        .objectName(objectName)
                        .contentLength(4L)
                        .putObjectBody(
                                new ByteArrayInputStream(objectContent.getBytes(StandardCharsets.UTF_8)))
                        .build();
        
        
        objectStorageClient.putObject(putObjectRequest);
        System.out.println("Bucket: compute_storage, Object: reserhotel_test.txt Updated with " + objectContent);
        
        
        
	}

}
