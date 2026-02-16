package com.douglasavila.cardservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name="card_status")
public class CardStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="card_status_id")
    private long cardStatusId;
    private String cardStatusName;

    public long getCardStatusId() {
        return cardStatusId;
    }

    public void setCardStatusId(long cardStatusId) {
        this.cardStatusId = cardStatusId;
    }

    public String getCardStatusName() {
        return cardStatusName;
    }

    public void setCardStatusName(String cardStatusName) {
        this.cardStatusName = cardStatusName;
    }

    public enum Values {
        VALID(1),
        INVALID(2),
        EXPIRED(3);

        private final long cardStatusId;

        Values(long cardStatusId) { this.cardStatusId = cardStatusId; }

        public long getCardStatusId() { return cardStatusId; }
    }
}
