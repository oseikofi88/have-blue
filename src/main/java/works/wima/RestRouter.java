package works.wima;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import works.wima.Routes.HealthCheckResponse;
import works.wima.Routes.Service;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


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
                .endRest()
                .get("/lookup-results")
                .param().name("results").type(RestParamType.header).required(true).endParam()
                .route()
                .log("Received results is $simple{header.results}")
                .removeHeaders("CamelHttp*")
                .setBody().method(Service.class, "formatResponse(${header.results})")
                .marshal().json()
                .endRest()
                .get("/webhook/twitter")
                .param().name("crc_token").type(RestParamType.header).required(true).endParam()
                .route()
                .log("${header.crc_token}")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        String twitterCrcToken = exchange.getIn().getHeader("crc_token").toString();
                        String consumerSecret = System.getenv("TWITTER_API_SECRET");
                        try {
                            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
                            SecretKeySpec secretKey = new SecretKeySpec(consumerSecret.getBytes(), "HmacSHA256");
                            sha256_HMAC.init(secretKey);
                            String hash = "sha256="+ Base64.encodeBase64String(sha256_HMAC.doFinal(twitterCrcToken.getBytes()));

                            ObjectMapper mapper = new ObjectMapper();
                            ObjectNode response = mapper.createObjectNode();
                            response.put("response_token", hash);
                            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
                            exchange.getIn().setBody(json);
                        }
                        catch (Exception e){
                            log.error("There was an error in producing the response token" + e.getMessage());
                        }
                    }
                })
                .endRest();
    }
}

