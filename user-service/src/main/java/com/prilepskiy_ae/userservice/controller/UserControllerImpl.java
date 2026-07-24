package com.prilepskiy_ae.userservice.controller;

import com.prilepskiy_ae.userservice.dto.user.UserRequest;
import com.prilepskiy_ae.userservice.dto.user.UserResponse;
import com.prilepskiy_ae.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserControllerImpl implements UserController {

    private final UserService userService;

    @Override
    public ResponseEntity<UserResponse> createUser(UserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Override
    public ResponseEntity<UserResponse> getUserById(Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> response = userService.getAllUsers();
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<UserResponse> updateUser(Long id, UserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteUser(Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }
}
