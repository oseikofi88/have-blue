package works.wima.Routes;

import com.amazonaws.services.dynamodbv2.xspec.S;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.redis.RedisConstants;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.el.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class Twitter extends RouteBuilder {


    @Override
    public void configure() {

        onException().stop();
        from("{{LABELS_QUEUE}}")
                .log("this is twitter's body ${body}")
                .unmarshal().json(JsonLibrary.Jackson)
                .log("this is twitter's body after marshal ${body.size()}")
                .setHeader("numberOfKeywords",  simple("${body.size()}"))
                .setHeader("keywords", simple("${body}"))
                .setHeader("userId",simple("2953199314"))
                .log("the number of keywords are ${header.numberOfKeywords}")
                .setHeader(Exchange.HTTP_QUERY, constant("user.fields=created_at,description,entities,id,location,name,pinned_tweet_id,profile_image_url,protected,public_metrics,url,username,verified,withheld&expansions=pinned_tweet_id"))
                .setHeader("Authorization",simple("Bearer ${env:TWITTER_BEARER_TOKEN}"))
                .setBody(simple("${env:TWITTER_BASE_URL}/users/${header.userId}/following?throwExceptionOnFailure=false,${env:TWITTER_BASE_URL}/users/${header.userId}/followers?throwExceptionOnFailure=false"))
                .split(body().tokenize(","))
                .streaming()
                .parallelProcessing()
                .log("This is the split ${body}")
                .toD("${body}")
                .aggregate(new ArrayListAggregationStrategy()).constant(false)
                .completionSize(2)
                .setHeader("redisKeyForTwitterUser",simple(UUID.randomUUID().toString()))
                .setProperty("initialBody", simple("${body}"))
                .loop(2)
                .log("the loop index is ${exchangeProperty.CamelLoopIndex}")
                .setBody(simple("${body[${exchangeProperty.CamelLoopIndex}]}"))
                .unmarshal().json(JsonLibrary.Jackson)
                .log("The after body is ${body}")
                .choice()
                .when(header(Exchange.HTTP_RESPONSE_CODE).isLessThan(300))
                .split()
                .jsonpath("$.data")
                .streaming()
                .loop(simple("${header.numberOfKeywords}"))
                .filter(simple("${body[description]} contains ${header.keywords[${exchangeProperty.CamelLoopIndex}]} " +
                        "|| ${body[name]} contains ${header.keywords[${exchangeProperty.CamelLoopIndex}]} " +
                        "|| ${body[username]} contains ${header.keywords[${exchangeProperty.CamelLoopIndex}]} "))
                .log("The twitter user ${body[username]} details contain something that is in the keyword being searched for")
                .setHeader("CamelRedis.Key",simple("${header.redisKeyForTwitterUser}"))
                .setHeader("CamelRedis.Value",simple("${body[username]}"))
                .setHeader(RedisConstants.COMMAND, constant("RPUSH"))
                .setHeader("bodyOfInterest", simple("${body}"))
                .toD("spring-redis://${env:REDIS_URL}?redisTemplate=#redisTemplate")
                .log("Result after saving it in redis is ${body}")
                .setBody(simple("${header.bodyOfInterest}"))
                .log("Old body is ${body}")
                .end()// end filter
                .end()// end loop;
//                 HTTP status >= 300 : would throw an exception if we had "throwExceptionOnFailure=true"
                .endChoice()
                .log("So the final usernames list are ")
                .setHeader(RedisConstants.COMMAND,constant("LRANGE"))
                .setHeader("CamelRedis.Key",simple("${header.redisKeyForTwitterUser}"))
                .setHeader("CamelRedis.Start", constant(0))
                .setHeader("CamelRedis.End", constant(-1))
                .toD("spring-redis://${env:REDIS_URL}?redisTemplate=#redisTemplate")
                .process(new Processor() {
                    //code smell, please refactor
                    public void process(Exchange exchange) throws Exception {
                        String payload = exchange.getMessage().getBody(String.class);
                        String[] content = payload.split(",");
                        ArrayList<String> finalContent = (ArrayList<String>) Arrays.stream(content).distinct().collect(Collectors.toList());
                        // do something with the payload and/or exchange here
                        exchange.getMessage().setBody(finalContent);
                    }})//                .log("Start process to save to s3")
                .log("${body}")
                .otherwise()
                .log("There was an error with some stuff so we cannot process")
                .end()
                .setBody(exchangeProperty("initialBody"))
                .end();

    }
}