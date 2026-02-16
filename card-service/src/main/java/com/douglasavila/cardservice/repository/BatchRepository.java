package com.douglasavila.cardservice.repository;

import com.douglasavila.cardservice.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BatchRepository extends JpaRepository<Batch, UUID> {
    Optional<Batch> findByBatchCodeAndFileName(String batchCode,  String fileName);
}
