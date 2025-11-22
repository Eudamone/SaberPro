package repository;

import dto.InternResultFilter;
import dto.InternResultInfo;
import dto.InternResultReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InternalResultRepositoryCustom {

    Page<InternResultInfo> findResults(Pageable pageable, InternResultFilter filter);

    long countResults(InternResultFilter filter);

    InternResultReport generateReport(InternResultFilter filter);
}
