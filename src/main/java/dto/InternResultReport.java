package dto;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class InternResultReport {

    private final ReportContext context;
    private final ScoreStatistics globalStatistics;
    private final List<ModulePerformance> modulePerformances;
    private final List<PeriodTrend> periodTrends;
    private final double trendVariation;

    public InternResultReport(ReportContext context,
                              ScoreStatistics globalStatistics,
                              List<ModulePerformance> modulePerformances,
                              List<PeriodTrend> periodTrends,
                              double trendVariation) {
        this.context = context;
        this.globalStatistics = globalStatistics;
        this.modulePerformances = modulePerformances;
        this.periodTrends = periodTrends;
        this.trendVariation = trendVariation;
    }

    public ReportContext getContext() {
        return context;
    }

    public ScoreStatistics getGlobalStatistics() {
        return globalStatistics;
    }

    public List<ModulePerformance> getModulePerformances() {
        return modulePerformances;
    }

    public List<PeriodTrend> getPeriodTrends() {
        return periodTrends;
    }

    public double getTrendVariation() {
        return trendVariation;
    }

    public Optional<ModulePerformance> getBestModule() {
        return modulePerformances.stream()
                .max(Comparator.comparingDouble(performance -> performance.getStatistics().getAverage()));
    }

    public Optional<ModulePerformance> getCriticalModule() {
        return modulePerformances.stream()
                .min(Comparator.comparingDouble(performance -> performance.getStatistics().getAverage()));
    }
}

