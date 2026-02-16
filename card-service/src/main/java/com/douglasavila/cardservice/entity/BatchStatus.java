package com.douglasavila.cardservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name="batch_status")
public class BatchStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="batch_status_id")
    private long batchStatusId;
    private String batchStatusName;

    public long getBatchStatusId() {
        return batchStatusId;
    }

    public void setBatchStatusId(long batchStatusId) {
        this.batchStatusId = batchStatusId;
    }

    public String getBatchStatusName() {
        return batchStatusName;
    }

    public void setBatchStatusName(String batchStatusName) {
        this.batchStatusName = batchStatusName;
    }

    public enum Values {
        RECEIVED(1L),
        PROCESSED(2L),
        PARTIALLY_PROCESSED(3L),
        FAILED(4L);

        private final long batchStatusId;

        Values(long batchStatusId) {
            this.batchStatusId = batchStatusId;
        }

        public long getBatchStatusId() {
            return batchStatusId;
        }
    }
}
