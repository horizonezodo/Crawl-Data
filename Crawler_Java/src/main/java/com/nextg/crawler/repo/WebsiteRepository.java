package com.nextg.crawler.repo;

import com.nextg.crawler.model.Website;
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
