package com.xyzcorp.resilience4j;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.vavr.control.Try;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class RetryTest {

    @Test
    void testRetry() {
        class Server {
            final Queue<Supplier<MyHttpResponse>> supplierQueue = new LinkedList<>();

            public Server() {
                supplierQueue.add(() -> new MyHttpResponse(500, "Server Error"));
                supplierQueue.add(() -> new MyHttpResponse(500, "Server Error"));
                supplierQueue.add(() -> new MyHttpResponse(500, "Server Error"));
                supplierQueue.add(() -> new MyHttpResponse(500, "Server Error"));
                supplierQueue.add(() -> new MyHttpResponse(200, "Hello"));
                supplierQueue.add(() -> new MyHttpResponse(200, "Hello"));
                supplierQueue.add(() -> new MyHttpResponse(200, "Hello"));
                supplierQueue.add(() -> new MyHttpResponse(200, "Hello"));
                supplierQueue.add(() -> new MyHttpResponse(200, "Hello"));
                supplierQueue.add(() -> new MyHttpResponse(200, "Hello"));
                supplierQueue.add(() -> new MyHttpResponse(200, "Hello"));
            }

            public MyHttpResponse process() {
                System.out.println("Invoked Again");
                return supplierQueue.remove().get();
            }
        }

        RetryConfig config = RetryConfig.<MyHttpResponse>custom()
                                        .maxAttempts(2) //change this to 5
                                        .waitDuration(Duration.ofMillis(1000))
                                        .retryOnResult(response -> response.status() == 500)
                                        .retryOnException(e -> e instanceof WebServiceException)
                                        .retryExceptions(IOException.class, TimeoutException.class)
                                        .ignoreExceptions(BusinessException.class, OtherBusinessException.class)
                                        .failAfterMaxAttempts(true)
                                        .build();

        RetryRegistry registry = RetryRegistry.of(config);
        Retry retry = registry.retry("main", config);

        Server server = new Server();
        retry.executeSupplier(server::process);
        displayStatus(retry);
    }

    private void displayStatus(Retry retry) {
        System.out.format("After retry");
        Retry.Metrics metrics = retry.getMetrics();
        System.out.format("\tFailed Calls without Retry: %d\n", metrics.getNumberOfFailedCallsWithoutRetryAttempt());
        System.out.format("\tNumber of Failed Calls: %d\n", metrics.getNumberOfFailedCallsWithRetryAttempt());
        System.out.format("\tSuccessful Calls: %d\n", metrics.getNumberOfSuccessfulCallsWithoutRetryAttempt());
        System.out.format("\tSuccessful Calls: %d\n", metrics.getNumberOfSuccessfulCallsWithRetryAttempt());
    }
}
