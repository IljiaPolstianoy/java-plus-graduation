package ru.practicum.mainservice.category;

import ru.practicum.category.Category;
import ru.practicum.category.CategoryDto;
import ru.practicum.mainservice.exception.CategoryIsRelatedToEventException;
import ru.practicum.mainservice.exception.CategoryNameUniqueException;
import ru.practicum.mainservice.exception.CategoryNotFoundException;
import ru.practicum.mainservice.exception.InvalidCategoryException;

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
