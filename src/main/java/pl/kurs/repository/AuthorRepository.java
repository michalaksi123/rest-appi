package pl.kurs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.kurs.model.Author;
import pl.kurs.model.Book;

import java.util.List;
import java.util.Map;

public interface AuthorRepository extends JpaRepository<Author, Integer> {
    @Query("select a from Author a left join fetch a.books")
    List<Author> findAllWithBooks();
}
