package com.reactor.webfluxclient.handler;

import com.reactor.webfluxclient.models.Product;
import com.reactor.webfluxclient.services.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class ProductHandler {

    @Autowired
    private IProductService service;

    public Mono<ServerResponse> list(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.findAll(), Product.class);
    }

    public Mono<ServerResponse> show(ServerRequest request) {
        return errorHandler(service.findById(request.pathVariable("id"))
                .flatMap(p -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(p))
                .switchIfEmpty(ServerResponse.notFound().build()));
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        Mono<Product> product = request.bodyToMono(Product.class);
        return product.flatMap(p -> {
            if (p.getCreateAt() == null) {
                p.setCreateAt(new Date());
            }
            return service.save(p);
        }).flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(p))
                .onErrorResume(error -> {
                    WebClientResponseException ex = (WebClientResponseException) error;
                    if (HttpStatus.BAD_REQUEST.equals(ex.getStatusCode())) {
                        return ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(((WebClientResponseException) error).getResponseBodyAsString());
                    }
                    return Mono.error(error);
                });
    }

    public Mono<ServerResponse> edit(ServerRequest request) {
        Mono<Product> product = request.bodyToMono(Product.class);
        String id = request.pathVariable("id");
        return errorHandler(product
                .flatMap(p -> service.update(id, p))
                .flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(id)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(p)));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");
        return errorHandler(service.delete(id).then(ServerResponse.noContent().build()));
    }

    public Mono<ServerResponse> upload(ServerRequest request) {
        String id = request.pathVariable("id");
        return errorHandler(request.multipartData()
                .map(m -> m.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(f -> service.upload(f, id))
                .flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(p))
        );
    }

    private Mono<ServerResponse> errorHandler(Mono<ServerResponse> response) {
        return response.onErrorResume(error -> {
            WebClientResponseException ex = (WebClientResponseException) error;
            if (HttpStatus.NOT_FOUND.equals(ex.getStatusCode())) {
                Map<String, Object> body = new HashMap<>();
                body.put("error", "No existe el producto: ".concat(error.getMessage()));
                body.put("timestamp", new Date());
                body.put("status", ((WebClientResponseException) error).getStatusCode().value());
                return ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(body);
            }
            return Mono.error(error);
        });
    }
}
