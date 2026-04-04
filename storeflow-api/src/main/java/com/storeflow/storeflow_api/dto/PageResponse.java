package com.storeflow.storeflow_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic pagination wrapper for offset-based pagination responses.
 * Provides standard pagination metadata alongside the requested page of data.
 *
 * @param <T> The type of items in the page
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {

    /**
     * The actual content/items for this page
     */
    private List<T> content;

    /**
     * Current page number (0-indexed)
     */
    private int currentPage;

    /**
     * Number of items per page
     */
    private int pageSize;

    /**
     * Total number of items across all pages
     */
    private long totalElements;

    /**
     * Total number of pages
     */
    private int totalPages;

    /**
     * Whether there is a next page available
     */
    private boolean hasNext;

    /**
     * Whether there is a previous page available
     */
    private boolean hasPrevious;

    /**
     * Build PageResponse from Spring Data Page object
     *
     * @param page Spring Data Page object
     * @param <T>  Type of page content
     * @return PageResponse instance
     */
    public static <T> PageResponse<T> from(org.springframework.data.domain.Page<T> page) {
        return PageResponse.<T>builder()
            .content(page.getContent())
            .currentPage(page.getNumber())
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
    }

    /**
     * Check if this is the first page
     *
     * @return true if currentPage == 0
     */
    public boolean isFirstPage() {
        return currentPage == 0;
    }

    /**
     * Check if this is the last page
     *
     * @return true if this is the last page
     */
    public boolean isLastPage() {
        return !hasNext;
    }

    /**
     * Get whether content is empty
     *
     * @return true if content list is empty
     */
    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }
}
