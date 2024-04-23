package com.example.newapp.repo;

import com.example.newapp.model.CarDescription;
import com.example.newapp.model.WebsiteDescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarDescriptionRepository extends JpaRepository<CarDescription,Long> {
    List<CarDescription> findAllByUrl(String url);

    Page<CarDescription> findAll(Pageable pageable);

    Page<CarDescription> findByUrlContaining(String url, Pageable pageable);

    @Query("SELECT wd FROM CarDescription wd WHERE wd.url LIKE %?1% OR wd.date LIKE %?1% OR wd.detail LIKE %?1% OR wd.title LIKE %?1% OR wd.price LIKE %?1% OR wd.gear LIKE %?1% or wd.type LIKE %?1%")
    Page<CarDescription> search(String keyword, Pageable pageable);

    @Query("SELECT wd FROM CarDescription wd WHERE (wd.url LIKE %?2%) AND (wd.url LIKE %?1% OR wd.date LIKE %?1% OR wd.detail LIKE %?1% OR wd.title LIKE %?1% OR wd.price LIKE %?1% OR wd.gear LIKE %?1% or wd.type LIKE %?1%)")
    Page<CarDescription> search2(String keyword, String urlPart, Pageable pageable);

    @Query("SELECT cd FROM CarDescription cd WHERE cd.url LIKE %?1%")
    List<CarDescription> findAllByContainingUrl(String url);

    @Query("SELECT cd FROM CarDescription cd WHERE cd.price LIKE %?1% OR cd.price IS NULL OR TRIM(cd.price) = ''")
    List<CarDescription> findAllByContainingPrice(String url);
}
