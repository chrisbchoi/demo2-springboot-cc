package com.cc.data.demo2springboot.controller;

import com.cc.data.demo2springboot.exception.ResourceNotFoundException;
import com.cc.data.demo2springboot.model.User;
import com.cc.data.demo2springboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * REST controller for managing User resources.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/users : Get all users
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
     *
     * @param user the user to create
     * @return the ResponseEntity with status 201 (Created) and with body the new user
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdUser.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdUser);
    }

    /**
     * PUT /api/users/{id} : Updates an existing user
     *
     * @param id          the id of the user to update
     * @param userDetails the user to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated user,
     * or with status 404 (Not Found) if the user is not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        try {
            User updatedUser = userService.updateUser(id, userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("User", "id", id);
        }
    }

    /**
     * DELETE /api/users/{id} : Delete the user with the specified id
     *
     * @param id the id of the user to delete
     * @return the ResponseEntity with status 204 (NO_CONTENT)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("User", "id", id);
        }
    }
}
