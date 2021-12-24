package io.greyparrot.Routes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.redis.RedisConstants;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class Twitter extends RouteBuilder {

    @Override
    public void configure() {
        from("rabbitmq:labels?queue=twitter&autoDelete=false&exchangeType=fanout")
                .log("this is twitter's body ${body}")
                .unmarshal().json(JsonLibrary.Jackson)
                .log("this is twitter's body after marshal ${body.size()}")
                .setHeader("numberOfKeywords",  simple("${body.size()}"))
                .setHeader("keywords", simple("${body}"))
                .log("the number of keywords are ${header.numberOfKeywords}")
                .setHeader(Exchange.HTTP_QUERY, constant("user.fields=created_at,description,entities,id,location,name,pinned_tweet_id,profile_image_url,protected,public_metrics,url,username,verified,withheld&expansions=pinned_tweet_id"))
                .setHeader("Authorization",simple("Bearer ${env:TWITTER_BEARER_TOKEN}"))
                .toD("${env:TWITTER_BASE_URL}/users/2953199314/followers?throwExceptionOnFailure=false")
                .setHeader("redisKeyForTwitterUser",simple(UUID.randomUUID().toString()))
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
                .log("${body}")
                .otherwise()
                .log("There was an error with some stuff so we cannot process")
                .end();

    }
}