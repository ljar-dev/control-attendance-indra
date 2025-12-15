package com.indra.attendance_control.commons;

import org.springframework.data.domain.PageImpl;

public interface IPaginationCommons<T> {
    PageImpl<T> getPagination(PaginationModel paginationModel);
}