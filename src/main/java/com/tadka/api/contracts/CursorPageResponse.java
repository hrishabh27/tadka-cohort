package com.tadka.api.contracts;

import java.util.List;

public record CursorPageResponse<T>(
    List<T> items,
    String nextCursor,
    boolean hasMore
) {}
