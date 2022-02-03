package io.greyparrot.Routes;

import com.amazonaws.services.dynamodbv2.xspec.S;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws.s3.S3Constants;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.processor.aggregate.GroupedExchangeAggregationStrategy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;


@Component
public class s3Upload extends RouteBuilder {


    @Override
    public void configure() {

        from("direct:mylinks")
                .log("this is the remaining valid image link ${headers.remainingValidImageLinks}")
                .aggregate(new ArrayListAggregationStrategy()).constant(true)
                .completionTimeout(500)

                // wait for 0.5 seconds to aggregate( this is terrible and we should get a better aggregation strategy
                .log("yayyyyyyy the aggregation body is ooooo ${body}")
                .log("we ended")
                .process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        String payload = exchange.getMessage().getBody(String.class);
                        String[] content = payload.split(",");
                        ArrayList<String> finalContent = (ArrayList<String>) Arrays.stream(content).distinct().collect(Collectors.toList());
                        // do something with the payload and/or exchange here
                        exchange.getMessage().setBody(finalContent);
                    }})//                .log("Start process to save to s3")
//                .log("Start process to save to s3")
//                .setHeader("CamelAwsS3Key", simple("${header.CamelFileName}"))
//                .setHeader("CamelAwsS3ContentLength", simple("${header.CamelFileLength}"))
//                .toD("aws-s3://${env:S3_BUCKET_NAME}")
//                .setBody(simple("${header.CamelFileName}"))
                .marshal().json(JsonLibrary.Jackson)
                .setExchangePattern(ExchangePattern.InOnly)
                .toD("rabbitmq:labels?queue=labels&autoDelete=false&" +
                        "exchangeType=fanout")
                .log("Done pushing to labels queue")
                .end();
    }
}
