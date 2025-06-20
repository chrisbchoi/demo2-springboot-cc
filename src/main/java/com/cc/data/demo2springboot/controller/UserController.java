package com.cc.data.demo2springboot.controller;

import com.cc.data.demo2springboot.config.UserConfig;
import com.cc.data.demo2springboot.exception.ResourceNotFoundException;
import com.cc.data.demo2springboot.model.User;
import com.cc.data.demo2springboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for managing User resources.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserConfig userConfig;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(UserService userService, UserConfig userConfig) {
        this.userService = userService;
        this.userConfig = userConfig;
    }

    /**
     * GET /api/users : Get all users with pagination support
     *
     * @param page The page number (zero-based), defaults to configured value
     * @param size The size of the page, defaults to configured value
     * @return the ResponseEntity with status 200 (OK) and the paged list of users in the body
     */
    @GetMapping(params = {"page", "size"})
    public ResponseEntity<Page<User>> getAllUsersPaginated(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        // Use configured defaults if parameters are not provided
        int pageNumber = (page != null) ? page : userConfig.getDefaultPage();
        int pageSize = (size != null) ? size : userConfig.getDefaultPageSize();

        logger.debug("Getting paginated users with page={}, size={}", pageNumber, pageSize);

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<User> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/users : Get all users (non-paginated)
     *
     * @return the ResponseEntity with status 200 (OK) and the list of users in the body
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/users/{id} : Get the user with the specified id
     *
     * @param id the id of the user to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the user, or with status 404 (Not Found)
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    /**
     * POST /api/users : Create a new user
     * Requires JWT token authentication with ROLE_ADMIN
     *
     * @param user the user to create
     * @return the ResponseEntity with status 201 (Created) and with body the new user
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("User creation requested by: {}", authentication.getName());

        // Set creation timestamp
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User createdUser = userService.createUser(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdUser.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdUser);
    }

    /**
     * POST /api/users/batch : Create multiple users at once
     * Requires JWT token authentication with ROLE_ADMIN
     *
     * @param users the list of users to create
     * @return the ResponseEntity with status 201 (Created) and with body the list of created users,
     *         or with status 400 (Bad Request) if the batch size exceeds the maximum allowed
     */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUsers(@RequestBody List<User> users) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Batch user creation requested by: {} for {} users", authentication.getName(), users.size());

        // Verify batch size doesn't exceed maximum allowed
        int maxBatchSize = userConfig.getMaxBatchSize();
        if (users.size() > maxBatchSize) {
            logger.warn("Batch user creation rejected: batch size {} exceeds maximum allowed {}",
                    users.size(), maxBatchSize);
            return ResponseEntity.badRequest()
                    .body("Batch size exceeds maximum allowed. Maximum " + maxBatchSize +
                          " users can be created per request.");
        }

        // Set creation timestamp for all users
        LocalDateTime now = LocalDateTime.now();
        users.forEach(user -> {
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
        });

        List<User> createdUsers = users.stream()
                .map(userService::createUser)
                .collect(Collectors.toList());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .build()
                .toUri();

        return ResponseEntity.created(location).body(createdUsers);
    }

    /**
     * PUT /api/users/{id} : Updates an existing user
     * Requires JWT token authentication with ROLE_ADMIN
     *
     * @param id          the id of the user to update
     * @param userDetails the user to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated user,
     * or with status 404 (Not Found) if the user is not found
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("User update requested by: {} for user id: {}", authentication.getName(), id);

        try {
            // Get the existing user and update only the allowed fields
            Optional<User> existingUser = userService.getUserById(id);
            if (existingUser.isPresent()) {
                // Set updated timestamp
                userDetails.setUpdatedAt(LocalDateTime.now());
                userDetails.setCreatedAt(existingUser.get().getCreatedAt());
            }

            User updatedUser = userService.updateUser(id, userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("User", "id", id);
        }
    }

    /**
     * DELETE /api/users/{id} : Delete the user with the specified id
     * Requires JWT token authentication with ROLE_ADMIN
     *
     * @param id the id of the user to delete
     * @return the ResponseEntity with status 204 (NO_CONTENT)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("User deletion requested by: {} for user id: {}", authentication.getName(), id);

        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("User", "id", id);
        }
    }
}
