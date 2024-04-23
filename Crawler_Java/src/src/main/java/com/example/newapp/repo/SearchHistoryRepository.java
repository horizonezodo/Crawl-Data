package com.example.newapp.repo;

import com.example.newapp.model.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory,Long> {
    Optional<SearchHistory> findByUserEmailAndCategoryId(String email, Long catId);
}
