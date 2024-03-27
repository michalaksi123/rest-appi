package pl.kurs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.kurs.Main;
import pl.kurs.model.Author;
import pl.kurs.model.Book;

import pl.kurs.model.command.EditBookCommand;
import pl.kurs.repository.AuthorRepository;
import pl.kurs.repository.BookRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Main.class)
@AutoConfigureMockMvc
class BookControllerTest {

    @Autowired
    private MockMvc postman;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private AuthorRepository authorRepository;

    @Test
    public void shouldReturnSingleBook() throws Exception {
        Author author = authorRepository.findAllWithBooks().get(0);
        int id = bookRepository.saveAndFlush(new Book("Pan Tadeusz", "LEKTURA", true, author)).getId();
        postman.perform(get("/api/v1/books/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Pan Tadeusz"))
                .andExpect(jsonPath("$.category").value("LEKTURA"))
                .andExpect(jsonPath("$.authorId").value(author.getId()))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    public void shouldDeleteBook() throws Exception {
        Author author = authorRepository.findAll().get(0);
        int bookId = bookRepository.saveAndFlush(new Book("testowa", "TESTOWA", true, author)).getId();

        postman.perform(delete("/api/v1/books/" + bookId))
                .andExpect(status().isNoContent());

        postman.perform(get("/api/v1/books/" + bookId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldEditBook() throws Exception {
        Author author = authorRepository.findAll().get(0);
        int bookId = bookRepository.saveAndFlush(new Book("testowa", "Testowa", true, author)).getId();

        EditBookCommand command = new EditBookCommand("new", "NEW", false);
        String json = objectMapper.writeValueAsString(command);

        postman.perform(put("/api/v1/books/{id}", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId))
                .andExpect(jsonPath("$.title").value("new"))
                .andExpect(jsonPath("$.category").value("NEW"))
                .andExpect(jsonPath("$.available").value(false));

        Book editBook = bookRepository.findById(bookId).get();
        assertNotNull(editBook);
        assertEquals("new", editBook.getTitle());
        assertEquals("NEW", editBook.getCategory());
        assertFalse(editBook.isAvailable());
    }

    @Test
    public void shouldEditBookPartially() throws Exception {
        Author author = authorRepository.findAll().get(0);
        int bookId = bookRepository.saveAndFlush(new Book("Pan Tadeusz", "LEKTURA", true, author)).getId();

        EditBookCommand command = new EditBookCommand("NEW", null, false);
        String json = objectMapper.writeValueAsString(command);

        postman.perform(patch("/api/v1/books/" + bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        Book newBook = bookRepository.findById(bookId).get();

        assertEquals("NEW", newBook.getTitle());
        assertEquals("LEKTURA", newBook.getCategory());
        assertFalse(newBook.isAvailable());
    }

}