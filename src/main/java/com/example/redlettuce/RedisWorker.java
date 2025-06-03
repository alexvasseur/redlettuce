package com.example.redlettuce;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Random;

public class RedisWorker implements Runnable {

    private final StringRedisTemplate redisTemplate;
    private final HistogramWriter histogramWriter;
    private final Random random = new Random();
    private volatile boolean running = true;

    private final int writeRatio;
    private final int readRatio;
    private final int totalRatio;

    public RedisWorker(StringRedisTemplate redisTemplate, HistogramWriter histogramWriter, int writeRatio, int readRatio) {
        this.redisTemplate = redisTemplate;
        this.histogramWriter = histogramWriter;
        this.writeRatio = writeRatio;
        this.readRatio = readRatio;
        this.totalRatio = writeRatio + readRatio;
    }

    @Override
    public void run() {
        while (running) {
            int key = random.nextInt(100_000);
            String redisKey = "key" + key;
            String redisValue = "val" + key;
            boolean doWrite = random.nextInt(totalRatio) < writeRatio;

            long start = System.nanoTime();
            try {
                if (doWrite) {
                    redisTemplate.opsForValue().set(redisKey, redisValue);
                } else {
                    redisTemplate.opsForValue().get(redisKey);
                }
            } catch (Exception e) {
                System.err.println("Error: "+e.getMessage());
            }
            long latencyMicros = (System.nanoTime() - start) / 1_000;
            histogramWriter.recordValue(latencyMicros);
        }
    }

    public void stop() {
        this.running = false;
    }
}
