package in.wealthinker.wealthinker.modules.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.wealthinker.wealthinker.modules.user.dto.request.UpdateProfileRequest;
import in.wealthinker.wealthinker.modules.user.dto.response.ProfileResponse;
import in.wealthinker.wealthinker.modules.user.service.ProfileService;
import in.wealthinker.wealthinker.shared.constants.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(AppConstants.USER_ENDPOINT + "/{userId}/profile")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "User profile management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    @Operation(
        summary = "Get user profile",
        description = "Get current user profile details"
    )
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable Long userId) {
        ProfileResponse response = profileService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @Operation(
        summary = "Update user profile",
        description = "Update user profile information"
    )
    public ResponseEntity<ProfileResponse> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        ProfileResponse response = profileService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/completion")
    @Operation(
        summary = "Get profile completion percentage",
        description = "Get user profile completion percentage"
    )
    public ResponseEntity<Integer> getProfileCompletion(@PathVariable Long userId) {
        int completion = profileService.calculateProfileCompletionPercentage(userId);
        return ResponseEntity.ok(completion);
    }

    @PostMapping("/image")
    @Operation(
        summary = "Upload profile image",
        description = "Upload user profile image"
    )
    public ResponseEntity<Void> uploadProfileImage(
            @PathVariable Long userId,
            @RequestParam String imageUrl) {
        profileService.uploadProfileImage(userId, imageUrl);
        return ResponseEntity.ok().build();
    }

}
