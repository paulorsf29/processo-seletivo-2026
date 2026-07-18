package com.example.Ecomerce.dto.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Plain, Jackson-friendly stand-in for Spring Data's {@link Page} (same field names/shape as its
 * default JSON) — used wherever a paginated response needs to be cached, since {@code PageImpl}
 * has no usable constructor for generic deserialization (e.g. reading it back from Redis).
 */
public record PageResponse<T>(
        List<T> content,
        int number,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static <T> PageResponse<T> fromPage(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
