package io.greyparrot.Routes;

import net.minidev.json.JSONArray;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.AggregationStrategies;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.language.bean.Bean;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class AWSRekognition extends RouteBuilder {


    @Override
    public void configure() {
        from("rabbitmq:image_links?queue=image_links&autoDelete=false")
                .log("${body}")
                .log("${header.numberOfLinks}")
                .split()
                .jsonpath("$")
                .streaming()
                .log("Split performed")
                .log("${body}")
                .to("https://run.mocky.io/v3/05b252dc-c5fd-4c83-835e-ceda4c71d09b")
                .convertBodyTo(String.class)
                .aggregate(new ArrayListAggregationStrategy())
                .constant(true)
                .parallelProcessing()
                .completionSize(simple("header.numberOfLinks"))
                .log("${body}")
                .end();

    }

    private AggregationStrategy batchAggregationStrategy() {
        return new ArrayListAggregationStrategy();
    }

}
