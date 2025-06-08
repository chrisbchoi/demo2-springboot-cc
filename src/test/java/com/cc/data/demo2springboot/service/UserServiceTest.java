package com.cc.data.demo2springboot.service;

import com.cc.data.demo2springboot.model.User;
import com.cc.data.demo2springboot.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        testUser = new User(1L, "testuser", "test@example.com", "Test User",
                now, now, true);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Arrange
        User user2 = new User(2L, "user2", "user2@example.com", "User Two",
                now, now, true);
        List<User> expectedUsers = Arrays.asList(testUser, user2);

        when(userRepository.findAll()).thenReturn(expectedUsers);

        // Act
        List<User> actualUsers = userService.getAllUsers();

        // Assert
        assertThat(actualUsers).isEqualTo(expectedUsers);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getAllUsers_WhenNoUsers_ShouldReturnEmptyList() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<User> actualUsers = userService.getAllUsers();

        // Assert
        assertThat(actualUsers).isEmpty();
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.getUserById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldReturnEmptyOptional() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.getUserById(99L);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(99L);
    }

    @Test
    void createUser_ShouldSaveAndReturnUser() {
        // Arrange
        User newUser = new User(null, "newuser", "new@example.com", "New User",
                null, null, true);
        User savedUser = new User(1L, "newuser", "new@example.com", "New User",
                now, now, true);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.createUser(newUser);

        // Assert
        assertThat(result).isEqualTo(savedUser);
        verify(userRepository, times(1)).save(newUser);
    }

    @Test
    void createUser_WithNullUser_ShouldThrowException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> userService.createUser(null));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_WhenUserExists_ShouldUpdateAndReturnUser() {
        // Arrange
        User updatedDetails = new User(1L, "updated", "updated@example.com", "Updated User",
                null, null, false);
        User existingUser = new User(1L, "testuser", "test@example.com", "Test User",
                now, now, true);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        User result = userService.updateUser(1L, updatedDetails);

        // Assert
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getId()).isEqualTo(1L);
        assertThat(savedUser.getUsername()).isEqualTo("updated");
        assertThat(savedUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(savedUser.getFullName()).isEqualTo("Updated User");
        assertThat(savedUser.isActive()).isFalse();
        assertThat(savedUser.getCreatedAt()).isEqualTo(existingUser.getCreatedAt());

        assertThat(result.getUsername()).isEqualTo(updatedDetails.getUsername());
        assertThat(result.getEmail()).isEqualTo(updatedDetails.getEmail());
        assertThat(result.getFullName()).isEqualTo(updatedDetails.getFullName());
        assertThat(result.isActive()).isEqualTo(updatedDetails.isActive());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_WhenUserDoesNotExist_ShouldThrowException() {
        // Arrange
        User updatedDetails = new User(99L, "updated", "updated@example.com", "Updated User",
                null, null, false);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUser(99L, updatedDetails);
        });

        assertThat(exception.getMessage()).contains("User not found");
        verify(userRepository, times(1)).findById(99L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_WithNullDetails_ShouldThrowException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> userService.updateUser(1L, null));
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_WhenUserExists_ShouldDeleteUser() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_WhenUserDoesNotExist_ShouldThrowException() {
        // Arrange
        when(userRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.deleteUser(99L);
        });

        assertThat(exception.getMessage()).contains("User not found");
        verify(userRepository, times(1)).existsById(99L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void getAllUsers_WithPageable_ShouldReturnPaginatedUsers() {
        // Arrange
        User user2 = new User(2L, "user2", "user2@example.com", "User Two",
                now, now, true);
        List<User> userList = Arrays.asList(testUser, user2);
        Page<User> expectedPage = new PageImpl<>(userList);

        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(pageable)).thenReturn(expectedPage);

        // Act
        Page<User> actualPage = userService.getAllUsers(pageable);

        // Assert
        assertThat(actualPage.getContent()).containsExactlyInAnyOrderElementsOf(userList);
        assertThat(actualPage.getTotalElements()).isEqualTo(userList.size());
        assertThat(actualPage.getTotalPages()).isEqualTo(1);
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    void getAllUsers_WithPageable_WhenNoUsers_ShouldReturnEmptyPage() {
        // Arrange
        Page<User> emptyPage = new PageImpl<>(Collections.emptyList());

        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<User> actualPage = userService.getAllUsers(pageable);

        // Assert
        assertThat(actualPage.getContent()).isEmpty();
        assertThat(actualPage.getTotalElements()).isEqualTo(0);
        // When creating an empty Page with PageImpl and a non-zero page size,
        // Spring Data JPA will return 1 total page instead of 0
        assertThat(actualPage.getTotalPages()).isEqualTo(1);
        verify(userRepository, times(1)).findAll(pageable);
    }
}
