package com.douglasavila.cardservice.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "cards",
        indexes = {
            @Index(name = "idx_cards_batch_id", columnList = "batch_id")
        }
)
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "card_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="batch_id")
    private Batch batch;

    private String batchSequenceNumber;

    @Column(nullable = false, unique = true)
    private String cardHash;

    @Column(nullable = false)
    private String cardLast4;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "card_status_id")
    private CardStatus cardStatus;

    @CreationTimestamp
    private Instant createdDate;

    public Card(String cardHash, String cardLast4, CardStatus cardstatus) {
        this.cardHash = cardHash;
        this.cardLast4 = cardLast4;
        this.cardStatus = cardstatus;
    }

    public Card() {
        this.cardHash = "";
        this.cardLast4 = "";
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public String getBatchSequenceNumber() {
        return batchSequenceNumber;
    }

    public void setBatchSequenceNumber(String batchSequenceNumber) {
        this.batchSequenceNumber = batchSequenceNumber;
    }

    public String getCardHash() {
        return cardHash;
    }

    public void setCardHash(String cardHash) {
        this.cardHash = cardHash;
    }

    public String getCardLast4() {
        return cardLast4;
    }

    public void setCardLast4(String cardLast4) {
        this.cardLast4 = cardLast4;
    }

    public CardStatus getCardStatus() {
        return cardStatus;
    }

    public void setCardStatus(CardStatus status) {
        this.cardStatus = status;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }
}
