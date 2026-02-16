package com.douglasavila.cardservice.util;

import com.douglasavila.cardservice.entity.Batch;
import com.douglasavila.cardservice.entity.Card;
import com.douglasavila.cardservice.entity.CardStatus;
import com.douglasavila.cardservice.repository.CardStatusRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchParserTest {

    @Mock private CardHasher hasher;
    @Mock private CardStatusRepository cardStatusRepository;

    @Test
    void parseHeaderLine_parsesFieldsCorrectly() {
        BatchParser parser = new BatchParser(hasher, cardStatusRepository);

        String header = "DESAFIO-HYPERATIVA           20180524LOTE0001000010";

        Batch batch = parser.parseHeaderLine(header);

        assertThat(batch.getFileName()).isEqualTo("DESAFIO-HYPERATIVA");
        assertThat(batch.getFileDate()).isEqualTo(LocalDate.of(2018, 5, 24));
        assertThat(batch.getBatchCode()).isEqualTo("LOTE0001");
        assertThat(batch.getExpectedRecords()).isEqualTo(10);
    }

    @Test
    void parseHeaderLine_throws_whenNull() {
        BatchParser parser = new BatchParser(hasher, cardStatusRepository);

        assertThatThrownBy(() -> parser.parseHeaderLine(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid header line.");
    }

    @Test
    void parseHeaderLine_throws_whenTooShort() {
        BatchParser parser = new BatchParser(hasher, cardStatusRepository);

        assertThatThrownBy(() -> parser.parseHeaderLine("SHORT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid header line.");
    }

    @Test
    void parseHeaderLine_throws_whenRecordCountIsNotNumeric() {
        BatchParser parser = new BatchParser(hasher, cardStatusRepository);

        String header = "DESAFIO-HYPERATIVA           20180524LOTE0001ABCDEF";

        assertThatThrownBy(() -> parser.parseHeaderLine(header))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    void parseCardLines_returnsEmpty_whenNull() {
        BatchParser parser = new BatchParser(hasher, cardStatusRepository);

        Optional<Card> result = parser.parseCardLines(null);

        assertThat(result).isEmpty();
        verifyNoInteractions(hasher, cardStatusRepository);
    }

    @Test
    void parseCardLines_returnsEmpty_whenTooShort() {
        BatchParser parser = new BatchParser(hasher, cardStatusRepository);

        Optional<Card> result = parser.parseCardLines("C1 4111111111111111");

        assertThat(result).isEmpty();
        verifyNoInteractions(hasher, cardStatusRepository);
    }

    @Test
    void parseCardLines_returnsEmpty_whenPanInvalid() {
        BatchParser parser = new BatchParser(hasher, cardStatusRepository);

        String line = "C1     123                                         ";

        Optional<Card> result = parser.parseCardLines(line);

        assertThat(result).isEmpty();
        verifyNoInteractions(hasher, cardStatusRepository);
    }

    @Test
    void parseCardLines_parsesAndBuildsCard_whenValid() {
        BatchParser parser = new BatchParser(hasher, cardStatusRepository);

        String pan = "4111111111111111";
        String hash = "hash-4111";
        String last4 = "1111";

        when(hasher.hash(pan)).thenReturn(hash);
        when(hasher.last4(pan)).thenReturn(last4);

        CardStatus validStatus = new CardStatus();
        validStatus.setCardStatusName("VALID");
        when(cardStatusRepository.findByCardStatusName(CardStatus.Values.VALID.toString()))
                .thenReturn(validStatus);

        String line = "C2     4111111111111111                               ";

        Optional<Card> result = parser.parseCardLines(line);

        assertThat(result).isPresent();
        Card card = result.orElseThrow();

        assertThat(card.getCardHash()).isEqualTo(hash);
        assertThat(card.getCardLast4()).isEqualTo(last4);
        assertThat(card.getBatchSequenceNumber()).isEqualTo("2");
        assertThat(card.getCardStatus()).isNotNull();
        assertThat(card.getCardStatus().getCardStatusName()).isEqualTo("VALID");

        verify(hasher).hash(pan);
        verify(hasher).last4(pan);
        verify(cardStatusRepository).findByCardStatusName(CardStatus.Values.VALID.toString());
        verifyNoMoreInteractions(hasher, cardStatusRepository);
    }

    @Test
    void parseCardLines_acceptsNonNumericSequence_andStillParsesCard_whenPanValid() {
        BatchParser parser = new BatchParser(hasher, cardStatusRepository);

        String pan = "4111111111111111";
        when(hasher.hash(pan)).thenReturn("hash");
        when(hasher.last4(pan)).thenReturn("1111");

        CardStatus validStatus = new CardStatus();
        validStatus.setCardStatusName("VALID");
        when(cardStatusRepository.findByCardStatusName(CardStatus.Values.VALID.toString()))
                .thenReturn(validStatus);

        String line = "CABCDEF4111111111111111                               ";

        Optional<Card> result = parser.parseCardLines(line);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getBatchSequenceNumber()).isEqualTo("ABCDEF");
    }
}
