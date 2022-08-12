package com.codestates.coffee;

// TODO CoffeeController 에 Spring WebFlux 를 적용해 주세요. Spring MVC 방식 아닙니다!!

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/v12/coffees")
public class CoffeeController {
    private final CoffeeService coffeeService;
    private final CoffeeMapper mapper;

    public CoffeeController(CoffeeService coffeeService, CoffeeMapper mapper) {
        this.coffeeService = coffeeService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity postCoffee(@Valid @RequestBody Mono<CoffeeDto.Post> requestBody){
        Mono<CoffeeDto.Response> result =
                requestBody
                        .flatMap(post -> coffeeService.createCoffee(mapper.coffeePostDtoToCoffee(post)))
                        .map(coffee -> mapper.coffeeToCoffeeResponseDto(coffee));

        return new ResponseEntity<>(result,HttpStatus.CREATED);
    }


}


//    @PostMapping
//    public ResponseEntity postCoffee(@Valid @RequestBody CoffeePostDto coffeePostDto) {
//        Coffee coffee = coffeeService.createCoffee(mapper.coffeePostDtoToCoffee(coffeePostDto));
//
//        return new ResponseEntity<>(
//                new SingleResponseDto<>(mapper.coffeeToCoffeeResponseDto(coffee)),
//                HttpStatus.CREATED);
//    }