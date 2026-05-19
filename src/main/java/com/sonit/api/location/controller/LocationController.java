package com.sonit.api.location.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sonit.api.common.dto.ApiResponse;
import com.sonit.api.common.util.SecurityUtils;
import com.sonit.api.location.dto.LocationUpdateRequest;
import com.sonit.api.location.service.LocationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PutMapping
    public ResponseEntity<ApiResponse<Void>> updateLocation(@Valid @RequestBody LocationUpdateRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        locationService.updateLocation(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Location updated successfully", null));
    }
}
