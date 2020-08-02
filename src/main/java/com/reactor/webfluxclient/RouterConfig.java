package com.reactor.webfluxclient;

import com.reactor.webfluxclient.handler.ProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(ProductHandler handler) {
        return RouterFunctions.route(GET("/api/client"), handler::list)
                .andRoute(GET("/api/client/{id}"), handler::show)
                .andRoute(POST("/api/client"), handler::create)
                .andRoute(PUT("/api/client/{id}"), handler::edit)
                .andRoute(DELETE("/api/client/{id}"), handler::delete)
                .andRoute(POST("/api/client/upload/{id}"), handler::upload);
    }
}
