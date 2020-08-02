package com.reactor.webfluxclient.services;

import com.reactor.webfluxclient.models.Product;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IProductService {

    Flux<Product> findAll();

    Mono<Product> findById(String id);

    Mono<Product> save(Product product);

    Mono<Product> update(String id, Product product);

    Mono<Void> delete(String id);

    Mono<Product> upload(FilePart file, String id);
}
