package io.greyparrot.Routes;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AWSRekognition extends RouteBuilder {


    @Override
    public void configure() {
        from("rabbitmq:image_links?queue=image_links&autoDelete=false")
                .log("${body}")
                .log("${header.numberOfLinks}")
                .setHeader("Accept", simple("binary"))//Change it according to the file content
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .split()
                .jsonpath("$")
                .streaming()
                .parallelProcessing()
//                .parallelAggregate()
                .log("Split performed")
                .toD("${body}")
                .toD("file:images?fileName=${header.CamelSplitIndex}-${header.traceID}-"+ LocalDateTime.now())
                .log("Done")
                //put it in folder
                .end();

    }

    private AggregationStrategy batchAggregationStrategy() {
        return new ArrayListAggregationStrategy();
    }

}
