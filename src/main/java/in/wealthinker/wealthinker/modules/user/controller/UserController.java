package in.wealthinker.wealthinker.modules.user.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(AppConstants.USER_ENDPOINT)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "User management APIs")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(
        summary = "Create new user",
        description = "Create a new user with profile information",
        responses = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
        }
    )
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

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
        description = "Get user details by user ID"
    )
    // @PreAuthorize("hasRole('ADMIN') or @userSecurityService.canAccessUser(authentication.principal.id, #userId)")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/email/{email}")
    @Operation(
        summary = "Get user by email",
        description = "Get user details by email address"
    )
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        UserResponse response = userService.getUserByEmail(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/username/{username}")
    @Operation(
        summary = "Get user by username",
        description = "Get user details by username"
    )
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        UserResponse response = userService.getUserByUsername(username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/phoneNumber/{phoneNumber}")
    @Operation(
        summary = "Get user by username",
        description = "Get user details by username"
    )
    public ResponseEntity<UserResponse> getUserByPhoneNumber(@PathVariable String phoneNumber) {
        UserResponse response = userService.getUserByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/identifier/{identifier}")
    @Operation(
        summary = "Get user by email or username or phone number",
        description = "Get user details by email or username or phone number"
    )
    public ResponseEntity<UserResponse> getUserByEmailOrUsernameOrPhoneNumber(@PathVariable String identifier) {
        UserResponse response = userService.getUserByEmailOrUsernameOrPhoneNumber(identifier);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/username/available/{username}")
    @Operation(
        summary = "Check username availability",
        description = "Check if username is available"
    )
    public ResponseEntity<Boolean> isUsernameAvailable(@PathVariable String username) {
        boolean available = userService.isUsernameAvailable(username);
        return ResponseEntity.ok(available);
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

    @GetMapping
    @Operation(
        summary = "Get all users",
        description = "Get paginated list of all users (Admin only)"
    )
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserSummaryResponse>> getAllUsers(Pageable pageable) {
        Page<UserSummaryResponse> response = userService.getAllUsers(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search users",
        description = "Search users by name or email (Admin only)"
    )
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserSummaryResponse>> searchUsers(
            @RequestParam String query,
            Pageable pageable) {
        Page<UserSummaryResponse> response = userService.searchUsers(query, pageable);
        return ResponseEntity.ok(response);
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

    @PutMapping("/{userId}/deactivate")
    @Operation(
        summary = "Deactivate user",
        description = "Deactivate user account (Admin only)"
    )
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long userId) {
        userService.deactivateUser(userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/activate")
    @Operation(
        summary = "Activate user",
        description = "Activate user account (Admin only)"
    )
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateUser(@PathVariable Long userId) {
        userService.activateUser(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}")
    @Operation(
        summary = "Delete user",
        description = "Delete user account (Admin only)"
    )
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    // Helper method to get current user ID
    // private Long getCurrentUserId(Authentication authentication) {
    //     return ((com.wealthinker.platform.shared.security.UserPrincipal) authentication.getPrincipal()).getId();
    // }
}
