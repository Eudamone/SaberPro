package repository;

import dto.InternResultFilter;
import dto.InternResultInfo;
import jakarta.persistence.criteria.*;
import model.ExternalGeneralResult;
import model.InternalModuleResult;
import model.InternalResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
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

    private List<Predicate> buildPredicates(InternResultFilter filter, CriteriaBuilder builder, Root<InternalResult> root, CriteriaQuery<?> query) {
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
                Join<InternalResult, InternalModuleResult> moduloJoin =
                        root.join("internalModuleResults", JoinType.LEFT)
                                .join("modulo", JoinType.LEFT);
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
