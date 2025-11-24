package dto;

public class ModulePerformance {
    private final String moduleName;
    private final ScoreStatistics statistics;
    private final Double referenceAverage;
    private final Double variation;

    public ModulePerformance(String moduleName, ScoreStatistics statistics) {
        this(moduleName, statistics, null, null);
    }

    public ModulePerformance(String moduleName,
                             ScoreStatistics statistics,
                             Double referenceAverage,
                             Double variation) {
        this.moduleName = moduleName;
        this.statistics = statistics;
        this.referenceAverage = referenceAverage;
        this.variation = variation;
    }

    public String getModuleName() {
        return moduleName;
    }

    public ScoreStatistics getStatistics() {
        return statistics;
    }

    public Double getReferenceAverage() {
        return referenceAverage;
    }

    public Double getVariation() {
        return variation;
    }
}