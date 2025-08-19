package in.wealthinker.wealthinker.modules.user.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import in.wealthinker.wealthinker.modules.user.dto.request.UpdateProfileRequest;
import in.wealthinker.wealthinker.modules.user.dto.response.ProfileResponse;
import in.wealthinker.wealthinker.modules.user.entity.UserProfile;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    @Mapping(target = "fullName", source = ".")
    @Mapping(target = "profileCompletionPercentage", expression = "java(profile.calculateProfileCompletionPercentage())")
    @Mapping(target = "address.addressLine1", source = "address.addressLine1")
    @Mapping(target = "address.addressLine2", source = "address.addressLine2")
    @Mapping(target = "address.city", source = "address.city")
    @Mapping(target = "address.state", source = "address.state")
    @Mapping(target = "address.country", source = "address.country")
    @Mapping(target = "address.postalCode", source = "address.postalCode")
    ProfileResponse toProfileResponse(UserProfile profile);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "profileCompleted", ignore = true)
    @Mapping(target = "profileImageUrl", ignore = true)
    @Mapping(target = "address.addressLine1", source = "addressLine1")
    @Mapping(target = "address.addressLine2", source = "addressLine2")
    @Mapping(target = "address.city", source = "city")
    @Mapping(target = "address.state", source = "state")
    @Mapping(target = "address.country", source = "country")
    @Mapping(target = "address.postalCode", source = "postalCode")
    void updateProfileFromRequest(UpdateProfileRequest request, @MappingTarget UserProfile profile);

    default String mapFullName(UserProfile profile) {
        return profile.getFullName();
    }

    @AfterMapping
    default void ensureAddressExists(@MappingTarget UserProfile profile) {
        if (profile.getAddress() == null) {
            profile.setAddress(new UserProfile.Address());
        }
    }
}
