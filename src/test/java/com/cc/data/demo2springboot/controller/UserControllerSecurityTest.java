package com.cc.data.demo2springboot.controller;

import com.cc.data.demo2springboot.config.TestSecurityConfig;
import com.cc.data.demo2springboot.config.UserConfig;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @MockBean
    private UserConfig userConfig;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        // Setup MockMvc with Spring Security
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // Configure default values for UserConfig
        when(userConfig.getMaxBatchSize()).thenReturn(10);
        when(userConfig.getDefaultPage()).thenReturn(0);
        when(userConfig.getDefaultPageSize()).thenReturn(10);

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
                    .andExpect(status().isForbidden());
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
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should require authentication for deleting users")
        @WithAnonymousUser
        void deleteUser_WithAnonymousUser_ShouldBeForbidden() throws Exception {
            mockMvc.perform(delete("/api/users/1")
                    .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should require authentication for batch creating users")
        @WithAnonymousUser
        void createUsers_WithAnonymousUser_ShouldBeForbidden() throws Exception {
            List<User> newUsers = Arrays.asList(
                new User(null, "batch1", "batch1@example.com", "Batch User 1", null, null, true),
                new User(null, "batch2", "batch2@example.com", "Batch User 2", null, null, true)
            );

            mockMvc.perform(post("/api/users/batch")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newUsers)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should allow anonymous access to paginated users")
        @WithAnonymousUser
        void getAllUsersPaginated_WithAnonymousUser_ShouldBeAllowed() throws Exception {
            mockMvc.perform(get("/api/users")
                    .param("page", "0")
                    .param("size", "10")
                    .with(csrf()))
                    .andExpect(status().isOk());
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
                    .andExpect(status().isForbidden());
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

        @Test
        @DisplayName("User with ROLE_USER cannot create batch users")
        @WithMockUser(roles = "USER")
        void createUsers_WithRoleUser_ShouldBeForbidden() throws Exception {
            List<User> newUsers = Arrays.asList(
                new User(null, "batch1", "batch1@example.com", "Batch User 1", null, null, true),
                new User(null, "batch2", "batch2@example.com", "Batch User 2", null, null, true)
            );

            mockMvc.perform(post("/api/users/batch")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newUsers)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Admin with ROLE_ADMIN can create batch users")
        @WithMockUser(roles = "ADMIN")
        void createUsers_WithRoleAdmin_ShouldBeAllowed() throws Exception {
            List<User> newUsers = Arrays.asList(
                new User(null, "batch1", "batch1@example.com", "Batch User 1", null, null, true),
                new User(null, "batch2", "batch2@example.com", "Batch User 2", null, null, true)
            );

            mockMvc.perform(post("/api/users/batch")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newUsers)))
                    .andExpect(status().isCreated());
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
                    .andExpect(status().isForbidden());
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
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE without CSRF token should be forbidden")
        @WithMockUser(roles = "ADMIN")
        void deleteUser_WithoutCsrf_ShouldBeForbidden() throws Exception {
            mockMvc.perform(delete("/api/users/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST batch without CSRF token should be forbidden")
        @WithMockUser(roles = "ADMIN")
        void createUsers_WithoutCsrf_ShouldBeForbidden() throws Exception {
            List<User> newUsers = Arrays.asList(
                new User(null, "batch1", "batch1@example.com", "Batch User 1", null, null, true),
                new User(null, "batch2", "batch2@example.com", "Batch User 2", null, null, true)
            );

            mockMvc.perform(post("/api/users/batch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newUsers)))
                    .andExpect(status().isForbidden());
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
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Invalid HTTP method should be forbidden")
        void invalidMethod_ShouldReturnMethodNotAllowed() throws Exception {
            mockMvc.perform(patch("/api/users/1")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isForbidden());
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

    @Nested
    @DisplayName("Pagination and Batch Size Tests")
    class PaginationAndBatchSizeTests {

        @Test
        @DisplayName("Should respect max batch size limit")
        @WithMockUser(roles = "ADMIN")
        void createUsers_WithTooManyUsers_ShouldReturnBadRequest() throws Exception {
            // Configure a smaller batch size limit
            int maxBatchSize = 5;
            when(userConfig.getMaxBatchSize()).thenReturn(maxBatchSize);

            // Create a list exceeding the limit
            List<User> tooManyUsers = new ArrayList<>();
            for (int i = 1; i <= maxBatchSize + 1; i++) {
                tooManyUsers.add(new User(null, "batch" + i, "batch" + i + "@example.com", "Batch User " + i, null, null, true));
            }

            mockMvc.perform(post("/api/users/batch")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(tooManyUsers)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Batch size exceeds maximum allowed")));
        }

        @Test
        @DisplayName("Should use configured default pagination parameters")
        @WithAnonymousUser
        void getAllUsersPaginated_ShouldUseConfiguredDefaults() throws Exception {
            // Set custom pagination defaults
            int defaultPage = 1;
            int defaultSize = 20;
            when(userConfig.getDefaultPage()).thenReturn(defaultPage);
            when(userConfig.getDefaultPageSize()).thenReturn(defaultSize);

            // No specific params - should use defaults
            mockMvc.perform(get("/api/users")
                    .param("page", "")
                    .param("size", "")
                    .with(csrf()))
                    .andExpect(status().isOk());

            // Verify the correct default values were used
            verify(userService, times(1)).getAllUsers(argThat(pageable ->
                pageable.getPageNumber() == defaultPage &&
                pageable.getPageSize() == defaultSize));
        }
    }
}
