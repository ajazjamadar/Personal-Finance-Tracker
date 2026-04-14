package com.qburst.training.personalfinancetracker.repository;

import com.qburst.training.personalfinancetracker.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findFirstByNameIgnoreCase(String name);
}
