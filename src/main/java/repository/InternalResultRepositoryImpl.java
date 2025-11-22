package repository;

import dto.InternResultFilter;
import dto.InternResultInfo;
import dto.InternResultReport;
import dto.ModulePerformance;
import dto.PeriodTrend;
import dto.ReportContext;
import dto.ScoreStatistics;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import model.ExternalGeneralResult;
import model.InternalModuleResult;
import model.InternalResult;
import model.Modulo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Repository
public class InternalResultRepositoryImpl implements InternalResultRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<InternResultInfo> findResults(Pageable pageable, InternResultFilter filter) {
        InternResultFilter safeFilter = filter == null ? new InternResultFilter() : filter;
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<InternResultInfo> query = builder.createQuery(InternResultInfo.class);
        Root<InternalResult> root = query.from(InternalResult.class);
        List<Predicate> predicates = buildPredicates(safeFilter, builder, root, query);

        query.select(builder.construct(
                        InternResultInfo.class,
                        root.get("periodo"),
                        root.get("semestre"),
                        root.get("nombre"),
                        root.get("numeroRegistro"),
                        root.get("programa"),
                        root.get("puntajeGlobal"),
                        root.get("grupoReferencia")
                ))
                .distinct(true);

        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }

        query.orderBy(
                builder.asc(root.get("periodo")),
                builder.asc(root.get("semestre")),
                builder.asc(root.get("numeroRegistro"))
        );

        TypedQuery<InternResultInfo> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<InternResultInfo> content = typedQuery.getResultList();
        long total = countResults(safeFilter);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public long countResults(InternResultFilter filter) {
        InternResultFilter safeFilter = filter == null ? new InternResultFilter() : filter;
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<InternalResult> root = query.from(InternalResult.class);
        List<Predicate> predicates = buildPredicates(safeFilter, builder, root, query);

        query.select(builder.countDistinct(root));
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public InternResultReport generateReport(InternResultFilter filter) {
        InternResultFilter safeFilter = filter == null ? new InternResultFilter() : filter;
        long evaluatedCount = countResults(safeFilter);
        ReportContext context = new ReportContext(
                safeFilter.getPeriods(),
                safeFilter.getSemesters(),
                safeFilter.getAreas(),
                safeFilter.getNbc(),
                evaluatedCount
        );
        ScoreStatistics globalStatistics = buildGlobalStatistics(safeFilter);
        List<ModulePerformance> modulePerformances = buildModulePerformances(safeFilter);
        List<PeriodTrend> periodTrends = buildPeriodTrends(safeFilter);
        double trendVariation = computeTrendVariation(periodTrends);

        return new InternResultReport(context, globalStatistics, modulePerformances, periodTrends, trendVariation);
    }

    private ScoreStatistics buildGlobalStatistics(InternResultFilter filter) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = builder.createTupleQuery();
        Root<InternalResult> root = query.from(InternalResult.class);
        List<Predicate> predicates = buildPredicates(filter, builder, root, query);

        Expression<Integer> globalScore = root.get("puntajeGlobal");
        query.multiselect(
                builder.avg(globalScore),
                builder.function("stddev_pop", Double.class, globalScore),
                builder.min(globalScore),
                builder.max(globalScore),
                builder.count(globalScore)
        );
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }

        Tuple tuple = entityManager.createQuery(query).getSingleResult();
        return ScoreStatistics.fromAggregate(
                tuple.get(0, Double.class),
                tuple.get(1, Double.class),
                tuple.get(2, Integer.class),
                tuple.get(3, Integer.class),
                tuple.get(4, Long.class)
        );
    }

    private List<ModulePerformance> buildModulePerformances(InternResultFilter filter) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = builder.createTupleQuery();
        Root<InternalResult> root = query.from(InternalResult.class);
        Join<InternalResult, InternalModuleResult> moduleJoin = root.join("internalModuleResults", JoinType.LEFT);
        Join<InternalModuleResult, Modulo> moduloJoin = moduleJoin.join("modulo", JoinType.LEFT);

        List<Predicate> predicates = buildPredicates(filter, builder, root, query, moduleJoin);

        Expression<String> moduleName = builder.upper(builder.trim(moduloJoin.get("nombre")));
        Expression<Integer> moduleScore = moduleJoin.get("puntaje");
        query.multiselect(
                moduleName,
                builder.avg(moduleScore),
                builder.function("stddev_pop", Double.class, moduleScore),
                builder.min(moduleScore),
                builder.max(moduleScore),
                builder.count(moduleScore)
        );

        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }
        query.groupBy(moduleName);
        query.orderBy(builder.asc(moduleName));

        List<Tuple> tuples = entityManager.createQuery(query).getResultList();
        List<ModulePerformance> results = new ArrayList<>();
        for (Tuple tuple : tuples) {
            String normalizedName = tuple.get(0, String.class);
            if (normalizedName == null) {
                continue;
            }
            Double avg = tuple.get(1, Double.class);
            Double stddev = tuple.get(2, Double.class);
            Integer min = tuple.get(3, Integer.class);
            Integer max = tuple.get(4, Integer.class);
            Long sample = tuple.get(5, Long.class);
            results.add(new ModulePerformance(normalizedName, ScoreStatistics.fromAggregate(avg, stddev, min, max, sample)));
        }
        return results;
    }

    private List<PeriodTrend> buildPeriodTrends(InternResultFilter filter) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = builder.createTupleQuery();
        Root<InternalResult> root = query.from(InternalResult.class);

        List<Predicate> predicates = buildPredicates(filter, builder, root, query);

        Expression<Integer> periodExpression = root.get("periodo");
        Expression<Integer> scoreExpression = root.get("puntajeGlobal");
        query.multiselect(
                periodExpression,
                builder.avg(scoreExpression),
                builder.count(root)
        );
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }
        query.groupBy(periodExpression);
        query.orderBy(builder.asc(periodExpression));

        List<Tuple> tuples = entityManager.createQuery(query).getResultList();
        List<PeriodTrend> trends = new ArrayList<>();
        for (Tuple tuple : tuples) {
            Integer period = tuple.get(0, Integer.class);
            Double average = tuple.get(1, Double.class);
            Long count = tuple.get(2, Long.class);
            trends.add(new PeriodTrend(period, average, count));
        }
        return trends;
    }

    private double computeTrendVariation(List<PeriodTrend> trends) {
        if (trends.size() < 2) {
            return 0.0;
        }
        PeriodTrend earliest = trends.get(0);
        PeriodTrend latest = trends.get(trends.size() - 1);
        double earliestAvg = earliest.getAverageScore() == null ? 0.0 : earliest.getAverageScore();
        double latestAvg = latest.getAverageScore() == null ? 0.0 : latest.getAverageScore();
        if (earliestAvg == 0.0) {
            return 0.0;
        }
        return ((latestAvg - earliestAvg) / earliestAvg) * 100.0;
    }

    private List<Predicate> buildPredicates(InternResultFilter filter, CriteriaBuilder builder, Root<InternalResult> root, CriteriaQuery<?> query) {
        return buildPredicates(filter, builder, root, query, null);
    }

    private List<Predicate> buildPredicates(InternResultFilter filter,
                                            CriteriaBuilder builder,
                                            Root<InternalResult> root,
                                            CriteriaQuery<?> query,
                                            Join<InternalResult, InternalModuleResult> existingModuleJoin) {
        List<Predicate> predicates = new ArrayList<>();

        if (filter.hasPeriods()) {
            predicates.add(root.get("periodo").in(filter.getPeriods()));
        }
        if (filter.hasSemesters()) {
            predicates.add(root.get("semestre").in(filter.getSemesters()));
        }
        if (filter.hasNbc()) {
            List<String> normalizedNbc = normalizeStrings(filter.getNbc());
            if (!normalizedNbc.isEmpty()) {
                Subquery<Integer> subquery = query.subquery(Integer.class);
                Root<ExternalGeneralResult> external = subquery.from(ExternalGeneralResult.class);
                Expression<String> externalProgram = builder.upper(builder.trim(external.get("estuPrgmAcademico")));
                Expression<String> internalProgram = builder.upper(builder.trim(root.get("programa")));
                Expression<String> externalNbc = builder.upper(builder.trim(external.get("estuNucleoPregrado")));
                subquery.select(builder.literal(1));
                subquery.where(
                        builder.and(
                                builder.equal(externalProgram, internalProgram),
                                externalNbc.in(normalizedNbc)
                        )
                );
                predicates.add(builder.exists(subquery));
            }
        }
        if (filter.hasAreas()) {
            List<String> normalizedAreas = normalizeStrings(filter.getAreas());
            if (!normalizedAreas.isEmpty()) {
                Join<InternalModuleResult, Modulo> moduloJoin;
                if (existingModuleJoin != null) {
                    moduloJoin = existingModuleJoin.join("modulo", JoinType.LEFT);
                } else {
                    moduloJoin = root.join("internalModuleResults", JoinType.LEFT)
                            .join("modulo", JoinType.LEFT);
                }
                Expression<String> moduloName = builder.upper(builder.trim(moduloJoin.get("nombre")));
                predicates.add(moduloName.in(normalizedAreas));
            }
        }
        return predicates;
    }

    private List<String> normalizeStrings(List<String> values) {
        List<String> normalized = new ArrayList<>();
        for (String value : values) {
            if (value == null) continue;
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                normalized.add(trimmed.toUpperCase());
            }
        }
        return normalized;
    }
}
