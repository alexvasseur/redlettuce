package com.example.redlettuce;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.resource.Delay;

//import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.time.Duration;

@SpringBootApplication
public class RedisBenchmarkApplication {

    private static final int NUM_THREADS = 10;
    private static final int ITERATIONS = 20;
    private static final int TEST_DURATION_SECONDS = 30;

    @Value("${wrRatio}")
    private String wrRatio;

    private int writeRatio = 1;
    private int readRatio = 1;

    private final StringRedisTemplate redisTemplate;
    private final HistogramWriter histogramWriter = new HistogramWriter();

    public RedisBenchmarkApplication(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public static void main(String[] args) {
        int thread = NUM_THREADS;
        if (args != null && args.length == 1) thread = Integer.parseInt(args[0]);
        System.out.println("Running with threads: " + thread);
        SpringApplication.run(RedisBenchmarkApplication.class, args);
    }

    //@PostConstruct
    @EventListener(ApplicationReadyEvent.class)
    public void runBenchmark() throws InterruptedException {
        parseRatio();

        Thread main = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            histogramWriter.dumpAndReset("latency-iteration-shutdown.txt");
            main.interrupt(); // this will stop the main thread from waiting the TEST_DURATION_SECONDS
        }));

        for (int iteration = 1; iteration <= ITERATIONS; iteration++) {
            System.out.println("Starting iteration " + iteration);

            List<RedisWorker> tasks = new ArrayList<>();
            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

            for (int i = 0; i < NUM_THREADS; i++) {
                RedisWorker task = new RedisWorker(redisTemplate, histogramWriter, writeRatio, readRatio);
                tasks.add(task);
                executor.submit(task);
            }

            try {
                for (int i = 0; i < TEST_DURATION_SECONDS/2; i++) {
                    TimeUnit.SECONDS.sleep(2);
                    if (Thread.currentThread().isInterrupted()) {
                        System.err.println("Interrupted - shutting down...");
                        return;
                    }
                }

            } finally {
                for (RedisWorker task : tasks) {
                    task.stop();
                }

                //executor.shutdownNow();
                executor.awaitTermination(5, TimeUnit.SECONDS); // let the thread finish - else we get RedisCommandInterruptedException
                histogramWriter.dumpAndReset("latency-iteration-" + iteration + ".txt");
            }
        }
    }

    private void parseRatio() {
        String[] parts = wrRatio.split(":");
        if (parts.length == 2) {
            try {
                writeRatio = Integer.parseInt(parts[0]);
                readRatio = Integer.parseInt(parts[1]);
                System.out.printf("Configured WR ratio: write=%d, read=%d%n", writeRatio, readRatio);
            } catch (NumberFormatException e) {
                System.err.println("Invalid wrRatio format. Using default 1:1.");
            }
        } else {
            System.err.println("Invalid wrRatio format. Expected format is write:read. Using default 1:1.");
        }
    }
}
