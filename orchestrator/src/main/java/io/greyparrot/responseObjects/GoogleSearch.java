package io.greyparrot.responseObjects;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GoogleSearch extends RouteBuilder {

    @Override
    public void configure() {


        from("direct:google")
                .toD("${env:GOOGLE_APIS_ENDPOINT}/customsearch/v1?cx=${env:GOOGLE_SEARCH_ENGINE_ID}&exactTerms=${body}&num=${env:GOOGLE_NUMBER_OF_RESULTS}&searchType=image&key=${env:GOOGLE_API_KEY}")
                .convertBodyTo(String.class)
                .log("response code google search is: ${header.CamelHttpResponseCode}")
                .log("response body from google search: ${body}")
                .choice()
                .when().jsonpath("$.searchInformation[?(@.totalResults == '0')]")
                .log("The search information yielded no results")
                .otherwise()
                .log("Search information yielded so much results")
                .end();

//                .split(simple("${body}"))


    }
}
