package com.shopsphere.user.service;

import com.shopsphere.user.dto.UserProfileResponse;
import com.shopsphere.user.dto.UserUpdateRequest;
import com.shopsphere.user.entity.User;

public interface UserService {
    UserProfileResponse getCurrentUserProfile(Long userId);
    UserProfileResponse updateCurrentUserProfile(Long userId, UserUpdateRequest request);
    User getUserById(Long userId);
}
