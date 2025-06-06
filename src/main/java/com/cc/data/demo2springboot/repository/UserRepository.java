package com.cc.data.demo2springboot.repository;

import com.cc.data.demo2springboot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for User entities.
 * Extends JpaRepository to enable CRUD operations with the H2 database.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data JPA provides all basic CRUD operations automatically
    // Custom query methods can be added here if needed
}
