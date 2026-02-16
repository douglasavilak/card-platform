package com.douglasavila.cardservice.repository;

import com.douglasavila.cardservice.entity.CardStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CardStatusJpaMappingTest {

    @Autowired
    private CardStatusRepository cardStatusRepository;

    @Test
    void seededStatuses_exist_byId() {
        CardStatus valid = cardStatusRepository.findById(CardStatus.Values.VALID.getCardStatusId()).orElseThrow();
        CardStatus invalid = cardStatusRepository.findById(CardStatus.Values.INVALID.getCardStatusId()).orElseThrow();
        CardStatus expired = cardStatusRepository.findById(CardStatus.Values.EXPIRED.getCardStatusId()).orElseThrow();

        assertThat(valid.getCardStatusName()).isEqualTo("VALID");
        assertThat(invalid.getCardStatusName()).isEqualTo("INVALID");
        assertThat(expired.getCardStatusName()).isEqualTo("EXPIRED");
    }

    @Test
    void findByCardStatusName_returnsMatch_whenSeeded() {
        CardStatus status = cardStatusRepository.findByCardStatusName("VALID");

        assertThat(status).isNotNull();
        assertThat(status.getCardStatusId()).isEqualTo(CardStatus.Values.VALID.getCardStatusId());
    }

    @Test
    void findByCardStatusName_returnsNull_whenMissing() {
        CardStatus status = cardStatusRepository.findByCardStatusName("DOES_NOT_EXIST");

        assertThat(status).isNull();
    }
}
