package io.greyparrot.Routes;


import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class Facebook extends RouteBuilder {
    @Override
    public void configure() {
        from("rabbitmq:labels?queue=facebook&autoDelete=false&exchangeType=fanout")
                .log("this is fb body ${body}");

    }
}
