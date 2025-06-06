package com.cc.data.demo2springboot.service;

import com.cc.data.demo2springboot.model.User;
import com.cc.data.demo2springboot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service class for managing User entities.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Get all users
     * @return list of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get a user by ID
     * @param id user ID
     * @return Optional containing the user if found
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Create a new user
     * @param user user to create
     * @return the created user
     * @throws NullPointerException if user is null
     */
    public User createUser(User user) {
        // Ensure the user is not null
        Objects.requireNonNull(user, "User cannot be null");

        // In a real application, you would validate the user data here
        // and potentially hash passwords if they're included
        return userRepository.save(user);
    }

    /**
     * Update an existing user
     * @param id user ID
     * @param userDetails updated user details
     * @return the updated user
     * @throws RuntimeException if the user is not found
     * @throws NullPointerException if userDetails is null
     */
    public User updateUser(Long id, User userDetails) {
        // Ensure userDetails is not null
        Objects.requireNonNull(userDetails, "User details cannot be null");

        return userRepository.findById(id).map(existingUser -> {
            // Update fields from userDetails
            existingUser.setUsername(userDetails.getUsername());
            existingUser.setEmail(userDetails.getEmail());
            existingUser.setFullName(userDetails.getFullName());
            existingUser.setActive(userDetails.isActive());

            // Save and return the updated user
            return userRepository.save(existingUser);
        }).orElseThrow(() -> new RuntimeException("User not found with id " + id));
    }

    /**
     * Delete a user
     * @param id user ID
     * @throws RuntimeException if the user is not found
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id " + id);
        }
        userRepository.deleteById(id);
    }
}
