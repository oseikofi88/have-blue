package io.greyparrot.Routes;

import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;


@Component
public class s3Upload extends RouteBuilder {


    @Override
    public void configure() {

        from("file:images")
                .setHeader("CamelAwsS3Key", simple("${header.CamelFileName}"))
                .setHeader("CamelAwsS3ContentLength", simple("${header.CamelFileLength}"))
                .toD("aws-s3://${env:S3_BUCKET_NAME}")
                .setBody(simple("${header.CamelFileName}"))
                .marshal().json(JsonLibrary.Jackson)
                .setExchangePattern(ExchangePattern.InOnly)
                .toD("rabbitmq:s3_links?queue=s3_links&autoDelete=false")
                .log("Done pushing to rabbit")
                .end();
    }
}
