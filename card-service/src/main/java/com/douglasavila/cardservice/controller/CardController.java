package com.douglasavila.cardservice.controller;

import com.douglasavila.cardservice.controller.dto.*;
import com.douglasavila.cardservice.entity.Card;
import com.douglasavila.cardservice.entity.CardStatus;
import com.douglasavila.cardservice.repository.CardRepository;
import com.douglasavila.cardservice.repository.CardStatusRepository;
import com.douglasavila.cardservice.service.CardBatchIngestService;
import com.douglasavila.cardservice.util.CardHasher;
import com.douglasavila.cardservice.util.CardNumberValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@RestController
public class CardController {

    private final CardRepository cardRepository;
    private final CardStatusRepository cardStatusRepository;
    private final CardHasher hasher;
    private final CardBatchIngestService ingestService;

    public CardController(CardRepository cardRepository, CardStatusRepository cardStatusRepository, CardHasher hasher, CardBatchIngestService ingestService) {
        this.cardRepository = cardRepository;
        this.cardStatusRepository = cardStatusRepository;
        this.hasher = hasher;
        this.ingestService = ingestService;
    }

    @PostMapping("/card")
    public ResponseEntity<?> createCard(@RequestBody CreateCardRequest dto) {

        var cardNumber = dto.cardNumber();

        if (!CardNumberValidator.isValidPan(cardNumber)) {
            return badRequestResponse("Card number is invalid");
        }

        String cardHash = hasher.hash(cardNumber);
        cardRepository.findByCardHash(cardHash)
                .orElseGet(() -> {
                    Card newCard = new Card(
                            cardHash,
                            hasher.last4(cardNumber),
                            cardStatusRepository.findByCardStatusName(CardStatus.Values.VALID.toString()));
                    return cardRepository.save(newCard);
                });

        return ResponseEntity.ok("Card created with success or already exists.");
    }

    @PostMapping(
            value = "/cards",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> createCardsBatch(@RequestPart("file") MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            return badRequestResponse("Cards file is null or empty");
        }

        try (InputStream in = file.getInputStream()) {
            ingestService.ingest(in, StandardCharsets.UTF_8);
        }

        return ResponseEntity.ok("Batch file processed with success.");
    }

    @GetMapping("/card")
    public ResponseEntity<?> getCard(@RequestBody GetCardRequest dto) {
        var cardNumber = dto.cardNumber();

        if (!CardNumberValidator.isValidPan(cardNumber)) {
            return badRequestResponse("Card number is invalid.");
        }

        String cardHash = hasher.hash(cardNumber);
        Optional<Card> card = cardRepository.findByCardHash(cardHash);

        if (card.isEmpty()) {
            var body = new ApiErrorResponse(
                     404,
                    "Not Found",
                    "Card with the provided number does not exist."
            );

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }

        return ResponseEntity.ok().body(new GetCardResponse(card.get().getId().toString()));
    }

    private static ResponseEntity<ApiErrorResponse> badRequestResponse(String message) {
        var body = new ApiErrorResponse(
                400,
                "Bad Request",
                message
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
