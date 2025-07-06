package com.p2p.service;

import com.p2p.model.User;
import com.p2p.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public void register(String username, String rawPassword) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (rawPassword == null || rawPassword.length() < 8 ||
            !rawPassword.matches(".*[A-Za-z].*") || !rawPassword.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must be at least 8 characters and contain letters and digits");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        String hashed = passwordEncoder.encode(rawPassword);
        User user = new User();
        user.setUsername(username);
        user.setPassword(hashed);
        userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public boolean checkPassword(String username, String rawPassword) {
        User user = findByUsername(username);
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public void updateName(String username, String name) {
        User user = findByUsername(username);
        user.setName(name);
        userRepository.update(user);
    }

    public String getName(String username) {
        User user = findByUsername(username);
        return user.getName();
    }

    public void updateProfile(String username, String name, Integer age, String photoUrl) {
        if (name != null && name.length() > 100) {
            throw new IllegalArgumentException("Name too long (max 100 characters)");
        }
        if (age != null && (age < 0 || age > 150)) {
            throw new IllegalArgumentException("Age must be between 0 and 150");
        }
        if (photoUrl != null && photoUrl.length() > 1000000) { // 1MB limit for base64
            throw new IllegalArgumentException("Photo data too large (max 1MB)");
        }
        
        if (photoUrl != null && !photoUrl.isEmpty()) {
            if (!photoUrl.startsWith("data:image/")) {
                throw new IllegalArgumentException("Photo must be base64 data URI");
            }
        }
        
        User user = findByUsername(username);
        user.setName(name);
        user.setAge(age);
        user.setPhotoUrl(photoUrl);
        userRepository.update(user);
    }

    public User getProfile(String username) {
        return findByUsername(username);
    }
} 