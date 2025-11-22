package dto;

import java.util.List;

public class ReportContext {

    private final List<Integer> periods;
    private final List<Integer> semesters;
    private final List<String> areas;
    private final List<String> nbc;
    private final long evaluatedCount;

    public ReportContext(List<Integer> periods, List<Integer> semesters, List<String> areas, List<String> nbc, long evaluatedCount) {
        this.periods = periods;
        this.semesters = semesters;
        this.areas = areas;
        this.nbc = nbc;
        this.evaluatedCount = evaluatedCount;
    }

    public List<Integer> getPeriods() {
        return periods;
    }

    public List<Integer> getSemesters() {
        return semesters;
    }

    public List<String> getAreas() {
        return areas;
    }

    public List<String> getNbc() {
        return nbc;
    }

    public long getEvaluatedCount() {
        return evaluatedCount;
    }
}

