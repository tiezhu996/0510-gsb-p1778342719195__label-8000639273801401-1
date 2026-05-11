package com.cardmanager.repository;

import com.cardmanager.entity.CardInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardInfoRepository extends JpaRepository<CardInfo, Long>, JpaSpecificationExecutor<CardInfo> {

    Optional<CardInfo> findByCardNumber(String cardNumber);

    Optional<CardInfo> findByCardNumberAndCardPassword(String cardNumber, String cardPassword);

    boolean existsByCardNumber(String cardNumber);

    @Modifying
    @Query("UPDATE CardInfo c SET c.isDeleted = 1, c.status = 2 WHERE c.batchNumber = ?1")
    int recycleByBatchNumber(String batchNumber);

    @Modifying
    @Query("UPDATE CardInfo c SET c.isDeleted = 1, c.status = 2 WHERE c.cardNumber = ?1")
    int recycleByCardNumber(String cardNumber);

    long countByStatus(Integer status);
}
