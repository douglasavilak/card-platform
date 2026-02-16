package com.douglasavila.cardservice.repository;

import com.douglasavila.cardservice.entity.Batch;
import com.douglasavila.cardservice.entity.BatchStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class BatchJpaMappingTest {

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private EntityManager em;

    @Test
    void save_persistsBatch_andGeneratesUuid() {
        Batch batch = new Batch("fileA.txt", LocalDate.of(2026, 2, 15), "LOTE0001", 10);

        Batch saved = batchRepository.saveAndFlush(batch);

        assertThat(saved.getBatchId()).isNotNull();
        assertThat(saved.getFileName()).isEqualTo("fileA.txt");
        assertThat(saved.getBatchCode()).isEqualTo("LOTE0001");
        assertThat(saved.getExpectedRecords()).isEqualTo(10);
    }

    @Test
    void save_enforcesUniqueConstraint_onBatchCodeAndFileName() {
        batchRepository.saveAndFlush(
                new Batch("fileA.txt", LocalDate.of(2026, 2, 15), "LOTE0001", 10)
        );

        assertThatThrownBy(() ->
                batchRepository.saveAndFlush(
                        new Batch("fileA.txt", LocalDate.of(2026, 2, 15), "LOTE0001", 10)
                )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

//    @Test
//    void save_withoutCascade_doesNotPersistTransientBatchStatus() {
//        Batch batch = new Batch("fileB.txt", LocalDate.of(2026, 2, 15), "LOTE0002", 5);
//
//        BatchStatus transientStatus = new BatchStatus();
//        batch.setBatchStatus(transientStatus);
//
//        assertThatThrownBy(() -> batchRepository.saveAndFlush(batch))
//                .isInstanceOfAny(RuntimeException.class);
//    }
//
//    @Test
//    void save_withoutCascade_allowsLinkingToAlreadyPersistedStatus() {
//        BatchStatus status = new BatchStatus();
//        em.persist(status);
//        em.flush();
//
//        Batch batch = new Batch("fileC.txt", LocalDate.of(2026, 2, 15), "LOTE0003", 3);
//        batch.setBatchStatus(status);
//
//        Batch saved = batchRepository.saveAndFlush(batch);
//
//        assertThat(saved.getBatchId()).isNotNull();
//        assertThat(saved.getBatchStatus()).isNotNull();
//    }
}
