package in.wealthinker.wealthinker.modules.user.mapper;

import in.wealthinker.wealthinker.modules.user.dto.request.CreateUserRequest;
import org.mapstruct.*;

import in.wealthinker.wealthinker.modules.user.dto.request.UpdateProfileRequest;
import in.wealthinker.wealthinker.modules.user.dto.response.ProfileResponse;
import in.wealthinker.wealthinker.modules.user.entity.UserProfile;

/**
 * User Profile Mapper
 *
 * SPECIALIZED MAPPING:
 * - Profile-specific business logic
 * - Address handling
 * - Financial data security filtering
 * - KYC information access control
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
        )
public interface ProfileMapper {

    /**
     * Convert UserProfile entity to response DTO
     */
    @Mapping(target = "fullName", source = ".")
    @Mapping(target = "age", expression = "java(profile.getAge())")
    ProfileResponse toProfileResponse(UserProfile profile);

    /**
     * Create profile from CreateUserRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "address", ignore = true) // Mapped separately
    @Mapping(target = "annualIncome", ignore = true) // Not in create request
    @Mapping(target = "incomeSource", ignore = true)
    @Mapping(target = "profileImageUrl", ignore = true)
    @Mapping(target = "kycStatus", ignore = true)
    @Mapping(target = "kycSubmittedAt", ignore = true)
    @Mapping(target = "kycApprovedAt", ignore = true)
    @Mapping(target = "kycRejectionReason", ignore = true)
    @Mapping(target = "profileCompleted", ignore = true)
    @Mapping(target = "profileCompletionPercentage", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    UserProfile createFromRequest(CreateUserRequest request);

    /**
     * Update profile from update request
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "kycStatus", ignore = true) // Can't update via this request
    @Mapping(target = "kycSubmittedAt", ignore = true)
    @Mapping(target = "kycApprovedAt", ignore = true)
    @Mapping(target = "kycRejectionReason", ignore = true)
    @Mapping(target = "profileCompleted", ignore = true) // Calculated
    @Mapping(target = "profileCompletionPercentage", ignore = true) // Calculated
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateProfileFromRequest(UpdateProfileRequest request, @MappingTarget UserProfile profile);

    /**
     * Map address from request to entity
     */
    @Mapping(target = "addressLine1", source = "addressLine1")
    @Mapping(target = "addressLine2", source = "addressLine2")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "state", source = "state")
    @Mapping(target = "postalCode", source = "postalCode")
    @Mapping(target = "country", source = "country")
    UserProfile.Address mapAddress(UpdateProfileRequest.AddressUpdateRequest addressRequest);


    /**
     * Custom mapping for full name
     */
    default String mapFullName(UserProfile profile) {
        return profile.getFullName();
    }

    /**
     * Security-filtered response (hide financial information)
     */
    @Named("toPublicResponse")
    @Mapping(target = "annualIncome", ignore = true)
    @Mapping(target = "incomeSource", ignore = true)
    @Mapping(target = "kycStatus", ignore = true)
    @Mapping(target = "kycSubmittedAt", ignore = true)
    @Mapping(target = "kycApprovedAt", ignore = true)
    ProfileResponse toUserProfilePublicResponse(UserProfile profile);

     /**
     * ✅ Custom method to handle address + isComplete flag
     */
    default ProfileResponse.AddressResponse addressToAddressResponse(UserProfile.Address address) {
        if (address == null) {
            return null;
        }

        return ProfileResponse.AddressResponse.builder()
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .isComplete(address.isComplete()) // ✅ handled here safely
                .build();
    }
}
