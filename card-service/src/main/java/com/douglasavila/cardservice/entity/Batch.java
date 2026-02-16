package com.douglasavila.cardservice.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "batches",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_batches_batch_code_file_name",
                        columnNames = {"batchCode", "fileName"}
                )
        }
)
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="batch_id")
    private UUID batchId;

    @Column(nullable = false)
    private String fileName;
    private LocalDate fileDate;

    @Column(nullable = false)
    private String batchCode;
    private Integer expectedRecords;
    private Integer processedRecords;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "batch_status_id")
    private BatchStatus batchStatus;

    private Instant processingDate;

    public Batch(String fileName, LocalDate fileDate, String batchCode, Integer expectedRecords) {
        this.fileName = fileName;
        this.fileDate = fileDate;
        this.batchCode = batchCode;
        this.expectedRecords = expectedRecords;
    }

    public Batch() {
        this.fileName = "";
        this.fileDate = LocalDate.now();
        this.batchCode = "";
        this.expectedRecords = 0;
    }

    public UUID getBatchId() {
        return batchId;
    }

    public void setBatchId(UUID batchId) {
        this.batchId = batchId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public LocalDate getFileDate() {
        return fileDate;
    }

    public void setFileDate(LocalDate fileDate) {
        this.fileDate = fileDate;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public void setBatchCode(String batchCode) {
        this.batchCode = batchCode;
    }

    public Integer getExpectedRecords() {
        return expectedRecords;
    }

    public void setExpectedRecords(Integer expectedRecords) {
        this.expectedRecords = expectedRecords;
    }

    public Integer getProcessedRecords() {
        return processedRecords;
    }

    public void setProcessedRecords(Integer processedRecords) {
        this.processedRecords = processedRecords;
    }

    public BatchStatus getBatchStatus() {
        return batchStatus;
    }

    public void setBatchStatus(BatchStatus batchStatus) {
        this.batchStatus = batchStatus;
    }

    public Instant getProcessingDate() {
        return processingDate;
    }

    public void setProcessingDate(Instant processingDate) {
        this.processingDate = processingDate;
    }
}
