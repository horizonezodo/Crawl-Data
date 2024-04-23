package com.example.newapp.repo;

import com.example.newapp.model.CarDescription;
import com.example.newapp.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {
    Page<Category> findAll(Pageable pageable);

    Optional<Category> findByCategoryName(String categoryName);
}
