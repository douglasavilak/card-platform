package com.douglasavila.cardservice.repository;

import com.douglasavila.cardservice.entity.BatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchStatusRepository extends JpaRepository<BatchStatus, Long> {
    BatchStatus findByBatchStatusName(String name);
}
