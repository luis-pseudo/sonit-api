package com.sonit.api.user.repository;

import java.util.Optional;

import com.sonit.api.user.model.User;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByAuthProvidersProviderAndAuthProvidersProviderId(String provider, String providerId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
