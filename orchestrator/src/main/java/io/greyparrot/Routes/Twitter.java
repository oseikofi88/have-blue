package io.greyparrot.Routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class Twitter extends RouteBuilder {
    @Override
    public void configure() {
        from("rabbitmq:labels?queue=twitter&autoDelete=false&exchangeType=fanout")
                .log("this is twitter's body ${body}");

    }
}
