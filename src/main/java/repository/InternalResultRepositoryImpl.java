package repository;

import dto.InternResultFilter;
import dto.InternResultInfo;
import model.InternalModuleResult;
import model.InternalResult;
import model.Modulo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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
        List<Predicate> predicates = buildPredicates(safeFilter, builder, root);

        query.select(builder.construct(
                        InternResultInfo.class,
                        root.get("periodo"),
                        root.get("nombre"),
                        root.get("numeroRegistro"),
                        root.get("programa"),
                        root.get("puntajeGlobal")
                ))
                .distinct(true);

        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }

        query.orderBy(builder.asc(root.get("id")));

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
        List<Predicate> predicates = buildPredicates(safeFilter, builder, root);

        query.select(builder.countDistinct(root));
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }

        return entityManager.createQuery(query).getSingleResult();
    }

    private List<Predicate> buildPredicates(InternResultFilter filter, CriteriaBuilder builder, Root<InternalResult> root) {
        List<Predicate> predicates = new ArrayList<>();

        if (filter.hasPeriods()) {
            predicates.add(root.get("periodo").in(filter.getPeriods()));
        }
        if (filter.hasNbc()) {
            predicates.add(root.get("grupoReferencia").in(filter.getNbc()));
        }
        if (filter.hasAreas()) {
            Join<InternalResult, InternalModuleResult> moduloJoin =
                    root.join("internalModuleResults", JoinType.LEFT)
                            .join("modulo", JoinType.LEFT);
            predicates.add(moduloJoin.get("nombre").in(filter.getAreas()));
        }

        return predicates;
    }
}

