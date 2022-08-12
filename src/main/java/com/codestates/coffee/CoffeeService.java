package com.codestates.coffee;

// TODO CoffeeService 에 Spring WebFlux 를 적용해 주세요. Spring MVC 방식 아닙니다!!

import com.codestates.exception.BusinessLogicException;
import com.codestates.exception.ExceptionCode;
import com.codestates.stamp.Stamp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Transactional
@Service
public class CoffeeService {
    private final CoffeeRepository coffeeRepository;

    private final R2dbcEntityTemplate template;

    public CoffeeService(CoffeeRepository coffeeRepository, R2dbcEntityTemplate template) {
        this.coffeeRepository = coffeeRepository;
        this.template = template;
    }

    public Mono<Coffee> createCoffee(Coffee coffee) {
        return verifyExistCode(coffee.getCoffeeCode())
                .then(coffeeRepository.save(coffee))
                .map(resultCoffee-> {
                    template.insert(new Stamp(resultCoffee.getCoffeeId())).subscribe();

                    return resultCoffee;
                });
    }

    public Mono<Coffee> updateCoffee(Coffee coffee) {
        return findVerifiedCoffee(coffee.getCoffeeId())
                .flatMap(updatingCoffee -> coffeeRepository.save(updatingCoffee));
    }

    @Transactional(readOnly = true)
    public Mono<Coffee> findCoffee(long coffeeId) {return findVerifiedCoffee(coffeeId);}

    @Transactional(readOnly = true)
    public Mono<Page<Coffee>> findCoffees(PageRequest pageRequest) {
        return coffeeRepository.findByAll(pageRequest)
                .collectList()
                .zipWith(coffeeRepository.count())
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageRequest, tuple.getT2()));
    }

    public Mono<Void> deleteCoffee(long coffeeId) {
        return findVerifiedCoffee(coffeeId)
                .flatMap(coffee -> template.delete(query(where("COFFEE_ID").is(coffeeId))))
                .then(coffeeRepository.deleteById(coffeeId));
    }


    private Mono<Void> verifyExistCode(String coffeeCode) {
        return coffeeRepository.findByCoffeeCode(coffeeCode)
                .flatMap(findCoffee -> {
                    if (findCoffee != null) {
                        return Mono.error(new BusinessLogicException(ExceptionCode.COFFEE_CODE_EXISTS));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Coffee> findVerifiedCoffee(long coffeeId) {
        return coffeeRepository
                .findById(coffeeId)
                .switchIfEmpty(Mono.error(new BusinessLogicException(ExceptionCode.COFFEE_NOT_FOUND)));
    }
}
