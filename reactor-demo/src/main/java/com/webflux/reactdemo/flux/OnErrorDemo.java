package com.webflux.reactdemo.flux;


import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class OnErrorDemo {



  public static void main1(String[] args) throws InterruptedException {
    // interval 操作符每x个时间单位周期增加 Long 值，并发送
    Flux<String> flux = Flux.interval(Duration.ofMillis(250)).map(input -> {
      if (input < 3)
        return "tick " + input;
      throw new RuntimeException("boom");
    }).onErrorReturn("Uh oh");

    flux.subscribe(System.out::println);
    Thread.sleep(2100);
  }


  public static void main2(String[] args) throws InterruptedException {
    Flux.interval(Duration.ofMillis(250)).map(input -> {
      if (input < 3)
        return "tick " + input;
      throw new RuntimeException("boom");
    })
        // 添加 retry(1) 重试一次，而不是使用 onErrorReturn
        .retry(1)
        // elapsed 将每个值与前一个值发出后的持续时间关联起来
        .elapsed()
        .subscribe(System.out::println, System.err::println);

    Thread.sleep(2100);
  }

  public static void main3(String[] args) throws InterruptedException {
    Flux<String> flux = Flux
        .<String>error(new IllegalArgumentException())
        .doOnError(System.out::println)
        .retryWhen(Retry.from(companion ->
            companion.take(3)));
    flux.subscribe(System.out::println, s -> System.err.println(s + " error"));
  }

  public static void main4(String[] args) throws InterruptedException {
    AtomicInteger errorCount = new AtomicInteger();
    Flux<String> flux =
        Flux.<String>error(new IllegalArgumentException())
            .doOnError(e -> System.out.println(errorCount.incrementAndGet()))
            .retryWhen(Retry.from(companion ->
                companion.map(rs -> {
                  if (rs.totalRetries() < 3) return rs.totalRetries();
                  else throw Exceptions.propagate(rs.failure());
                })
            ));
    flux.subscribe(System.out::println, s -> System.err.println(s + " error"));
  }


  public static void main5(String[] args) throws InterruptedException {
    AtomicInteger errorCount = new AtomicInteger();
    AtomicInteger transientHelper = new AtomicInteger();
    Flux<Integer> transientFlux = Flux.<Integer>generate(sink -> {
          int i = transientHelper.getAndIncrement();
          if (i == 10) {
            sink.next(i);
            sink.complete();
          }
          else if (i % 3 == 0) {
            sink.next(i);
          }
          else {
            sink.error(new IllegalStateException("Transient error at " + i));
          }
        })
        .doOnError(e -> errorCount.incrementAndGet());

    transientFlux.retryWhen(Retry.max(2)
        // 如果没有 transientErrors(true)，在第二次突发错误时，将达到配置的最大尝试数 2，并且在发出 onNext(3) 后，序列将失败。
            .transientErrors(true)
        )
        .blockLast();
    // assertThat(errorCount).hasValue(6);
    System.out.println("errorCount = " + errorCount);
  }
  public static void main(String[] args) throws InterruptedException {
    main5(args);
  }
}
