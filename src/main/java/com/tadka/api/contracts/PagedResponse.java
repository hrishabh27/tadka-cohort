package com.tadka.api.contracts;

import java.util.List;

public record PagedResponse<T>(
    List<T> items,
    int totalCount,
    int page,
    int pageSize
) {}
