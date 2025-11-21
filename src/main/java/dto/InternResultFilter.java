package dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InternResultFilter {

    private final List<Integer> periods;
    private final List<String> areas;
    private final List<String> nbc;

    public InternResultFilter() {
        this.periods = new ArrayList<>();
        this.areas = new ArrayList<>();
        this.nbc = new ArrayList<>();
    }

    public InternResultFilter(List<Integer> periods, List<String> areas, List<String> nbc) {
        this.periods = periods == null ? new ArrayList<>() : new ArrayList<>(periods);
        this.areas = areas == null ? new ArrayList<>() : new ArrayList<>(areas);
        this.nbc = nbc == null ? new ArrayList<>() : new ArrayList<>(nbc);
    }

    public boolean hasPeriods() {
        return !periods.isEmpty();
    }

    public boolean hasAreas() {
        return !areas.isEmpty();
    }

    public boolean hasNbc() {
        return !nbc.isEmpty();
    }

    public List<Integer> getPeriods() {
        return Collections.unmodifiableList(periods);
    }

    public List<String> getAreas() {
        return Collections.unmodifiableList(areas);
    }

    public List<String> getNbc() {
        return Collections.unmodifiableList(nbc);
    }
}

