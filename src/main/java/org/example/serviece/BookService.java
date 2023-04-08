package org.example.serviece;

import org.example.entity.Book;
import org.example.exception.BookNotFoundException;
import org.example.exception.IncorrectParametersException;
import org.example.mapper.BookMapper;
import org.example.model.BookDto;
import org.example.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final PeopleService peopleService;
    private final BookRepository repository;
    private final BookMapper bookMapper;

    @Autowired
    public BookService(PeopleService peopleService, final BookRepository repository, final BookMapper bookMapper) {
        this.peopleService = peopleService;
        this.repository = repository;
        this.bookMapper = bookMapper;
    }

    public List<BookDto> findAll(final Integer page, final int booksPerPage, final boolean isSortByYear) throws IncorrectParametersException {
        List<Book> books;
        if (page == 0 && booksPerPage == 0) {
            books = findAll(isSortByYear);
        } else if (isSortByYear) {
            books = repository
                    .findAll(PageRequest.of(page, booksPerPage, Sort.by("yearOfPublication", "bookName")))
                    .getContent();
        } else {
            books = repository
                    .findAll(PageRequest.of(page, booksPerPage))
                    .getContent();
        }
        return bookMapper.toDtoLists(books);
    }

    private List<Book> findAll(final boolean isSortByYear) {
        if (isSortByYear) {
            return repository.findAll(Sort.by("yearOfPublication", "bookName"));
        }
        return repository.findAll();
    }

    public List<BookDto> findByBookNameStartsWith(final String startsWith) {
        if (startsWith.equals("")) {
            throw new BookNotFoundException(String.format("Book starts with %s not found", startsWith));
        } else {
            return bookMapper.toDtoLists(repository.findByBookNameStartsWith(startsWith));
        }
    }

    public BookDto findById(final int id) {
        Optional<Book> foundBook = repository.findById(id);
        return bookMapper.toDto(foundBook.orElseThrow(() -> new BookNotFoundException(id)));
    }

    @Transactional
    public int save(final BookDto book) {
        Book entity = bookMapper.toEntity(book);
        repository.save(entity);
        // по сути id у entity == 0, однако getId() вернет id, так как запрос его в рамках транзакции
        return entity.getId();
    }

    @Transactional
    public BookDto update(final int id, final BookDto updatedBook) {
        Book bookToBeUpdated = bookMapper.toEntity(updatedBook);

        bookToBeUpdated.setId(id);
        bookToBeUpdated.setOwner(bookToBeUpdated.getOwner()); // чтобы не терялась связь при обновлении

        return bookMapper.toDto(repository.save(bookToBeUpdated));
    }

    @Transactional
    public void delete(final int id) {
        repository.deleteById(id);
    }

    @Transactional
    public BookDto assign(final int bookId, final int personId) {
        BookDto dto = findById(bookId);

        dto.setOwner(peopleService.findById(personId));
        dto.setTakenAt(new Date());

        Book entity = bookMapper.toEntity(dto);
        entity.setId(bookId);
        repository.save(entity);

        return dto;
    }

    @Transactional
    public BookDto returnBook(final int id) {
        Book entity = repository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
        entity.setOwner(null);
        entity.setTakenAt(null);
        
        BookDto book = bookMapper.toDto(entity);
        book.setExpired(false);
        return book;
//  save(entity); - не нужен, так как entity лежит в persistence context и Hibernate знает об этом entity, поэтому сам обновит сущность в бд при каждом вызове setter
    }
}
