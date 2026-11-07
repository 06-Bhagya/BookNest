
package com.booknest.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.booknest.entity.Book;
import com.booknest.repository.BookRepository;

@Service
public class OpenLibraryService {

    private final BookRepository bookRepository;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public OpenLibraryService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public int importBooks(String subject, int limit) throws Exception {
        int safeLimit = Math.max(1, Math.min(limit, 500));

        String encodedSubject = URLEncoder.encode(
                subject, StandardCharsets.UTF_8);

        String url = "https://openlibrary.org/subjects/"
                + encodedSubject + ".json?limit=" + safeLimit;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "BookNest/1.0")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                    "Open Library request failed: "
                    + response.statusCode());
        }

        JSONObject json = new JSONObject(response.body());
        JSONArray works = json.optJSONArray("works");

        if (works == null) {
            return 0;
        }

        List<Book> existingBooks = bookRepository.findAll();
        Set<String> existingTitles = new HashSet<>();

        for (Book existing : existingBooks) {
            if (existing.getTitle() != null) {
                existingTitles.add(
                        existing.getTitle().trim().toLowerCase());
            }
        }

        int imported = 0;

        for (int i = 0; i < works.length(); i++) {
            JSONObject work = works.getJSONObject(i);

            String title = work.optString("title", "").trim();

            if (title.isBlank() || title.length() > 100) {
                continue;
            }

            if (!existingTitles.add(title.toLowerCase())) {
                continue;
            }

            String author = "Unknown author";
            JSONArray authors = work.optJSONArray("authors");

            if (authors != null && authors.length() > 0) {
                author = authors.getJSONObject(0)
                        .optString("name", "Unknown author");
            }

            if (author.length() > 100) {
                author = author.substring(0, 100);
            }

            Book book = new Book();
            book.setTitle(title);
            book.setAuthor(author);
            book.setCategory(subject.length() > 50
                    ? subject.substring(0, 50) : subject);
            book.setAvailable(true);

            int coverId = work.optInt("cover_id", 0);
            if (coverId > 0) {
                book.setCoverUrl(
                        "https://covers.openlibrary.org/b/id/"
                        + coverId + "-L.jpg");
            }

            bookRepository.save(book);
            imported++;
        }

        return imported;
    }
}
