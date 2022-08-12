package com.codestates.coffee;


import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CoffeeRepository extends R2dbcRepository<Coffee, Long> {
    Mono<Coffee> findByCoffeeCode(String coffeeCode);
    Flux<Coffee> findByAll(Pageable pageable);

}
