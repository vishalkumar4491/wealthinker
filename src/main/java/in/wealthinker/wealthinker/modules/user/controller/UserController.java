package in.wealthinker.wealthinker.modules.user.controller;

import in.wealthinker.wealthinker.modules.user.dto.request.UpdateUserRequest;
import in.wealthinker.wealthinker.shared.exceptions.ResourceNotFoundException;
import in.wealthinker.wealthinker.shared.response.ApiResponseCustom;
import in.wealthinker.wealthinker.shared.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.wealthinker.wealthinker.modules.user.dto.request.CreateUserRequest;
import in.wealthinker.wealthinker.modules.user.dto.request.UpdateUsernameRequest;
import in.wealthinker.wealthinker.modules.user.dto.response.UserResponse;
import in.wealthinker.wealthinker.modules.user.dto.response.UserSummaryResponse;
import in.wealthinker.wealthinker.modules.user.service.UserService;
import in.wealthinker.wealthinker.shared.constants.AppConstants;
import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;

/**
 * User Controller - Core user management endpoints
 *
 * DESIGN PRINCIPLES:
 * - RESTful URL patterns
 * - Consistent response format using ApiResponse wrapper
 * - Comprehensive API documentation with OpenAPI
 * - Role-based access control
 * - Request/response logging for audit
 *
 * SECURITY:
 * - JWT-based authentication required for most endpoints
 * - Role-based authorization for admin operations
 * - Resource-level access control (users can only access their own data)
 * - Input validation and sanitization
 */

@RestController
@RequestMapping(AppConstants.USER_ENDPOINT)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "Core user management operations")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    // =================== USER CREATION ===================

    @PostMapping
    @Operation(
        summary = "Create new user",
        description = "Create a new user with profile information",
        responses = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Email, username, or phone already exists")
        }
    )
    public ResponseEntity<ApiResponseCustom<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            HttpServletRequest httpRequest) {

        String requestId = UUID.randomUUID().toString();
        log.info("Creating user - RequestId: {}, Email: {}", requestId, request.getEmail());


        try {
            UserResponse userResponse = userService.createUser(request);

            log.info("User created successfully - RequestId: {}, UserId: {}", requestId, userResponse.getId());


            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseCustom.success(userResponse, "User created successfully"));

        } catch (Exception e) {
            log.error("Failed to create user - RequestId: {}, Email: {}", requestId, request.getEmail(), e);
            throw e;
        }
    }

    // =================== USER RETRIEVAL ===================

    // @GetMapping("/user")
    // @Operation(
    //     summary = "Get current user",
    //     description = "Get current authenticated user details"
    // )
    // public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
    //     String email = authentication.getName();
    //     UserResponse response = userService.getUserByEmail(email);
    //     return ResponseEntity.ok(response);
    // }

    // @GetMapping("/user/summary")
    // @Operation(
    //     summary = "Get current user summary",
    //     description = "Get current authenticated user summary"
    // )
    // public ResponseEntity<UserSummaryResponse> getCurrentUserSummary(Authentication authentication) {
    //     Long userId = (Long) authentication.getPrincipal();
    //     UserSummaryResponse response = userService.getUserSummary(userId);
    //     return ResponseEntity.ok(response);
    // }

    @GetMapping("/{userId}")
    @Operation(
            summary = "Get user by ID",
            description = "Retrieve user information by ID. Users can only access their own information unless they are admin."
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<UserResponse>> getUserById(
            @Parameter(description = "User ID") @PathVariable Long userId) {

        log.debug("Getting user by ID: {}", userId);


        Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        UserResponse userResponse = userService.getCurrentUserById(userId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        log.info("User Response : {}", userResponse);

        return ResponseEntity.ok(ApiResponseCustom.success(userResponse));
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get current user profile",
            description = "Retrieve the profile of the currently authenticated user"
    )
    public ResponseEntity<ApiResponseCustom<UserResponse>> getCurrentUser() {

        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new SecurityException("No authenticated user found"));

        log.debug("Getting current user profile: {}", currentUserId);

        UserResponse userResponse = userService.getUserById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        return ResponseEntity.ok(ApiResponseCustom.success(userResponse));
    }

    @GetMapping("/email/{email}")
    @Operation(
            summary = "Get user by email",
            description = "Retrieve user information by email address. Admin access required."
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseCustom<UserResponse>> getUserByEmail(
            @Parameter(description = "Email address") @PathVariable String email) {

        log.debug("Getting user by email: {}", email);

        UserResponse userResponse = userService.getUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return ResponseEntity.ok(ApiResponseCustom.success(userResponse));
    }

    @GetMapping("/username/{username}")
    @Operation(
            summary = "Get user by username",
            description = "Retrieve user information by username. Admin access required."
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseCustom<UserResponse>> getUserByUsername(
            @Parameter(description = "Username") @PathVariable String username) {

        log.debug("Getting user by username: {}", username);

        UserResponse userResponse = userService.getUserByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        return ResponseEntity.ok(ApiResponseCustom.success(userResponse));
    }

    @GetMapping("/phoneNumber/{phoneNumber}")
    @Operation(
            summary = "Get user by phoneNumber",
            description = "Retrieve user information by phoneNumber. Admin access required."
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseCustom<UserResponse>> getUserByPhoneNumber(
            @Parameter(description = "PhoneNumber") @PathVariable String phoneNumber) {

        log.debug("Getting user by phoneNumber: {}", phoneNumber);

        UserResponse userResponse = userService.getUserByUsername(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User", "phoneNumber", phoneNumber));

        return ResponseEntity.ok(ApiResponseCustom.success(userResponse));
    }

    @GetMapping("/identifier/{identifier}")
    @Operation(
        summary = "Get user by email or username or phone number",
        description = "Get user details by email or username or phone number"
    )
    public ResponseEntity<ApiResponseCustom<UserResponse>> getUserByEmailOrUsernameOrPhoneNumber(@PathVariable String identifier) {
        UserResponse userResponse = userService.getUserByEmailOrUsernameOrPhoneNumber(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User", "identifier", identifier));;
        return ResponseEntity.ok(ApiResponseCustom.success(userResponse));
    }

    // =================== USER UPDATES ===================

    @PutMapping("/{userId}")
    @Operation(
            summary = "Update user information",
            description = "Update basic user account information. Users can only update their own information."
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<UserResponse>> updateUser(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request) {

        log.info("Updating user: {}, fields: {}", userId, request.getUpdatedFields());

        UserResponse userResponse = userService.updateUser(userId, request);

        return ResponseEntity.ok(ApiResponseCustom.success(userResponse, "User updated successfully"));
    }

    @PutMapping("/{userId}/username")
    @Operation(
        summary = "Update username",
        description = "Update user's username"
    )
    public ResponseEntity<UserResponse> updateUsername(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUsernameRequest request) {
        UserResponse response = userService.updateUsername(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/summary")
    @Operation(
        summary = "Get user summary",
        description = "Get user summary information"
    )
    public ResponseEntity<UserSummaryResponse> getUserSummary(@PathVariable Long userId) {
        UserSummaryResponse response = userService.getUserSummary(userId);
        return ResponseEntity.ok(response);
    }

    // =================== USER SEARCH & LISTING ===================

    @GetMapping
    @Operation(
        summary = "Get all users",
        description = "Get paginated list of all users (Admin only)"
    )
     @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseCustom<Page<UserSummaryResponse>>> getAllUsers(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,

            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDir) {

        log.debug("Getting all users - page: {}, size: {}, sortBy: {}, sortDir: {}",
                page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserSummaryResponse> users = userService.getAllUsers(pageable);

        return ResponseEntity.ok(ApiResponseCustom.success(users));
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search users",
        description = "Search users by name or email (Admin only)"
    )
     @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseCustom<Page<UserSummaryResponse>>> searchUsers(
            @Parameter(description = "Search term")
            @RequestParam(required = false) String query,

            @Parameter(description = "User role filter")
            @RequestParam(required = false) String role,

            @Parameter(description = "User status filter")
            @RequestParam(required = false) String status,

            @Parameter(description = "Email verified filter")
            @RequestParam(required = false) Boolean emailVerified,

            @Parameter(description = "Page number")
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,

            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDir) {

        log.debug("Searching users - query: {}, role: {}, status: {}, emailVerified: {}",
                query, role, status, emailVerified);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserSummaryResponse> users = userService.searchUsers(
                query, role, status, emailVerified, pageable);

        return ResponseEntity.ok(ApiResponseCustom.success(users));
    }

    // @PutMapping("/user/change-password")
    // @Operation(
    //     summary = "Change password",
    //     description = "Change current user password"
    // )
    // public ResponseEntity<Void> changePassword(
    //         @Valid @RequestBody ChangePasswordRequest request,
    //         Authentication authentication) {
    //     Long userId = (Long) authentication.getPrincipal();
    //     userService.changePassword(userId, request);
    //     return ResponseEntity.ok().build();
    // }

    // =================== ACCOUNT MANAGEMENT ===================

    @PutMapping("/{userId}/deactivate")
    @Operation(
        summary = "Deactivate user",
        description = "Deactivate user account (Admin only)"
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseCustom<Void>> deactivateUser(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Reason for deactivation") @RequestParam String reason) {

        log.info("Deactivating user: {} for reason: {}", userId, reason);

        userService.deactivateUser(userId, reason);

        return ResponseEntity.ok(ApiResponseCustom.success(null, "User deactivated successfully"));
    }

    @PutMapping("/{userId}/activate")
    @Operation(
        summary = "Activate user",
        description = "Activate user account (Admin only)"
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseCustom<Void>> activateUser(
            @Parameter(description = "User ID") @PathVariable Long userId) {

        log.info("Activating user: {}", userId);

        userService.activateUser(userId);

        return ResponseEntity.ok(ApiResponseCustom.success(null, "User activated successfully"));
    }

    @DeleteMapping("/{userId}")
    @Operation(
            summary = "Delete user account",
            description = "Soft delete a user account. Users can delete their own account, admins can delete any account."
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId) or hasRole('ADMIN')")
    public ResponseEntity<ApiResponseCustom<Void>> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Reason for deletion") @RequestParam String reason) {

        log.warn("Deleting user: {} for reason: {}", userId, reason);

        userService.deleteUser(userId, reason);

        return ResponseEntity.ok(ApiResponseCustom.success(null, "User deleted successfully"));
    }

    // =================== UTILITY ENDPOINTS ===================

    @GetMapping("/check-email")
    @Operation(
            summary = "Check email availability",
            description = "Check if an email address is available for registration. Public endpoint."
    )
    public ResponseEntity<ApiResponseCustom<Boolean>> checkEmailAvailability(
            @Parameter(description = "Email address to check") @RequestParam String email) {

        log.debug("Checking email availability: {}", email);

        boolean available = userService.isEmailAvailable(email);

        return ResponseEntity.ok(ApiResponseCustom.success(available,
                available ? "Email is available" : "Email is already taken"));
    }

    @GetMapping("/check-username")
    @Operation(
            summary = "Check username availability",
            description = "Check if a username is available for registration. Public endpoint."
    )
    public ResponseEntity<ApiResponseCustom<Boolean>> checkUsernameAvailability(
            @Parameter(description = "Username to check") @RequestParam String username) {

        log.debug("Checking username availability: {}", username);

        boolean available = userService.isUsernameAvailable(username);

        return ResponseEntity.ok(ApiResponseCustom.success(available,
                available ? "Username is available" : "Username is already taken"));
    }

    @GetMapping("/stats")
    @Operation(
            summary = "Get user statistics",
            description = "Retrieve user statistics for admin dashboard. Admin access required."
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseCustom<Map<String, Object>>> getUserStatistics() {

        log.debug("Getting user statistics");

        Map<String, Object> stats = userService.getUserStatistics();

        return ResponseEntity.ok(ApiResponseCustom.success(stats));
    }
}
