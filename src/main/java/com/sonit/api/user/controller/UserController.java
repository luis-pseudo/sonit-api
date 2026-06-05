package com.sonit.api.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sonit.api.common.dto.ApiResponse;
import com.sonit.api.common.util.SecurityUtils;
import com.sonit.api.user.dto.UserProfileDto;
import com.sonit.api.user.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getCurrentUserProfile() {
        String userId = SecurityUtils.getCurrentUserId();
        UserProfileDto profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("User profile fetched successfully", profile));
    }
}
