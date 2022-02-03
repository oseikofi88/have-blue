package io.greyparrot.Routes;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
//import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
//import com.amazonaws.services.rekognition.model.DetectLabelsResult;
//import com.amazonaws.services.rekognition.model.Image;
//import com.amazonaws.services.rekognition.model.Instance;
//import com.amazonaws.services.rekognition.model.Label;
//import com.amazonaws.services.rekognition.model.Parent;
//import com.amazonaws.services.rekognition.model.S3Object;
//import com.amazonaws.services.rekognition.AmazonRekognition;
//import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
//import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.dynamodbv2.xspec.S;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;

public class Service {


//    public static void getOldLabelDetails(String photoName, Exchange exchange){
//        System.out.println("This is the photoName " +  photoName );
//        String bucketName = System.getenv("S3_BUCKET_NAME");
//        String awsRegion = System.getenv("AWS_REGION_SMALL_LETTERS");
//
//        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard().withRegion(awsRegion).build();
//
//        DetectLabelsRequest request = new DetectLabelsRequest()
//                .withImage(new Image().withS3Object(new S3Object().withName(photoName).withBucket(bucketName)))
//                .withMaxLabels(10).withMinConfidence(70F);
//
//        try {
//            DetectLabelsResult result = rekognitionClient.detectLabels(request);
//            List<Label> labels = result.getLabels();
//            ArrayList<String> labelNames = new ArrayList<>();
//
//            System.out.println("Detected labels for " + photoName + "\n");
//            for (Label label : labels) {
//                System.out.println("Label: " + label.getName());
//                System.out.println("Confidence: " + label.getConfidence().toString() + "\n");
//                labelNames.add(label.getName());
//
//                List<Instance> instances = label.getInstances();
//                System.out.println("Instances of " + label.getName());
//                if (instances.isEmpty()) {
//                    System.out.println("  " + "None");
//                } else {
//                    for (Instance instance : instances) {
//                        System.out.println("  Confidence: " + instance.getConfidence().toString());
//                        System.out.println("  Bounding box: " + instance.getBoundingBox().toString());
//                    }
//                }
//                System.out.println("Parent labels for " + label.getName() + ":");
//                List<Parent> parents = label.getParents();
//                if (parents.isEmpty()) {
//                    System.out.println("  None");
//                } else {
//                    for (Parent parent : parents) {
//                        if(!labelNames.contains(parent.getName())){
//                            labelNames.add(parent.getName());
//                        }
//                        System.out.println("  " + parent.getName());
//                    }
//                }
//                System.out.println("--------------------");
//                System.out.println();
//
//            }
//            exchange.getIn().setBody(labelNames);
//        } catch (AmazonRekognitionException e) {
//            e.printStackTrace();
//        }
//    }

    public  static void getLabelsDetails(String imageLink, Exchange exchange){
        System.out.println("start getting labels details from link "+ imageLink);

        Region region = Region.US_EAST_1;
        RekognitionClient rekClient = RekognitionClient.builder()
                .region(region)
                .build();


        try {



            int numberOfValidImageLinks =  Integer.parseInt(exchange.getProperty("numberOfValidImageLinks").toString());
            System.out.println("numberOfValidImageLinks  in labels code = " + numberOfValidImageLinks);
            InputStream sourceStream = new URL(imageLink).openStream();
                // InputStream sourceStream = new FileInputStream(sourceImage);
                SdkBytes sourceBytes = SdkBytes.fromInputStream(sourceStream);

                // Create an Image object for the source image.
                Image souImage = Image.builder()
                        .bytes(sourceBytes)
                        .build();

                DetectLabelsRequest detectLabelsRequest = DetectLabelsRequest.builder()
                        .image(souImage)
                        .maxLabels(10)
                        .build();

                DetectLabelsResponse labelsResponse = rekClient.detectLabels(detectLabelsRequest);
                List<Label> labels = labelsResponse.labels();

            ArrayList<String> obtainedLabels = new ArrayList<>();

            System.out.println("Detected labels for the given photo");
                for (Label label: labels) {
                    System.out.println(label.name() + ": " + label.confidence().toString());
                    obtainedLabels.add(label.name());
                }
            numberOfValidImageLinks--;
            System.out.println("The number of valid links are in the labels function are "+numberOfValidImageLinks);
            exchange.setProperty("remainingValidImageLinks",numberOfValidImageLinks);
            exchange.getIn().setBody(obtainedLabels);

        } catch (RekognitionException | FileNotFoundException | MalformedURLException e) {
            rekClient.close();
                System.out.println(e.getMessage());
                System.exit(1);
            } catch (IOException e) {
            rekClient.close();
                e.printStackTrace();
            }
        rekClient.close();

        System.out.println("Done with label things");
    }

    public void getNameOfFile(Exchange exchange){
        //todo make sure only files in the format jpg and jpeg are stored in the header. if not put it in a not appropriate file queue



        int numberOfValidImageLinks = 0;
        if(exchange.getProperty("numberOfValidImageLinks") != null) {
            System.out.println("the number of valid image links is not null");
            numberOfValidImageLinks =  Integer.parseInt(exchange.getProperty("numberOfValidImageLinks").toString());
        }
        else {
            System.out.println("the number of valid image links is nulllllll and it is emtpy as well");

        }
        String imageUrl = exchange.getIn().getBody(String.class);
        int lastIndex = imageUrl.lastIndexOf("/");
        String nameOfImageFile = imageUrl.substring(lastIndex+1);
        if(nameOfImageFile.substring(nameOfImageFile.lastIndexOf(".")+1).equalsIgnoreCase("jpg") ||
                nameOfImageFile.substring(nameOfImageFile.lastIndexOf(".")+1).equalsIgnoreCase("jpeg") ){
            exchange.getIn().setHeader("nameOfFile",nameOfImageFile);
            exchange.getIn().setHeader("fileFormatIsCorrect",true);
            numberOfValidImageLinks++;
            exchange.setProperty("numberOfValidImageLinks",numberOfValidImageLinks);

        }
        else {
            exchange.getIn().setHeader("fileFormatIsCorrect",false);
        }
    }

    public String addUserNameInHeader(String username,String usernames){

        if(usernames == null){
            usernames = username;
        }
        else {
            usernames = usernames.concat(","+username);
        }

       return usernames;
    }

    public void myMethod(Exchange exchange){
        String usernames = exchange.getIn().getHeader("usernames", String.class);
        System.out.println(usernames);

    }


    public SearchResults formatResponse(String results){
        System.out.println("This is the results passed " + results);

        ArrayList<SearchResults.TwitterUsers> twitterUsers = new ArrayList<>();
        String[] twitterUsernames = results.split(",");
        for (String username :
                twitterUsernames) {
            twitterUsers.add(new SearchResults.TwitterUsers(username));
        }

        return new SearchResults(twitterUsers);

    }












}
