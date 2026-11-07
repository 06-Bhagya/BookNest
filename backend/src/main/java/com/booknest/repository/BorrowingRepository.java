
package com.booknest.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.booknest.entity.Borrowing;

public interface BorrowingRepository
        extends JpaRepository<Borrowing, Long> {

    List<Borrowing> findByMemberEmail(String email);
}
