package in.wealthinker.wealthinker.modules.user.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import in.wealthinker.wealthinker.modules.user.dto.request.UpdatePreferencesRequest;
import in.wealthinker.wealthinker.modules.user.dto.response.PreferenceResponse;
import in.wealthinker.wealthinker.modules.user.entity.UserPreference;

@Mapper(componentModel = "spring")
public interface PreferenceMapper {

    PreferenceResponse toPreferenceResponse(UserPreference preference);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updatePreferencesFromRequest(UpdatePreferencesRequest request, @MappingTarget UserPreference preference);

}
