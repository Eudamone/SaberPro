package dto;

public class ModulePerformance {

    private final String moduleName;
    private final ScoreStatistics statistics;

    public ModulePerformance(String moduleName, ScoreStatistics statistics) {
        this.moduleName = moduleName;
        this.statistics = statistics;
    }

    public String getModuleName() {
        return moduleName;
    }

    public ScoreStatistics getStatistics() {
        return statistics;
    }
}

