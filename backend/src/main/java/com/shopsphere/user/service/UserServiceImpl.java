package com.shopsphere.user.service;

import com.shopsphere.common.exception.ResourceNotFoundException;
import com.shopsphere.user.dto.UserProfileResponse;
import com.shopsphere.user.dto.UserUpdateRequest;
import com.shopsphere.user.entity.User;
import com.shopsphere.user.mapper.UserMapper;
import com.shopsphere.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(Long userId) {
        User user = getUserById(userId);
        return userMapper.toProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateCurrentUserProfile(Long userId, UserUpdateRequest request) {
        User user = getUserById(userId);
        userMapper.updateUserFromDto(request, user);
        User saved = userRepository.save(user);
        log.info("Updated profile for user ID: {}", userId);
        return userMapper.toProfileResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
}

