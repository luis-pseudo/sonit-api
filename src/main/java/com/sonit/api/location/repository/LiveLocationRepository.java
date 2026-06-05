package com.sonit.api.location.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.sonit.api.location.model.LiveLocation;

public interface LiveLocationRepository extends MongoRepository<LiveLocation, String> {

    Optional<LiveLocation> findByUserId(String userId);

    List<LiveLocation> findByVisibleTrueAndPositionNear(Point point, Distance distance, Pageable pageable);
}
