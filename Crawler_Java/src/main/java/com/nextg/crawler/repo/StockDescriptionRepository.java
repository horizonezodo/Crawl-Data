package com.nextg.crawler.repo;

import com.nextg.crawler.model.StockDescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockDescriptionRepository extends JpaRepository<StockDescription, Long> {
    List<StockDescription> findAllByUrl(String url);

    Page<StockDescription> findAll(Pageable pageable);

    Page<StockDescription> findByUrlContaining(String url, Pageable pageable);

    @Query("SELECT sd FROM StockDescription sd WHERE sd.url LIKE %?1% OR sd.floor LIKE %?1% OR sd.career LIKE %?1% OR sd.companyName LIKE %?1% OR sd.stockCode LIKE %?1% OR sd.price LIKE %?1% OR sd.date LIKE %?1%")
    Page<StockDescription> search(String keyword, Pageable pageable);

    @Query("SELECT sd FROM StockDescription sd WHERE (sd.url LIKE %?2%) AND (sd.url LIKE %?1% OR sd.floor LIKE %?1% OR sd.career LIKE %?1% OR sd.companyName LIKE %?1% OR sd.stockCode LIKE %?1% OR sd.price LIKE %?1% OR sd.date LIKE %?1% )")
    Page<StockDescription> search2(String keyword, String urlPart, Pageable pageable);

    @Query("SELECT sd FROM StockDescription sd WHERE sd.url LIKE %?1%")
    List<StockDescription> findAllByContainingUrl(String url);


}
