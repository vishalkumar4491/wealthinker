package in.wealthinker.wealthinker.modules.user.service.impl;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.wealthinker.wealthinker.modules.user.dto.request.UpdatePreferencesRequest;
import in.wealthinker.wealthinker.modules.user.dto.response.PreferenceResponse;
import in.wealthinker.wealthinker.modules.user.entity.User;
import in.wealthinker.wealthinker.modules.user.entity.UserPreference;
import in.wealthinker.wealthinker.modules.user.mapper.PreferenceMapper;
import in.wealthinker.wealthinker.modules.user.repository.UserRepository;
import in.wealthinker.wealthinker.modules.user.service.PreferenceService;
import in.wealthinker.wealthinker.shared.constants.CacheConstants;
import in.wealthinker.wealthinker.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PreferenceServiceImpl implements PreferenceService {

    private final PreferenceMapper preferenceMapper;
    private final UserRepository userRepository;

    @Override
    @Cacheable(value = CacheConstants.USER_PROFILE_CACHE, key = "'preferences_' + #userId")
    public PreferenceResponse getPreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return preferenceMapper.toPreferenceResponse(user.getPreference());
    }

    @Override
    @Transactional
    @CacheEvict(value = {CacheConstants.USER_CACHE, CacheConstants.USER_PROFILE_CACHE}, allEntries = true)
    public PreferenceResponse updatePreferences(Long userId, UpdatePreferencesRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        UserPreference preference = user.getPreference();
        if (preference == null) {
            preference = UserPreference.builder().user(user).build();
        }

        // Update preferences
        preferenceMapper.updatePreferencesFromRequest(request, preference);

        user.setPreference(preference);
        userRepository.save(user);

        log.info("Preferences updated for user: {}", user.getEmail());

        return preferenceMapper.toPreferenceResponse(preference);
    }

}