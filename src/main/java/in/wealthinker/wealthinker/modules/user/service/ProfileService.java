package in.wealthinker.wealthinker.modules.user.service;

import in.wealthinker.wealthinker.modules.user.dto.request.UpdateProfileRequest;
import in.wealthinker.wealthinker.modules.user.dto.response.ProfileResponse;

public interface ProfileService {
    ProfileResponse getProfile(Long userId);
    
    ProfileResponse updateProfile(Long userId, UpdateProfileRequest request);
    
    void uploadProfileImage(Long userId, String imageUrl);
    
    int calculateProfileCompletionPercentage(Long userId);
}
