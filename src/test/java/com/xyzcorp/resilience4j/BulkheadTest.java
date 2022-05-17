package com.xyzcorp.resilience4j;

import io.github.resilience4j.bulkhead.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class BulkheadTest {


    public Returnable sleepFor(long millis) {
        try {
            Thread.sleep(millis);
            return new Returnable();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSemaphoreBulkhead() throws InterruptedException {
        //Regular Bulkhead config will be a semaphore bulkhead
        BulkheadConfig bulkheadConfig = BulkheadConfig
            .custom()
            .maxConcurrentCalls(10)
            .maxWaitDuration(Duration.ofSeconds(2))
            .fairCallHandlingStrategyEnabled(false)
            .writableStackTraceEnabled(false).build();
        BulkheadRegistry bulkheadRegistry = BulkheadRegistry.of(bulkheadConfig);
        Bulkhead bulkhead = bulkheadRegistry.bulkhead("service-a");

        IntStream.range(1, 60)
                 .boxed()
                 .map(i -> Bulkhead.decorateSupplier(bulkhead,
                     () -> sleepFor(5000).andReturn(String.format("done-%d", i))))
                 .map(CompletableFuture::supplyAsync)
                 .forEach(cf -> cf.exceptionally(Throwable::getMessage).thenAccept(System.out::println));

        ScheduledExecutorService scheduledExecutorService =
            Executors.newScheduledThreadPool(3);
        scheduledExecutorService.scheduleWithFixedDelay(() ->
            displayStatus(bulkhead), 1, 1, TimeUnit.SECONDS);

        Thread.sleep(60000);
    }

    @Test
    void testThreadPoolBulkhead() throws InterruptedException {
        ThreadPoolBulkheadConfig config =
            ThreadPoolBulkheadConfig
                .custom()
                .maxThreadPoolSize(10)
                .coreThreadPoolSize(2)
                .queueCapacity(20)
                .keepAliveDuration(Duration.of(10, ChronoUnit.MILLIS))
                .build();

        ThreadPoolBulkheadRegistry registry =
            ThreadPoolBulkheadRegistry.of(config);
        ThreadPoolBulkhead bulkhead = registry.bulkhead("service-a");

        IntStream.range(1, 10)
                 .boxed()
                 .map(i -> ThreadPoolBulkhead.decorateSupplier(bulkhead,
                     () -> sleepFor(5000).andReturn(String.format("done-%d", i))))
                 .map(Supplier::get)
                 .forEach(cf -> cf.exceptionally(Throwable::getMessage).thenAccept(System.out::println));

        ScheduledExecutorService scheduledExecutorService =
            Executors.newScheduledThreadPool(3);
        scheduledExecutorService.scheduleWithFixedDelay(() ->
            displayStatus(bulkhead), 1, 1, TimeUnit.SECONDS);

        Thread.sleep(30000);
    }

    private void displayStatus(ThreadPoolBulkhead serviceABulkhead) {
        System.out.println("After step for ThreadPool:");
        ThreadPoolBulkhead.Metrics metrics = serviceABulkhead.getMetrics();
        System.out.format("\tCore Thread Pool Size: %d\n",
            metrics.getCoreThreadPoolSize());
        System.out.format("\tMaximum Thread Pool Size: %s\n",
            metrics.getMaximumThreadPoolSize());
        System.out.format("\tThread Pool Size: %s\n",
            metrics.getThreadPoolSize());
        System.out.format("\tQueue Capacity: %s\n", metrics.getQueueCapacity());
        System.out.format("\tQueue Depth: %s\n", metrics.getQueueDepth());
        System.out.format("\tRemain Queue Capacity: %s\n",
            metrics.getRemainingQueueCapacity());
    }

    private void displayStatus(Bulkhead serviceABulkhead) {
        System.out.println("After step for SemaphoreBulkhead:");
        Bulkhead.Metrics metrics = serviceABulkhead.getMetrics();
        System.out.format("\tAvailable Concurrent Calls: %d\n",
            metrics.getAvailableConcurrentCalls());
        System.out.format("\tMaximum Allowed Concurrent Calls: %s\n",
            metrics.getMaxAllowedConcurrentCalls());
    }
}
