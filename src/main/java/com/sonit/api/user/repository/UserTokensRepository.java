package com.sonit.api.user.repository;

import java.util.Optional;

import com.sonit.api.user.model.UserTokens;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserTokensRepository extends MongoRepository<UserTokens, String> {

    Optional<UserTokens> findByUserIdAndService(String userId, String service);
}
