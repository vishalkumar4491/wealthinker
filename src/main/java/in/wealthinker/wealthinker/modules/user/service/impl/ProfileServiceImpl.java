package in.wealthinker.wealthinker.modules.user.service.impl;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import in.wealthinker.wealthinker.modules.user.dto.request.UpdateProfileRequest;
import in.wealthinker.wealthinker.modules.user.dto.response.ProfileResponse;
import in.wealthinker.wealthinker.modules.user.entity.User;
import in.wealthinker.wealthinker.modules.user.entity.UserProfile;
import in.wealthinker.wealthinker.modules.user.mapper.ProfileMapper;
import in.wealthinker.wealthinker.modules.user.repository.UserRepository;
import in.wealthinker.wealthinker.modules.user.service.ProfileService;
import in.wealthinker.wealthinker.shared.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {
    private final UserRepository userRepository;
    private final ProfileMapper profileMapper;

    @Override
    @Cacheable(value = "userProfiles", key = "#userId")
    public ProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return profileMapper.toProfileResponse(user.getProfile());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users", "userProfiles"}, key = "#userId")
    public ProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = UserProfile.builder().user(user).build();
        }

        // Update profile fields
        profileMapper.updateProfileFromRequest(request, profile);

        // Update profile completion status
        int completionPercentage = profile.calculateProfileCompletionPercentage();
        profile.setProfileCompleted(completionPercentage >= 80);

        user.setProfile(profile);
        userRepository.save(user);

        log.info("Profile updated for user: {}, completion: {}%", user.getEmail(), completionPercentage);

        return profileMapper.toProfileResponse(profile);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users", "userProfiles"}, key = "#userId")
    public void uploadProfileImage(Long userId, String imageUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        UserProfile profile = user.getProfile();
        if (profile != null) {
            profile.setProfileImageUrl(imageUrl);
            userRepository.save(user);
            log.info("Profile image updated for user: {}", user.getEmail());
        }
    }

    @Override
    public int calculateProfileCompletionPercentage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getProfile() == null) {
            return 0;
        }

        return user.getProfile().calculateProfileCompletionPercentage();
    }
}
