package com.p2p.repository;

import com.p2p.model.User;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    void save(User user);
    void update(User user);
} 