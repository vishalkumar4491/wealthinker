package in.wealthinker.wealthinker.modules.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import in.wealthinker.wealthinker.modules.user.dto.request.CreateUserRequest;
import in.wealthinker.wealthinker.modules.user.dto.response.UserResponse;
import in.wealthinker.wealthinker.modules.user.dto.response.UserSummaryResponse;
import in.wealthinker.wealthinker.modules.user.entity.User;
import in.wealthinker.wealthinker.modules.user.entity.UserPreference;
import in.wealthinker.wealthinker.modules.user.entity.UserProfile;

@Mapper(componentModel = "spring", uses = {ProfileMapper.class, PreferenceMapper.class})
public interface UserMapper {

    @Mapping(target = "fullName", source = ".")
    @Mapping(target= "userName", source = "username")
    UserResponse toUserResponse(User user);


    @Mapping(target = "firstName", source = "profile.firstName")
    @Mapping(target = "lastName", source = "profile.lastName")
    @Mapping(target = "fullName", source = ".")
    @Mapping(target = "profileCompleted", source = "profile.profileCompleted")
    @Mapping(target = "profileImageUrl", source = "profile.profileImageUrl")
    UserSummaryResponse toUserSummaryResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "provider", ignore = true)
    @Mapping(target = "providerId", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "loginAttempts", ignore = true)
    @Mapping(target = "accountLockedUntil", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "preference", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    User toUser(CreateUserRequest request);

    // Custom mapping method for fullName
    default String mapFullName(User user) {
        return user.getDisplayName();
    }

    default UserProfile createProfileFromRequest(CreateUserRequest request, User user) {
        return UserProfile.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .occupation(request.getOccupation())
                .company(request.getCompany())
                .profileCompleted(false)
                .build();
    }

    default UserPreference createDefaultPreference(User user) {
        return UserPreference.builder()
                .user(user)
                .build();
    }
}