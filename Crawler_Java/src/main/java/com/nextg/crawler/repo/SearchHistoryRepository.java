package com.nextg.crawler.repo;

import com.nextg.crawler.model.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory,Long> {
    Optional<SearchHistory> findByUserEmailAndCategoryId(String email, Long catId);
}
