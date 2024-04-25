package com.nextg.crawler.service;

import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@Service
public class deleteDuplicateRecordService {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void deleteWebsiteDuplicateRecords() {
        entityManager.createNativeQuery(
                "CREATE TEMPORARY TABLE temp_ids AS " +
                        "(SELECT id FROM website_description) "
        ).executeUpdate();

        entityManager.createNativeQuery(
                "CREATE TEMPORARY TABLE to_delete AS " +
                        "(SELECT id FROM temp_ids " +
                        "WHERE id NOT IN (SELECT MAX(id) FROM website_description GROUP BY detail, title))"
        ).executeUpdate();

        entityManager.createNativeQuery(
                "DELETE FROM website_description WHERE id IN " +
                        "(SELECT id FROM to_delete)"
        ).executeUpdate();

        entityManager.createNativeQuery(
                "DROP TEMPORARY TABLE IF EXISTS temp_ids"
        ).executeUpdate();

        entityManager.createNativeQuery(
                "DROP TEMPORARY TABLE IF EXISTS to_delete"
        ).executeUpdate();
    }

    @Transactional
    public void deleteCarDuplicateRecords() {
        entityManager.createNativeQuery(
                "CREATE TEMPORARY TABLE temp_ids AS " +
                        "(SELECT id FROM car_description) "
        ).executeUpdate();

        entityManager.createNativeQuery(
                "CREATE TEMPORARY TABLE to_delete AS " +
                        "(SELECT id FROM temp_ids " +
                        "WHERE id NOT IN (SELECT MAX(id) FROM car_description GROUP BY detail, title))"
        ).executeUpdate();

        entityManager.createNativeQuery(
                "DELETE FROM car_description WHERE id IN " +
                        "(SELECT id FROM to_delete)"
        ).executeUpdate();

        entityManager.createNativeQuery(
                "DROP TEMPORARY TABLE IF EXISTS temp_ids"
        ).executeUpdate();

        entityManager.createNativeQuery(
                "DROP TEMPORARY TABLE IF EXISTS to_delete"
        ).executeUpdate();
    }

}
