package in.wealthinker.wealthinker.shared.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard API Response Wrapper
 *
 * PURPOSE:
 * - Consistent response format across all APIs
 * - Include metadata like timestamps, request IDs
 * - Support for success/error states
 * - Client-friendly structure
 *
 * INDUSTRY STANDARD:
 * - Includes success flag, message, data, and metadata
 * - Consistent error handling structure
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseCustom<T> {

    @Builder.Default
    private Boolean success = true;

    private String message;

    private T data;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private String requestId;

    // Pagination metadata (if applicable)
    private PageMetadata pagination;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageMetadata {
        private Integer page;
        private Integer size;
        private Long totalElements;
        private Integer totalPages;
        private Boolean first;
        private Boolean last;
        private Boolean hasNext;
        private Boolean hasPrevious;
    }

    // Factory methods for common response types

    public static <T> ApiResponseCustom<T> success(T data) {
        return ApiResponseCustom.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponseCustom<T> success(T data, String message) {
        return ApiResponseCustom.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponseCustom<T> error(String message) {
        return ApiResponseCustom.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    public static <T> ApiResponseCustom<T> error(String message, String requestId) {
        return ApiResponseCustom.<T>builder()
                .success(false)
                .message(message)
                .requestId(requestId)
                .build();
    }
}
