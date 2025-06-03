package com.example.redlettuce;

import org.HdrHistogram.Histogram;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class HistogramWriter {
    private final Histogram histogram = new Histogram(3600000000000L, 3);

    // TODO - not ideal that this is synchronized
    public synchronized void recordValue(long valueMicros) {
        histogram.recordValue(valueMicros);
    }

    public synchronized void dumpAndReset(String filename) {
        System.out.println("Writing Histogram: "+filename);
        try (PrintStream writer = new PrintStream(new FileOutputStream(filename, true))) {
            histogram.outputPercentileDistribution(writer, 1000.0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        histogram.reset();
    }
}
