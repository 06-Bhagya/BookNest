
package com.booknest.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.booknest.entity.Book;
import com.booknest.entity.Borrowing;
import com.booknest.entity.user;
import com.booknest.exception.BookNotFoundException;
import com.booknest.repository.BookRepository;
import com.booknest.repository.BorrowingRepository;
import com.booknest.repository.UserRepository;

@Service
public class BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public BorrowingService(
            BorrowingRepository borrowingRepository,
            BookRepository bookRepository,
            UserRepository userRepository) {
        this.borrowingRepository = borrowingRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Borrowing borrowBook(Long bookId, String email) {

        user member = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!member.isVerified()) {
            throw new RuntimeException("Please verify your account");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new BookNotFoundException(
                                "Book not found with ID: " + bookId));

        if (!book.isAvailable()) {
            throw new RuntimeException("Book is not available");
        }

        book.setAvailable(false);
        bookRepository.save(book);

        Borrowing borrowing = new Borrowing();
        borrowing.setMember(member);
        borrowing.setBook(book);
        borrowing.setBorrowDate(LocalDate.now());
        borrowing.setReturned(false);

        return borrowingRepository.save(borrowing);
    }

    @Transactional
    public Borrowing returnBook(Long borrowingId, String email) {

        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() ->
                        new RuntimeException("Borrowing record not found"));

        if (!borrowing.getMember().getEmail().equals(email)) {
            throw new RuntimeException(
                    "You can only return books you borrowed");
        }

        if (borrowing.isReturned()) {
            throw new RuntimeException("Book is already returned");
        }

        borrowing.setReturned(true);
        borrowing.setReturnDate(LocalDate.now());

        Book book = borrowing.getBook();
        book.setAvailable(true);
        bookRepository.save(book);

        return borrowingRepository.save(borrowing);
    }

    public List<Borrowing> getMyBorrowings(String email) {

        return borrowingRepository.findByMemberEmail(email);
    }
}
