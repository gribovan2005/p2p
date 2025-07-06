package com.p2p;

import com.p2p.model.User;
import com.p2p.repository.UserRepository;
import com.p2p.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    void register_success() {
        when(userRepository.existsByUsername("user")).thenReturn(false);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        userService.register("user", "Password1");
        verify(userRepository).save(captor.capture());
        assertEquals("user", captor.getValue().getUsername());
        assertNotNull(captor.getValue().getPassword());
    }

    @Test
    void register_duplicateUsername_throws() {
        when(userRepository.existsByUsername("user")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> userService.register("user", "pass"));
    }

    @Test
    void findByUsername_success() {
        User user = new User();
        user.setUsername("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        assertEquals(user, userService.findByUsername("user"));
    }

    @Test
    void findByUsername_notFound_throws() {
        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.findByUsername("user"));
    }

    @Test
    void checkPassword_success() {
        User user = new User();
        user.setUsername("user");
        userService.register("user", "Password1");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        user.setPassword(captor.getValue().getPassword());
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        assertTrue(userService.checkPassword("user", "Password1"));
    }

    @Test
    void register_emptyUsername_throws() {
        assertThrows(IllegalArgumentException.class, () -> userService.register("", "Password1"));
        assertThrows(IllegalArgumentException.class, () -> userService.register("   ", "Password1"));
        assertThrows(IllegalArgumentException.class, () -> userService.register(null, "Password1"));
    }

    @Test
    void register_shortPassword_throws() {
        assertThrows(IllegalArgumentException.class, () -> userService.register("user", "Abc12"));
    }

    @Test
    void register_passwordNoDigit_throws() {
        assertThrows(IllegalArgumentException.class, () -> userService.register("user", "Password"));
    }

    @Test
    void register_passwordNoLetter_throws() {
        assertThrows(IllegalArgumentException.class, () -> userService.register("user", "12345678"));
    }
} 