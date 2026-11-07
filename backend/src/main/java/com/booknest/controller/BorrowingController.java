package com.booknest.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.booknest.entity.Borrowing;
import com.booknest.service.BorrowingService;

@RestController
@RequestMapping("/api/borrowings")
public class BorrowingController {

    private final BorrowingService borrowingService;

    public BorrowingController(BorrowingService borrowingService) {
        this.borrowingService = borrowingService;
    }

    @PostMapping("/{bookId}")
    public Borrowing borrowBook(
            @PathVariable Long bookId,
            Authentication authentication) {

        String email = authentication.getName();

        return borrowingService.borrowBook(bookId, email);
    }

    @PutMapping("/{borrowingId}/return")
    public Borrowing returnBook(
            @PathVariable Long borrowingId,
            Authentication authentication) {

        String email = authentication.getName();

        return borrowingService.returnBook(borrowingId, email);
    }

    @GetMapping("/my")
    public List<Borrowing> getMyBorrowings(
            Authentication authentication) {

        String email = authentication.getName();

        return borrowingService.getMyBorrowings(email);
    }
}