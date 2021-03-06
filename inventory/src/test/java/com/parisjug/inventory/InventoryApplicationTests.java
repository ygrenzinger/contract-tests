package com.parisjug.inventory;

import com.parisjug.inventory.domain.Book;
import com.parisjug.inventory.domain.BookIdGenerator;
import com.parisjug.inventory.provider.InMemoryBookInventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.math.BigDecimal;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureWebTestClient
class InventoryApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private InMemoryBookInventory bookInventory;

    @MockBean
    private BookIdGenerator bookIdGenerator;

    private Book java = new Book("d4d37e73-77a0-4616-8bd2-5ed983d45d14", "Java", BigDecimal.valueOf(100), 100);
    private Book kotlin = new Book("8364948b-6221-4cd8-9fd9-db0d17d45ef8", "Kotlin", BigDecimal.valueOf(120), 20);

    @BeforeEach
    void before() {
        Mockito.when(bookIdGenerator.randomId()).thenReturn("dc8493d6-e2e3-47da-a806-d1e8ff7cd4df");
        bookInventory.removeAllStocks();
        bookInventory.addBook(java);
        bookInventory.addBook(kotlin);
    }

    @Test
    void should_create_book_into_inventory() {
        Book newBook = Book.builder()
                .name("Kotlin")
                .price(BigDecimal.valueOf(120))
                .build();

        Book expected = Book.builder()
                .id("dc8493d6-e2e3-47da-a806-d1e8ff7cd4df")
                .name("Kotlin")
                .price(BigDecimal.valueOf(120))
                .stock(0)
                .build();

        webTestClient
                .post().uri("/v1/books")
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(newBook))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Book.class).isEqualTo(singletonList(expected));
    }

    @Test
    void should_retrieve_all_books_from_inventory() {
        webTestClient
                .get().uri("/v1/books")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Book.class).isEqualTo(asList(java, kotlin));
    }

    @Test
    void should_retrieve_book_from_inventory() {
        webTestClient
                .get().uri("/v1/books/d4d37e73-77a0-4616-8bd2-5ed983d45d14")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Book.class).isEqualTo(java);
    }

    @Test
    void should_reduce_stock_from_inventory() {
        webTestClient
                .post().uri("/v1/books/d4d37e73-77a0-4616-8bd2-5ed983d45d14/reduce-stock/2")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Integer.class).isEqualTo(98);
    }

}

