package com.douglasavila.cardservice.util;

import com.douglasavila.cardservice.entity.Batch;
import com.douglasavila.cardservice.entity.Card;
import com.douglasavila.cardservice.entity.CardStatus;
import com.douglasavila.cardservice.repository.CardRepository;
import com.douglasavila.cardservice.repository.CardStatusRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class BatchParser {

    private final CardHasher hasher;
    private final CardStatusRepository cardStatusRepository;

    public BatchParser(CardHasher hasher, CardStatusRepository cardStatusRepository) {
        this.hasher = hasher;
        this.cardStatusRepository = cardStatusRepository;
    }

    private final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    public Batch parseHeaderLine(String line) {

        if (line == null || line.length() < 51) {
            throw new IllegalArgumentException("Invalid header line.");
        }

        String fileName = line.substring(0, 29).trim();
        String dateStr = line.substring(29, 37);
        String batchCode = line.substring(37, 45).trim();
        String recordCountStr = line.substring(45, 51).trim();
        int recordCount = Integer.parseInt(recordCountStr);

        LocalDate fileDate = LocalDate.parse(dateStr, DATE_FORMAT);

        return new Batch(
                fileName,
                fileDate,
                batchCode,
                recordCount
        );
    }

    public Optional<Card> parseCardLines(String line) {

        if (line == null || line.length() < 51) {
            System.out.println("Invalid card line.");
            return Optional.empty();
        }

        String batchSequenceNumber = line.substring(1, 7).trim();
        String cardNumber = line.substring(7, 26).trim();

        if (!CardNumberValidator.isValidPan(cardNumber)) {
            System.out.println("Invalid card number.");
            return Optional.empty();
        }

        String cardHash = hasher.hash(cardNumber);

        Card card = new Card(
                cardHash,
                hasher.last4(cardNumber),
                cardStatusRepository.findByCardStatusName(CardStatus.Values.VALID.toString()));
        card.setBatchSequenceNumber(batchSequenceNumber);

        return Optional.of(card);
    }
}
