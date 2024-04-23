package com.example.newapp.repo;

import com.example.newapp.model.Website;
import com.example.newapp.model.WebsiteDescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebsiteRepository extends JpaRepository<Website,Long> {
    Optional<Website> getWebsiteById(Long id);

    Page<Website> findAll(Pageable pageable);

    List<Website> findAllByType(String type);
}
