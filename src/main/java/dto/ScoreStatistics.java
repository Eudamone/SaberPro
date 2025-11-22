package dto;

import java.util.Collections;
import java.util.List;

public class ScoreStatistics {

    private final double average;
    private final double standardDeviation;
    private final Integer min;
    private final Integer max;
    private final long sampleSize;

    public ScoreStatistics(double average, double standardDeviation, Integer min, Integer max, long sampleSize) {
        this.average = average;
        this.standardDeviation = standardDeviation;
        this.min = min;
        this.max = max;
        this.sampleSize = sampleSize;
    }

    public static ScoreStatistics empty() {
        return new ScoreStatistics(0.0, 0.0, null, null, 0);
    }

    public static ScoreStatistics from(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return empty();
        }
        long count = values.size();
        double sum = 0.0;
        for (Integer value : values) {
            sum += value;
        }
        double average = sum / count;
        double variance = 0.0;
        for (Integer value : values) {
            double delta = value - average;
            variance += delta * delta;
        }
        variance = variance / count;
        double standardDeviation = Math.sqrt(variance);
        Integer min = Collections.min(values);
        Integer max = Collections.max(values);
        return new ScoreStatistics(average, standardDeviation, min, max, count);
    }

    public static ScoreStatistics fromAggregate(Double average, Double stddev, Integer min, Integer max, Long sampleSize) {
        if (sampleSize == null || sampleSize == 0) {
            return empty();
        }
        double avg = average == null ? 0.0 : average;
        double sd = stddev == null ? 0.0 : stddev;
        return new ScoreStatistics(avg, sd, min, max, sampleSize);
    }

    public double getAverage() {
        return average;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public Integer getMin() {
        return min;
    }

    public Integer getMax() {
        return max;
    }

    public long getSampleSize() {
        return sampleSize;
    }
}
