package ru.practicum.controller;

import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.category.Category;
import ru.practicum.CategoryService;
import ru.practicum.exception.CategoryNotFoundException;

@RestController
@RequestMapping("/internal/category")
public class InternalCategoryController {

    private final CategoryService categoryService;

    public InternalCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/{categoryId}")
    public Category findById(@PathVariable @Positive final Long categoryId) {
        return categoryService.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(String.format("Category with id=%d was not found", categoryId)));
    }
}
