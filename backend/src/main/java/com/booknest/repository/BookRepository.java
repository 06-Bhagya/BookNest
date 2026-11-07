package com.booknest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.booknest.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

}