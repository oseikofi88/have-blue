package io.greyparrot.Routes;

import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class GoogleSearch extends RouteBuilder {

    @Override
    public void configure() {


        from("direct:google")
//                .toD("${env:GOOGLE_APIS_ENDPOINT}/customsearch/v1?cx=${env:GOOGLE_SEARCH_ENGINE_ID}&exactTerms=${body}&num=${env:GOOGLE_NUMBER_OF_RESULTS}&searchType=image&key=${env:GOOGLE_API_KEY}")
                .toD("https://run.mocky.io/v3/49944f3a-3d9e-4e60-a7ba-cdb5f3310093")
                .convertBodyTo(String.class)
                .log("response code google search is: ${header.CamelHttpResponseCode}")
                .log("response body from google search: ${body}")
//                .when().jsonpath("$.searchInformation[?(@.totalResults == '0')]")
//                .log("The search information yielded no results")
                .split(jsonpath("$.items.*.link"))
                .to("direct:imageLink")
//                .otherwise()
//                .log("Search information yielded so much results")
                .end();

//                .split(simple("${body}"))

        from("direct:imageLink")
                .log("Getting some links hold on")
                .log("${body}")
                .setExchangePattern(ExchangePattern.InOnly)
                .toD("rabbitmq:image_links?queue=image_links&autoDelete=false");


    }


}
