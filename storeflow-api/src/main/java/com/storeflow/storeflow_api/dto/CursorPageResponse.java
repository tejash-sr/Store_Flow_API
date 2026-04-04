package com.storeflow.storeflow_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Base64;
import java.util.List;

/**
 * Cursor-based pagination response wrapper.
 * Ideal for large datasets and infinite scroll patterns.
 * Uses Base64-encoded cursor strings for traversal.
 *
 * @param <T> The type of items in the page
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursorPageResponse<T> {

    /**
     * The actual items for this page
     */
    private List<T> items;

    /**
     * Base64-encoded cursor to fetch the next page
     * Null if no more items available
     */
    private String nextCursor;

    /**
     * Base64-encoded cursor to fetch the previous page
     * Null if at the beginning
     */
    private String previousCursor;

    /**
     * Whether more items are available after this page
     */
    private boolean hasMore;

    /**
     * Number of items in this page
     */
    private int pageSize;

    /**
     * Encode a cursor string to Base64
     *
     * @param cursorData Raw cursor data (e.g., "productId:100")
     * @return Base64-encoded cursor string
     */
    public static String encodeCursor(String cursorData) {
        return Base64.getEncoder().encodeToString(cursorData.getBytes());
    }

    /**
     * Decode a Base64 cursor string
     *
     * @param encodedCursor Base64-encoded cursor
     * @return Decoded cursor data
     */
    public static String decodeCursor(String encodedCursor) {
        return new String(Base64.getDecoder().decode(encodedCursor));
    }

    /**
     * Check if this is the first page (no previous cursor)
     *
     * @return true if previousCursor is null
     */
    public boolean isFirstPage() {
        return previousCursor == null;
    }

    /**
     * Check if this is the last page (no more items)
     *
     * @return true if !hasMore
     */
    public boolean isLastPage() {
        return !hasMore;
    }

    /**
     * Check if items are empty
     *
     * @return true if items list is empty
     */
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    /**
     * Build CursorPageResponse with automatic cursor generation
     *
     * @param items        List of items for this page
     * @param nextCursor   Encoded cursor for next page
     * @param previousCursor Encoded cursor for previous page
     * @param hasMore      Whether more items exist
     * @param pageSize     Size of this page
     * @param <T>          Type of items
     * @return CursorPageResponse instance
     */
    public static <T> CursorPageResponse<T> of(List<T> items, String nextCursor, String previousCursor, 
                                                boolean hasMore, int pageSize) {
        return CursorPageResponse.<T>builder()
            .items(items)
            .nextCursor(nextCursor)
            .previousCursor(previousCursor)
            .hasMore(hasMore)
            .pageSize(pageSize)
            .build();
    }
}
