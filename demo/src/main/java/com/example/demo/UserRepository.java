package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmailIgnoreCase(String email);

    @Query("""
            SELECT u FROM User u
            WHERE (:role IS NULL OR u.type = :role)
              AND (
                :q IS NULL OR :q = '' OR
                LOWER(u.name) LIKE LOWER(CONCAT('%', :q, '%')) OR
                LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')) OR
                LOWER(u.phone) LIKE LOWER(CONCAT('%', :q, '%')) OR
                LOWER(u.user_id) LIKE LOWER(CONCAT('%', :q, '%'))
              )
            ORDER BY u.name ASC, u.email ASC
            """)
    Page<User> searchUsers(@Param("q") String q, @Param("role") UserType role, Pageable pageable);
}
