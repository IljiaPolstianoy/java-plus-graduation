package ru.practicum.mainservice.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.category.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);
}
