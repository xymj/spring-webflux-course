package com.webflux.reactdemo.flux;


import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class StepVerifierDemo {

    public static  <T> Flux<T> appendBoomError(Flux<T> source) {
        return source.concatWith(Mono.error(new IllegalArgumentException("boom")));
    }

    public static void testAppendBoomError() {
        Flux<String> source = Flux.just("thing1", "thing2");

        StepVerifier.create(
                appendBoomError(source))
            .expectNext("thing1")
            .expectNext("thing2")
            .expectErrorMessage("boom")
            .verify();
    }


    public static void main(String[] args) {
        testAppendBoomError();
    }
}
