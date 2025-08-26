package in.wealthinker.wealthinker.modules.user.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import in.wealthinker.wealthinker.modules.user.dto.request.UpdatePreferencesRequest;
import in.wealthinker.wealthinker.modules.user.dto.response.PreferenceResponse;
import in.wealthinker.wealthinker.modules.user.entity.UserPreference;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PreferenceMapper {
    @Mapping(target = "hasMarketingConsent", expression = "java(preference.hasMarketingConsent())")
    @Mapping(target = "allowsDataCollection", expression = "java(preference.allowsDataCollection())")
    PreferenceResponse toResponse(UserPreference preference);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateFromRequest(UpdatePreferencesRequest request, @MappingTarget UserPreference preference);
}
