package com.booknest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.booknest.service.OpenLibraryService;

import java.util.Map;

@RestController
@RequestMapping("/api/books")
public class BookImportController {

    private final OpenLibraryService openLibraryService;

    public BookImportController(OpenLibraryService openLibraryService) {
        this.openLibraryService = openLibraryService;
    }

    @PostMapping("/import")
    public ResponseEntity<?> importBooks(
            @RequestParam(defaultValue = "programming") String subject,
            @RequestParam(defaultValue = "100") int limit) {

        try {
            int imported = openLibraryService.importBooks(subject, limit);

            return ResponseEntity.ok(Map.of(
                    "message", "Book import completed successfully",
                    "subject", subject,
                    "importedBooks", imported
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "Book import failed: "
                            + e.getMessage()));
        }
    }
}