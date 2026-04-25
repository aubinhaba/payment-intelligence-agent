package com.aubin.pia.api.dto;

import java.util.List;

public record PagedResponse<T>(List<T> content, int page, int size, int totalElements) {

    public static <T> PagedResponse<T> of(List<T> content, int page, int size) {
        return new PagedResponse<>(content, page, size, content.size());
    }
}
