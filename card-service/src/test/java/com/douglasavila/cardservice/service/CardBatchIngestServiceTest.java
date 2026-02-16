package com.douglasavila.cardservice.service;

import com.douglasavila.cardservice.entity.*;
import com.douglasavila.cardservice.repository.*;
import com.douglasavila.cardservice.util.BatchParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardBatchIngestServiceTest {

    @Mock private CardRepository cardRepository;
    @Mock private CardStatusRepository cardStatusRepository;
    @Mock private BatchRepository batchRepository;
    @Mock private BatchStatusRepository batchStatusRepository;
    @Mock private BatchParser batchParser;

    @InjectMocks private CardBatchIngestService service;

    @Captor private ArgumentCaptor<Batch> batchCaptor;
    @Captor private ArgumentCaptor<Card> cardCaptor;

    private static final String HEADER =
            "DESAFIO-HYPERATIVA           20180524LOTE0001000010";

    private static final String TRAILER =
            "LOTE0001000010                                        ";

    @Test
    void ingest_returnsEarly_whenExistingBatchIsNotFailedOrPartiallyProcessed() throws Exception {
        Batch newBatch = new Batch("DESAFIO-HYPERATIVA", LocalDate.of(2018, 5, 24), "LOTE0001", 10);

        BatchStatus received = status("RECEIVED");
        BatchStatus processed = status("PROCESSED");
        BatchStatus partially = status("PARTIALLY_PROCESSED");
        BatchStatus failed = status("FAILED");

        Batch existing = new Batch("DESAFIO-HYPERATIVA", LocalDate.of(2018, 5, 24), "LOTE0001", 10);
        existing.setBatchId(UUID.randomUUID());
        existing.setBatchStatus(status("PROCESSED"));

        when(batchParser.parseHeaderLine(HEADER)).thenReturn(newBatch);

        when(batchStatusRepository.findByBatchStatusName(BatchStatus.Values.RECEIVED.name())).thenReturn(received);
        when(batchStatusRepository.findByBatchStatusName(BatchStatus.Values.PROCESSED.name())).thenReturn(processed);
        when(batchStatusRepository.findByBatchStatusName(BatchStatus.Values.PARTIALLY_PROCESSED.name())).thenReturn(partially);
        when(batchStatusRepository.findByBatchStatusName(BatchStatus.Values.FAILED.name())).thenReturn(failed);

        when(cardStatusRepository.findByCardStatusName(CardStatus.Values.VALID.name())).thenReturn(cardStatus("VALID"));

        when(batchRepository.findByBatchCodeAndFileName("LOTE0001", "DESAFIO-HYPERATIVA"))
                .thenReturn(Optional.of(existing));

        ByteArrayInputStream in = new ByteArrayInputStream(("""
                %s
                C1     4456897922969999
                %s
                """.formatted(HEADER, TRAILER)).getBytes(StandardCharsets.UTF_8));

        service.ingest(in, StandardCharsets.UTF_8);

        verify(batchRepository, never()).save(any(Batch.class));
        verify(cardRepository, never()).save(any(Card.class));
        verify(cardRepository, never()).findByCardHash(anyString());
        verify(batchParser, never()).parseCardLines(anyString());
    }

    @Test
    void ingest_reprocessesExistingBatch_whenExistingIsFailed_andPersistsCards_andSetsProcessed() throws Exception {
        Batch newBatch = new Batch("DESAFIO-HYPERATIVA", LocalDate.of(2018, 5, 24), "LOTE0001", 2);

        BatchStatus received = status("RECEIVED");
        BatchStatus processed = status("PROCESSED");
        BatchStatus partially = status("PARTIALLY_PROCESSED");
        BatchStatus failed = status("FAILED");

        CardStatus valid = cardStatus("VALID");

        UUID existingId = UUID.randomUUID();
        Batch existing = new Batch("DESAFIO-HYPERATIVA", LocalDate.of(2018, 5, 24), "LOTE0001", 2);
        existing.setBatchId(existingId);
        existing.setBatchStatus(failed);

        when(batchParser.parseHeaderLine(HEADER)).thenReturn(newBatch);

        when(batchStatusRepository.findByBatchStatusName(BatchStatus.Values.RECEIVED.name())).thenReturn(received);
        when(batchStatusRepository.findByBatchStatusName(BatchStatus.Values.PROCESSED.name())).thenReturn(processed);
        when(batchStatusRepository.findByBatchStatusName(BatchStatus.Values.PARTIALLY_PROCESSED.name())).thenReturn(partially);
        when(batchStatusRepository.findByBatchStatusName(BatchStatus.Values.FAILED.name())).thenReturn(failed);

        when(cardStatusRepository.findByCardStatusName(CardStatus.Values.VALID.name())).thenReturn(valid);

        when(batchRepository.findByBatchCodeAndFileName("LOTE0001", "DESAFIO-HYPERATIVA"))
                .thenReturn(Optional.of(existing));

        Card card1 = card("hash-1", "9999");
        Card card2 = card("hash-2", "6999");

        when(batchParser.parseCardLines("C1     4456897922969999")).thenReturn(Optional.of(card1));
        when(batchParser.parseCardLines("C2     4456897999999999")).thenReturn(Optional.of(card2));

        when(cardRepository.findByCardHash("hash-1")).thenReturn(Optional.empty());
        when(cardRepository.findByCardHash("hash-2")).thenReturn(Optional.empty());

        ByteArrayInputStream in = new ByteArrayInputStream(("""
                %s
                C1     4456897922969999
                C2     4456897999999999
                %s
                """.formatted(HEADER, TRAILER)).getBytes(StandardCharsets.UTF_8));

        service.ingest(in, StandardCharsets.UTF_8);

        verify(batchRepository, times(2)).save(batchCaptor.capture());
        Batch savedBatch = batchCaptor.getValue();

        assertThat(savedBatch.getBatchId()).isEqualTo(existingId);
        assertThat(savedBatch.getProcessedRecords()).isEqualTo(2);
        assertThat(savedBatch.getProcessingDate()).isNotNull();
        assertThat(savedBatch.getBatchStatus()).isNotNull();
        assertThat(savedBatch.getBatchStatus().getBatchStatusName()).isEqualTo("PROCESSED");

        verify(cardRepository, times(2)).save(cardCaptor.capture());
        assertThat(cardCaptor.getAllValues()).allSatisfy(c -> {
            assertThat(c.getBatch()).isSameAs(newBatch);
            assertThat(c.getCardStatus()).isSameAs(valid);
        });
    }

    @Test
    void ingest_setsPartiallyProcessed_whenExpectedRecordsDoesNotMatchProcessed() throws Exception {
        Batch newBatch = new Batch("DESAFIO-HYPERATIVA", LocalDate.of(2018, 5, 24), "LOTE0001", 3);

        BatchStatus received = status("RECEIVED");
        BatchStatus processed = status("PROCESSED");
        BatchStatus partially = status("PARTIALLY_PROCESSED");
        BatchStatus failed = status("FAILED");

        when(batchParser.parseHeaderLine(HEADER)).thenReturn(newBatch);

        when(batchStatusRepository.findByBatchStatusName(BatchStatus.Values.RECEIVED.name())).thenReturn(received);
        when(batchStatusRepository.findByBatchStatusName(BatchStatus.Values.PROCESSED.name())).thenReturn(processed);
        when(batchStatusRepository.findByBatchStatusName(BatchStatus.Values.PARTIALLY_PROCESSED.name())).thenReturn(partially);
        when(batchStatusRepository.findByBatchStatusName(BatchStatus.Values.FAILED.name())).thenReturn(failed);

        when(cardStatusRepository.findByCardStatusName(CardStatus.Values.VALID.name())).thenReturn(cardStatus("VALID"));

        when(batchRepository.findByBatchCodeAndFileName("LOTE0001", "DESAFIO-HYPERATIVA"))
                .thenReturn(Optional.empty());

        Card card1 = card("hash-1", "9999");
        Card card2 = card("hash-2", "6999");

        when(batchParser.parseCardLines("C1     4456897922969999")).thenReturn(Optional.of(card1));
        when(batchParser.parseCardLines("C2     4456897999999999")).thenReturn(Optional.of(card2));

        when(cardRepository.findByCardHash("hash-1")).thenReturn(Optional.empty());
        when(cardRepository.findByCardHash("hash-2")).thenReturn(Optional.empty());

        ByteArrayInputStream in = new ByteArrayInputStream(("""
                %s
                C1     4456897922969999
                C2     4456897999999999
                %s
                """.formatted(HEADER, TRAILER)).getBytes(StandardCharsets.UTF_8));

        service.ingest(in, StandardCharsets.UTF_8);

        verify(batchRepository, times(2)).save(batchCaptor.capture());
        Batch savedBatch = batchCaptor.getValue();

        assertThat(savedBatch.getProcessedRecords()).isEqualTo(2);
        assertThat(savedBatch.getBatchStatus()).isNotNull();
        assertThat(savedBatch.getBatchStatus().getBatchStatusName()).isEqualTo("PARTIALLY_PROCESSED");
    }

    @Test
    void ingest_doesNotSaveCard_whenAlreadyExists_butCountsRecord() throws Exception {
        Batch newBatch = new Batch("DESAFIO-HYPERATIVA", LocalDate.of(2018, 5, 24), "LOTE0001", 2);

        BatchStatus received = status("RECEIVED");
        BatchStatus processed = status("PROCESSED");
        BatchStatus partially = status("PARTIALLY_PROCESSED");
        BatchStatus failed = status("FAILED");

        CardStatus valid = cardStatus("VALID");

        when(batchParser.parseHeaderLine(HEADER)).thenReturn(newBatch);

        when(batchStatusRepository.findByBatchStatusName(BatchStatus.Values.RECEIVED.name())).thenReturn(received);
        when(batchStatusRepository.findByBatchStatusName(BatchStatus.Values.PROCESSED.name())).thenReturn(processed);
        when(batchStatusRepository.findByBatchStatusName(BatchStatus.Values.PARTIALLY_PROCESSED.name())).thenReturn(partially);
        when(batchStatusRepository.findByBatchStatusName(BatchStatus.Values.FAILED.name())).thenReturn(failed);

        when(cardStatusRepository.findByCardStatusName(CardStatus.Values.VALID.name())).thenReturn(valid);

        when(batchRepository.findByBatchCodeAndFileName("LOTE0001", "DESAFIO-HYPERATIVA"))
                .thenReturn(Optional.empty());

        Card card1 = card("hash-existing", "9999");
        Card card2 = card("hash-new", "6999");

        when(batchParser.parseCardLines("C1     4456897922969999")).thenReturn(Optional.of(card1));
        when(batchParser.parseCardLines("C2     4456897999999999")).thenReturn(Optional.of(card2));

        when(cardRepository.findByCardHash("hash-existing")).thenReturn(Optional.of(new Card()));
        when(cardRepository.findByCardHash("hash-new")).thenReturn(Optional.empty());

        ByteArrayInputStream in = new ByteArrayInputStream(("""
                %s
                C1     4456897922969999
                C2     4456897999999999
                %s
                """.formatted(HEADER, TRAILER)).getBytes(StandardCharsets.UTF_8));

        service.ingest(in, StandardCharsets.UTF_8);

        verify(cardRepository, times(1)).save(cardCaptor.capture());
        assertThat(cardCaptor.getValue().getCardHash()).isEqualTo("hash-new");

        verify(batchRepository, times(2)).save(batchCaptor.capture());
        Batch savedBatch = batchCaptor.getValue();

        assertThat(savedBatch.getProcessedRecords()).isEqualTo(2);
        assertThat(savedBatch.getBatchStatus()).isNotNull();
        assertThat(savedBatch.getBatchStatus().getBatchStatusName()).isEqualTo("PROCESSED");
    }

    @Test
    void ingest_skipsBlankLines_stopsAtTrailer_andIgnoresEmptyOptional() throws Exception {
        Batch newBatch = new Batch("DESAFIO-HYPERATIVA", LocalDate.of(2018, 5, 24), "LOTE0001", 1);

        BatchStatus received = status("RECEIVED");
        BatchStatus processed = status("PROCESSED");
        BatchStatus partially = status("PARTIALLY_PROCESSED");
        BatchStatus failed = status("FAILED");

        CardStatus valid = cardStatus("VALID");

        when(batchParser.parseHeaderLine(HEADER)).thenReturn(newBatch);

        when(batchStatusRepository.findByBatchStatusName(BatchStatus.Values.RECEIVED.name())).thenReturn(received);
        when(batchStatusRepository.findByBatchStatusName(BatchStatus.Values.PROCESSED.name())).thenReturn(processed);
        when(batchStatusRepository.findByBatchStatusName(BatchStatus.Values.PARTIALLY_PROCESSED.name())).thenReturn(partially);
        when(batchStatusRepository.findByBatchStatusName(BatchStatus.Values.FAILED.name())).thenReturn(failed);

        when(cardStatusRepository.findByCardStatusName(CardStatus.Values.VALID.name())).thenReturn(valid);

        when(batchRepository.findByBatchCodeAndFileName("LOTE0001", "DESAFIO-HYPERATIVA"))
                .thenReturn(Optional.empty());

        when(batchParser.parseCardLines("C0     INVALID")).thenReturn(Optional.empty());

        Card card1 = card("hash-1", "9999");
        when(batchParser.parseCardLines("C1     4456897922969999")).thenReturn(Optional.of(card1));
        when(cardRepository.findByCardHash("hash-1")).thenReturn(Optional.empty());

        ByteArrayInputStream in = new ByteArrayInputStream(("""
                %s

                C0     INVALID
                C1     4456897922969999
                %s
                C9     4456897999099999
                """.formatted(HEADER, TRAILER)).getBytes(StandardCharsets.UTF_8));

        service.ingest(in, StandardCharsets.UTF_8);

        verify(cardRepository, times(1)).save(any(Card.class));
        verify(batchRepository, times(2)).save(batchCaptor.capture());

        Batch savedBatch = batchCaptor.getValue();
        assertThat(savedBatch.getProcessedRecords()).isEqualTo(1);
        assertThat(savedBatch.getBatchStatus()).isNotNull();
        assertThat(savedBatch.getBatchStatus().getBatchStatusName()).isEqualTo("PROCESSED");

        verify(batchParser, never()).parseCardLines("C9     4456897999099999");
    }

    private static BatchStatus status(String name) {
        BatchStatus s = new BatchStatus();
        s.setBatchStatusName(name);
        return s;
    }

    private static CardStatus cardStatus(String name) {
        CardStatus s = new CardStatus();
        s.setCardStatusName(name);
        return s;
    }

    private static Card card(String hash, String last4) {
        Card c = new Card();
        c.setCardHash(hash);
        c.setCardLast4(last4);
        return c;
    }
}
