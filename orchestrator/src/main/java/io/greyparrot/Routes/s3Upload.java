package io.greyparrot.Routes;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.util.json.Jackson;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws.s3.S3Constants;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class s3Upload extends RouteBuilder {


    @Override
    public void configure() {

        from("file:/home/richard/IdeaProjects/have-blue/orchestrator/images/")
                .setHeader("CamelAwsS3Key", simple("${header.CamelFileName}"))
                .setHeader("CamelAwsS3ContentLength", simple("${header.CamelFileLength}"))
        .to("aws-s3://have-blue")
        .log("${headers}");
}
}
