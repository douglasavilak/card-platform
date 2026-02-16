package com.douglasavila.cardservice.repository;

import com.douglasavila.cardservice.entity.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CardJpaMappingTest {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private EntityManager em;

    @Test
    void save_persistsCard_andGeneratesUuid_andSetsCreatedDate() {
        CardStatus status = em.find(CardStatus.class, 1L);

        Card card = new Card("hash-1", "1234", status);

        Card saved = cardRepository.saveAndFlush(card);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedDate()).isNotNull();
        assertThat(saved.getCardHash()).isEqualTo("hash-1");
        assertThat(saved.getCardLast4()).isEqualTo("1234");
        assertThat(saved.getCardStatus()).isNotNull();
    }

    @Test
    void save_enforcesUniqueConstraint_onCardHash() {
        CardStatus status = em.find(CardStatus.class, 1L);

        cardRepository.saveAndFlush(new Card("hash-unique", "1111", status));

        assertThatThrownBy(() ->
                cardRepository.saveAndFlush(new Card("hash-unique", "2222", status))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_requiresNonNullFields() {
        CardStatus status = em.find(CardStatus.class, 1L);

        Card missingHash = new Card(null, "1234", status);
        assertThatThrownBy(() -> cardRepository.saveAndFlush(missingHash))
                .isInstanceOf(DataIntegrityViolationException.class);

        Card missingLast4 = new Card("hash-x", null, status);
        assertThatThrownBy(() -> cardRepository.saveAndFlush(missingLast4))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_allowsLinkingToBatch() {
        BatchStatus batchStatus = em.find(BatchStatus.class, 1L);
        Batch batch = new Batch("batch-file.txt", LocalDate.of(2026, 2, 15), "LOTE0001", 2);
        batch.setBatchStatus(batchStatus);
        em.persist(batch);
        em.flush();

        CardStatus cardStatus = em.find(CardStatus.class, 1L);

        Card card = new Card("hash-batch", "9999", cardStatus);
        card.setBatch(batch);
        card.setBatchSequenceNumber("1");

        Card saved = cardRepository.saveAndFlush(card);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getBatch()).isNotNull();
        assertThat(saved.getBatchSequenceNumber()).isEqualTo("1");
    }

    @Test
    void save_doesNotCreateNewCardStatus_whenUsingSeededStatuses() {
        long before = ((Number) em.createQuery("select count(cs) from CardStatus cs").getSingleResult()).longValue();

        CardStatus status = em.find(CardStatus.class, 1L);
        Card card = new Card("hash-status", "0000", status);
        cardRepository.saveAndFlush(card);

        long after = ((Number) em.createQuery("select count(cs) from CardStatus cs").getSingleResult()).longValue();

        assertThat(after).isEqualTo(before);
    }

    @Test
    void findByCardHash_returnsMatch_whenExists() {
        CardStatus status = em.find(CardStatus.class, 1L);
        Card saved = cardRepository.saveAndFlush(new Card("hash-find", "5555", status));

        Optional<Card> found = cardRepository.findByCardHash("hash-find");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    void findByCardHash_returnsEmpty_whenMissing() {
        Optional<Card> found = cardRepository.findByCardHash("does-not-exist");

        assertThat(found).isEmpty();
    }
}
