package in.wealthinker.wealthinker.modules.user.service;

import in.wealthinker.wealthinker.modules.user.dto.request.UpdateFinancialInfoRequest;
import in.wealthinker.wealthinker.modules.user.dto.request.UpdateProfileRequest;
import in.wealthinker.wealthinker.modules.user.dto.response.ProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface ProfileService {
    // Profile retrieval
    ProfileResponse getUserProfile(Long userId, boolean includeFinancialData);
    ProfileResponse getPublicProfile(Long userId);
    Map<String, Object> getProfileCompletion(Long userId);

    // Profile updates
    ProfileResponse updateProfile(Long userId, UpdateProfileRequest request);
    ProfileResponse updateFinancialInfo(Long userId, UpdateFinancialInfoRequest request);

    // Image management
    Map<String, String> uploadProfileImage(Long userId, MultipartFile image);
    void deleteProfileImage(Long userId);

    // KYC operations
    Map<String, Object> submitKyc(Long userId, MultipartFile identityDocument,
                                  MultipartFile addressProof, MultipartFile[] additionalDocuments);
    Map<String, Object> getKycStatus(Long userId);
    void approveKyc(Long userId, String notes);
    void rejectKyc(Long userId, String reason);

    // Admin operations
    Page<ProfileResponse> getIncompleteProfiles(Integer maxCompletion, Pageable pageable);
    Page<ProfileResponse> getPendingKycProfiles(Pageable pageable);
    Map<String, Object> getProfileStatistics();

    // Export
    byte[] exportProfileData(Long userId, String format);
}
