package com.webflux.reactdemo.flux;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.CountDownLatch;


public class SubscribeOnDemo {

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        System.out.println("main " + Thread.currentThread().getName());
        Scheduler s = Schedulers.newParallel("parallel-scheduler", 4);

        final Flux<String> flux = Flux
            .range(1, 2)
            .map(i -> {
                System.out.println("map1 " + Thread.currentThread().getName());
                return 10 + i;
            })
            .subscribeOn(s)
            .map(i -> {
                System.out.println("map2 " + Thread.currentThread().getName());
                return "value " + i;
            }).doOnComplete(countDownLatch::countDown);

        new Thread(() -> flux.subscribe(ss -> {
            System.out.println("subscriber " + Thread.currentThread().getName());
            System.out.println(ss);
        })).start();
        System.out.println("main end " + Thread.currentThread().getName());
        countDownLatch.await();
    }
}
