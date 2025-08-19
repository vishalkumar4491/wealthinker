package in.wealthinker.wealthinker.modules.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import in.wealthinker.wealthinker.modules.user.dto.request.UpdatePreferencesRequest;
import in.wealthinker.wealthinker.modules.user.dto.response.PreferenceResponse;
import in.wealthinker.wealthinker.modules.user.service.PreferenceService;
import in.wealthinker.wealthinker.shared.constants.AppConstants;

import jakarta.validation.Valid;

@RestController
@RequestMapping(AppConstants.USER_ENDPOINT + "/{userId}/preferences")
@RequiredArgsConstructor
@Tag(name = "User Preferences", description = "User preferences management APIs")
public class PreferenceController {
    private final PreferenceService preferenceService;

    @GetMapping
    @Operation(
        summary = "Get user preferences",
        description = "Get user preferences"
    )
    public ResponseEntity<PreferenceResponse> getPreferences(@PathVariable Long userId) {
        PreferenceResponse response = preferenceService.getPreferences(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @Operation(
        summary = "Update user preferences",
        description = "Update user preferences"
    )
    public ResponseEntity<PreferenceResponse> updatePreferences(
            @PathVariable Long userId,
            @Valid @RequestBody UpdatePreferencesRequest request) {
        PreferenceResponse response = preferenceService.updatePreferences(userId, request);
        return ResponseEntity.ok(response);
    }
}
