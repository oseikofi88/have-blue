package io.greyparrot.Routes;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AWSRekognition extends RouteBuilder {

    //todo  the links are more than 1 make sure all you can query for all the link and get all of them asyncly and get the name of the top 5 or 10

    @Override
    public void configure() {

        from("rabbitmq:s3_links?queue=s3_links&autoDelete=false")
                .log("${body}")
                .bean(Service.class,"getLabelDetails(${body},${exchange})")
                .log("${body}")
                .marshal().json(JsonLibrary.Jackson)
                .setExchangePattern(ExchangePattern.InOnly)
                .toD("rabbitmq:labels?queue=labels&autoDelete=false&" +
                        "exchangeType=fanout")
                .log("Done pushing to labels queue");


    }

}
