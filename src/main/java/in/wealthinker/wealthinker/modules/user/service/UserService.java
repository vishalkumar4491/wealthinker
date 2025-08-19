package in.wealthinker.wealthinker.modules.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import in.wealthinker.wealthinker.modules.user.dto.request.CreateUserRequest;
import in.wealthinker.wealthinker.modules.user.dto.request.UpdateUsernameRequest;
import in.wealthinker.wealthinker.modules.user.dto.response.UserResponse;
import in.wealthinker.wealthinker.modules.user.dto.response.UserSummaryResponse;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(Long userId);
    
    UserResponse getUserByEmail(String email);

    UserResponse getUserByUsername(String username);

    UserResponse getUserByPhoneNumber(String phoneNumber);

    UserResponse getUserByEmailOrUsernameOrPhoneNumber(String identifier);

    UserResponse updateUsername(Long userId, UpdateUsernameRequest request);

    boolean isUsernameAvailable(String username);
    
    UserSummaryResponse getUserSummary(Long userId);
    
    Page<UserSummaryResponse> getAllUsers(Pageable pageable);
    
    Page<UserSummaryResponse> searchUsers(String searchTerm, Pageable pageable);
    
    // void changePassword(Long userId, ChangePasswordRequest request);
    
    void deactivateUser(Long userId);
    
    void activateUser(Long userId);
    
    void deleteUser(Long userId);
}
