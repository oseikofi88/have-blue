package io.greyparrot;

import io.greyparrot.responseObjects.HealthCheckResponse;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class RestRouter extends RouteBuilder {

    @Override
    public void configure() {

        restConfiguration()
                .component("servlet")
                //.bindingMode(RestBindingMode.json)
                .producerComponent("http");

        rest()
                .get("/").route().bean(HealthCheckResponse.class)
                .endRest();

    }

}

