package io.greyparrot.Routes;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ImagesSave extends RouteBuilder {


    @Override
    public void configure() {
        from("rabbitmq:image_links?queue=image_links&autoDelete=false")
                .log("${body}")
                .log("${header.numberOfLinks}")
                .log("${header.traceId}")
                .setHeader("Accept", simple("binary"))//Change it according to the file content
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .split()
                .jsonpath("$")
                .streaming()
                .parallelProcessing()
                .bean(Service.class,"getNameOfFile(${exchange})")
                .choice()
                .when(simple("${header.fileFormatIsCorrect} == true"))
                .log("${header.nameOfFile}")
                .toD("${body}")
                .toD("file:images?fileName="+ UUID.randomUUID()+"-"+"${header.nameOfFile}")
                .log("Done downloading images")
                .to("direct:s3Upload")
                .otherwise()
                .log("The file is not in the correct format pushed to incorrect format queues")
                .end();
    }


}
