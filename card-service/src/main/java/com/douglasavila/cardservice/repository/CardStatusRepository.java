package com.douglasavila.cardservice.repository;

import com.douglasavila.cardservice.entity.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardStatusRepository extends JpaRepository<CardStatus, Long> {
    CardStatus findByCardStatusName(String name);
}
