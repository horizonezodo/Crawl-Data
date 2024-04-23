package com.example.newapp.repo;

import com.example.newapp.model.User;
import com.example.newapp.model.WebsiteDescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);

    Page<User> findAll(Pageable pageable);

    boolean existsByEmail(String email);
}