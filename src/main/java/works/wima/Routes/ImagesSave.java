package works.wima.Routes;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.processor.aggregate.GroupedExchangeAggregationStrategy;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ImagesSave extends RouteBuilder {


    @Override
    public void configure() {
        from("rabbitmq:image_links?queue=image_links&autoDelete=false")
                .log("${body}")
                .log("${header.traceId}")
                .unmarshal().json(JsonLibrary.Jackson)
                .split()
                .jsonpath("$")
                .streaming()
                .parallelProcessing()
                .bean(Service.class,"getNameOfFile(${exchange})")
                .log("the number of valid links from the route are ${header.numberOfValidImageLinks}")
                .choice()
                .when(simple("${header.fileFormatIsCorrect} == true"))
                .log("${header.nameOfFile}")
                .bean(Service.class, "getLabelsDetails(${body})")
                .log("${body}")
                .to("direct:mylinks")
                .otherwise()
                .log("The file ${header.nameOfFile} is not in the correct format pushed to incorrect format queues")
                .end();
    }


}
