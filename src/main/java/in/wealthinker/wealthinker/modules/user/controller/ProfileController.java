package in.wealthinker.wealthinker.modules.user.controller;

import in.wealthinker.wealthinker.modules.user.dto.request.UpdateFinancialInfoRequest;
import in.wealthinker.wealthinker.shared.response.ApiResponseCustom;
import in.wealthinker.wealthinker.shared.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import in.wealthinker.wealthinker.modules.user.dto.request.UpdateProfileRequest;
import in.wealthinker.wealthinker.modules.user.dto.response.ProfileResponse;
import in.wealthinker.wealthinker.modules.user.service.ProfileService;
import in.wealthinker.wealthinker.shared.constants.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * User Profile Controller - Profile management operations
 *
 * PURPOSE:
 * - Manage user profile information (personal details, demographics)
 * - Handle KYC (Know Your Customer) processes
 * - Manage financial information and investment preferences
 * - Profile image upload and management
 * - Profile completion tracking
 *
 * SECURITY:
 * - Users can only access/modify their own profiles
 * - Admins can view any profile
 * - Financial information requires additional verification
 * - KYC operations restricted to compliance team
 */

@RestController
@RequestMapping(AppConstants.USER_ENDPOINT + "/{userId}/profile")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Profile", description = "User profile management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final ProfileService userProfileService;

    // =================== PROFILE RETRIEVAL ===================

    @GetMapping
    @Operation(
            summary = "Get user profile",
            description = "Retrieve complete user profile information. Users can access their own profile, admins can access any profile.",
            responses = {
                    @ApiResponse(responseCode = "201", description =  "Profile retrieved successfully"),
                    @ApiResponse(responseCode = "403", description = "Access denied"),
                    @ApiResponse(responseCode = "404", description = "Profile not found")
            }
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<ProfileResponse>> getUserProfile(
            @Parameter(description = "User ID") @PathVariable Long userId) {

        log.debug("Getting profile for user: {}", userId);

        Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        boolean isOwner = userId.equals(currentUserId);
        boolean isAdmin = SecurityUtils.isAdmin();

        ProfileResponse profile = userProfileService.getUserProfile(userId, isOwner || isAdmin);

        return ResponseEntity.ok(ApiResponseCustom.success(profile));
    }

    @GetMapping("/public")
    @Operation(
            summary = "Get public profile",
            description = "Retrieve public profile information (no sensitive data). No authentication required if profile is public."
    )
    public ResponseEntity<ApiResponseCustom<ProfileResponse>> getPublicProfile(
            @Parameter(description = "User ID") @PathVariable Long userId) {

        log.debug("Getting public profile for user: {}", userId);

        ProfileResponse profile = userProfileService.getPublicProfile(userId);

        return ResponseEntity.ok(ApiResponseCustom.success(profile));
    }

    @GetMapping("/completion")
    @Operation(
            summary = "Get profile completion status",
            description = "Get profile completion percentage and missing fields"
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<Map<String, Object>>> getProfileCompletion(
            @Parameter(description = "User ID") @PathVariable Long userId) {

        log.debug("Getting profile completion for user: {}", userId);

        Map<String, Object> completion = userProfileService.getProfileCompletion(userId);

        return ResponseEntity.ok(ApiResponseCustom.success(completion));
    }

    // =================== PROFILE UPDATES ===================

    @PutMapping
    @Operation(
            summary = "Update user profile",
            description = "Update user profile information. Users can only update their own profile."
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<ProfileResponse>> updateProfile(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {

        log.info("Updating profile for user: {}", userId);

        ProfileResponse profile = userProfileService.updateProfile(userId, request);

        return ResponseEntity.ok(ApiResponseCustom.success(profile, "Profile updated successfully"));
    }

    @PutMapping("/financial")
    @Operation(
            summary = "Update financial information",
            description = "Update sensitive financial information. Requires password confirmation and additional security checks."
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<ProfileResponse>> updateFinancialInfo(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody UpdateFinancialInfoRequest request) {

        log.info("Updating financial information for user: {}", userId);

        ProfileResponse profile = userProfileService.updateFinancialInfo(userId, request);

        return ResponseEntity.ok(ApiResponseCustom.success(profile, "Financial information updated successfully"));
    }

    // =================== PROFILE IMAGE MANAGEMENT ===================

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload profile image",
            description = "Upload and set user profile image. Supports JPEG, PNG formats up to 5MB."
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<Map<String, String>>> uploadProfileImage(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Profile image file") @RequestParam("image") MultipartFile image) {

        log.info("Uploading profile image for user: {}", userId);

        Map<String, String> result = userProfileService.uploadProfileImage(userId, image);

        return ResponseEntity.ok(ApiResponseCustom.success(result, "Profile image uploaded successfully"));
    }

    @DeleteMapping("/image")
    @Operation(
            summary = "Delete profile image",
            description = "Remove current profile image"
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<Void>> deleteProfileImage(
            @Parameter(description = "User ID") @PathVariable Long userId) {

        log.info("Deleting profile image for user: {}", userId);

        userProfileService.deleteProfileImage(userId);

        return ResponseEntity.ok(ApiResponseCustom.success(null, "Profile image deleted successfully"));
    }

    // =================== KYC OPERATIONS ===================

    @PostMapping("/kyc/submit")
    @Operation(
            summary = "Submit KYC documents",
            description = "Submit KYC (Know Your Customer) documents for verification"
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<Map<String, Object>>> submitKyc(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Identity document") @RequestParam("identityDocument") MultipartFile identityDocument,
            @Parameter(description = "Address proof") @RequestParam(value = "addressProof", required = false) MultipartFile addressProof,
            @Parameter(description = "Additional documents") @RequestParam(value = "additionalDocuments", required = false) MultipartFile[] additionalDocuments) {

        log.info("KYC submission for user: {}", userId);

        Map<String, Object> result = userProfileService.submitKyc(userId, identityDocument, addressProof, additionalDocuments);

        return ResponseEntity.ok(ApiResponseCustom.success(result, "KYC documents submitted successfully"));
    }

    @GetMapping("/kyc/status")
    @Operation(
            summary = "Get KYC status",
            description = "Get current KYC verification status and details"
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<Map<String, Object>>> getKycStatus(
            @Parameter(description = "User ID") @PathVariable Long userId) {

        log.debug("Getting KYC status for user: {}", userId);

        Map<String, Object> status = userProfileService.getKycStatus(userId);

        return ResponseEntity.ok(ApiResponseCustom.success(status));
    }

    @PutMapping("/kyc/approve")
    @Operation(
            summary = "Approve KYC documents",
            description = "Approve user's KYC verification. Compliance team access required."
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMPLIANCE')")
    public ResponseEntity<ApiResponseCustom<Void>> approveKyc(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Approval notes") @RequestParam(required = false) String notes) {

        log.info("KYC approval for user: {} by admin", userId);

        userProfileService.approveKyc(userId, notes);

        return ResponseEntity.ok(ApiResponseCustom.success(null, "KYC approved successfully"));
    }

    @PutMapping("/kyc/reject")
    @Operation(
            summary = "Reject KYC documents",
            description = "Reject user's KYC verification with reason. Compliance team access required."
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMPLIANCE')")
    public ResponseEntity<ApiResponseCustom<Void>> rejectKyc(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Rejection reason") @RequestParam String reason) {

        log.info("KYC rejection for user: {} by admin, reason: {}", userId, reason);

        userProfileService.rejectKyc(userId, reason);

        return ResponseEntity.ok(ApiResponseCustom.success(null, "KYC rejected"));
    }

    // =================== ADMIN OPERATIONS ===================

    @GetMapping("/admin/incomplete")
    @Operation(
            summary = "Get profiles with incomplete information",
            description = "Get paginated list of profiles with incomplete information. Admin access required."
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseCustom<Page<ProfileResponse>>> getIncompleteProfiles(
            @Parameter(description = "Maximum completion percentage")
            @RequestParam(defaultValue = "80") Integer maxCompletion,

            @Parameter(description = "Page number")
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {

        log.debug("Getting incomplete profiles with max completion: {}%", maxCompletion);

        Pageable pageable = PageRequest.of(page, size, Sort.by("profileCompletionPercentage").ascending());
        Page<ProfileResponse> profiles = userProfileService.getIncompleteProfiles(maxCompletion, pageable);

        return ResponseEntity.ok(ApiResponseCustom.success(profiles));
    }

    @GetMapping("/admin/kyc-pending")
    @Operation(
            summary = "Get profiles with pending KYC",
            description = "Get paginated list of profiles with pending KYC verification. Admin access required."
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMPLIANCE')")
    public ResponseEntity<ApiResponseCustom<Page<ProfileResponse>>> getPendingKycProfiles(
            @Parameter(description = "Page number")
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {

        log.debug("Getting profiles with pending KYC");

        Pageable pageable = PageRequest.of(page, size, Sort.by("kycSubmittedAt").ascending());
        Page<ProfileResponse> profiles = userProfileService.getPendingKycProfiles(pageable);

        return ResponseEntity.ok(ApiResponseCustom.success(profiles));
    }

    @GetMapping("/admin/statistics")
    @Operation(
            summary = "Get profile statistics",
            description = "Get profile completion and KYC statistics. Admin access required."
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseCustom<Map<String, Object>>> getProfileStatistics() {

        log.debug("Getting profile statistics");

        Map<String, Object> statistics = userProfileService.getProfileStatistics();

        return ResponseEntity.ok(ApiResponseCustom.success(statistics));
    }

    // =================== EXPORT & COMPLIANCE ===================

    @GetMapping("/export")
    @Operation(
            summary = "Export profile data",
            description = "Export complete profile data for GDPR compliance. Users can export their own data."
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<byte[]> exportProfileData(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Export format (json/pdf)") @RequestParam(defaultValue = "json") String format) {

        log.info("Exporting profile data for user: {} in format: {}", userId, format);

        byte[] exportData = userProfileService.exportProfileData(userId, format);

        String filename = "profile-data-" + userId + "." + format;
        String contentType = "json".equals(format) ? "application/json" : "application/pdf";

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .header("Content-Type", contentType)
                .body(exportData);
    }


}
