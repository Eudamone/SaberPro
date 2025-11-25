package repository;

import dto.InternResultFilter;
import dto.InternResultInfo;
import dto.InternResultReport;
import dto.ModulePerformance;
import dto.PeriodTrend;
import dto.ScoreStatistics;
import dto.ReportContextStats;
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
import model.ExternalModuleResult;
import model.InternalModuleResult;
import model.InternalResult;
import model.Modulo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class InternalResultRepositoryImpl implements InternalResultRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    private final ExternalModuleResultRepository externalModuleResultRepository;

    public InternalResultRepositoryImpl(EntityManager entityManager,
                                        ExternalModuleResultRepository externalModuleResultRepository) {
        this.entityManager = entityManager;
        this.externalModuleResultRepository = externalModuleResultRepository;
    }

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

        query.select(builder.countDistinct(root.get("documento")));
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public InternResultReport generateReport(InternResultFilter filter) {
        InternResultFilter safeFilter = filter == null ? new InternResultFilter() : filter;
        ReportContextStats stats = buildContextStats(safeFilter);
        ScoreStatistics internalGlobal = buildGlobalStatistics(safeFilter);
        ScoreStatistics externalGlobal = buildExternalGlobalStatistics(safeFilter);
        List<ModulePerformance> internalModules = buildModulePerformances(safeFilter);
        List<ModulePerformance> externalModules = buildExternalModulePerformances(safeFilter);
        List<PeriodTrend> periodTrends = buildPeriodTrends(safeFilter);
        double variation = computeTrendVariation(periodTrends);

        return new InternResultReport(stats, internalGlobal, externalGlobal, internalModules, externalModules, periodTrends, variation);
    }

    private ReportContextStats buildContextStats(InternResultFilter filter) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = builder.createTupleQuery();
        Root<InternalResult> root = query.from(InternalResult.class);
        List<Predicate> predicates = buildPredicates(filter, builder, root, query);

        query.multiselect(
                builder.countDistinct(root.get("documento")),
                builder.sum(builder.<Integer>selectCase().when(builder.isTrue(root.get("presentoPrueba")), 1).otherwise(0)),
                builder.avg(root.get("puntajeGlobal"))
        );
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }
        Tuple tuple = entityManager.createQuery(query).getSingleResult();
        Number evaluatedNumber = tuple.get(0, Number.class);
        Number attendeesNumber = tuple.get(1, Number.class);
        long evaluated = evaluatedNumber == null ? 0L : evaluatedNumber.longValue();
        long attendees = attendeesNumber == null ? evaluated : attendeesNumber.longValue();
        return new ReportContextStats(filter.getPeriods(), filter.getSemesters(), filter.getAreas(), filter.getNbc(), evaluated, attendees);
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
                builder.min(builder.coalesce(globalScore, 0)),
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

    private ScoreStatistics buildExternalGlobalStatistics(InternResultFilter filter) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = builder.createTupleQuery();
        Root<ExternalGeneralResult> root = query.from(ExternalGeneralResult.class);
        List<Predicate> predicates = buildExternalPredicates(filter, builder, root);

        Expression<BigDecimal> scoreExpression = root.get("puntGlobal");
        query.multiselect(
                builder.avg(scoreExpression),
                builder.function("stddev_pop", Double.class, scoreExpression),
                builder.min(scoreExpression),
                builder.max(scoreExpression),
                builder.count(scoreExpression)
        );
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }
        Tuple tuple = entityManager.createQuery(query).getSingleResult();
        return ScoreStatistics.fromAggregate(
                tuple.get(0, Double.class),
                tuple.get(1, Double.class),
                tuple.get(2, BigDecimal.class),
                tuple.get(3, BigDecimal.class),
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
        Predicate scorePresent = builder.greaterThan(moduleJoin.get("puntaje"), 0);
        predicates.add(scorePresent);

        Expression<Integer> sanitizedScore = builder.<Integer>selectCase()
                .when(scorePresent, moduleJoin.get("puntaje"))
                .otherwise((Integer) null);
        Expression<String> moduleName = builder.upper(builder.trim(moduloJoin.get("nombre")));
        query.multiselect(
                moduleName,
                builder.avg(sanitizedScore),
                builder.function("stddev_pop", Double.class, sanitizedScore),
                builder.min(sanitizedScore),
                builder.max(sanitizedScore),
                builder.countDistinct(moduleJoin.get("internoId"))
        );

        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }
        query.groupBy(moduleName);
        query.orderBy(builder.asc(moduleName));

        List<Tuple> tuples = entityManager.createQuery(query).getResultList();
        return tuples.stream()
                .map(tuple -> {
                    String name = tuple.get(0, String.class);
                    if (name == null) return null;
                    ScoreStatistics stats = ScoreStatistics.fromAggregate(
                            tuple.get(1, Double.class),
                            tuple.get(2, Double.class),
                            tuple.get(3, Integer.class),
                            tuple.get(4, Integer.class),
                            tuple.get(5, Long.class)
                    );
                    return new ModulePerformance(name, stats);
                })
                .filter(java.util.Objects::nonNull)
                .sorted(java.util.Comparator.comparing(ModulePerformance::getModuleName))
                .collect(Collectors.toList());
    }

    private List<ModulePerformance> buildExternalModulePerformances(InternResultFilter filter) {
        List<ModulePerformance> baseline = new ArrayList<>();
        Map<String, ScoreStatistics> stats = buildExternalModuleStatistics(filter);
        stats.forEach((name, stat) -> baseline.add(new ModulePerformance(name, stat)));
        return baseline;
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
        if (trends == null || trends.size() < 2) {
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
                if (existingModuleJoin != null) {
                    Join<InternalModuleResult, Modulo> moduloJoin = existingModuleJoin.join("modulo", JoinType.LEFT);
                    Expression<String> moduloName = normalizedModuleName(builder, moduloJoin.get("nombre"));
                    predicates.add(moduloName.in(normalizedAreas));
                } else {
                    Subquery<Integer> moduleSubquery = query.subquery(Integer.class);
                    Root<InternalModuleResult> moduleRoot = moduleSubquery.from(InternalModuleResult.class);
                    Join<InternalModuleResult, Modulo> moduloJoin = moduleRoot.join("modulo", JoinType.LEFT);
                    Expression<String> moduloName = normalizedModuleName(builder, moduloJoin.get("nombre"));
                    moduleSubquery.select(builder.literal(1));
                    moduleSubquery.where(
                            builder.and(
                                    builder.equal(moduleRoot.get("internoId"), root.get("documento")),
                                    moduloName.in(normalizedAreas)
                            )
                    );
                    predicates.add(builder.exists(moduleSubquery));
                }
            }
        }
        return predicates;
    }

    private List<Predicate> buildExternalPredicates(InternResultFilter filter, CriteriaBuilder builder, Root<ExternalGeneralResult> root) {
        List<Predicate> predicates = new ArrayList<>();
        if (filter.hasPeriods()) {
            predicates.add(root.get("periodo").in(filter.getPeriods()));
        }
        if (filter.hasNbc()) {
            predicates.add(builder.upper(builder.trim(root.get("estuNucleoPregrado"))).in(normalizeStrings(filter.getNbc())));
        }
        if (filter.hasAreas()) {
            predicates.add(builder.upper(builder.trim(root.get("estuPrgmAcademico"))).in(normalizeStrings(filter.getAreas())));
        }
        return predicates;
    }

    private List<Predicate> buildExternalModulePredicates(InternResultFilter filter,
                                                          CriteriaBuilder builder,
                                                          Join<ExternalModuleResult, ExternalGeneralResult> externalJoin,
                                                          Join<ExternalModuleResult, Modulo> moduloJoin) {
        List<Predicate> predicates = new ArrayList<>();
        if (filter.hasPeriods()) {
            predicates.add(externalJoin.get("periodo").in(filter.getPeriods()));
        }
        if (filter.hasNbc()) {
            predicates.add(builder.upper(builder.trim(externalJoin.get("estuNucleoPregrado"))).in(normalizeStrings(filter.getNbc())));
        }
        if (filter.hasAreas()) {
            Expression<String> normalizedModule = normalizedModuleName(builder, moduloJoin.get("nombre"));
            predicates.add(normalizedModule.in(normalizeStrings(filter.getAreas())));
        }
        return predicates;
    }

    private Expression<String> normalizedModuleName(CriteriaBuilder builder, Expression<String> rawName) {
        Expression<String> upperTrim = builder.upper(builder.trim(rawName));
        return builder.function(
                "translate",
                String.class,
                upperTrim,
                builder.literal("ÁÀÂÄÃÉÈÊËÍÌÎÏÓÒÔÖÕÚÙÛÜÑ"),
                builder.literal("AAAAAEEEEIIIIOOOOOUUUUN")
        );
    }

    private List<String> normalizeStrings(List<String> values) {
        List<String> normalized = new ArrayList<>();
        if (values == null) return normalized;
        for (String value : values) {
            if (value == null) continue;
            String trimmed = value.trim();
            if (trimmed.isEmpty()) continue;
            String withoutDiacritics = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
                    .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            normalized.add(withoutDiacritics.toUpperCase());
        }
        return normalized;
    }

    private Map<String, ScoreStatistics> buildExternalModuleStatistics(InternResultFilter filter) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = builder.createTupleQuery();
        Root<ExternalModuleResult> moduleRoot = query.from(ExternalModuleResult.class);
        Join<ExternalModuleResult, ExternalGeneralResult> externalJoin = moduleRoot.join("externalGeneralResult", JoinType.LEFT);
        Join<ExternalModuleResult, Modulo> moduloJoin = moduleRoot.join("modulo", JoinType.LEFT);

        List<Predicate> predicates = buildExternalModulePredicates(filter, builder, externalJoin, moduloJoin);
        Predicate scorePresent = builder.greaterThan(moduleRoot.get("resultPuntaje"), BigDecimal.ZERO);
        predicates.add(scorePresent);

        Expression<BigDecimal> sanitizedScore = builder.<BigDecimal>selectCase()
                .when(scorePresent, moduleRoot.get("resultPuntaje"))
                .otherwise((BigDecimal) null);
        query.multiselect(
                moduloJoin.get("nombre"),
                builder.avg(sanitizedScore),
                builder.function("stddev_pop", Double.class, sanitizedScore),
                builder.min(sanitizedScore),
                builder.max(sanitizedScore),
                builder.countDistinct(externalJoin.get("estConsecutivo"))
        );
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }
        query.groupBy(moduloJoin.get("nombre"));

        List<Tuple> tuples = entityManager.createQuery(query).getResultList();
        return tuples.stream()
                .map(tuple -> {
                    String name = tuple.get(0, String.class);
                    if (name == null) return null;
                    Number avgRaw = tuple.get(1, Number.class);
                    Number stdRaw = tuple.get(2, Number.class);
                    Number minRaw = tuple.get(3, Number.class);
                    Number maxRaw = tuple.get(4, Number.class);
                    Double avg = avgRaw == null ? null : avgRaw.doubleValue();
                    Double std = stdRaw == null ? null : stdRaw.doubleValue();
                    ScoreStatistics stats = ScoreStatistics.fromAggregate(
                            avg,
                            std,
                            minRaw,
                            maxRaw,
                            tuple.get(5, Long.class)
                    );
                    return Map.entry(name, stats);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> existing,
                        java.util.LinkedHashMap::new
                ));
    }
}