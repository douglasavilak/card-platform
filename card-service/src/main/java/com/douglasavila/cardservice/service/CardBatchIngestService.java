package com.douglasavila.cardservice.service;

import com.douglasavila.cardservice.entity.Batch;
import com.douglasavila.cardservice.entity.BatchStatus;
import com.douglasavila.cardservice.entity.Card;
import com.douglasavila.cardservice.entity.CardStatus;
import com.douglasavila.cardservice.repository.BatchRepository;
import com.douglasavila.cardservice.repository.BatchStatusRepository;
import com.douglasavila.cardservice.repository.CardRepository;
import com.douglasavila.cardservice.repository.CardStatusRepository;
import com.douglasavila.cardservice.util.BatchParser;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Optional;

@Service
public class CardBatchIngestService {

    private final CardRepository cardRepository;
    private final CardStatusRepository cardStatusRepository;
    private final BatchRepository batchRepository;
    private final BatchStatusRepository batchStatusRepository;
    private final BatchParser batchParser;

    public CardBatchIngestService(
            CardRepository cardRepository,
            CardStatusRepository cardStatusRepository,
            BatchRepository batchRepository,
            BatchStatusRepository batchStatusRepository, BatchParser batchParser) {
        this.cardRepository = cardRepository;
        this.cardStatusRepository = cardStatusRepository;
        this.batchRepository = batchRepository;
        this.batchStatusRepository = batchStatusRepository;
        this.batchParser = batchParser;
    }

    @Transactional
    public void ingest(InputStream in, Charset charset) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, charset), 64 * 1024)) {

            String line = br.readLine();
            Batch newBatch = batchParser.parseHeaderLine(line);

            BatchStatus batchReceivedStatus = batchStatusRepository.
                    findByBatchStatusName(BatchStatus.Values.RECEIVED.name());
            BatchStatus batchProcessedStatus = batchStatusRepository.
                    findByBatchStatusName(BatchStatus.Values.PROCESSED.name());
            BatchStatus batchPartiallyProcessedStatus = batchStatusRepository.
                    findByBatchStatusName(BatchStatus.Values.PARTIALLY_PROCESSED.name());
            BatchStatus batchFailedStatus = batchStatusRepository.
                    findByBatchStatusName(BatchStatus.Values.FAILED.name());

            CardStatus cardValidStatus = cardStatusRepository.
                    findByCardStatusName(CardStatus.Values.VALID.name());

            var existingBatch = batchRepository.findByBatchCodeAndFileName(
                    newBatch.getBatchCode(), newBatch.getFileName());

            if(existingBatch.isPresent()) {
                if (!batchFailedStatus.equals(existingBatch.get().getBatchStatus())
                && !batchPartiallyProcessedStatus.equals(existingBatch.get().getBatchStatus())) {
                    return;
                }

                newBatch.setBatchId(existingBatch.get().getBatchId());
            }

            // Finish creating initial batch
            newBatch.setBatchStatus(batchReceivedStatus);
            batchRepository.save(newBatch);


            // Start processing cards
            int processedRecordsCount = 0;

            // Create cards
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;

                // Trailer line(Cards list end)
                if (line.substring(0, 8).equals(newBatch.getBatchCode())) break;

                Optional<Card> optionalCard = batchParser.parseCardLines(line);

                if (optionalCard.isPresent()) {
                    Card card = optionalCard.get();

                    if (cardRepository.findByCardHash(card.getCardHash()).isEmpty()) {
                        card.setBatch(newBatch);
                        card.setCardStatus(cardValidStatus);

                        cardRepository.save(card);
                    }

                    processedRecordsCount++;
                }
            }

            // Done processing
            newBatch.setProcessingDate(Instant.now());
            newBatch.setProcessedRecords(processedRecordsCount);
            newBatch.setBatchStatus(batchProcessedStatus);

            if (newBatch.getExpectedRecords() != processedRecordsCount) {
                newBatch.setBatchStatus(batchPartiallyProcessedStatus);
            }

            batchRepository.save(newBatch);
        }
    }
}
