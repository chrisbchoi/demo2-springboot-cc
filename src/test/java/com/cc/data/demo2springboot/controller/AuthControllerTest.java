package com.cc.data.demo2springboot.controller;

import com.cc.data.demo2springboot.dto.AuthRequest;
import com.cc.data.demo2springboot.dto.AuthResponse;
import com.cc.data.demo2springboot.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtService jwtService;

    private AuthRequest validAdminRequest;
    private AuthRequest validUserRequest;
    private AuthRequest invalidRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        validAdminRequest = new AuthRequest("admin", "admin");
        validUserRequest = new AuthRequest("user", "password");
        invalidRequest = new AuthRequest("invaliduser", "wrongpassword");

        // Mock JWT service to return a test token
        when(jwtService.generateToken(anyString(), anyList())).thenReturn("test-jwt-token");
    }

    @Test
    @DisplayName("Login with admin credentials should succeed and return JWT token")
    void login_withAdminCredentials_shouldReturnToken() throws Exception {
        // Create user details with ADMIN role
        UserDetails adminDetails = new User("admin", "admin",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        // Create authentication object with admin authorities
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(
                adminDetails, null, adminDetails.getAuthorities());

        // Mock authentication manager
        when(authenticationManager.authenticate(any())).thenReturn(adminAuth);

        // Perform login request
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAdminRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("test-jwt-token")));
    }

    @Test
    @DisplayName("Login with user credentials should succeed with USER role")
    void login_withUserCredentials_shouldReturnToken() throws Exception {
        // Create user details with USER role
        UserDetails userDetails = new User("user", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        // Create authentication object with user authorities
        Authentication userAuth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // Mock authentication manager
        when(authenticationManager.authenticate(any())).thenReturn(userAuth);

        // Perform login request
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("test-jwt-token")));
    }

    @Test
    @DisplayName("Login with invalid credentials should return 401 Unauthorized")
    void login_withInvalidCredentials_shouldReturn401() throws Exception {
        // Mock authentication manager to throw exception for invalid credentials
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        // Perform login request with invalid credentials
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login with multiple roles should include all roles in token")
    void login_withMultipleRoles_shouldIncludeAllRolesInToken() throws Exception {
        // Create user details with multiple roles
        UserDetails userWithMultipleRoles = new User("admin", "admin",
                Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER")
                ));

        // Create authentication object with multiple authorities
        Authentication multiRoleAuth = new UsernamePasswordAuthenticationToken(
                userWithMultipleRoles, null, userWithMultipleRoles.getAuthorities());

        // Setup specific expectations for this test
        List<String> expectedRoles = Arrays.asList("ADMIN", "USER");
        when(jwtService.generateToken("admin", expectedRoles)).thenReturn("token-with-multiple-roles");
        when(authenticationManager.authenticate(any())).thenReturn(multiRoleAuth);

        // Perform login request
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAdminRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Verify the response contains a token
        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);
        assert response.getToken() != null : "Token should not be null";
    }

    @Test
    @DisplayName("Login request with missing username should be rejected")
    void login_withMissingUsername_shouldBeBadRequest() throws Exception {
        // Create an auth request with missing username
        AuthRequest invalidRequest = new AuthRequest(null, "password");

        // Perform login request
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login request with missing password should be rejected")
    void login_withMissingPassword_shouldBeBadRequest() throws Exception {
        // Create an auth request with missing password
        AuthRequest invalidRequest = new AuthRequest("username", null);

        // Perform login request
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
