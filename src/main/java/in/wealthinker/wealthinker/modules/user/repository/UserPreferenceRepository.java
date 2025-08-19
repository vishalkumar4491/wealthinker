package in.wealthinker.wealthinker.modules.user.repository;

import java.util.Optional;

import in.wealthinker.wealthinker.modules.user.entity.UserPreference;

public interface UserPreferenceRepository {
    Optional<UserPreference> findByUserId(Long userId);
}
