package dto;

import java.util.List;

public class ReportContextStats extends ReportContext {
    private final long evaluatedCount;
    private final long testedCount;

    public ReportContextStats(List<Integer> periods,
                              List<Integer> semesters,
                              List<String> areas,
                              List<String> nbc,
                              long evaluatedCount,
                              long testedCount) {
        super(periods, semesters, areas, nbc, evaluatedCount);
        this.evaluatedCount = evaluatedCount;
        this.testedCount = testedCount;
    }

    public long getEvaluatedCount() {
        return evaluatedCount;
    }

    public long getTestedCount() {
        return testedCount;
    }
}
