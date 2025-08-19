package in.wealthinker.wealthinker.modules.user.service;

import in.wealthinker.wealthinker.modules.user.dto.request.UpdatePreferencesRequest;
import in.wealthinker.wealthinker.modules.user.dto.response.PreferenceResponse;

public interface PreferenceService {

    PreferenceResponse getPreferences(Long userId);
    
    PreferenceResponse updatePreferences(Long userId, UpdatePreferencesRequest request);
    
}
