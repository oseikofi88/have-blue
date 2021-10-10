package io.greyparrot;

import io.greyparrot.Routes.HealthCheckResponse;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.stereotype.Component;


@Component
public class RestRouter extends RouteBuilder {

    @Override
    public void configure() {


        restConfiguration()
                .component("servlet")
                .producerComponent("http");

        rest()
                .get("/").route().bean(HealthCheckResponse.class)
                .endRest()
                .get("/search")
                .param().name("searchPhrase").type(RestParamType.header).required(true).endParam()
                .route()
                .log("Received search phrase is $simple{header.searchPhrase}")
                .removeHeaders("CamelHttp*")
                .setBody(simple("${header.searchPhrase}"))
                .to("direct:google")
                .endRest();
    }

}

