package com.douglasavila.cardservice.repository;

import com.douglasavila.cardservice.entity.BatchStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class BatchStatusJpaMappingTest {

    @Autowired
    private BatchStatusRepository batchStatusRepository;

    @Test
    void save_persistsEntity_andGeneratesIdentityId() {
        BatchStatus status = new BatchStatus();
        status.setBatchStatusName("CUSTOM");

        BatchStatus saved = batchStatusRepository.saveAndFlush(status);

        assertThat(saved.getBatchStatusId()).isGreaterThan(0L);
        assertThat(saved.getBatchStatusName()).isEqualTo("CUSTOM");
    }

    @Test
    void seededStatuses_exist_byId() {
        BatchStatus received = batchStatusRepository.findById(BatchStatus.Values.RECEIVED.getBatchStatusId()).orElseThrow();
        BatchStatus processed = batchStatusRepository.findById(BatchStatus.Values.PROCESSED.getBatchStatusId()).orElseThrow();
        BatchStatus partiallyProcessed = batchStatusRepository.findById(BatchStatus.Values.PARTIALLY_PROCESSED.getBatchStatusId()).orElseThrow();
        BatchStatus failed = batchStatusRepository.findById(BatchStatus.Values.FAILED.getBatchStatusId()).orElseThrow();

        assertThat(processed.getBatchStatusName()).isEqualTo("PROCESSED");
        assertThat(partiallyProcessed.getBatchStatusName()).isEqualTo("PARTIALLY_PROCESSED");
        assertThat(failed.getBatchStatusName()).isEqualTo("FAILED");
    }

    @Test
    void findByBatchStatusName_returnsMatch_whenSeeded() {
        BatchStatus status = batchStatusRepository.findByBatchStatusName("PROCESSED");

        assertThat(status).isNotNull();
        assertThat(status.getBatchStatusId()).isEqualTo(BatchStatus.Values.PROCESSED.getBatchStatusId());
    }

    @Test
    void findByBatchStatusName_returnsNull_whenMissing() {
        BatchStatus status = batchStatusRepository.findByBatchStatusName("DOES_NOT_EXIST");

        assertThat(status).isNull();
    }
}
