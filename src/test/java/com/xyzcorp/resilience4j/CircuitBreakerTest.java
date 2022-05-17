package com.xyzcorp.resilience4j;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.vavr.control.Try;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class CircuitBreakerTest {

    @Test
    void testCircuitBreaker() {
        class Server {
            final Queue<Supplier<String>> supplierQueue = new LinkedList<>();

            public Server() {
                supplierQueue.add(() -> "Hello");
                supplierQueue.add(() -> "Hello");
                supplierQueue.add(() -> "Hello");
                supplierQueue.add(() -> {
                    throw new RuntimeException();
                });
                supplierQueue.add(() -> {
                    throw new RuntimeException();
                });
                supplierQueue.add(() -> {
                    throw new RuntimeException();
                });
                supplierQueue.add(() -> {
                    throw new RuntimeException();
                });
                supplierQueue.add(() -> "Hello");
                supplierQueue.add(() -> "Hello");
                supplierQueue.add(() -> "Hello");
                supplierQueue.add(() -> "Hello");
                supplierQueue.add(() -> "Hello");
                supplierQueue.add(() -> "Hello");
                supplierQueue.add(() -> "Hello");
                supplierQueue.add(() -> "Hello");
                supplierQueue.add(() -> "Hello");
                supplierQueue.add(() -> "Hello");
                supplierQueue.add(() -> "Hello");
                supplierQueue.add(() -> "Hello");
            }

            public String process() {
                return supplierQueue.remove().get();
            }
        }

        CircuitBreakerConfig circuitBreakerConfig =
            CircuitBreakerConfig
                .custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(4000))
                .permittedNumberOfCallsInHalfOpenState(2)
                .slidingWindowSize(2)
                .recordExceptions(IOException.class,
                    TimeoutException.class, RuntimeException.class)
                .build();
        CircuitBreakerRegistry circuitBreakerRegistry =
            CircuitBreakerRegistry.of(circuitBreakerConfig);

        CircuitBreaker announce = circuitBreakerRegistry.circuitBreaker(
            "announce");

        Server server = new Server();
        Supplier<String> stringSupplier =
            CircuitBreaker.decorateSupplier(announce, server::process);


        for (int i = 0; i < 20; i++) {
            invoke(String.valueOf(i), stringSupplier);
            displayStatus(String.valueOf(i), announce);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void displayStatus(String tag, CircuitBreaker circuitBreaker) {
        System.out.format("After step %s:\n", tag);
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        System.out.format("\tCurrent State: %s\n", circuitBreaker.getState());
        System.out.format("\tFailure Rate: %f\n", metrics.getFailureRate());
        System.out.format("\tFailure Calls: %d\n", metrics.getNumberOfFailedCalls());
        System.out.format("\tSuccessful Calls: %d\n", metrics.getNumberOfSuccessfulCalls());
    }

    private void invoke(String tag, Supplier<String> stringSupplier) {
        Try.ofSupplier(stringSupplier).onFailure(x -> System.out.format("Step %s failed: %s\n", tag, x.getMessage())); //Hello
    }
}
