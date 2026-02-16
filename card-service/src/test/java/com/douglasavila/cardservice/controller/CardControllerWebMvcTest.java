package com.douglasavila.cardservice.controller;

import com.douglasavila.cardservice.entity.Card;
import com.douglasavila.cardservice.entity.CardStatus;
import com.douglasavila.cardservice.repository.CardRepository;
import com.douglasavila.cardservice.repository.CardStatusRepository;
import com.douglasavila.cardservice.service.CardBatchIngestService;
import com.douglasavila.cardservice.util.CardHasher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@AutoConfigureMockMvc(addFilters = false)
class CardControllerWebMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CardRepository cardRepository;

    @MockitoBean
    private CardStatusRepository cardStatusRepository;

    @MockitoBean
    private CardHasher hasher;

    @MockitoBean
    private CardBatchIngestService ingestService;

    private static final String VALID_PAN = "4111111111111111";
    private static final String INVALID_PAN = "123";

    @Test
    void createCard_returns400_whenPanIsInvalid() throws Exception {
        mvc.perform(post("/card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cardNumber":"%s"}
                                """.formatted(INVALID_PAN)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Card number is invalid"));

        verifyNoInteractions(hasher, cardRepository, cardStatusRepository);
    }

    @Test
    void createCard_returns200_andSaves_whenCardDoesNotExist() throws Exception {
        String hash = "hash-abc";

        when(hasher.hash(VALID_PAN)).thenReturn(hash);
        when(hasher.last4(VALID_PAN)).thenReturn("1111");
        when(cardRepository.findByCardHash(hash)).thenReturn(Optional.empty());
        when(cardStatusRepository.findByCardStatusName(CardStatus.Values.VALID.toString()))
                .thenReturn(mock(CardStatus.class));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(post("/card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cardNumber":"%s"}
                                """.formatted(VALID_PAN)))
                .andExpect(status().isOk())
                .andExpect(content().string("Card created with success or already exists."));

        verify(hasher).hash(VALID_PAN);
        verify(hasher).last4(VALID_PAN);
        verify(cardRepository).findByCardHash(hash);
        verify(cardStatusRepository).findByCardStatusName(CardStatus.Values.VALID.toString());
        verify(cardRepository).save(any(Card.class));
        verifyNoMoreInteractions(cardRepository, hasher, cardStatusRepository);
    }

    @Test
    void createCard_returns200_andDoesNotSave_whenCardAlreadyExists() throws Exception {
        String hash = "hash-existing";

        when(hasher.hash(VALID_PAN)).thenReturn(hash);
        when(cardRepository.findByCardHash(hash)).thenReturn(Optional.of(mock(Card.class)));

        mvc.perform(post("/card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cardNumber":"%s"}
                                """.formatted(VALID_PAN)))
                .andExpect(status().isOk())
                .andExpect(content().string("Card created with success or already exists."));

        verify(hasher).hash(VALID_PAN);
        verify(cardRepository).findByCardHash(hash);
        verify(hasher, never()).last4(anyString());
        verify(cardRepository, never()).save(any(Card.class));
        verifyNoInteractions(cardStatusRepository);
    }

    @Test
    void getCard_returns400_whenPanIsInvalid() throws Exception {
        mvc.perform(get("/card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cardNumber":"%s"}
                                """.formatted(INVALID_PAN)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Card number is invalid."));

        verifyNoInteractions(hasher, cardRepository);
    }

    @Test
    void getCard_returns404_whenCardDoesNotExist() throws Exception {
        String hash = "hash-notfound";

        when(hasher.hash(VALID_PAN)).thenReturn(hash);
        when(cardRepository.findByCardHash(hash)).thenReturn(Optional.empty());

        mvc.perform(get("/card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cardNumber":"%s"}
                                """.formatted(VALID_PAN)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Card with the provided number does not exist."));

        verify(hasher).hash(VALID_PAN);
        verify(cardRepository).findByCardHash(hash);
        verifyNoMoreInteractions(hasher, cardRepository);
    }

    @Test
    void getCard_returns200_withId_whenCardExists() throws Exception {
        String hash = "hash-found";
        UUID id = UUID.randomUUID();

        Card existing = mock(Card.class);
        when(existing.getId()).thenReturn(id);
        when(hasher.hash(VALID_PAN)).thenReturn(hash);
        when(cardRepository.findByCardHash(hash)).thenReturn(Optional.of(existing));

        mvc.perform(get("/card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cardNumber":"%s"}
                                """.formatted(VALID_PAN)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cardId").value(id.toString()));

        verify(hasher).hash(VALID_PAN);
        verify(cardRepository).findByCardHash(hash);
        verifyNoMoreInteractions(hasher, cardRepository);
    }

    @Test
    void createCardsBatch_returns400_whenFileIsEmpty() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "cards.txt",
                MediaType.TEXT_PLAIN_VALUE,
                new byte[0]
        );

        mvc.perform(multipart("/cards").file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Cards file is null or empty"));

        verifyNoInteractions(ingestService);
    }

    @Test
    void createCardsBatch_returns200_andCallsIngest_whenFileHasContent() throws Exception {
        byte[] content = """
                DESAFIO-HYPERATIVA           20180524LOTE0001000010
                C1     4111111111111111
                """.getBytes(StandardCharsets.UTF_8);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cards.txt",
                MediaType.TEXT_PLAIN_VALUE,
                content
        );

        mvc.perform(multipart("/cards").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Batch file processed with success."));

        verify(ingestService).ingest(any(), eq(StandardCharsets.UTF_8));
        verifyNoMoreInteractions(ingestService);
    }
}
