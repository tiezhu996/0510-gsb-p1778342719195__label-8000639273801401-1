package com.cardmanager.repository;

import com.cardmanager.entity.CardBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardBatchRepository extends JpaRepository<CardBatch, String> {
}
