package dto;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InternResultReport {
    private final ReportContextStats context;
    private final ScoreStatistics internalGlobal;
    private final ScoreStatistics externalGlobal;
    private final List<ModulePerformance> internalModules;
    private final List<ModulePerformance> externalModules;
    private final List<PeriodTrend> periodTrends;
    private final double trendVariation;

    public InternResultReport(ReportContextStats context,
                              ScoreStatistics internalGlobal,
                              ScoreStatistics externalGlobal,
                              List<ModulePerformance> internalModules,
                              List<ModulePerformance> externalModules,
                              List<PeriodTrend> periodTrends,
                              double trendVariation) {
        this.context = context;
        this.internalGlobal = internalGlobal;
        this.externalGlobal = externalGlobal;
        this.internalModules = internalModules;
        this.externalModules = externalModules;
        this.periodTrends = periodTrends;
        this.trendVariation = trendVariation;
    }

    public ReportContextStats getContext() {
        return context;
    }

    public ScoreStatistics getInternalGlobal() {
        return internalGlobal;
    }

    public ScoreStatistics getExternalGlobal() {
        return externalGlobal;
    }

    public List<ModulePerformance> getInternalModules() {
        return internalModules;
    }

    public List<ModulePerformance> getExternalModules() {
        return externalModules;
    }

    public List<ModulePerformance> pairModulesWithExternalAverage() {
        return internalModules.stream()
                .map(performance -> {
                    Double reference = externalModules.stream()
                            .filter(ext -> ext.getModuleName().equals(performance.getModuleName()))
                            .map(ext -> ext.getStatistics().getAverage())
                            .findFirst()
                            .orElse(null);
                    Double internalAverage = performance.getStatistics().getAverage();
                    Double variation = reference == null || internalAverage == null
                            ? null
                            : internalAverage - reference;
                    return new ModulePerformance(
                            performance.getModuleName(),
                            performance.getStatistics(),
                            reference,
                            variation
                    );
                })
                .collect(Collectors.toList());
    }

    public List<PeriodTrend> getPeriodTrends() {
        return periodTrends;
    }

    public double getTrendVariation() {
        return trendVariation;
    }

    public Optional<ModulePerformance> getBestModule() {
        return internalModules.stream()
                .max(Comparator.comparingDouble(performance -> performance.getStatistics().getAverage()));
    }

    public Optional<ModulePerformance> getCriticalModule() {
        return internalModules.stream()
                .min(Comparator.comparingDouble(performance -> performance.getStatistics().getAverage()));
    }
}
