package dto;

public class PeriodTrend {

    private final Integer period;
    private final Double averageScore;
    private final Long evaluatedCount;

    public PeriodTrend(Integer period, Double averageScore, Long evaluatedCount) {
        this.period = period;
        this.averageScore = averageScore;
        this.evaluatedCount = evaluatedCount;
    }

    public Integer getPeriod() {
        return period;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public Long getEvaluatedCount() {
        return evaluatedCount;
    }
}

