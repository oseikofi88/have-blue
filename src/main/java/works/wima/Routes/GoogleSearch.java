package works.wima.Routes;

import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GoogleSearch extends RouteBuilder {

    @Override
    public void configure() {


        //todo for errors throw it to the top of the function and handle it as appropriate
        //todo  handling for files less than 12mb and only jpg and png files need to be done


        from("direct:google")
                .toD("${env:GOOGLE_APIS_ENDPOINT}/customsearch/v1?cx=${env:GOOGLE_SEARCH_ENGINE_ID}&exactTerms=${body}&num=${env:GOOGLE_NUMBER_OF_RESULTS}&searchType=image&key=${env:GOOGLE_API_KEY}")
                .convertBodyTo(String.class)
                .log("response code google search is: ${header.CamelHttpResponseCode}")
                .log("response body from google search: ${body}")
                .setBody().jsonpath("$.items[?(@.image.byteSize < 500000 )].link")
                .setHeader("numberOfLinks",jsonpath("$.length()"))
                .setHeader("traceId",constant(UUID.randomUUID().toString()))
                .log("The number of links are ${header.numberOfLinks}")
                .marshal().json(JsonLibrary.Jackson)
                .setExchangePattern(ExchangePattern.InOnly)
                .toD("{{IMAGE_LINKS_QUEUE}}")
                .end();



    }


}
