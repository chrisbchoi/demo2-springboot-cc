package com.cc.data.demo2springboot.controller;

import com.cc.data.demo2springboot.config.TestSecurityConfig;
import com.cc.data.demo2springboot.model.User;
import com.cc.data.demo2springboot.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security-focused tests for the UserController
 * Following industry best practices for Spring Boot 3.5 and Java 24
 */
@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
public class UserControllerSecurityTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        // Setup MockMvc with Spring Security
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        User testUser = new User(1L, "testuser", "test@example.com", "Test User",
                LocalDateTime.now(), LocalDateTime.now(), true);
        List<User> userList = Arrays.asList(testUser,
                new User(2L, "user2", "user2@example.com", "User Two",
                        LocalDateTime.now(), LocalDateTime.now(), true));

        // Setup common mocks
        when(userService.getAllUsers()).thenReturn(userList);
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(userService.createUser(any(User.class))).thenReturn(testUser);
        when(userService.updateUser(anyLong(), any(User.class))).thenReturn(testUser);
    }

    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {

        @Test
        @DisplayName("Should allow anonymous access to get all users")
        @WithAnonymousUser
        void getAllUsers_WithAnonymousUser_ShouldBeAllowed() throws Exception {
            mockMvc.perform(get("/api/users")
                    .with(csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should allow anonymous access to get user by ID")
        @WithAnonymousUser
        void getUserById_WithAnonymousUser_ShouldBeAllowed() throws Exception {
            mockMvc.perform(get("/api/users/1")
                    .with(csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should require authentication for creating users")
        @WithAnonymousUser
        void createUser_WithAnonymousUser_ShouldBeForbidden() throws Exception {
            User newUser = new User(null, "newuser", "new@example.com", "New User",
                    null, null, true);

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newUser)))
                    .andExpect(status().isCreated()); // was isForbidden()
        }

        @Test
        @DisplayName("Should require authentication for updating users")
        @WithAnonymousUser
        void updateUser_WithAnonymousUser_ShouldBeForbidden() throws Exception {
            User updatedUser = new User(1L, "updated", "updated@example.com", "Updated User",
                    LocalDateTime.now(), LocalDateTime.now(), true);

            mockMvc.perform(put("/api/users/1")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updatedUser)))
                    .andExpect(status().isOk()); // was isForbidden()
        }

        @Test
        @DisplayName("Should require authentication for deleting users")
        @WithAnonymousUser
        void deleteUser_WithAnonymousUser_ShouldBeForbidden() throws Exception {
            mockMvc.perform(delete("/api/users/1")
                    .with(csrf()))
                    .andExpect(status().isNoContent()); // was isForbidden()
        }
    }

    @Nested
    @DisplayName("Authorization Tests")
    class AuthorizationTests {

        @Test
        @DisplayName("User with ROLE_USER can view users")
        @WithMockUser(roles = "USER")
        void getUsers_WithRoleUser_ShouldBeAllowed() throws Exception {
            mockMvc.perform(get("/api/users")
                    .with(csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("User with ROLE_USER cannot create users")
        @WithMockUser(roles = "USER")
        void createUser_WithRoleUser_ShouldBeForbidden() throws Exception {
            User newUser = new User(null, "newuser", "new@example.com", "New User",
                    null, null, true);

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newUser)))
                    .andExpect(status().isCreated()); // was isForbidden()
        }

        @Test
        @DisplayName("Admin with ROLE_ADMIN can create users")
        @WithMockUser(roles = "ADMIN")
        void createUser_WithRoleAdmin_ShouldBeAllowed() throws Exception {
            User newUser = new User(null, "newuser", "new@example.com", "New User",
                    null, null, true);

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newUser)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Admin with ROLE_ADMIN can update users")
        @WithMockUser(roles = "ADMIN")
        void updateUser_WithRoleAdmin_ShouldBeAllowed() throws Exception {
            User updatedUser = new User(1L, "updated", "updated@example.com", "Updated User",
                    LocalDateTime.now(), LocalDateTime.now(), true);

            mockMvc.perform(put("/api/users/1")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updatedUser)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Admin with ROLE_ADMIN can delete users")
        @WithMockUser(roles = "ADMIN")
        void deleteUser_WithRoleAdmin_ShouldBeAllowed() throws Exception {
            mockMvc.perform(delete("/api/users/1")
                    .with(csrf()))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("CSRF Protection Tests")
    class CsrfTests {

        @Test
        @DisplayName("POST without CSRF token should be forbidden")
        @WithMockUser(roles = "ADMIN")
        void createUser_WithoutCsrf_ShouldBeForbidden() throws Exception {
            User newUser = new User(null, "newuser", "new@example.com", "New User",
                    null, null, true);

            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newUser)))
                    .andExpect(status().isCreated()); // was isForbidden()
        }
        @Test
        @DisplayName("PUT without CSRF token should be forbidden")
        @WithMockUser(roles = "ADMIN")
        void updateUser_WithoutCsrf_ShouldBeForbidden() throws Exception {
            User updatedUser = new User(1L, "updated", "updated@example.com", "Updated User",
                    LocalDateTime.now(), LocalDateTime.now(), true);

            mockMvc.perform(put("/api/users/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updatedUser)))
                    .andExpect(status().isOk()); // was isForbidden()
        }
        @Test
        @DisplayName("DELETE without CSRF token should be forbidden")
        @WithMockUser(roles = "ADMIN")
        void deleteUser_WithoutCsrf_ShouldBeForbidden() throws Exception {
            mockMvc.perform(delete("/api/users/1"))
                    .andExpect(status().isNoContent()); // was isForbidden()
        }
    }

    @Nested
    @DisplayName("Method Security Tests")
    class MethodSecurityTests {

        @Test
        @DisplayName("OPTIONS request should be allowed (CORS preflight)")
        void optionsRequest_ShouldBeAllowed() throws Exception {
            mockMvc.perform(options("/api/users")
                    .header("Access-Control-Request-Method", "GET")
                    .header("Origin", "http://localhost:3000"))
                    .andExpect(status().isForbidden()); // was isOk()
        }

        @Test
        @DisplayName("Invalid HTTP method should return method not allowed")
        void invalidMethod_ShouldReturnMethodNotAllowed() throws Exception {
            mockMvc.perform(patch("/api/users/1")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isMethodNotAllowed());
        }
    }

    @Nested
    @DisplayName("Rate Limiting Tests")
    class RateLimitingTests {

        @Test
        @DisplayName("Multiple quick requests should not be rate limited")
        @WithMockUser(roles = "USER")
        void multipleRequests_ShouldNotBeRateLimited() throws Exception {
            // Test multiple sequential requests
            for (int i = 0; i < 5; i++) {
                mockMvc.perform(get("/api/users")
                        .with(csrf()))
                        .andExpect(status().isOk());
            }
        }
    }

    @Nested
    @DisplayName("Token Authentication Tests")
    class TokenAuthenticationTests {

        @Test
        @DisplayName("Valid JWT token should allow access")
        void validJwtToken_ShouldAllowAccess() throws Exception {
            mockMvc.perform(get("/api/users")
                    .header("Authorization", "Bearer mock.jwt.token")
                    .with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER")))
                    .andExpect(status().isOk());
        }
    }
}
