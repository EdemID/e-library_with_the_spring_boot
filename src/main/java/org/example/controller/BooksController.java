package org.example.controller;

import jakarta.validation.Valid;
import org.example.model.Book;
import org.example.model.Person;
import org.example.serviece.BookService;
import org.example.serviece.PeopleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/books")
public class BooksController {

    private final BookService bookService;
    private final PeopleService personService;

    public BooksController(BookService bookService, PeopleService personService) {
        this.bookService = bookService;
        this.personService = personService;
    }

    @GetMapping
    public List<Book> index(@RequestParam(required = false) Integer page,
                        @RequestParam(name = "books_per_page", required = false) Integer booksPerPage,
                        @RequestParam(name = "sort_by_year", required = false) boolean sortByYear) {
        List<Book> books;
        if (page == null && booksPerPage == null) {
            books = bookService.findAll(sortByYear);
        } else {
            books = bookService.findAll(page, booksPerPage, sortByYear);
        }

//        model.addAttribute("books", books);
        return books;
    }

    @GetMapping("/{id}")
    public Book show(@PathVariable("id") int id,
                       @ModelAttribute("person") Person person) {
        Book book = bookService.findById(id);
//        model.addAttribute("book", book);
//        model.addAttribute("people", personService.findAll());
//        model.addAttribute("namePerson", book.getOwner());

        return book;
    }

    @GetMapping("/new")
    public void newBook(@ModelAttribute("book") Book book) {

    }

    @PostMapping()
    public void create(@ModelAttribute("book") @Valid Book book,
                         BindingResult bindingResult) {
        if (bindingResult.hasErrors())
//            return "books/new";

        bookService.save(book);
//        return "redirect:/books";
    }

    @GetMapping("/{id}/edit")
    public void edit(@PathVariable("id") int id) {
//        model.addAttribute("book", bookService.findById(id));
//        return "books/edit";
    }

    @PatchMapping("/{id}")
    public void update(@ModelAttribute("book") @Valid Book book, BindingResult bindingResult,
                         @PathVariable("id") int id) {
        if (bindingResult.hasErrors())
//            return "books/edit";

        bookService.update(id, book);
//        return "redirect:/books";
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") int id) {
        bookService.delete(id);
//        return "redirect:/books";
    }

    @PatchMapping("/{id}/add")
    public void assignBook(@ModelAttribute("person") Person person,
                             @PathVariable("id") int id) {
        bookService.assignBook(id, person);
//        return "redirect:/books/{id}";
    }

    @GetMapping("/{id}/return")
    public void returnBook(@PathVariable("id") int id) {
        bookService.returnBook(id);

//        return "redirect:/books/{id}";
    }

    @GetMapping("/search")
    public void searchBook() {
//        return "books/search";
    }

    @PostMapping("/search")
    public void makeSearch(Model model, @ModelAttribute("searchTerm") String searchTerm) {
        model.addAttribute("searchResults", bookService.findByBookNameStartsWith(searchTerm));

//        return "books/search";
    }
}
