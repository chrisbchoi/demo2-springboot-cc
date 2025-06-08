package com.cc.data.demo2springboot.controller;

import com.cc.data.demo2springboot.config.TestSecurityConfig;
import com.cc.data.demo2springboot.exception.ResourceNotFoundException;
import com.cc.data.demo2springboot.model.User;
import com.cc.data.demo2springboot.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
public class UserControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();

        User testUser = new User(1L, "testuser", "test@example.com", "Test User",
                LocalDateTime.now(), LocalDateTime.now(), true);

        // Common mock setups
        when(userService.getAllUsers()).thenReturn(
            Arrays.asList(
                testUser,
                new User(2L, "user2", "user2@example.com", "User Two",
                    LocalDateTime.now(), LocalDateTime.now(), true)
            )
        );

        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(userService.getUserById(99L)).thenReturn(Optional.empty());
        doThrow(new ResourceNotFoundException("User", "id", 99L))
                .when(userService).getUserById(99L);

        User savedUser = new User(1L, "newuser", "new@example.com", "New User",
                LocalDateTime.now(), LocalDateTime.now(), true);
        when(userService.createUser(any(User.class))).thenReturn(savedUser);

        User updatedUser = new User(1L, "updated", "updated@example.com", "Updated User",
                LocalDateTime.now(), LocalDateTime.now(), false);
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(updatedUser);
        doThrow(new ResourceNotFoundException("User", "id", 99L))
                .when(userService).updateUser(eq(99L), any(User.class));

        doNothing().when(userService).deleteUser(1L);
        doThrow(new ResourceNotFoundException("User", "id", 99L))
                .when(userService).deleteUser(99L);
    }

    @Test
    void getAllUsers_ShouldReturnUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].username", is("testuser")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].username", is("user2")));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.fullName", is("Test User")))
                .andExpect(jsonPath("$.active", is(true)));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUser_ShouldCreateAndReturnUser() throws Exception {
        User newUser = new User(null, "newuser", "new@example.com", "New User",
                null, null, true);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/users/1")))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("newuser")))
                .andExpect(jsonPath("$.email", is("new@example.com")))
                .andExpect(jsonPath("$.fullName", is("New User")))
                .andExpect(jsonPath("$.active", is(true)));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    void updateUser_WhenUserExists_ShouldUpdateAndReturnUser() throws Exception {
        User updatedUser = new User(1L, "updated", "updated@example.com", "Updated User",
                LocalDateTime.now(), LocalDateTime.now(), false);

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("updated")))
                .andExpect(jsonPath("$.email", is("updated@example.com")))
                .andExpect(jsonPath("$.fullName", is("Updated User")))
                .andExpect(jsonPath("$.active", is(false)));

        verify(userService, times(1)).updateUser(eq(1L), any(User.class));
    }

    @Test
    void updateUser_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        User updatedUser = new User(99L, "updated", "updated@example.com", "Updated User",
                LocalDateTime.now(), LocalDateTime.now(), false);

        mockMvc.perform(put("/api/users/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_WhenUserExists_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void deleteUser_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllUsersPaginated_ShouldReturnPaginatedUsers() throws Exception {
        User user1 = new User(1L, "testuser", "test@example.com", "Test User",
                LocalDateTime.now(), LocalDateTime.now(), true);
        User user2 = new User(2L, "user2", "user2@example.com", "User Two",
                LocalDateTime.now(), LocalDateTime.now(), true);
        User user3 = new User(3L, "user3", "user3@example.com", "User Three",
                LocalDateTime.now(), LocalDateTime.now(), true);

        List<User> allUsers = Arrays.asList(user1, user2, user3);
        when(userService.getAllUsers(any(Pageable.class))).thenAnswer(invocation -> {
            Pageable pageable = invocation.getArgument(0);
            int pageSize = pageable.getPageSize();
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageSize, allUsers.size());
            Page<User> userPage = new PageImpl<>(allUsers.subList(start, end), pageable, allUsers.size());
            return userPage;
        });

        mockMvc.perform(get("/api/users?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.size", is(2)))
                .andExpect(jsonPath("$.number", is(0)));

        verify(userService, times(1)).getAllUsers(any(Pageable.class));
    }
}
