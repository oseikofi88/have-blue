package io.greyparrot.Routes;

import java.util.ArrayList;
import java.util.List;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Instance;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.rekognition.model.Parent;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;

public class Service {


    public static void getLabelDetails(String photo,Exchange exchange){
        String bucketName = System.getenv("S3_BUCKET_NAME");
        String awsRegion = System.getenv("AWS_REGION_SMALL_LETTERS");

        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard().withRegion(awsRegion).build();

        DetectLabelsRequest request = new DetectLabelsRequest()
                .withImage(new Image().withS3Object(new S3Object().withName(photo).withBucket(bucketName)))
                .withMaxLabels(10).withMinConfidence(70F);

        try {
            DetectLabelsResult result = rekognitionClient.detectLabels(request);
            List<Label> labels = result.getLabels();
            ArrayList<String> labelNames = new ArrayList<>();

            System.out.println("Detected labels for " + photo + "\n");
            for (Label label : labels) {
                System.out.println("Label: " + label.getName());
                System.out.println("Confidence: " + label.getConfidence().toString() + "\n");
                labelNames.add(label.getName());

                List<Instance> instances = label.getInstances();
                System.out.println("Instances of " + label.getName());
                if (instances.isEmpty()) {
                    System.out.println("  " + "None");
                } else {
                    for (Instance instance : instances) {
                        System.out.println("  Confidence: " + instance.getConfidence().toString());
                        System.out.println("  Bounding box: " + instance.getBoundingBox().toString());
                    }
                }
                System.out.println("Parent labels for " + label.getName() + ":");
                List<Parent> parents = label.getParents();
                if (parents.isEmpty()) {
                    System.out.println("  None");
                } else {
                    for (Parent parent : parents) {
                        if(!labelNames.contains(parent.getName())){
                            labelNames.add(parent.getName());
                        }
                        System.out.println("  " + parent.getName());
                    }
                }
                System.out.println("--------------------");
                System.out.println();

            }
            exchange.getIn().setBody(labelNames);
        } catch (AmazonRekognitionException e) {
            e.printStackTrace();
        }
    }

    public void getNameOfFile(Exchange exchange){
        //todo make sure only files in the format jpg and jpeg are stored in the header. if not put it in a not appropriate file queue
        String imageUrl = exchange.getIn().getBody(String.class);
        int lastIndex = imageUrl.lastIndexOf("/");
        String nameOfImageFile = imageUrl.substring(lastIndex+1);
        if(nameOfImageFile.substring(nameOfImageFile.lastIndexOf(".")+1).equalsIgnoreCase("jpg") ||
                nameOfImageFile.substring(nameOfImageFile.lastIndexOf(".")+1).equalsIgnoreCase("jpeg") ){
            exchange.getIn().setHeader("nameOfFile",nameOfImageFile);
            exchange.getIn().setHeader("fileFormatIsCorrect",true);
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
