package com.sonit.api.user.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sonit.api.common.exception.ResourceNotFoundException;
import com.sonit.api.user.dto.UserProfileDto;
import com.sonit.api.user.model.User;
import com.sonit.api.user.repository.UserRepository;

@Service
public class UserService {

    private static final String SPOTIFY_KEY = "spotify";

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfileDto getUserProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return UserProfileDto.builder()
                .id(user.getId())
                .displayName(user.getDisplayName())
                .username(user.getUsername())
                .email(user.getEmail())
                .photoUrl(user.getPhotoUrl())
                .connectedServices(mapConnectedServices(user))
                .visible(user.isVisible())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private Map<String, UserProfileDto.ConnectedServiceDto> mapConnectedServices(User user) {
        Map<String, UserProfileDto.ConnectedServiceDto> mappedServices = new HashMap<>();

        if (user.getConnectedServices() != null) {
            user.getConnectedServices().forEach((key, value) ->
                    mappedServices.put(
                            key,
                            UserProfileDto.ConnectedServiceDto.builder()
                                    .connected(value != null && value.isConnected())
                                    .build()));
        }

        mappedServices.putIfAbsent(
                SPOTIFY_KEY,
                UserProfileDto.ConnectedServiceDto.builder()
                        .connected(false)
                        .build());

        return mappedServices;
    }
}
