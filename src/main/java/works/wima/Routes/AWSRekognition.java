package works.wima.Routes;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class AWSRekognition extends RouteBuilder {

    //todo  the links are more than 1 make sure all you can query for all the link and get all of them asyncly and get the name of the top 5 or 10

    @Override
    public void configure() {

        from("{{S3_LINKS_QUEUE}}")
                .log("${body}")
//                .bean(Service.class, "getOldLabelDetails")
                .log(" body after label call is ${body}")
                .marshal().json(JsonLibrary.Jackson)
                .setExchangePattern(ExchangePattern.InOnly)
                .toD("{{LABELS_QUEUE}}")
                .log("Done pushing to labels queue");


    }

}
