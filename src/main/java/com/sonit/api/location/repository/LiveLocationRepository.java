package com.sonit.api.location.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.sonit.api.location.model.LiveLocation;

public interface LiveLocationRepository extends MongoRepository<LiveLocation, String> {

    Optional<LiveLocation> findByUserId(String userId);
}
