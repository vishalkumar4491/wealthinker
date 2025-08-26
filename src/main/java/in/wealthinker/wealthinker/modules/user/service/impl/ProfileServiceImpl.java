package in.wealthinker.wealthinker.modules.user.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.wealthinker.wealthinker.modules.user.dto.request.UpdateFinancialInfoRequest;
import in.wealthinker.wealthinker.modules.user.repository.UserProfileRepository;
import in.wealthinker.wealthinker.shared.constants.CacheConstants;
import in.wealthinker.wealthinker.shared.enums.UserStatus;
import in.wealthinker.wealthinker.shared.exceptions.BusinessException;
import in.wealthinker.wealthinker.shared.utils.SecurityUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final ProfileMapper userProfileMapper;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    // TODO: Inject when implemented
    // private final FileStorageService fileStorageService;
    // private final ImageProcessingService imageProcessingService;
    // private final KycDocumentService kycDocumentService;

    // =================== PROFILE RETRIEVAL ===================

    @Override
    @Cacheable(value = CacheConstants.USER_PROFILE_CACHE, key = "#userId", unless = "#result == null")
    public ProfileResponse getUserProfile(Long userId, boolean includeFinancialData) {
        log.debug("Getting profile for user: {}, includeFinancialData: {}", userId, includeFinancialData);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", userId));

        if (includeFinancialData) {
            return userProfileMapper.toProfileResponse(profile);
        } else {
            return userProfileMapper.toUserProfilePublicResponse(profile);
        }
    }

    @Override
    @Cacheable(value = CacheConstants.USER_PROFILE_CACHE, key = "'public_' + #userId", unless = "#result == null")
    public ProfileResponse getPublicProfile(Long userId) {
        log.debug("Getting public profile for user: {}", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", userId));

        // Check if profile is set to public
        User user = profile.getUser();
        if (user.getPreference() != null && !user.getPreference().getProfilePublic()) {
            throw new BusinessException("Profile is not public", "PROFILE_NOT_PUBLIC");
        }

        return userProfileMapper.toUserProfilePublicResponse(profile);
    }

    @Override
    public Map<String, Object> getProfileCompletion(Long userId) {
        log.debug("Getting profile completion for user: {}", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", userId));

        Map<String, Object> completion = new HashMap<>();
        completion.put("completionPercentage", profile.getProfileCompletionPercentage());
        completion.put("isCompleted", profile.getProfileCompleted());
        completion.put("missingFields", getMissingFields(profile));
        completion.put("completedSections", getCompletedSections(profile));
        completion.put("nextSteps", getNextSteps(profile));

        return completion;
    }

    // =================== PROFILE UPDATES ===================

    @Override
    @Transactional
    @CacheEvict(value = {CacheConstants.USER_PROFILE_CACHE, CacheConstants.USER_CACHE}, allEntries = true)
    public ProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", userId));

        // Store old values for comparison
        String oldFirstName = profile.getFirstName();
        String oldLastName = profile.getLastName();

        // Update profile
        userProfileMapper.updateProfileFromRequest(request, profile);

        // Handle address update
        if (request.getAddress() != null) {
            if (profile.getAddress() == null) {
                profile.setAddress(new UserProfile.Address());
            }
            updateAddress(profile.getAddress(), request.getAddress());
        }

        // Recalculate completion percentage
        profile.calculateAndSetCompletionPercentage();

        // Save changes
        profile = userProfileRepository.save(profile);

        // Log name changes for audit
        if (!Objects.equals(oldFirstName, profile.getFirstName()) ||
                !Objects.equals(oldLastName, profile.getLastName())) {
            log.warn("Name changed for user {}: {} {} -> {} {}",
                    userId, oldFirstName, oldLastName, profile.getFirstName(), profile.getLastName());
        }

        // Publish profile updated event
        publishProfileUpdatedEvent(profile);

        log.info("Profile updated successfully for user: {}", userId);
        return userProfileMapper.toProfileResponse(profile);
    }

    @Override
    @Transactional
    @CacheEvict(value = {CacheConstants.USER_PROFILE_CACHE, CacheConstants.USER_CACHE}, allEntries = true)
    public ProfileResponse updateFinancialInfo(Long userId, UpdateFinancialInfoRequest request) {
        log.info("Updating financial information for user: {}", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", userId));

        // Store old values for audit
        Long oldIncome = profile.getAnnualIncome();
        UserProfile.IncomeSource oldIncomeSource = profile.getIncomeSource();

        // Update financial information
        if (request.getAnnualIncome() != null) {
            profile.setAnnualIncome(request.getAnnualIncome());
        }
        if (request.getIncomeSource() != null) {
            profile.setIncomeSource(request.getIncomeSource());
        }
        if (request.getInvestmentExperience() != null) {
            profile.setInvestmentExperience(request.getInvestmentExperience());
        }
        if (request.getRiskTolerance() != null) {
            profile.setRiskTolerance(request.getRiskTolerance());
        }

        // Recalculate completion percentage
        profile.calculateAndSetCompletionPercentage();

        // Save changes
        profile = userProfileRepository.save(profile);

        // Log financial changes for audit (be careful with sensitive data)
        log.warn("Financial information updated for user: {}, income changed: {}, income source changed: {}",
                userId, !Objects.equals(oldIncome, profile.getAnnualIncome()),
                !Objects.equals(oldIncomeSource, profile.getIncomeSource()));

        // Publish financial info updated event
        publishFinancialInfoUpdatedEvent(profile);

        return userProfileMapper.toProfileResponse(profile);
    }

    // =================== IMAGE MANAGEMENT ===================

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_PROFILE_CACHE, key = "#userId")
    public Map<String, String> uploadProfileImage(Long userId, MultipartFile image) {
        log.info("Uploading profile image for user: {}", userId);

        // Validate image
        validateImageFile(image);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", userId));

        try {
            // TODO: Implement file storage service
            // String imageUrl = fileStorageService.storeFile(image, "profile-images", userId.toString());

            // For now, simulate image upload
            String imageUrl = "https://api.wealthinker.com/files/profile-images/" + userId + "/" +
                    UUID.randomUUID().toString() + getFileExtension(image.getOriginalFilename());

            // Delete old image if exists
            if (profile.getProfileImageUrl() != null) {
                // TODO: Delete old image file
                log.debug("Deleting old profile image for user: {}", userId);
            }

            // Update profile with new image URL
            profile.setProfileImageUrl(imageUrl);
            userProfileRepository.save(profile);

            log.info("Profile image uploaded successfully for user: {}", userId);

            Map<String, String> result = new HashMap<>();
            result.put("imageUrl", imageUrl);
            result.put("thumbnailUrl", imageUrl + "?size=thumbnail");
            result.put("message", "Profile image uploaded successfully");

            return result;

        } catch (Exception e) {
            log.error("Failed to upload profile image for user: {}", userId, e);
            throw new BusinessException("Failed to upload profile image", "IMAGE_UPLOAD_FAILED");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_PROFILE_CACHE, key = "#userId")
    public void deleteProfileImage(Long userId) {
        log.info("Deleting profile image for user: {}", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", userId));

        if (profile.getProfileImageUrl() != null) {
            // TODO: Delete file from storage
            // fileStorageService.deleteFile(profile.getProfileImageUrl());

            profile.setProfileImageUrl(null);
            userProfileRepository.save(profile);

            log.info("Profile image deleted successfully for user: {}", userId);
        }
    }

    // =================== KYC OPERATIONS ===================

    @Override
    @Transactional
    public Map<String, Object> submitKyc(Long userId, MultipartFile identityDocument,
                                         MultipartFile addressProof, MultipartFile[] additionalDocuments) {
        log.info("KYC submission for user: {}", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", userId));

        // Validate KYC submission
        validateKycSubmission(profile, identityDocument);

        try {
            // TODO: Store KYC documents securely
            // List<String> documentUrls = kycDocumentService.storeKycDocuments(userId,
            //     identityDocument, addressProof, additionalDocuments);

            // Update KYC status
            profile.setKycStatus(UserProfile.KycStatus.SUBMITTED);
            profile.setKycSubmittedAt(LocalDateTime.now());
            profile.setKycRejectionReason(null); // Clear any previous rejection

            userProfileRepository.save(profile);

            // TODO: Notify compliance team
            // notificationService.notifyComplianceTeam("New KYC submission", userId);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "SUBMITTED");
            result.put("submittedAt", profile.getKycSubmittedAt());
            result.put("message", "KYC documents submitted successfully. Review typically takes 1-3 business days.");
            result.put("expectedReviewTime", "1-3 business days");

            log.info("KYC submitted successfully for user: {}", userId);
            return result;

        } catch (Exception e) {
            log.error("Failed to submit KYC for user: {}", userId, e);
            throw new BusinessException("Failed to submit KYC documents", "KYC_SUBMISSION_FAILED");
        }
    }

    @Override
    public Map<String, Object> getKycStatus(Long userId) {
        log.debug("Getting KYC status for user: {}", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", userId));

        Map<String, Object> status = new HashMap<>();
        status.put("status", profile.getKycStatus());
        status.put("submittedAt", profile.getKycSubmittedAt());
        status.put("approvedAt", profile.getKycApprovedAt());
        status.put("rejectionReason", profile.getKycRejectionReason());
        status.put("canResubmit", canResubmitKyc(profile));
        status.put("requiredDocuments", getRequiredKycDocuments());

        return status;
    }

    @Override
    @Transactional
    @CacheEvict(value = {CacheConstants.USER_PROFILE_CACHE, CacheConstants.USER_CACHE}, allEntries = true)
    public void approveKyc(Long userId, String notes) {
        log.info("Approving KYC for user: {} by admin", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", userId));

        if (profile.getKycStatus() != UserProfile.KycStatus.SUBMITTED) {
            throw new BusinessException("KYC is not in submitted status", "INVALID_KYC_STATUS");
        }

        profile.setKycStatus(UserProfile.KycStatus.APPROVED);
        profile.setKycApprovedAt(LocalDateTime.now());
        profile.setKycRejectionReason(null);

        // Update user status if needed
        User user = profile.getUser();
        if (user.getStatus() == UserStatus.PENDING_KYC) {
            user.setStatus(UserStatus.ACTIVE);
        }

        userProfileRepository.save(profile);

        // TODO: Send approval notification
        // emailService.sendKycApprovalNotification(user.getEmail(), notes);

        // Log approval for audit
        log.warn("KYC approved for user: {} by admin: {}, notes: {}",
                userId, SecurityUtils.getCurrentUserEmail().orElse("system"), notes);
    }

    @Override
    @Transactional
    @CacheEvict(value = {CacheConstants.USER_PROFILE_CACHE, CacheConstants.USER_CACHE}, allEntries = true)
    public void rejectKyc(Long userId, String reason) {
        log.info("Rejecting KYC for user: {} by admin", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", userId));

        if (profile.getKycStatus() != UserProfile.KycStatus.SUBMITTED) {
            throw new BusinessException("KYC is not in submitted status", "INVALID_KYC_STATUS");
        }

        profile.setKycStatus(UserProfile.KycStatus.REJECTED);
        profile.setKycRejectionReason(reason);
        profile.setKycApprovedAt(null);

        // Update user status
        User user = profile.getUser();
        user.setStatus(UserStatus.KYC_REJECTED);

        userProfileRepository.save(profile);

        // TODO: Send rejection notification
        // emailService.sendKycRejectionNotification(user.getEmail(), reason);

        // Log rejection for audit
        log.warn("KYC rejected for user: {} by admin: {}, reason: {}",
                userId, SecurityUtils.getCurrentUserEmail().orElse("system"), reason);
    }

    // =================== ADMIN OPERATIONS ===================

    @Override
    public Page<ProfileResponse> getIncompleteProfiles(Integer maxCompletion, Pageable pageable) {
        log.debug("Getting incomplete profiles with max completion: {}%", maxCompletion);

        return userProfileRepository.findByCompletionPercentageRange(0, maxCompletion, pageable)
                .map(userProfileMapper::toProfileResponse);
    }

    @Override
    public Page<ProfileResponse> getPendingKycProfiles(Pageable pageable) {
        log.debug("Getting profiles with pending KYC");

        return userProfileRepository.findByKycStatus(UserProfile.KycStatus.SUBMITTED, pageable)
                .map(userProfileMapper::toProfileResponse);
    }

    @Override
    @Cacheable(value = "profileStats", unless = "#result.isEmpty()")
    public Map<String, Object> getProfileStatistics() {
        log.debug("Calculating profile statistics");

        Map<String, Object> stats = new HashMap<>();

        // Profile completion statistics
        stats.put("totalProfiles", userProfileRepository.count());
        stats.put("completedProfiles", userProfileRepository.countCompletedProfiles());
        stats.put("averageCompletionPercentage", userProfileRepository.getAverageCompletionPercentage());
        stats.put("completionDistribution", userProfileRepository.getCompletionDistribution());

        // KYC statistics
        stats.put("kycStatusDistribution", userProfileRepository.getKycStatusDistribution());

        // Demographics (anonymized)
        stats.put("ageDistribution", getAgeDistribution());
        stats.put("genderDistribution", getGenderDistribution());

        return stats;
    }

    // =================== EXPORT ===================

    @Override
    public byte[] exportProfileData(Long userId, String format) {
        log.info("Exporting profile data for user: {} in format: {}", userId, format);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", userId));

        try {
            if ("json".equalsIgnoreCase(format)) {
                ProfileResponse response = userProfileMapper.toProfileResponse(profile);
                return objectMapper.writeValueAsBytes(response);
            } else if ("pdf".equalsIgnoreCase(format)) {
                // TODO: Generate PDF export
                throw new BusinessException("PDF export not yet implemented", "PDF_EXPORT_NOT_IMPLEMENTED");
            } else {
                throw new BusinessException("Unsupported export format: " + format, "UNSUPPORTED_FORMAT");
            }
        } catch (Exception e) {
            log.error("Failed to export profile data for user: {}", userId, e);
            throw new BusinessException("Failed to export profile data", "EXPORT_FAILED");
        }
    }

    // =================== PRIVATE HELPER METHODS ===================

    private void updateAddress(UserProfile.Address address, UpdateProfileRequest.AddressUpdateRequest request) {
        if (request.getAddressLine1() != null) address.setAddressLine1(request.getAddressLine1());
        if (request.getAddressLine2() != null) address.setAddressLine2(request.getAddressLine2());
        if (request.getCity() != null) address.setCity(request.getCity());
        if (request.getState() != null) address.setState(request.getState());
        if (request.getPostalCode() != null) address.setPostalCode(request.getPostalCode());
        if (request.getCountry() != null) address.setCountry(request.getCountry());
    }

    private List<String> getMissingFields(UserProfile profile) {
        List<String> missing = new ArrayList<>();

        if (profile.getFirstName() == null || profile.getFirstName().trim().isEmpty()) missing.add("firstName");
        if (profile.getLastName() == null || profile.getLastName().trim().isEmpty()) missing.add("lastName");
        if (profile.getDateOfBirth() == null) missing.add("dateOfBirth");
        if (profile.getGender() == null) missing.add("gender");
        if (profile.getAddress() == null || !profile.getAddress().isComplete()) missing.add("address");
        if (profile.getOccupation() == null || profile.getOccupation().trim().isEmpty()) missing.add("occupation");
        if (profile.getAnnualIncome() == null || profile.getAnnualIncome() <= 0) missing.add("annualIncome");
        if (profile.getInvestmentExperience() == null) missing.add("investmentExperience");
        if (profile.getRiskTolerance() == null) missing.add("riskTolerance");
        if (profile.getKycStatus() != UserProfile.KycStatus.APPROVED) missing.add("kycVerification");

        return missing;
    }

    private Map<String, Boolean> getCompletedSections(UserProfile profile) {
        Map<String, Boolean> sections = new HashMap<>();

        sections.put("personalInfo",
                profile.getFirstName() != null && profile.getLastName() != null &&
                        profile.getDateOfBirth() != null && profile.getGender() != null);

        sections.put("contactInfo",
                profile.getAddress() != null && profile.getAddress().isComplete());

        sections.put("professionalInfo",
                profile.getOccupation() != null && !profile.getOccupation().trim().isEmpty());

        sections.put("financialInfo",
                profile.getAnnualIncome() != null && profile.getAnnualIncome() > 0 &&
                        profile.getIncomeSource() != null);

        sections.put("investmentProfile",
                profile.getInvestmentExperience() != null && profile.getRiskTolerance() != null);

        sections.put("kycVerification",
                profile.getKycStatus() == UserProfile.KycStatus.APPROVED);

        return sections;
    }

    private List<String> getNextSteps(UserProfile profile) {
        List<String> steps = new ArrayList<>();

        if (profile.getFirstName() == null || profile.getLastName() == null) {
            steps.add("Complete basic personal information");
        }
        if (profile.getAddress() == null || !profile.getAddress().isComplete()) {
            steps.add("Add complete address information");
        }
        if (profile.getAnnualIncome() == null || profile.getAnnualIncome() <= 0) {
            steps.add("Provide financial information");
        }
        if (profile.getInvestmentExperience() == null || profile.getRiskTolerance() == null) {
            steps.add("Complete investment profile assessment");
        }
        if (profile.getKycStatus() != UserProfile.KycStatus.APPROVED) {
            steps.add("Submit KYC documents for verification");
        }

        if (steps.isEmpty()) {
            steps.add("Profile is complete! You can now access all platform features.");
        }

        return steps;
    }

    private void validateImageFile(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new BusinessException("Image file is required", "IMAGE_REQUIRED");
        }

        // Check file size (5MB limit)
        if (image.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("Image file size cannot exceed 5MB", "IMAGE_TOO_LARGE");
        }

        // Check file type
        String contentType = image.getContentType();
        if (contentType == null || (!contentType.startsWith("image/jpeg") &&
                !contentType.startsWith("image/png") &&
                !contentType.startsWith("image/gif"))) {
            throw new BusinessException("Only JPEG, PNG, and GIF images are allowed", "INVALID_IMAGE_TYPE");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    private void validateKycSubmission(UserProfile profile, MultipartFile identityDocument) {
        if (profile.getKycStatus() == UserProfile.KycStatus.APPROVED) {
            throw new BusinessException("KYC is already approved", "KYC_ALREADY_APPROVED");
        }

        if (profile.getKycStatus() == UserProfile.KycStatus.SUBMITTED) {
            throw new BusinessException("KYC is already submitted and under review", "KYC_ALREADY_SUBMITTED");
        }

        if (identityDocument == null || identityDocument.isEmpty()) {
            throw new BusinessException("Identity document is required", "IDENTITY_DOCUMENT_REQUIRED");
        }

        // Basic profile information must be complete
        if (profile.getFirstName() == null || profile.getLastName() == null ||
                profile.getDateOfBirth() == null) {
            throw new BusinessException("Basic profile information must be completed before KYC submission",
                    "INCOMPLETE_PROFILE_FOR_KYC");
        }
    }

    private boolean canResubmitKyc(UserProfile profile) {
        return profile.getKycStatus() == UserProfile.KycStatus.REJECTED ||
                profile.getKycStatus() == UserProfile.KycStatus.EXPIRED ||
                profile.getKycStatus() == UserProfile.KycStatus.NOT_STARTED;
    }

    private List<String> getRequiredKycDocuments() {
        return Arrays.asList(
                "Government-issued photo ID (passport, driver's license, national ID)",
                "Proof of address (utility bill, bank statement, lease agreement)",
                "Additional documents may be requested based on verification needs"
        );
    }

    private Map<String, Long> getAgeDistribution() {
        // TODO: Implement age distribution calculation
        // This would involve calculating ages from date of birth and grouping
        return Map.of(
                "18-25", 0L,
                "26-35", 0L,
                "36-45", 0L,
                "46-55", 0L,
                "56+", 0L
        );
    }

    private Map<String, Long> getGenderDistribution() {
        // TODO: Implement gender distribution calculation
        return Map.of(
                "MALE", 0L,
                "FEMALE", 0L,
                "OTHER", 0L,
                "PREFER_NOT_TO_SAY", 0L
        );
    }

    private void publishProfileUpdatedEvent(UserProfile profile) {
        // TODO: Implement profile updated event
        log.debug("Publishing profile updated event for user: {}", profile.getUser().getId());
    }

    private void publishFinancialInfoUpdatedEvent(UserProfile profile) {
        // TODO: Implement financial info updated event
        log.debug("Publishing financial info updated event for user: {}", profile.getUser().getId());
    }
}
