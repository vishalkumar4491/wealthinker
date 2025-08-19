package in.wealthinker.wealthinker.modules.user.service.impl;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
// import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import in.wealthinker.wealthinker.modules.user.dto.request.ChangePasswordRequest;
import in.wealthinker.wealthinker.modules.user.dto.request.CreateUserRequest;
import in.wealthinker.wealthinker.modules.user.dto.request.UpdateUsernameRequest;
import in.wealthinker.wealthinker.modules.user.dto.response.UserResponse;
import in.wealthinker.wealthinker.modules.user.dto.response.UserSummaryResponse;
import in.wealthinker.wealthinker.modules.user.entity.User;
import in.wealthinker.wealthinker.modules.user.entity.UserPreference;
import in.wealthinker.wealthinker.modules.user.entity.UserProfile;
import in.wealthinker.wealthinker.modules.user.mapper.UserMapper;
import in.wealthinker.wealthinker.modules.user.repository.UserRepository;
import in.wealthinker.wealthinker.modules.user.service.UserService;
import in.wealthinker.wealthinker.shared.constants.CacheConstants;
import in.wealthinker.wealthinker.shared.enums.UserStatus;
import in.wealthinker.wealthinker.shared.exceptions.BusinessException;
import in.wealthinker.wealthinker.shared.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    // private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating new user with email: {}", request.getEmail());

        // Check if user already exists by email or username
        if (request.getUsername() != null) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new BusinessException("Username already exists", "USERNAME_ALREADY_EXISTS");
            }
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists", "EMAIL_ALREADY_EXISTS");
        }

        if (request.getPhoneNumber() != null && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BusinessException("Phone number already exists", "PHONE_NUMBER_ALREADY_EXISTS");
        }

        // Create user entity
        User user = userMapper.toUser(request);
        user.setStatus(UserStatus.ACTIVE); // For manual user creation, set as active

        // Create user profile
        UserProfile profile = userMapper.createProfileFromRequest(request, user);
        
        // Create user preferences with defaults
        UserPreference preference = userMapper.createDefaultPreference(user);

        user.setProfile(profile);
        user.setPreference(preference);

        // Save user
        user = userRepository.save(user);

        log.info("User created successfully with ID: {}", user.getId());
        return userMapper.toUserResponse(user);
    }

    @Override
    @Cacheable(value = CacheConstants.USER_CACHE, key = "#userId")
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        return userMapper.toUserResponse(user);
    }

    @Override
    @Cacheable(value = CacheConstants.USER_CACHE, key = "#email")
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        
        return userMapper.toUserResponse(user);
    }

    @Override
    @Cacheable(value = CacheConstants.USER_CACHE, key = "#username")
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));    
        return userMapper.toUserResponse(user);
    }

    @Override
    @Cacheable(value = CacheConstants.USER_CACHE, key = "#phoneNumber")
    public UserResponse getUserByPhoneNumber(String phoneNumber) {
        User user = userRepository.findByPhoneNumberAndIsActiveTrue(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User", "phone number", phoneNumber));      
        return userMapper.toUserResponse(user);
    }

    @Override
    @Cacheable(value = CacheConstants.USER_CACHE, key = "#identifier")
    public UserResponse getUserByEmailOrUsernameOrPhoneNumber(String identifier) {
        User user = userRepository.findByEmailOrUsernameOrPhoneAndIsActiveTrue(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email or username or phone", identifier));
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_CACHE, allEntries = true)
    public UserResponse updateUsername(Long userId, UpdateUsernameRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check if username is available
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username is already taken", "USERNAME_ALREADY_EXISTS");
        }

        user.setUsername(request.getUsername());
        userRepository.save(user);

        log.info("Username updated for user: {}", user.getEmail());
        return userMapper.toUserResponse(user);
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Override
    public UserSummaryResponse getUserSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));   
        return userMapper.toUserSummaryResponse(user);
    }

    @Override
    public Page<UserSummaryResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toUserSummaryResponse);
    }

    @Override
    public Page<UserSummaryResponse> searchUsers(String searchTerm, Pageable pageable) {
        return userRepository.searchUsers(searchTerm, pageable)
                .map(userMapper::toUserSummaryResponse);
    }

    // @Override
    // @Transactional
    // public void changePassword(Long userId, ChangePasswordRequest request) {
    //     User user = userRepository.findById(userId)
    //             .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    //     // Verify current password
    //     if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
    //         throw new BusinessException("Current password is incorrect", "INVALID_CURRENT_PASSWORD");
    //     }

    //     // Validate new password
    //     if (!request.isPasswordMatching()) {
    //         throw new BusinessException("New passwords do not match", "PASSWORD_MISMATCH");
    //     }

    //     // Update password
    //     user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    //     userRepository.save(user);

    //     log.info("Password changed successfully for user: {}", user.getEmail());
    // }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_CACHE, allEntries = true)
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setIsActive(false);
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);

        log.info("User deactivated: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setIsActive(true);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        log.info("User activated: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Soft delete - mark as deleted instead of actual deletion
        user.setIsActive(false);
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);

        log.info("User deleted: {}", user.getEmail());
    }

}
