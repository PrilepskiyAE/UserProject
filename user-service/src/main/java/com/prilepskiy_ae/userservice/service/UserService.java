package com.prilepskiy_ae.userservice.service;

import com.prilepskiy_ae.userservice.dto.user.UserRequest;
import com.prilepskiy_ae.userservice.dto.user.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserRequest user);
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    UserResponse updateUser(Long id, UserRequest request);
    void deleteUserById(Long id);
    boolean isEmailExists(String email);
}
