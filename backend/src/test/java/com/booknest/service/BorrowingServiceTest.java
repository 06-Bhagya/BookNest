package com.booknest.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.booknest.entity.Book;
import com.booknest.entity.user;
import com.booknest.repository.BookRepository;
import com.booknest.repository.BorrowingRepository;
import com.booknest.repository.UserRepository;

import com.booknest.entity.Borrowing;
import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
class BorrowingServiceTest {

    @Mock
    private BorrowingRepository borrowingRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BorrowingService borrowingService;

    private user member;
    private Book book;

    @BeforeEach
    void setUp() {
        member = new user();
        member.setEmail("member@example.com");
        member.setVerified(true);

        book = new Book();
        book.setId(1L);
        book.setTitle("Java Basics");
        book.setAuthor("Test Author");
        book.setCategory("Programming");
        book.setAvailable(true);
    }
    
    
    //This test checks that borrowing a book creates a borrowing record and marks the book unavailable.

    @Test
    void borrowBookSuccessfully() {
        when(userRepository.findByEmail("member@example.com"))
                .thenReturn(Optional.of(member));

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        when(bookRepository.save(any(Book.class)))
                .thenReturn(book);

        when(borrowingRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = borrowingService.borrowBook(1L, "member@example.com");

        assertNotNull(result);
        assertEquals("Java Basics", result.getBook().getTitle());
        assertFalse(result.getBook().isAvailable());
        assertFalse(result.isReturned());

        verify(bookRepository).save(book);
        verify(borrowingRepository).save(any());
        
    }  
    
    //Book not found: borrowing should fail when the requested book ID doesn't exist.
    
        @Test
        void borrowBookWhenBookDoesNotExist() {
            when(userRepository.findByEmail("member@example.com"))
                    .thenReturn(Optional.of(member));

            when(bookRepository.findById(99L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    com.booknest.exception.BookNotFoundException.class,
                    () -> borrowingService.borrowBook(99L, "member@example.com")
            );
        }

        
        //Book unavailable: borrowing should fail when another member has already borrowed the book.
        @Test
        void borrowBookWhenBookIsUnavailable() {
            book.setAvailable(false);

            when(userRepository.findByEmail("member@example.com"))
                    .thenReturn(Optional.of(member));

            when(bookRepository.findById(1L))
                    .thenReturn(Optional.of(book));

            assertThrows(
                    RuntimeException.class,
                    () -> borrowingService.borrowBook(1L, "member@example.com")
            );
        }  
            
        
        
        
        //This verifies that the service retrieves the current member's borrowing history, even when the member hasn't borrowed any books.
            @Test
            void getMyBorrowingsSuccessfully() {
                when(borrowingRepository.findByMemberEmail("member@example.com"))
                        .thenReturn(java.util.Collections.emptyList());

                var result = borrowingService.getMyBorrowings("member@example.com");

                assertNotNull(result);
                assertTrue(result.isEmpty());

                verify(borrowingRepository).findByMemberEmail("member@example.com");
            }
            
            @Test
            void returnBookSuccessfully() {
                Borrowing borrowing = new Borrowing();

                borrowing.setId(1L);
                borrowing.setMember(member);
                borrowing.setBook(book);
                borrowing.setBorrowDate(LocalDate.now());
                borrowing.setReturned(false);

                when(borrowingRepository.findById(1L))
                        .thenReturn(Optional.of(borrowing));

                when(borrowingRepository.save(any()))
                        .thenAnswer(invocation -> invocation.getArgument(0));

                when(bookRepository.save(any(Book.class)))
                        .thenAnswer(invocation -> invocation.getArgument(0));

                var result = borrowingService.returnBook(
                        1L, "member@example.com");

                assertTrue(result.isReturned());
                assertNotNull(result.getReturnDate());
                assertTrue(book.isAvailable());

                verify(borrowingRepository).save(borrowing);
                verify(bookRepository).save(book);
            }
        
    }
