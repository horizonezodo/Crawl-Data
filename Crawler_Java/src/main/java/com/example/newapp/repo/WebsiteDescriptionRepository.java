package com.example.newapp.repo;

import com.example.newapp.model.WebsiteDescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface WebsiteDescriptionRepository extends JpaRepository<WebsiteDescription ,Long> {
    List<WebsiteDescription> findAllByUrl(String url);

    Page<WebsiteDescription> findAll(Pageable pageable);

    Page<WebsiteDescription> findByUrlContaining(String url, Pageable pageable);

    //Search cho tat ca cac doi tuong
    @Query("SELECT wd FROM WebsiteDescription wd WHERE wd.url LIKE %?1% OR wd.date LIKE %?1% OR wd.detail LIKE %?1% OR wd.title LIKE %?1% OR wd.price LIKE %?1% OR wd.square LIKE %?1%")
    Page<WebsiteDescription> search(String keyword, Pageable pageable);

    //Search voi doi tuong nam trong 1 vung chi dinh
    @Query("SELECT wd FROM WebsiteDescription wd WHERE (wd.url LIKE %?2%) AND (wd.url LIKE %?1% OR wd.date LIKE %?1% OR wd.detail LIKE %?1% OR wd.title LIKE %?1% OR wd.price LIKE %?1% OR wd.square LIKE %?1%)")
    Page<WebsiteDescription> search2(String keyword, String urlPart, Pageable pageable);

    default Page<WebsiteDescription> searchWithSort(String keyword, Pageable pageable) {
        Sort sort = Sort.by(Sort.Direction.DESC, "date");
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return search(keyword, pageable);
    }

    WebsiteDescription findByUrlAndDetailAndTitleAndPriceAndSquare(String url, String detail,String title,String price, String square);

    List<WebsiteDescription> findAllByUrlAndDetailAndTitleAndPriceAndSquare(String url, String detail,String title,String price, String square);
}
