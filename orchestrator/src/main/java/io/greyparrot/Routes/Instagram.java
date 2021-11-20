package io.greyparrot.Routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class Instagram extends RouteBuilder {
    @Override
    public void configure() {
        from("rabbitmq:labels?queue=instagram&autoDelete=false&exchangeType=fanout")
                .log("this is ig's ${body}");

    }
}
