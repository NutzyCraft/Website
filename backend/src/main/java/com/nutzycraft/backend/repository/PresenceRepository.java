package com.nutzycraft.backend.repository;

import com.nutzycraft.backend.entity.UserPresence;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PresenceRepository extends MongoRepository<UserPresence, Long> {
}
