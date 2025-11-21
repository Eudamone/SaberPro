package repository;

import dto.InternResultFilter;
import dto.InternResultInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InternalResultRepositoryCustom {

    Page<InternResultInfo> findResults(Pageable pageable, InternResultFilter filter);

    long countResults(InternResultFilter filter);
}

