package ru.practicum.category;

import ru.practicum.exception.CategoryIsRelatedToEventException;
import ru.practicum.exception.CategoryNameUniqueException;
import ru.practicum.exception.CategoryNotFoundException;
import ru.practicum.exception.InvalidCategoryException;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    CategoryDto createCategory(CategoryDto categoryDto) throws CategoryNameUniqueException, InvalidCategoryException;

    CategoryDto updateCategory(CategoryDto categoryDto) throws CategoryNotFoundException, CategoryNameUniqueException, InvalidCategoryException;

    boolean deleteCategory(Long catId) throws CategoryIsRelatedToEventException;

    CategoryDto findCategoryById(Long catId) throws CategoryNotFoundException;

    List<CategoryDto> findAllCategories(Integer from, Integer size);

    Optional<Category> findById(Long categoryId);
}
