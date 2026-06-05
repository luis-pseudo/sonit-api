package com.sonit.api.user.repository;

import java.util.Optional;

import com.sonit.api.user.model.User;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    @Query("{ 'authProviders': { $elemMatch: { 'provider': ?0, 'providerId': ?1 } } }")
    Optional<User> findByAuthProvidersProviderAndAuthProvidersProviderId(String provider, String providerId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
